package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SPlayerPositionLookPacket implements IPacket<IClientPlayNetHandler> {
   private double x;
   private double y;
   private double z;
   private float yRot;
   private float xRot;
   private Set<SPlayerPositionLookPacket.Flags> relativeArguments;
   private int id;

   public SPlayerPositionLookPacket() {
   }

   public SPlayerPositionLookPacket(double p_i46928_1_, double p_i46928_3_, double p_i46928_5_, float p_i46928_7_, float p_i46928_8_, Set<SPlayerPositionLookPacket.Flags> p_i46928_9_, int p_i46928_10_) {
      this.x = p_i46928_1_;
      this.y = p_i46928_3_;
      this.z = p_i46928_5_;
      this.yRot = p_i46928_7_;
      this.xRot = p_i46928_8_;
      this.relativeArguments = p_i46928_9_;
      this.id = p_i46928_10_;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.x = p_148837_1_.readDouble();
      this.y = p_148837_1_.readDouble();
      this.z = p_148837_1_.readDouble();
      this.yRot = p_148837_1_.readFloat();
      this.xRot = p_148837_1_.readFloat();
      this.relativeArguments = SPlayerPositionLookPacket.Flags.unpack(p_148837_1_.readUnsignedByte());
      this.id = p_148837_1_.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeDouble(this.x);
      pBuffer.writeDouble(this.y);
      pBuffer.writeDouble(this.z);
      pBuffer.writeFloat(this.yRot);
      pBuffer.writeFloat(this.xRot);
      pBuffer.writeByte(SPlayerPositionLookPacket.Flags.pack(this.relativeArguments));
      pBuffer.writeVarInt(this.id);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleMovePlayer(this);
   }

   @OnlyIn(Dist.CLIENT)
   public double getX() {
      return this.x;
   }

   @OnlyIn(Dist.CLIENT)
   public double getY() {
      return this.y;
   }

   @OnlyIn(Dist.CLIENT)
   public double getZ() {
      return this.z;
   }

   @OnlyIn(Dist.CLIENT)
   public float getYRot() {
      return this.yRot;
   }

   @OnlyIn(Dist.CLIENT)
   public float getXRot() {
      return this.xRot;
   }

   @OnlyIn(Dist.CLIENT)
   public int getId() {
      return this.id;
   }

   /**
    * Returns a set of which fields are relative. Items in this set indicate that the value is a relative change applied
    * to the player's position, rather than an exact value.
    */
   @OnlyIn(Dist.CLIENT)
   public Set<SPlayerPositionLookPacket.Flags> getRelativeArguments() {
      return this.relativeArguments;
   }

   public static enum Flags {
      X(0),
      Y(1),
      Z(2),
      Y_ROT(3),
      X_ROT(4);

      private final int bit;

      private Flags(int p_i46690_3_) {
         this.bit = p_i46690_3_;
      }

      private int getMask() {
         return 1 << this.bit;
      }

      /**
       * Checks if this flag is set within the given set of flags.
       */
      private boolean isSet(int pFlags) {
         return (pFlags & this.getMask()) == this.getMask();
      }

      public static Set<SPlayerPositionLookPacket.Flags> unpack(int pFlags) {
         Set<SPlayerPositionLookPacket.Flags> set = EnumSet.noneOf(SPlayerPositionLookPacket.Flags.class);

         for(SPlayerPositionLookPacket.Flags splayerpositionlookpacket$flags : values()) {
            if (splayerpositionlookpacket$flags.isSet(pFlags)) {
               set.add(splayerpositionlookpacket$flags);
            }
         }

         return set;
      }

      public static int pack(Set<SPlayerPositionLookPacket.Flags> pFlags) {
         int i = 0;

         for(SPlayerPositionLookPacket.Flags splayerpositionlookpacket$flags : pFlags) {
            i |= splayerpositionlookpacket$flags.getMask();
         }

         return i;
      }
   }
}