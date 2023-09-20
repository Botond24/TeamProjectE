package net.minecraft.network;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Queue;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import net.minecraft.network.login.ServerLoginNetHandler;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.server.SDisconnectPacket;
import net.minecraft.util.LazyValue;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class NetworkManager extends SimpleChannelInboundHandler<IPacket<?>> {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final Marker ROOT_MARKER = MarkerManager.getMarker("NETWORK");
   public static final Marker PACKET_MARKER = MarkerManager.getMarker("NETWORK_PACKETS", ROOT_MARKER);
   public static final AttributeKey<ProtocolType> ATTRIBUTE_PROTOCOL = AttributeKey.valueOf("protocol");
   public static final LazyValue<NioEventLoopGroup> NETWORK_WORKER_GROUP = new LazyValue<>(() -> {
      return new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Client IO #%d").setDaemon(true).build());
   });
   public static final LazyValue<EpollEventLoopGroup> NETWORK_EPOLL_WORKER_GROUP = new LazyValue<>(() -> {
      return new EpollEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Epoll Client IO #%d").setDaemon(true).build());
   });
   public static final LazyValue<DefaultEventLoopGroup> LOCAL_WORKER_GROUP = new LazyValue<>(() -> {
      return new DefaultEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Local Client IO #%d").setDaemon(true).build());
   });
   private final PacketDirection receiving;
   /**
    * The queue for packets that get sent before the channel is connected.
    * Every tick or whenever a new packet is sent the connection will try to flush this queue, if the channel has since
    * finished connecting.
    */
   private final Queue<NetworkManager.QueuedPacket> queue = Queues.newConcurrentLinkedQueue();
   /** The active channel */
   private Channel channel;
   /** The address of the remote party */
   private SocketAddress address;
   /** The PacketListener instance responsible for processing received packets */
   private INetHandler packetListener;
   /** A Component indicating why the network has shutdown. */
   private ITextComponent disconnectedReason;
   private boolean encrypted;
   private boolean disconnectionHandled;
   private int receivedPackets;
   private int sentPackets;
   private float averageReceivedPackets;
   private float averageSentPackets;
   private int tickCount;
   private boolean handlingFault;
   private java.util.function.Consumer<NetworkManager> activationHandler;

   public NetworkManager(PacketDirection p_i46004_1_) {
      this.receiving = p_i46004_1_;
   }

   public void channelActive(ChannelHandlerContext p_channelActive_1_) throws Exception {
      super.channelActive(p_channelActive_1_);
      this.channel = p_channelActive_1_.channel();
      this.address = this.channel.remoteAddress();
      if (activationHandler != null) activationHandler.accept(this);

      try {
         this.setProtocol(ProtocolType.HANDSHAKING);
      } catch (Throwable throwable) {
         LOGGER.fatal(throwable);
      }

   }

   /**
    * Sets the new connection state and registers which packets this channel may send and receive
    */
   public void setProtocol(ProtocolType pNewState) {
      this.channel.attr(ATTRIBUTE_PROTOCOL).set(pNewState);
      this.channel.config().setAutoRead(true);
      LOGGER.debug("Enabled auto read");
   }

   public void channelInactive(ChannelHandlerContext p_channelInactive_1_) throws Exception {
      this.disconnect(new TranslationTextComponent("disconnect.endOfStream"));
   }

   public void exceptionCaught(ChannelHandlerContext p_exceptionCaught_1_, Throwable p_exceptionCaught_2_) {
      if (p_exceptionCaught_2_ instanceof SkipableEncoderException) {
         LOGGER.debug("Skipping packet due to errors", p_exceptionCaught_2_.getCause());
      } else {
         boolean flag = !this.handlingFault;
         this.handlingFault = true;
         if (this.channel.isOpen()) {
            if (p_exceptionCaught_2_ instanceof TimeoutException) {
               LOGGER.debug("Timeout", p_exceptionCaught_2_);
               this.disconnect(new TranslationTextComponent("disconnect.timeout"));
            } else {
               ITextComponent itextcomponent = new TranslationTextComponent("disconnect.genericReason", "Internal Exception: " + p_exceptionCaught_2_);
               if (flag) {
                  LOGGER.debug("Failed to sent packet", p_exceptionCaught_2_);
                  this.send(new SDisconnectPacket(itextcomponent), (p_211391_2_) -> {
                     this.disconnect(itextcomponent);
                  });
                  this.setReadOnly();
               } else {
                  LOGGER.debug("Double fault", p_exceptionCaught_2_);
                  this.disconnect(itextcomponent);
               }
            }

         }
      }
   }

   protected void channelRead0(ChannelHandlerContext p_channelRead0_1_, IPacket<?> p_channelRead0_2_) throws Exception {
      if (this.channel.isOpen()) {
         try {
            genericsFtw(p_channelRead0_2_, this.packetListener);
         } catch (ThreadQuickExitException threadquickexitexception) {
         }

         ++this.receivedPackets;
      }

   }

   private static <T extends INetHandler> void genericsFtw(IPacket<T> p_197664_0_, INetHandler p_197664_1_) {
      p_197664_0_.handle((T)p_197664_1_);
   }

   /**
    * Sets the NetHandler for this NetworkManager, no checks are made if this handler is suitable for the particular
    * connection state (protocol)
    */
   public void setListener(INetHandler pHandler) {
      Validate.notNull(pHandler, "packetListener");
      this.packetListener = pHandler;
   }

   public void send(IPacket<?> pPacket) {
      this.send(pPacket, (GenericFutureListener<? extends Future<? super Void>>)null);
   }

   public void send(IPacket<?> pPacket, @Nullable GenericFutureListener<? extends Future<? super Void>> p_201058_2_) {
      if (this.isConnected()) {
         this.flushQueue();
         this.sendPacket(pPacket, p_201058_2_);
      } else {
         this.queue.add(new NetworkManager.QueuedPacket(pPacket, p_201058_2_));
      }

   }

   /**
    * Will commit the packet to the channel. If the current thread 'owns' the channel it will write and flush the
    * packet, otherwise it will add a task for the channel eventloop thread to do that.
    */
   private void sendPacket(IPacket<?> pInPacket, @Nullable GenericFutureListener<? extends Future<? super Void>> pFutureListeners) {
      ProtocolType protocoltype = ProtocolType.getProtocolForPacket(pInPacket);
      ProtocolType protocoltype1 = this.channel.attr(ATTRIBUTE_PROTOCOL).get();
      ++this.sentPackets;
      if (protocoltype1 != protocoltype) {
         LOGGER.debug("Disabled auto read");
         this.channel.eventLoop().execute(()->this.channel.config().setAutoRead(false));
      }

      if (this.channel.eventLoop().inEventLoop()) {
         if (protocoltype != protocoltype1) {
            this.setProtocol(protocoltype);
         }

         ChannelFuture channelfuture = this.channel.writeAndFlush(pInPacket);
         if (pFutureListeners != null) {
            channelfuture.addListener(pFutureListeners);
         }

         channelfuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
      } else {
         this.channel.eventLoop().execute(() -> {
            if (protocoltype != protocoltype1) {
               this.setProtocol(protocoltype);
            }

            ChannelFuture channelfuture1 = this.channel.writeAndFlush(pInPacket);
            if (pFutureListeners != null) {
               channelfuture1.addListener(pFutureListeners);
            }

            channelfuture1.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
         });
      }

   }

   /**
    * Will iterate through the outboundPacketQueue and dispatch all Packets
    */
   private void flushQueue() {
      if (this.channel != null && this.channel.isOpen()) {
         synchronized(this.queue) {
            NetworkManager.QueuedPacket networkmanager$queuedpacket;
            while((networkmanager$queuedpacket = this.queue.poll()) != null) {
               this.sendPacket(networkmanager$queuedpacket.packet, networkmanager$queuedpacket.listener);
            }

         }
      }
   }

   /**
    * Checks timeouts and processes all packets received
    */
   public void tick() {
      this.flushQueue();
      if (this.packetListener instanceof ServerLoginNetHandler) {
         ((ServerLoginNetHandler)this.packetListener).tick();
      }

      if (this.packetListener instanceof ServerPlayNetHandler) {
         ((ServerPlayNetHandler)this.packetListener).tick();
      }

      if (this.channel != null) {
         this.channel.flush();
      }

      if (this.tickCount++ % 20 == 0) {
         this.tickSecond();
      }

   }

   protected void tickSecond() {
      this.averageSentPackets = MathHelper.lerp(0.75F, (float)this.sentPackets, this.averageSentPackets);
      this.averageReceivedPackets = MathHelper.lerp(0.75F, (float)this.receivedPackets, this.averageReceivedPackets);
      this.sentPackets = 0;
      this.receivedPackets = 0;
   }

   /**
    * Returns the socket address of the remote side. Server-only.
    */
   public SocketAddress getRemoteAddress() {
      return this.address;
   }

   /**
    * Closes the channel with a given reason. The reason is stored for later and will be used for informational purposes
    * (info log on server,
    * disconnection screen on the client). This method is also called on the client when the server requests
    * disconnection via
    * {@code ClientboundDisconnectPacket}.
    * 
    * Closing the channel this way does not send any disconnection packets, it simply terminates the underlying netty
    * channel.
    */
   public void disconnect(ITextComponent pMessage) {
      if (this.channel.isOpen()) {
         this.channel.close().awaitUninterruptibly();
         this.disconnectedReason = pMessage;
      }

   }

   /**
    * True if this NetworkManager uses a memory connection (single player game). False may imply both an active TCP
    * connection or simply no active connection at all
    */
   public boolean isMemoryConnection() {
      return this.channel instanceof LocalChannel || this.channel instanceof LocalServerChannel;
   }

   @OnlyIn(Dist.CLIENT)
   public static NetworkManager connectToServer(InetAddress p_181124_0_, int p_181124_1_, boolean p_181124_2_) {
      if (p_181124_0_ instanceof java.net.Inet6Address) System.setProperty("java.net.preferIPv4Stack", "false");
      final NetworkManager networkmanager = new NetworkManager(PacketDirection.CLIENTBOUND);
      networkmanager.activationHandler = net.minecraftforge.fml.network.NetworkHooks::registerClientLoginChannel;
      Class<? extends SocketChannel> oclass;
      LazyValue<? extends EventLoopGroup> lazyvalue;
      if (Epoll.isAvailable() && p_181124_2_) {
         oclass = EpollSocketChannel.class;
         lazyvalue = NETWORK_EPOLL_WORKER_GROUP;
      } else {
         oclass = NioSocketChannel.class;
         lazyvalue = NETWORK_WORKER_GROUP;
      }

      (new Bootstrap()).group(lazyvalue.get()).handler(new ChannelInitializer<Channel>() {
         protected void initChannel(Channel p_initChannel_1_) throws Exception {
            try {
               p_initChannel_1_.config().setOption(ChannelOption.TCP_NODELAY, true);
            } catch (ChannelException channelexception) {
            }

            p_initChannel_1_.pipeline().addLast("timeout", new ReadTimeoutHandler(30)).addLast("splitter", new NettyVarint21FrameDecoder()).addLast("decoder", new NettyPacketDecoder(PacketDirection.CLIENTBOUND)).addLast("prepender", new NettyVarint21FrameEncoder()).addLast("encoder", new NettyPacketEncoder(PacketDirection.SERVERBOUND)).addLast("packet_handler", networkmanager);
         }
      }).channel(oclass).connect(p_181124_0_, p_181124_1_).syncUninterruptibly();
      return networkmanager;
   }

   /**
    * Prepares a clientside Connection for a local in-memory connection ("single player").
    * Establishes a connection to the socket supplied and configures the channel pipeline (only the packet handler is
    * necessary,
    * since this is for an in-memory connection). Returns the newly created instance.
    */
   @OnlyIn(Dist.CLIENT)
   public static NetworkManager connectToLocalServer(SocketAddress pAddress) {
      final NetworkManager networkmanager = new NetworkManager(PacketDirection.CLIENTBOUND);
      networkmanager.activationHandler = net.minecraftforge.fml.network.NetworkHooks::registerClientLoginChannel;
      (new Bootstrap()).group(LOCAL_WORKER_GROUP.get()).handler(new ChannelInitializer<Channel>() {
         protected void initChannel(Channel p_initChannel_1_) throws Exception {
            p_initChannel_1_.pipeline().addLast("packet_handler", networkmanager);
         }
      }).channel(LocalChannel.class).connect(pAddress).syncUninterruptibly();
      return networkmanager;
   }

   /**
    * Enables encryption for this connection using the given decrypting and encrypting ciphers.
    * This adds new handlers to this connection's pipeline which handle the decrypting and encrypting.
    * This happens as part of the normal network handshake.
    * 
    * @see ClientboundHelloPacket
    * @see ServerboundKeyPacket
    */
   public void setEncryptionKey(Cipher pDecryptingCipher, Cipher pEncryptingCipher) {
      this.encrypted = true;
      this.channel.pipeline().addBefore("splitter", "decrypt", new NettyEncryptingDecoder(pDecryptingCipher));
      this.channel.pipeline().addBefore("prepender", "encrypt", new NettyEncryptingEncoder(pEncryptingCipher));
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isEncrypted() {
      return this.encrypted;
   }

   /**
    * Returns true if this NetworkManager has an active channel, false otherwise
    */
   public boolean isConnected() {
      return this.channel != null && this.channel.isOpen();
   }

   /**
    * Returns true while this connection is still connecting, i.e. {@link #channelActive} has not fired yet.
    */
   public boolean isConnecting() {
      return this.channel == null;
   }

   /**
    * Gets the current handler for processing packets
    */
   public INetHandler getPacketListener() {
      return this.packetListener;
   }

   /**
    * If this channel is closed, returns the exit message, null otherwise.
    */
   @Nullable
   public ITextComponent getDisconnectedReason() {
      return this.disconnectedReason;
   }

   /**
    * Switches the channel to manual reading modus
    */
   public void setReadOnly() {
      this.channel.config().setAutoRead(false);
   }

   public void setupCompression(int pThreshold) {
      if (pThreshold >= 0) {
         if (this.channel.pipeline().get("decompress") instanceof NettyCompressionDecoder) {
            ((NettyCompressionDecoder)this.channel.pipeline().get("decompress")).setThreshold(pThreshold);
         } else {
            this.channel.pipeline().addBefore("decoder", "decompress", new NettyCompressionDecoder(pThreshold));
         }

         if (this.channel.pipeline().get("compress") instanceof NettyCompressionEncoder) {
            ((NettyCompressionEncoder)this.channel.pipeline().get("compress")).setThreshold(pThreshold);
         } else {
            this.channel.pipeline().addBefore("encoder", "compress", new NettyCompressionEncoder(pThreshold));
         }
      } else {
         if (this.channel.pipeline().get("decompress") instanceof NettyCompressionDecoder) {
            this.channel.pipeline().remove("decompress");
         }

         if (this.channel.pipeline().get("compress") instanceof NettyCompressionEncoder) {
            this.channel.pipeline().remove("compress");
         }
      }

   }

   /**
    * Checks if the channle is no longer active and if so, processes the disconnection
    * by notifying the current packet listener, which will handle things like removing the player from the world
    * (serverside) or
    * showing the disconnection screen (clientside).
    */
   public void handleDisconnection() {
      if (this.channel != null && !this.channel.isOpen()) {
         if (this.disconnectionHandled) {
            LOGGER.warn("handleDisconnection() called twice");
         } else {
            this.disconnectionHandled = true;
            if (this.getDisconnectedReason() != null) {
               this.getPacketListener().onDisconnect(this.getDisconnectedReason());
            } else if (this.getPacketListener() != null) {
               this.getPacketListener().onDisconnect(new TranslationTextComponent("multiplayer.disconnect.generic"));
            }
         }

      }
   }

   public float getAverageReceivedPackets() {
      return this.averageReceivedPackets;
   }

   @OnlyIn(Dist.CLIENT)
   public float getAverageSentPackets() {
      return this.averageSentPackets;
   }

   public Channel channel() {
      return channel;
   }

   public PacketDirection getDirection() {
      return this.receiving;
   }

   static class QueuedPacket {
      private final IPacket<?> packet;
      @Nullable
      private final GenericFutureListener<? extends Future<? super Void>> listener;

      public QueuedPacket(IPacket<?> p_i48604_1_, @Nullable GenericFutureListener<? extends Future<? super Void>> p_i48604_2_) {
         this.packet = p_i48604_1_;
         this.listener = p_i48604_2_;
      }
   }
}
