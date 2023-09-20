package net.minecraft.network.play.server;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SExplosionPacket implements IPacket<IClientPlayNetHandler> {
   private double x;
   private double y;
   private double z;
   private float power;
   private List<BlockPos> toBlow;
   private float knockbackX;
   private float knockbackY;
   private float knockbackZ;

   public SExplosionPacket() {
   }

   public SExplosionPacket(double pX, double pY, double pZ, float pPower, List<BlockPos> pToBlow, Vector3d pKnockback) {
      this.x = pX;
      this.y = pY;
      this.z = pZ;
      this.power = pPower;
      this.toBlow = Lists.newArrayList(pToBlow);
      if (pKnockback != null) {
         this.knockbackX = (float)pKnockback.x;
         this.knockbackY = (float)pKnockback.y;
         this.knockbackZ = (float)pKnockback.z;
      }

   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.x = (double)p_148837_1_.readFloat();
      this.y = (double)p_148837_1_.readFloat();
      this.z = (double)p_148837_1_.readFloat();
      this.power = p_148837_1_.readFloat();
      int i = p_148837_1_.readInt();
      this.toBlow = Lists.newArrayListWithCapacity(i);
      int j = MathHelper.floor(this.x);
      int k = MathHelper.floor(this.y);
      int l = MathHelper.floor(this.z);

      for(int i1 = 0; i1 < i; ++i1) {
         int j1 = p_148837_1_.readByte() + j;
         int k1 = p_148837_1_.readByte() + k;
         int l1 = p_148837_1_.readByte() + l;
         this.toBlow.add(new BlockPos(j1, k1, l1));
      }

      this.knockbackX = p_148837_1_.readFloat();
      this.knockbackY = p_148837_1_.readFloat();
      this.knockbackZ = p_148837_1_.readFloat();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeFloat((float)this.x);
      pBuffer.writeFloat((float)this.y);
      pBuffer.writeFloat((float)this.z);
      pBuffer.writeFloat(this.power);
      pBuffer.writeInt(this.toBlow.size());
      int i = MathHelper.floor(this.x);
      int j = MathHelper.floor(this.y);
      int k = MathHelper.floor(this.z);

      for(BlockPos blockpos : this.toBlow) {
         int l = blockpos.getX() - i;
         int i1 = blockpos.getY() - j;
         int j1 = blockpos.getZ() - k;
         pBuffer.writeByte(l);
         pBuffer.writeByte(i1);
         pBuffer.writeByte(j1);
      }

      pBuffer.writeFloat(this.knockbackX);
      pBuffer.writeFloat(this.knockbackY);
      pBuffer.writeFloat(this.knockbackZ);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleExplosion(this);
   }

   @OnlyIn(Dist.CLIENT)
   public float getKnockbackX() {
      return this.knockbackX;
   }

   @OnlyIn(Dist.CLIENT)
   public float getKnockbackY() {
      return this.knockbackY;
   }

   @OnlyIn(Dist.CLIENT)
   public float getKnockbackZ() {
      return this.knockbackZ;
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
   public float getPower() {
      return this.power;
   }

   @OnlyIn(Dist.CLIENT)
   public List<BlockPos> getToBlow() {
      return this.toBlow;
   }
}