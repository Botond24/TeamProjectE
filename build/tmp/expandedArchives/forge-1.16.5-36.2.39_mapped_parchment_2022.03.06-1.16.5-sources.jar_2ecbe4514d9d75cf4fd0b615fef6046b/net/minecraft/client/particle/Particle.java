package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class Particle {
   private static final AxisAlignedBB INITIAL_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
   protected final ClientWorld level;
   protected double xo;
   protected double yo;
   protected double zo;
   protected double x;
   protected double y;
   protected double z;
   protected double xd;
   protected double yd;
   protected double zd;
   private AxisAlignedBB bb = INITIAL_AABB;
   protected boolean onGround;
   protected boolean hasPhysics = true;
   private boolean stoppedByCollision;
   protected boolean removed;
   protected float bbWidth = 0.6F;
   protected float bbHeight = 1.8F;
   protected final Random random = new Random();
   protected int age;
   protected int lifetime;
   protected float gravity;
   protected float rCol = 1.0F;
   protected float gCol = 1.0F;
   protected float bCol = 1.0F;
   protected float alpha = 1.0F;
   protected float roll;
   protected float oRoll;

   protected Particle(ClientWorld pLevel, double pX, double pY, double pZ) {
      this.level = pLevel;
      this.setSize(0.2F, 0.2F);
      this.setPos(pX, pY, pZ);
      this.xo = pX;
      this.yo = pY;
      this.zo = pZ;
      this.lifetime = (int)(4.0F / (this.random.nextFloat() * 0.9F + 0.1F));
   }

   public Particle(ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
      this(pLevel, pX, pY, pZ);
      this.xd = pXSpeed + (Math.random() * 2.0D - 1.0D) * (double)0.4F;
      this.yd = pYSpeed + (Math.random() * 2.0D - 1.0D) * (double)0.4F;
      this.zd = pZSpeed + (Math.random() * 2.0D - 1.0D) * (double)0.4F;
      float f = (float)(Math.random() + Math.random() + 1.0D) * 0.15F;
      float f1 = MathHelper.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
      this.xd = this.xd / (double)f1 * (double)f * (double)0.4F;
      this.yd = this.yd / (double)f1 * (double)f * (double)0.4F + (double)0.1F;
      this.zd = this.zd / (double)f1 * (double)f * (double)0.4F;
   }

   public Particle setPower(float pMultiplier) {
      this.xd *= (double)pMultiplier;
      this.yd = (this.yd - (double)0.1F) * (double)pMultiplier + (double)0.1F;
      this.zd *= (double)pMultiplier;
      return this;
   }

   public Particle scale(float pScale) {
      this.setSize(0.2F * pScale, 0.2F * pScale);
      return this;
   }

   public void setColor(float pParticleRed, float pParticleGreen, float pParticleBlue) {
      this.rCol = pParticleRed;
      this.gCol = pParticleGreen;
      this.bCol = pParticleBlue;
   }

   /**
    * Sets the particle alpha (float)
    */
   protected void setAlpha(float pAlpha) {
      this.alpha = pAlpha;
   }

   public void setLifetime(int pParticleLifeTime) {
      this.lifetime = pParticleLifeTime;
   }

   public int getLifetime() {
      return this.lifetime;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         this.yd -= 0.04D * (double)this.gravity;
         this.move(this.xd, this.yd, this.zd);
         this.xd *= (double)0.98F;
         this.yd *= (double)0.98F;
         this.zd *= (double)0.98F;
         if (this.onGround) {
            this.xd *= (double)0.7F;
            this.zd *= (double)0.7F;
         }

      }
   }

   public abstract void render(IVertexBuilder pBuffer, ActiveRenderInfo pRenderInfo, float pPartialTicks);

   public abstract IParticleRenderType getRenderType();

   public String toString() {
      return this.getClass().getSimpleName() + ", Pos (" + this.x + "," + this.y + "," + this.z + "), RGBA (" + this.rCol + "," + this.gCol + "," + this.bCol + "," + this.alpha + "), Age " + this.age;
   }

   /**
    * Called to indicate that this particle effect has expired and should be discontinued.
    */
   public void remove() {
      this.removed = true;
   }

   protected void setSize(float pWidth, float pHeight) {
      if (pWidth != this.bbWidth || pHeight != this.bbHeight) {
         this.bbWidth = pWidth;
         this.bbHeight = pHeight;
         AxisAlignedBB axisalignedbb = this.getBoundingBox();
         double d0 = (axisalignedbb.minX + axisalignedbb.maxX - (double)pWidth) / 2.0D;
         double d1 = (axisalignedbb.minZ + axisalignedbb.maxZ - (double)pWidth) / 2.0D;
         this.setBoundingBox(new AxisAlignedBB(d0, axisalignedbb.minY, d1, d0 + (double)this.bbWidth, axisalignedbb.minY + (double)this.bbHeight, d1 + (double)this.bbWidth));
      }

   }

   public void setPos(double pX, double pY, double pZ) {
      this.x = pX;
      this.y = pY;
      this.z = pZ;
      float f = this.bbWidth / 2.0F;
      float f1 = this.bbHeight;
      this.setBoundingBox(new AxisAlignedBB(pX - (double)f, pY, pZ - (double)f, pX + (double)f, pY + (double)f1, pZ + (double)f));
   }

   public void move(double pX, double pY, double pZ) {
      if (!this.stoppedByCollision) {
         double d0 = pX;
         double d1 = pY;
         double d2 = pZ;
         if (this.hasPhysics && (pX != 0.0D || pY != 0.0D || pZ != 0.0D)) {
            Vector3d vector3d = Entity.collideBoundingBoxHeuristically((Entity)null, new Vector3d(pX, pY, pZ), this.getBoundingBox(), this.level, ISelectionContext.empty(), new ReuseableStream<>(Stream.empty()));
            pX = vector3d.x;
            pY = vector3d.y;
            pZ = vector3d.z;
         }

         if (pX != 0.0D || pY != 0.0D || pZ != 0.0D) {
            this.setBoundingBox(this.getBoundingBox().move(pX, pY, pZ));
            this.setLocationFromBoundingbox();
         }

         if (Math.abs(d1) >= (double)1.0E-5F && Math.abs(pY) < (double)1.0E-5F) {
            this.stoppedByCollision = true;
         }

         this.onGround = d1 != pY && d1 < 0.0D;
         if (d0 != pX) {
            this.xd = 0.0D;
         }

         if (d2 != pZ) {
            this.zd = 0.0D;
         }

      }
   }

   protected void setLocationFromBoundingbox() {
      AxisAlignedBB axisalignedbb = this.getBoundingBox();
      this.x = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
      this.y = axisalignedbb.minY;
      this.z = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
   }

   protected int getLightColor(float pPartialTick) {
      BlockPos blockpos = new BlockPos(this.x, this.y, this.z);
      return this.level.hasChunkAt(blockpos) ? WorldRenderer.getLightColor(this.level, blockpos) : 0;
   }

   /**
    * Returns true if this effect has not yet expired. "I feel happy! I feel happy!"
    */
   public boolean isAlive() {
      return !this.removed;
   }

   public AxisAlignedBB getBoundingBox() {
      return this.bb;
   }

   public void setBoundingBox(AxisAlignedBB pBb) {
      this.bb = pBb;
   }

   /**
    * Forge added method that controls if a particle should be culled to it's bounding box.
    * Default behaviour is culling enabled
    */
   public boolean shouldCull() {
      return true;
   }
}
