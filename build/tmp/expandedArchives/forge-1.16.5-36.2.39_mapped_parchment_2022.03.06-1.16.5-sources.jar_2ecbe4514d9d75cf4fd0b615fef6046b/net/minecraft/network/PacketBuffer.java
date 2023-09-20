package net.minecraft.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ByteProcessor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;

public class PacketBuffer extends ByteBuf implements net.minecraftforge.common.extensions.IForgePacketBuffer {
   private final ByteBuf source;

   public PacketBuffer(ByteBuf p_i45154_1_) {
      this.source = p_i45154_1_;
   }

   /**
    * Calculates the number of bytes ({@code [1-5]}) required to fit the supplied int if it were to be read/written
    * using readVarInt/writeVarInt
    */
   public static int getVarIntSize(int pInput) {
      for(int i = 1; i < 5; ++i) {
         if ((pInput & -1 << i * 7) == 0) {
            return i;
         }
      }

      return 5;
   }

   /**
    * Read a value of type {@code T} from this buffer using the given Codec.
    * NBT is used under the hood to read the data.
    * 
    * @see #writeWithCodec
    */
   public <T> T readWithCodec(Codec<T> pCodec) throws IOException {
      CompoundNBT compoundnbt = this.readAnySizeNbt();
      DataResult<T> dataresult = pCodec.parse(NBTDynamicOps.INSTANCE, compoundnbt);
      if (dataresult.error().isPresent()) {
         throw new IOException("Failed to decode: " + dataresult.error().get().message() + " " + compoundnbt);
      } else {
         return dataresult.result().get();
      }
   }

   /**
    * Write a value of type {@code T} to this buffer using the given Codec.
    * NBT is used under the hood to write the data.
    * 
    * @see #readWithCodec
    */
   public <T> void writeWithCodec(Codec<T> p_240629_1_, T p_240629_2_) throws IOException {
      DataResult<INBT> dataresult = p_240629_1_.encodeStart(NBTDynamicOps.INSTANCE, p_240629_2_);
      if (dataresult.error().isPresent()) {
         throw new IOException("Failed to encode: " + dataresult.error().get().message() + " " + p_240629_2_);
      } else {
         this.writeNbt((CompoundNBT)dataresult.result().get());
      }
   }

   public PacketBuffer writeByteArray(byte[] pArray) {
      this.writeVarInt(pArray.length);
      this.writeBytes(pArray);
      return this;
   }

   public byte[] readByteArray() {
      return this.readByteArray(this.readableBytes());
   }

   public byte[] readByteArray(int pMaxLength) {
      int i = this.readVarInt();
      if (i > pMaxLength) {
         throw new DecoderException("ByteArray with size " + i + " is bigger than allowed " + pMaxLength);
      } else {
         byte[] abyte = new byte[i];
         this.readBytes(abyte);
         return abyte;
      }
   }

   /**
    * Writes an array of VarInts to the buffer, prefixed by the length of the array (as a VarInt).
    * 
    * @see #readVarIntArray
    */
   public PacketBuffer writeVarIntArray(int[] pArray) {
      this.writeVarInt(pArray.length);

      for(int i : pArray) {
         this.writeVarInt(i);
      }

      return this;
   }

   /**
    * Reads an array of VarInts from this buffer.
    * 
    * @see #writeVarIntArray
    */
   public int[] readVarIntArray() {
      return this.readVarIntArray(this.readableBytes());
   }

   /**
    * Reads an array of VarInts with a maximum length from this buffer.
    * 
    * @see #writeVarIntArray
    */
   public int[] readVarIntArray(int pMaxLength) {
      int i = this.readVarInt();
      if (i > pMaxLength) {
         throw new DecoderException("VarIntArray with size " + i + " is bigger than allowed " + pMaxLength);
      } else {
         int[] aint = new int[i];

         for(int j = 0; j < aint.length; ++j) {
            aint[j] = this.readVarInt();
         }

         return aint;
      }
   }

   /**
    * Writes an array of longs to the buffer, prefixed by the length of the array (as a VarInt).
    * 
    * @see #readLongArray
    */
   public PacketBuffer writeLongArray(long[] pArray) {
      this.writeVarInt(pArray.length);

      for(long i : pArray) {
         this.writeLong(i);
      }

      return this;
   }

   /**
    * Reads a length-prefixed array of longs from the buffer.
    * Will try to use the given long[] if possible. Note that if an array with the correct size is given, maxLength is
    * ignored.
    */
   public long[] readLongArray(@Nullable long[] pArray) {
      return this.readLongArray(pArray, this.readableBytes() / 8);
   }

   /**
    * Reads a length-prefixed array of longs with a maximum length from the buffer.
    * Will try to use the given long[] if possible. Note that if an array with the correct size is given, maxLength is
    * ignored.
    */
   public long[] readLongArray(@Nullable long[] pArray, int pMaxLength) {
      int i = this.readVarInt();
      if (pArray == null || pArray.length != i) {
         if (i > pMaxLength) {
            throw new DecoderException("LongArray with size " + i + " is bigger than allowed " + pMaxLength);
         }

         pArray = new long[i];
      }

      for(int j = 0; j < pArray.length; ++j) {
         pArray[j] = this.readLong();
      }

      return pArray;
   }

   /**
    * Reads a BlockPos encoded as a long from the buffer.
    * 
    * @see #writeBlockPos
    */
   public BlockPos readBlockPos() {
      return BlockPos.of(this.readLong());
   }

   /**
    * Writes a BlockPos encoded as a long to the buffer.
    * 
    * @see #readBlockPos
    */
   public PacketBuffer writeBlockPos(BlockPos pPos) {
      this.writeLong(pPos.asLong());
      return this;
   }

   /**
    * Reads a SectionPos encoded as a long from the buffer.
    * 
    * @see #writeSectionPos
    */
   public SectionPos readSectionPos() {
      return SectionPos.of(this.readLong());
   }

   /**
    * Reads a Component encoded as a JSON string from the buffer.
    * 
    * @see #writeComponent
    */
   public ITextComponent readComponent() {
      return ITextComponent.Serializer.fromJson(this.readUtf(262144));
   }

   /**
    * Writes a Component encoded as a JSON string to the buffer.
    * 
    * @see #readComponent
    */
   public PacketBuffer writeComponent(ITextComponent pComponent) {
      return this.writeUtf(ITextComponent.Serializer.toJson(pComponent), 262144);
   }

   /**
    * Reads an enum of the given type T using the ordinal encoded as a VarInt from the buffer.
    * 
    * @see #writeEnum
    */
   public <T extends Enum<T>> T readEnum(Class<T> pEnumClass) {
      return (pEnumClass.getEnumConstants())[this.readVarInt()];
   }

   /**
    * Writes an enum of the given type T using the ordinal encoded as a VarInt to the buffer.
    * 
    * @see #readEnum
    */
   public PacketBuffer writeEnum(Enum<?> pValue) {
      return this.writeVarInt(pValue.ordinal());
   }

   /**
    * Reads a compressed int from the buffer. To do so it maximally reads 5 byte-sized chunks whose most significant bit
    * dictates whether another byte should be read.
    * 
    * @see #writeVarInt
    */
   public int readVarInt() {
      int i = 0;
      int j = 0;

      byte b0;
      do {
         b0 = this.readByte();
         i |= (b0 & 127) << j++ * 7;
         if (j > 5) {
            throw new RuntimeException("VarInt too big");
         }
      } while((b0 & 128) == 128);

      return i;
   }

   /**
    * Reads a compressed long from the buffer. To do so it maximally reads 10 byte-sized chunks whose most significant
    * bit dictates whether another byte should be read.
    * 
    * @see #writeVarLong
    */
   public long readVarLong() {
      long i = 0L;
      int j = 0;

      byte b0;
      do {
         b0 = this.readByte();
         i |= (long)(b0 & 127) << j++ * 7;
         if (j > 10) {
            throw new RuntimeException("VarLong too big");
         }
      } while((b0 & 128) == 128);

      return i;
   }

   /**
    * Writes a UUID encoded as two longs to this buffer.
    * 
    * @see #readUUID
    */
   public PacketBuffer writeUUID(UUID pUuid) {
      this.writeLong(pUuid.getMostSignificantBits());
      this.writeLong(pUuid.getLeastSignificantBits());
      return this;
   }

   /**
    * Reads a UUID encoded as two longs from this buffer.
    * 
    * @see #writeUUID
    */
   public UUID readUUID() {
      return new UUID(this.readLong(), this.readLong());
   }

   /**
    * Writes a compressed int to the buffer. The smallest number of bytes to fit the passed int will be written. Of each
    * such byte only 7 bits will be used to describe the actual value since its most significant bit dictates whether
    * the next byte is part of that same int. Micro-optimization for int values that are usually small.
    */
   public PacketBuffer writeVarInt(int pInput) {
      while((pInput & -128) != 0) {
         this.writeByte(pInput & 127 | 128);
         pInput >>>= 7;
      }

      this.writeByte(pInput);
      return this;
   }

   /**
    * Writes a compressed long to the buffer. The smallest number of bytes to fit the passed long will be written. Of
    * each such byte only 7 bits will be used to describe the actual value since its most significant bit dictates
    * whether the next byte is part of that same long. Micro-optimization for long values that are usually small.
    */
   public PacketBuffer writeVarLong(long pValue) {
      while((pValue & -128L) != 0L) {
         this.writeByte((int)(pValue & 127L) | 128);
         pValue >>>= 7;
      }

      this.writeByte((int)pValue);
      return this;
   }

   /**
    * Writes the given NBT CompoundTag to this buffer.
    * {@code null} is a valid value and can be encoded by this method.
    * 
    * @see #readNbt()
    * @see #readAnySizeNbt
    * @see #readNbt(NbtAccounter)
    */
   public PacketBuffer writeNbt(@Nullable CompoundNBT pNbt) {
      if (pNbt == null) {
         this.writeByte(0);
      } else {
         try {
            CompressedStreamTools.write(pNbt, new ByteBufOutputStream(this));
         } catch (IOException ioexception) {
            throw new EncoderException(ioexception);
         }
      }

      return this;
   }

   /**
    * Reads a NBT CompoundTag from this buffer.
    * {@code null} is a valid value and may be returned.
    * 
    * This method will read a maximum of 0x200000 bytes.
    * 
    * @see #writeNbt
    * @see #readAnySizeNbt
    * @see #readNbt(NbtAccounter)
    */
   @Nullable
   public CompoundNBT readNbt() {
      return this.readNbt(new NBTSizeTracker(2097152L));
   }

   /**
    * Reads a NBT CompoundTag from this buffer.
    * {@code null} is a valid value and may be returned.
    * 
    * This method has no size limit on the NBT data.
    * 
    * @see #writeNbt
    * @see #readNbt()
    * @see #readNbt(NbtAccounter)
    */
   @Nullable
   public CompoundNBT readAnySizeNbt() {
      return this.readNbt(NBTSizeTracker.UNLIMITED);
   }

   /**
    * Reads a NBT CompoundTag from this buffer.
    * {@code null} is a valid value and may be returned.
    * 
    * This method limits the size of the data using the given {@code NbtAccounter}.
    * 
    * @see #writeNbt
    * @see #readNbt()
    * @see #readAnySizeNbt
    */
   @Nullable
   public CompoundNBT readNbt(NBTSizeTracker p_244272_1_) {
      int i = this.readerIndex();
      byte b0 = this.readByte();
      if (b0 == 0) {
         return null;
      } else {
         this.readerIndex(i);

         try {
            return CompressedStreamTools.read(new ByteBufInputStream(this), p_244272_1_);
         } catch (IOException ioexception) {
            throw new EncoderException(ioexception);
         }
      }
   }

   /**
    * Writes an ItemStack to this buffer.
    * 
    * @see #readItem
    */
   public PacketBuffer writeItem(ItemStack pStack) {
      return writeItemStack(pStack, true);
   }

   /**
    * Most ItemStack serialization is Server to Client,and doesn't need to know the FULL tag details.
    * One exception is items from the creative menu, which must be sent from Client to Server with their full NBT.
    * If you want to send the FULL tag set limitedTag to false
    */
   public PacketBuffer writeItemStack(ItemStack pStack, boolean limitedTag) {
      if (pStack.isEmpty()) {
         this.writeBoolean(false);
      } else {
         this.writeBoolean(true);
         Item item = pStack.getItem();
         this.writeVarInt(Item.getId(item));
         this.writeByte(pStack.getCount());
         CompoundNBT compoundnbt = null;
         if (item.isDamageable(pStack) || item.shouldOverrideMultiplayerNbt()) {
            compoundnbt = limitedTag ? pStack.getShareTag() : pStack.getTag();
         }

         this.writeNbt(compoundnbt);
      }

      return this;
   }

   /**
    * Reads an ItemStack from this buffer.
    * 
    * @see #writeItem
    */
   public ItemStack readItem() {
      if (!this.readBoolean()) {
         return ItemStack.EMPTY;
      } else {
         int i = this.readVarInt();
         int j = this.readByte();
         ItemStack itemstack = new ItemStack(Item.byId(i), j);
         itemstack.readShareTag(this.readNbt());
         return itemstack;
      }
   }

   /**
    * Reads a String with a maximum length of {@code Short.MAX_VALUE}.
    * 
    * @see #readUtf(int)
    * @see #writeUtf
    */
   public String readUtf() {
      return this.readUtf(32767);
   }

   /**
    * Reads a string with a maximum length from this buffer.
    * 
    * @see #writeUtf
    */
   public String readUtf(int pMaxLength) {
      int i = this.readVarInt();
      if (i > pMaxLength * 4) {
         throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + i + " > " + pMaxLength * 4 + ")");
      } else if (i < 0) {
         throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
      } else {
         String s = this.toString(this.readerIndex(), i, StandardCharsets.UTF_8);
         this.readerIndex(this.readerIndex() + i);
         if (s.length() > pMaxLength) {
            throw new DecoderException("The received string length is longer than maximum allowed (" + i + " > " + pMaxLength + ")");
         } else {
            return s;
         }
      }
   }

   /**
    * Writes a String with a maximum length of {@code Short.MAX_VALUE}.
    * 
    * @see #readUtf
    */
   public PacketBuffer writeUtf(String pString) {
      return this.writeUtf(pString, 32767);
   }

   /**
    * Writes a String with a maximum length.
    * 
    * @see #readUtf
    */
   public PacketBuffer writeUtf(String pString, int pMaxLength) {
      byte[] abyte = pString.getBytes(StandardCharsets.UTF_8);
      if (abyte.length > pMaxLength) {
         throw new EncoderException("String too big (was " + abyte.length + " bytes encoded, max " + pMaxLength + ")");
      } else {
         this.writeVarInt(abyte.length);
         this.writeBytes(abyte);
         return this;
      }
   }

   /**
    * Read a ResourceLocation using its String representation.
    * 
    * @see #writeResourceLocation
    */
   public ResourceLocation readResourceLocation() {
      return new ResourceLocation(this.readUtf(32767));
   }

   /**
    * Write a ResourceLocation using its String representation.
    * 
    * @see #readResourceLocation
    */
   public PacketBuffer writeResourceLocation(ResourceLocation pResourceLocation) {
      this.writeUtf(pResourceLocation.toString());
      return this;
   }

   /**
    * Read a timestamp as milliseconds since the unix epoch.
    * 
    * @see #writeDate
    */
   public Date readDate() {
      return new Date(this.readLong());
   }

   /**
    * Write a timestamp as milliseconds since the unix epoch.
    * 
    * @see #readDate
    */
   public PacketBuffer writeDate(Date pTime) {
      this.writeLong(pTime.getTime());
      return this;
   }

   /**
    * Read a BlockHitResult.
    * 
    * @see #writeBlockHitResult
    */
   public BlockRayTraceResult readBlockHitResult() {
      BlockPos blockpos = this.readBlockPos();
      Direction direction = this.readEnum(Direction.class);
      float f = this.readFloat();
      float f1 = this.readFloat();
      float f2 = this.readFloat();
      boolean flag = this.readBoolean();
      return new BlockRayTraceResult(new Vector3d((double)blockpos.getX() + (double)f, (double)blockpos.getY() + (double)f1, (double)blockpos.getZ() + (double)f2), direction, blockpos, flag);
   }

   /**
    * Write a BlockHitResult.
    * 
    * @see #readBlockHitResult
    */
   public void writeBlockHitResult(BlockRayTraceResult pResult) {
      BlockPos blockpos = pResult.getBlockPos();
      this.writeBlockPos(blockpos);
      this.writeEnum(pResult.getDirection());
      Vector3d vector3d = pResult.getLocation();
      this.writeFloat((float)(vector3d.x - (double)blockpos.getX()));
      this.writeFloat((float)(vector3d.y - (double)blockpos.getY()));
      this.writeFloat((float)(vector3d.z - (double)blockpos.getZ()));
      this.writeBoolean(pResult.isInside());
   }

   public int capacity() {
      return this.source.capacity();
   }

   public ByteBuf capacity(int p_capacity_1_) {
      return this.source.capacity(p_capacity_1_);
   }

   public int maxCapacity() {
      return this.source.maxCapacity();
   }

   public ByteBufAllocator alloc() {
      return this.source.alloc();
   }

   public ByteOrder order() {
      return this.source.order();
   }

   public ByteBuf order(ByteOrder p_order_1_) {
      return this.source.order(p_order_1_);
   }

   public ByteBuf unwrap() {
      return this.source.unwrap();
   }

   public boolean isDirect() {
      return this.source.isDirect();
   }

   public boolean isReadOnly() {
      return this.source.isReadOnly();
   }

   public ByteBuf asReadOnly() {
      return this.source.asReadOnly();
   }

   public int readerIndex() {
      return this.source.readerIndex();
   }

   public ByteBuf readerIndex(int p_readerIndex_1_) {
      return this.source.readerIndex(p_readerIndex_1_);
   }

   public int writerIndex() {
      return this.source.writerIndex();
   }

   public ByteBuf writerIndex(int p_writerIndex_1_) {
      return this.source.writerIndex(p_writerIndex_1_);
   }

   public ByteBuf setIndex(int p_setIndex_1_, int p_setIndex_2_) {
      return this.source.setIndex(p_setIndex_1_, p_setIndex_2_);
   }

   public int readableBytes() {
      return this.source.readableBytes();
   }

   public int writableBytes() {
      return this.source.writableBytes();
   }

   public int maxWritableBytes() {
      return this.source.maxWritableBytes();
   }

   public boolean isReadable() {
      return this.source.isReadable();
   }

   public boolean isReadable(int p_isReadable_1_) {
      return this.source.isReadable(p_isReadable_1_);
   }

   public boolean isWritable() {
      return this.source.isWritable();
   }

   public boolean isWritable(int p_isWritable_1_) {
      return this.source.isWritable(p_isWritable_1_);
   }

   public ByteBuf clear() {
      return this.source.clear();
   }

   public ByteBuf markReaderIndex() {
      return this.source.markReaderIndex();
   }

   public ByteBuf resetReaderIndex() {
      return this.source.resetReaderIndex();
   }

   public ByteBuf markWriterIndex() {
      return this.source.markWriterIndex();
   }

   public ByteBuf resetWriterIndex() {
      return this.source.resetWriterIndex();
   }

   public ByteBuf discardReadBytes() {
      return this.source.discardReadBytes();
   }

   public ByteBuf discardSomeReadBytes() {
      return this.source.discardSomeReadBytes();
   }

   public ByteBuf ensureWritable(int p_ensureWritable_1_) {
      return this.source.ensureWritable(p_ensureWritable_1_);
   }

   public int ensureWritable(int p_ensureWritable_1_, boolean p_ensureWritable_2_) {
      return this.source.ensureWritable(p_ensureWritable_1_, p_ensureWritable_2_);
   }

   public boolean getBoolean(int p_getBoolean_1_) {
      return this.source.getBoolean(p_getBoolean_1_);
   }

   public byte getByte(int p_getByte_1_) {
      return this.source.getByte(p_getByte_1_);
   }

   public short getUnsignedByte(int p_getUnsignedByte_1_) {
      return this.source.getUnsignedByte(p_getUnsignedByte_1_);
   }

   public short getShort(int p_getShort_1_) {
      return this.source.getShort(p_getShort_1_);
   }

   public short getShortLE(int p_getShortLE_1_) {
      return this.source.getShortLE(p_getShortLE_1_);
   }

   public int getUnsignedShort(int p_getUnsignedShort_1_) {
      return this.source.getUnsignedShort(p_getUnsignedShort_1_);
   }

   public int getUnsignedShortLE(int p_getUnsignedShortLE_1_) {
      return this.source.getUnsignedShortLE(p_getUnsignedShortLE_1_);
   }

   public int getMedium(int p_getMedium_1_) {
      return this.source.getMedium(p_getMedium_1_);
   }

   public int getMediumLE(int p_getMediumLE_1_) {
      return this.source.getMediumLE(p_getMediumLE_1_);
   }

   public int getUnsignedMedium(int p_getUnsignedMedium_1_) {
      return this.source.getUnsignedMedium(p_getUnsignedMedium_1_);
   }

   public int getUnsignedMediumLE(int p_getUnsignedMediumLE_1_) {
      return this.source.getUnsignedMediumLE(p_getUnsignedMediumLE_1_);
   }

   public int getInt(int p_getInt_1_) {
      return this.source.getInt(p_getInt_1_);
   }

   public int getIntLE(int p_getIntLE_1_) {
      return this.source.getIntLE(p_getIntLE_1_);
   }

   public long getUnsignedInt(int p_getUnsignedInt_1_) {
      return this.source.getUnsignedInt(p_getUnsignedInt_1_);
   }

   public long getUnsignedIntLE(int p_getUnsignedIntLE_1_) {
      return this.source.getUnsignedIntLE(p_getUnsignedIntLE_1_);
   }

   public long getLong(int p_getLong_1_) {
      return this.source.getLong(p_getLong_1_);
   }

   public long getLongLE(int p_getLongLE_1_) {
      return this.source.getLongLE(p_getLongLE_1_);
   }

   public char getChar(int p_getChar_1_) {
      return this.source.getChar(p_getChar_1_);
   }

   public float getFloat(int p_getFloat_1_) {
      return this.source.getFloat(p_getFloat_1_);
   }

   public double getDouble(int p_getDouble_1_) {
      return this.source.getDouble(p_getDouble_1_);
   }

   public ByteBuf getBytes(int p_getBytes_1_, ByteBuf p_getBytes_2_) {
      return this.source.getBytes(p_getBytes_1_, p_getBytes_2_);
   }

   public ByteBuf getBytes(int p_getBytes_1_, ByteBuf p_getBytes_2_, int p_getBytes_3_) {
      return this.source.getBytes(p_getBytes_1_, p_getBytes_2_, p_getBytes_3_);
   }

   public ByteBuf getBytes(int p_getBytes_1_, ByteBuf p_getBytes_2_, int p_getBytes_3_, int p_getBytes_4_) {
      return this.source.getBytes(p_getBytes_1_, p_getBytes_2_, p_getBytes_3_, p_getBytes_4_);
   }

   public ByteBuf getBytes(int p_getBytes_1_, byte[] p_getBytes_2_) {
      return this.source.getBytes(p_getBytes_1_, p_getBytes_2_);
   }

   public ByteBuf getBytes(int p_getBytes_1_, byte[] p_getBytes_2_, int p_getBytes_3_, int p_getBytes_4_) {
      return this.source.getBytes(p_getBytes_1_, p_getBytes_2_, p_getBytes_3_, p_getBytes_4_);
   }

   public ByteBuf getBytes(int p_getBytes_1_, ByteBuffer p_getBytes_2_) {
      return this.source.getBytes(p_getBytes_1_, p_getBytes_2_);
   }

   public ByteBuf getBytes(int p_getBytes_1_, OutputStream p_getBytes_2_, int p_getBytes_3_) throws IOException {
      return this.source.getBytes(p_getBytes_1_, p_getBytes_2_, p_getBytes_3_);
   }

   public int getBytes(int p_getBytes_1_, GatheringByteChannel p_getBytes_2_, int p_getBytes_3_) throws IOException {
      return this.source.getBytes(p_getBytes_1_, p_getBytes_2_, p_getBytes_3_);
   }

   public int getBytes(int p_getBytes_1_, FileChannel p_getBytes_2_, long p_getBytes_3_, int p_getBytes_5_) throws IOException {
      return this.source.getBytes(p_getBytes_1_, p_getBytes_2_, p_getBytes_3_, p_getBytes_5_);
   }

   public CharSequence getCharSequence(int p_getCharSequence_1_, int p_getCharSequence_2_, Charset p_getCharSequence_3_) {
      return this.source.getCharSequence(p_getCharSequence_1_, p_getCharSequence_2_, p_getCharSequence_3_);
   }

   public ByteBuf setBoolean(int p_setBoolean_1_, boolean p_setBoolean_2_) {
      return this.source.setBoolean(p_setBoolean_1_, p_setBoolean_2_);
   }

   public ByteBuf setByte(int p_setByte_1_, int p_setByte_2_) {
      return this.source.setByte(p_setByte_1_, p_setByte_2_);
   }

   public ByteBuf setShort(int p_setShort_1_, int p_setShort_2_) {
      return this.source.setShort(p_setShort_1_, p_setShort_2_);
   }

   public ByteBuf setShortLE(int p_setShortLE_1_, int p_setShortLE_2_) {
      return this.source.setShortLE(p_setShortLE_1_, p_setShortLE_2_);
   }

   public ByteBuf setMedium(int p_setMedium_1_, int p_setMedium_2_) {
      return this.source.setMedium(p_setMedium_1_, p_setMedium_2_);
   }

   public ByteBuf setMediumLE(int p_setMediumLE_1_, int p_setMediumLE_2_) {
      return this.source.setMediumLE(p_setMediumLE_1_, p_setMediumLE_2_);
   }

   public ByteBuf setInt(int p_setInt_1_, int p_setInt_2_) {
      return this.source.setInt(p_setInt_1_, p_setInt_2_);
   }

   public ByteBuf setIntLE(int p_setIntLE_1_, int p_setIntLE_2_) {
      return this.source.setIntLE(p_setIntLE_1_, p_setIntLE_2_);
   }

   public ByteBuf setLong(int p_setLong_1_, long p_setLong_2_) {
      return this.source.setLong(p_setLong_1_, p_setLong_2_);
   }

   public ByteBuf setLongLE(int p_setLongLE_1_, long p_setLongLE_2_) {
      return this.source.setLongLE(p_setLongLE_1_, p_setLongLE_2_);
   }

   public ByteBuf setChar(int p_setChar_1_, int p_setChar_2_) {
      return this.source.setChar(p_setChar_1_, p_setChar_2_);
   }

   public ByteBuf setFloat(int p_setFloat_1_, float p_setFloat_2_) {
      return this.source.setFloat(p_setFloat_1_, p_setFloat_2_);
   }

   public ByteBuf setDouble(int p_setDouble_1_, double p_setDouble_2_) {
      return this.source.setDouble(p_setDouble_1_, p_setDouble_2_);
   }

   public ByteBuf setBytes(int p_setBytes_1_, ByteBuf p_setBytes_2_) {
      return this.source.setBytes(p_setBytes_1_, p_setBytes_2_);
   }

   public ByteBuf setBytes(int p_setBytes_1_, ByteBuf p_setBytes_2_, int p_setBytes_3_) {
      return this.source.setBytes(p_setBytes_1_, p_setBytes_2_, p_setBytes_3_);
   }

   public ByteBuf setBytes(int p_setBytes_1_, ByteBuf p_setBytes_2_, int p_setBytes_3_, int p_setBytes_4_) {
      return this.source.setBytes(p_setBytes_1_, p_setBytes_2_, p_setBytes_3_, p_setBytes_4_);
   }

   public ByteBuf setBytes(int p_setBytes_1_, byte[] p_setBytes_2_) {
      return this.source.setBytes(p_setBytes_1_, p_setBytes_2_);
   }

   public ByteBuf setBytes(int p_setBytes_1_, byte[] p_setBytes_2_, int p_setBytes_3_, int p_setBytes_4_) {
      return this.source.setBytes(p_setBytes_1_, p_setBytes_2_, p_setBytes_3_, p_setBytes_4_);
   }

   public ByteBuf setBytes(int p_setBytes_1_, ByteBuffer p_setBytes_2_) {
      return this.source.setBytes(p_setBytes_1_, p_setBytes_2_);
   }

   public int setBytes(int p_setBytes_1_, InputStream p_setBytes_2_, int p_setBytes_3_) throws IOException {
      return this.source.setBytes(p_setBytes_1_, p_setBytes_2_, p_setBytes_3_);
   }

   public int setBytes(int p_setBytes_1_, ScatteringByteChannel p_setBytes_2_, int p_setBytes_3_) throws IOException {
      return this.source.setBytes(p_setBytes_1_, p_setBytes_2_, p_setBytes_3_);
   }

   public int setBytes(int p_setBytes_1_, FileChannel p_setBytes_2_, long p_setBytes_3_, int p_setBytes_5_) throws IOException {
      return this.source.setBytes(p_setBytes_1_, p_setBytes_2_, p_setBytes_3_, p_setBytes_5_);
   }

   public ByteBuf setZero(int p_setZero_1_, int p_setZero_2_) {
      return this.source.setZero(p_setZero_1_, p_setZero_2_);
   }

   public int setCharSequence(int p_setCharSequence_1_, CharSequence p_setCharSequence_2_, Charset p_setCharSequence_3_) {
      return this.source.setCharSequence(p_setCharSequence_1_, p_setCharSequence_2_, p_setCharSequence_3_);
   }

   public boolean readBoolean() {
      return this.source.readBoolean();
   }

   public byte readByte() {
      return this.source.readByte();
   }

   public short readUnsignedByte() {
      return this.source.readUnsignedByte();
   }

   public short readShort() {
      return this.source.readShort();
   }

   public short readShortLE() {
      return this.source.readShortLE();
   }

   public int readUnsignedShort() {
      return this.source.readUnsignedShort();
   }

   public int readUnsignedShortLE() {
      return this.source.readUnsignedShortLE();
   }

   public int readMedium() {
      return this.source.readMedium();
   }

   public int readMediumLE() {
      return this.source.readMediumLE();
   }

   public int readUnsignedMedium() {
      return this.source.readUnsignedMedium();
   }

   public int readUnsignedMediumLE() {
      return this.source.readUnsignedMediumLE();
   }

   public int readInt() {
      return this.source.readInt();
   }

   public int readIntLE() {
      return this.source.readIntLE();
   }

   public long readUnsignedInt() {
      return this.source.readUnsignedInt();
   }

   public long readUnsignedIntLE() {
      return this.source.readUnsignedIntLE();
   }

   public long readLong() {
      return this.source.readLong();
   }

   public long readLongLE() {
      return this.source.readLongLE();
   }

   public char readChar() {
      return this.source.readChar();
   }

   public float readFloat() {
      return this.source.readFloat();
   }

   public double readDouble() {
      return this.source.readDouble();
   }

   public ByteBuf readBytes(int p_readBytes_1_) {
      return this.source.readBytes(p_readBytes_1_);
   }

   public ByteBuf readSlice(int p_readSlice_1_) {
      return this.source.readSlice(p_readSlice_1_);
   }

   public ByteBuf readRetainedSlice(int p_readRetainedSlice_1_) {
      return this.source.readRetainedSlice(p_readRetainedSlice_1_);
   }

   public ByteBuf readBytes(ByteBuf p_readBytes_1_) {
      return this.source.readBytes(p_readBytes_1_);
   }

   public ByteBuf readBytes(ByteBuf p_readBytes_1_, int p_readBytes_2_) {
      return this.source.readBytes(p_readBytes_1_, p_readBytes_2_);
   }

   public ByteBuf readBytes(ByteBuf p_readBytes_1_, int p_readBytes_2_, int p_readBytes_3_) {
      return this.source.readBytes(p_readBytes_1_, p_readBytes_2_, p_readBytes_3_);
   }

   public ByteBuf readBytes(byte[] p_readBytes_1_) {
      return this.source.readBytes(p_readBytes_1_);
   }

   public ByteBuf readBytes(byte[] p_readBytes_1_, int p_readBytes_2_, int p_readBytes_3_) {
      return this.source.readBytes(p_readBytes_1_, p_readBytes_2_, p_readBytes_3_);
   }

   public ByteBuf readBytes(ByteBuffer p_readBytes_1_) {
      return this.source.readBytes(p_readBytes_1_);
   }

   public ByteBuf readBytes(OutputStream p_readBytes_1_, int p_readBytes_2_) throws IOException {
      return this.source.readBytes(p_readBytes_1_, p_readBytes_2_);
   }

   public int readBytes(GatheringByteChannel p_readBytes_1_, int p_readBytes_2_) throws IOException {
      return this.source.readBytes(p_readBytes_1_, p_readBytes_2_);
   }

   public CharSequence readCharSequence(int p_readCharSequence_1_, Charset p_readCharSequence_2_) {
      return this.source.readCharSequence(p_readCharSequence_1_, p_readCharSequence_2_);
   }

   public int readBytes(FileChannel p_readBytes_1_, long p_readBytes_2_, int p_readBytes_4_) throws IOException {
      return this.source.readBytes(p_readBytes_1_, p_readBytes_2_, p_readBytes_4_);
   }

   public ByteBuf skipBytes(int p_skipBytes_1_) {
      return this.source.skipBytes(p_skipBytes_1_);
   }

   public ByteBuf writeBoolean(boolean p_writeBoolean_1_) {
      return this.source.writeBoolean(p_writeBoolean_1_);
   }

   public ByteBuf writeByte(int p_writeByte_1_) {
      return this.source.writeByte(p_writeByte_1_);
   }

   public ByteBuf writeShort(int p_writeShort_1_) {
      return this.source.writeShort(p_writeShort_1_);
   }

   public ByteBuf writeShortLE(int p_writeShortLE_1_) {
      return this.source.writeShortLE(p_writeShortLE_1_);
   }

   public ByteBuf writeMedium(int p_writeMedium_1_) {
      return this.source.writeMedium(p_writeMedium_1_);
   }

   public ByteBuf writeMediumLE(int p_writeMediumLE_1_) {
      return this.source.writeMediumLE(p_writeMediumLE_1_);
   }

   public ByteBuf writeInt(int p_writeInt_1_) {
      return this.source.writeInt(p_writeInt_1_);
   }

   public ByteBuf writeIntLE(int p_writeIntLE_1_) {
      return this.source.writeIntLE(p_writeIntLE_1_);
   }

   public ByteBuf writeLong(long p_writeLong_1_) {
      return this.source.writeLong(p_writeLong_1_);
   }

   public ByteBuf writeLongLE(long p_writeLongLE_1_) {
      return this.source.writeLongLE(p_writeLongLE_1_);
   }

   public ByteBuf writeChar(int p_writeChar_1_) {
      return this.source.writeChar(p_writeChar_1_);
   }

   public ByteBuf writeFloat(float p_writeFloat_1_) {
      return this.source.writeFloat(p_writeFloat_1_);
   }

   public ByteBuf writeDouble(double p_writeDouble_1_) {
      return this.source.writeDouble(p_writeDouble_1_);
   }

   public ByteBuf writeBytes(ByteBuf p_writeBytes_1_) {
      return this.source.writeBytes(p_writeBytes_1_);
   }

   public ByteBuf writeBytes(ByteBuf p_writeBytes_1_, int p_writeBytes_2_) {
      return this.source.writeBytes(p_writeBytes_1_, p_writeBytes_2_);
   }

   public ByteBuf writeBytes(ByteBuf p_writeBytes_1_, int p_writeBytes_2_, int p_writeBytes_3_) {
      return this.source.writeBytes(p_writeBytes_1_, p_writeBytes_2_, p_writeBytes_3_);
   }

   public ByteBuf writeBytes(byte[] p_writeBytes_1_) {
      return this.source.writeBytes(p_writeBytes_1_);
   }

   public ByteBuf writeBytes(byte[] p_writeBytes_1_, int p_writeBytes_2_, int p_writeBytes_3_) {
      return this.source.writeBytes(p_writeBytes_1_, p_writeBytes_2_, p_writeBytes_3_);
   }

   public ByteBuf writeBytes(ByteBuffer p_writeBytes_1_) {
      return this.source.writeBytes(p_writeBytes_1_);
   }

   public int writeBytes(InputStream p_writeBytes_1_, int p_writeBytes_2_) throws IOException {
      return this.source.writeBytes(p_writeBytes_1_, p_writeBytes_2_);
   }

   public int writeBytes(ScatteringByteChannel p_writeBytes_1_, int p_writeBytes_2_) throws IOException {
      return this.source.writeBytes(p_writeBytes_1_, p_writeBytes_2_);
   }

   public int writeBytes(FileChannel p_writeBytes_1_, long p_writeBytes_2_, int p_writeBytes_4_) throws IOException {
      return this.source.writeBytes(p_writeBytes_1_, p_writeBytes_2_, p_writeBytes_4_);
   }

   public ByteBuf writeZero(int p_writeZero_1_) {
      return this.source.writeZero(p_writeZero_1_);
   }

   public int writeCharSequence(CharSequence p_writeCharSequence_1_, Charset p_writeCharSequence_2_) {
      return this.source.writeCharSequence(p_writeCharSequence_1_, p_writeCharSequence_2_);
   }

   public int indexOf(int p_indexOf_1_, int p_indexOf_2_, byte p_indexOf_3_) {
      return this.source.indexOf(p_indexOf_1_, p_indexOf_2_, p_indexOf_3_);
   }

   public int bytesBefore(byte p_bytesBefore_1_) {
      return this.source.bytesBefore(p_bytesBefore_1_);
   }

   public int bytesBefore(int p_bytesBefore_1_, byte p_bytesBefore_2_) {
      return this.source.bytesBefore(p_bytesBefore_1_, p_bytesBefore_2_);
   }

   public int bytesBefore(int p_bytesBefore_1_, int p_bytesBefore_2_, byte p_bytesBefore_3_) {
      return this.source.bytesBefore(p_bytesBefore_1_, p_bytesBefore_2_, p_bytesBefore_3_);
   }

   public int forEachByte(ByteProcessor p_forEachByte_1_) {
      return this.source.forEachByte(p_forEachByte_1_);
   }

   public int forEachByte(int p_forEachByte_1_, int p_forEachByte_2_, ByteProcessor p_forEachByte_3_) {
      return this.source.forEachByte(p_forEachByte_1_, p_forEachByte_2_, p_forEachByte_3_);
   }

   public int forEachByteDesc(ByteProcessor p_forEachByteDesc_1_) {
      return this.source.forEachByteDesc(p_forEachByteDesc_1_);
   }

   public int forEachByteDesc(int p_forEachByteDesc_1_, int p_forEachByteDesc_2_, ByteProcessor p_forEachByteDesc_3_) {
      return this.source.forEachByteDesc(p_forEachByteDesc_1_, p_forEachByteDesc_2_, p_forEachByteDesc_3_);
   }

   public ByteBuf copy() {
      return this.source.copy();
   }

   public ByteBuf copy(int p_copy_1_, int p_copy_2_) {
      return this.source.copy(p_copy_1_, p_copy_2_);
   }

   public ByteBuf slice() {
      return this.source.slice();
   }

   public ByteBuf retainedSlice() {
      return this.source.retainedSlice();
   }

   public ByteBuf slice(int p_slice_1_, int p_slice_2_) {
      return this.source.slice(p_slice_1_, p_slice_2_);
   }

   public ByteBuf retainedSlice(int p_retainedSlice_1_, int p_retainedSlice_2_) {
      return this.source.retainedSlice(p_retainedSlice_1_, p_retainedSlice_2_);
   }

   public ByteBuf duplicate() {
      return this.source.duplicate();
   }

   public ByteBuf retainedDuplicate() {
      return this.source.retainedDuplicate();
   }

   public int nioBufferCount() {
      return this.source.nioBufferCount();
   }

   public ByteBuffer nioBuffer() {
      return this.source.nioBuffer();
   }

   public ByteBuffer nioBuffer(int p_nioBuffer_1_, int p_nioBuffer_2_) {
      return this.source.nioBuffer(p_nioBuffer_1_, p_nioBuffer_2_);
   }

   public ByteBuffer internalNioBuffer(int p_internalNioBuffer_1_, int p_internalNioBuffer_2_) {
      return this.source.internalNioBuffer(p_internalNioBuffer_1_, p_internalNioBuffer_2_);
   }

   public ByteBuffer[] nioBuffers() {
      return this.source.nioBuffers();
   }

   public ByteBuffer[] nioBuffers(int p_nioBuffers_1_, int p_nioBuffers_2_) {
      return this.source.nioBuffers(p_nioBuffers_1_, p_nioBuffers_2_);
   }

   public boolean hasArray() {
      return this.source.hasArray();
   }

   public byte[] array() {
      return this.source.array();
   }

   public int arrayOffset() {
      return this.source.arrayOffset();
   }

   public boolean hasMemoryAddress() {
      return this.source.hasMemoryAddress();
   }

   public long memoryAddress() {
      return this.source.memoryAddress();
   }

   public String toString(Charset p_toString_1_) {
      return this.source.toString(p_toString_1_);
   }

   public String toString(int p_toString_1_, int p_toString_2_, Charset p_toString_3_) {
      return this.source.toString(p_toString_1_, p_toString_2_, p_toString_3_);
   }

   public int hashCode() {
      return this.source.hashCode();
   }

   public boolean equals(Object p_equals_1_) {
      return this.source.equals(p_equals_1_);
   }

   public int compareTo(ByteBuf p_compareTo_1_) {
      return this.source.compareTo(p_compareTo_1_);
   }

   public String toString() {
      return this.source.toString();
   }

   public ByteBuf retain(int p_retain_1_) {
      return this.source.retain(p_retain_1_);
   }

   public ByteBuf retain() {
      return this.source.retain();
   }

   public ByteBuf touch() {
      return this.source.touch();
   }

   public ByteBuf touch(Object p_touch_1_) {
      return this.source.touch(p_touch_1_);
   }

   public int refCnt() {
      return this.source.refCnt();
   }

   public boolean release() {
      return this.source.release();
   }

   public boolean release(int p_release_1_) {
      return this.source.release(p_release_1_);
   }
}
