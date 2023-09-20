package net.minecraft.client.renderer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ActiveRenderInfo {
   private boolean initialized;
   private IBlockReader level;
   private Entity entity;
   private Vector3d position = Vector3d.ZERO;
   private final BlockPos.Mutable blockPosition = new BlockPos.Mutable();
   private final Vector3f forwards = new Vector3f(0.0F, 0.0F, 1.0F);
   private final Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
   private final Vector3f left = new Vector3f(1.0F, 0.0F, 0.0F);
   private float xRot;
   private float yRot;
   private final Quaternion rotation = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
   private boolean detached;
   private boolean mirror;
   private float eyeHeight;
   private float eyeHeightOld;

   public void setup(IBlockReader pLevel, Entity pRenderViewEntity, boolean pThirdPerson, boolean pThirdPersonReverse, float pPartialTicks) {
      this.initialized = true;
      this.level = pLevel;
      this.entity = pRenderViewEntity;
      this.detached = pThirdPerson;
      this.mirror = pThirdPersonReverse;
      this.setRotation(pRenderViewEntity.getViewYRot(pPartialTicks), pRenderViewEntity.getViewXRot(pPartialTicks));
      this.setPosition(MathHelper.lerp((double)pPartialTicks, pRenderViewEntity.xo, pRenderViewEntity.getX()), MathHelper.lerp((double)pPartialTicks, pRenderViewEntity.yo, pRenderViewEntity.getY()) + (double)MathHelper.lerp(pPartialTicks, this.eyeHeightOld, this.eyeHeight), MathHelper.lerp((double)pPartialTicks, pRenderViewEntity.zo, pRenderViewEntity.getZ()));
      if (pThirdPerson) {
         if (pThirdPersonReverse) {
            this.setRotation(this.yRot + 180.0F, -this.xRot);
         }

         this.move(-this.getMaxZoom(4.0D), 0.0D, 0.0D);
      } else if (pRenderViewEntity instanceof LivingEntity && ((LivingEntity)pRenderViewEntity).isSleeping()) {
         Direction direction = ((LivingEntity)pRenderViewEntity).getBedOrientation();
         this.setRotation(direction != null ? direction.toYRot() - 180.0F : 0.0F, 0.0F);
         this.move(0.0D, 0.3D, 0.0D);
      }

   }

   public void tick() {
      if (this.entity != null) {
         this.eyeHeightOld = this.eyeHeight;
         this.eyeHeight += (this.entity.getEyeHeight() - this.eyeHeight) * 0.5F;
      }

   }

   /**
    * Checks for collision of the third person camera and returns the distance
    */
   private double getMaxZoom(double pStartingDistance) {
      for(int i = 0; i < 8; ++i) {
         float f = (float)((i & 1) * 2 - 1);
         float f1 = (float)((i >> 1 & 1) * 2 - 1);
         float f2 = (float)((i >> 2 & 1) * 2 - 1);
         f = f * 0.1F;
         f1 = f1 * 0.1F;
         f2 = f2 * 0.1F;
         Vector3d vector3d = this.position.add((double)f, (double)f1, (double)f2);
         Vector3d vector3d1 = new Vector3d(this.position.x - (double)this.forwards.x() * pStartingDistance + (double)f + (double)f2, this.position.y - (double)this.forwards.y() * pStartingDistance + (double)f1, this.position.z - (double)this.forwards.z() * pStartingDistance + (double)f2);
         RayTraceResult raytraceresult = this.level.clip(new RayTraceContext(vector3d, vector3d1, RayTraceContext.BlockMode.VISUAL, RayTraceContext.FluidMode.NONE, this.entity));
         if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
            double d0 = raytraceresult.getLocation().distanceTo(this.position);
            if (d0 < pStartingDistance) {
               pStartingDistance = d0;
            }
         }
      }

      return pStartingDistance;
   }

   /**
    * Moves the render position relative to the view direction, for third person camera
    */
   protected void move(double pDistanceOffset, double pVerticalOffset, double pHorizontalOffset) {
      double d0 = (double)this.forwards.x() * pDistanceOffset + (double)this.up.x() * pVerticalOffset + (double)this.left.x() * pHorizontalOffset;
      double d1 = (double)this.forwards.y() * pDistanceOffset + (double)this.up.y() * pVerticalOffset + (double)this.left.y() * pHorizontalOffset;
      double d2 = (double)this.forwards.z() * pDistanceOffset + (double)this.up.z() * pVerticalOffset + (double)this.left.z() * pHorizontalOffset;
      this.setPosition(new Vector3d(this.position.x + d0, this.position.y + d1, this.position.z + d2));
   }

   protected void setRotation(float pPitch, float pYaw) {
      this.xRot = pYaw;
      this.yRot = pPitch;
      this.rotation.set(0.0F, 0.0F, 0.0F, 1.0F);
      this.rotation.mul(Vector3f.YP.rotationDegrees(-pPitch));
      this.rotation.mul(Vector3f.XP.rotationDegrees(pYaw));
      this.forwards.set(0.0F, 0.0F, 1.0F);
      this.forwards.transform(this.rotation);
      this.up.set(0.0F, 1.0F, 0.0F);
      this.up.transform(this.rotation);
      this.left.set(1.0F, 0.0F, 0.0F);
      this.left.transform(this.rotation);
   }

   /**
    * Sets the position and blockpos of the active render
    */
   protected void setPosition(double pX, double pY, double pZ) {
      this.setPosition(new Vector3d(pX, pY, pZ));
   }

   protected void setPosition(Vector3d pPos) {
      this.position = pPos;
      this.blockPosition.set(pPos.x, pPos.y, pPos.z);
   }

   public Vector3d getPosition() {
      return this.position;
   }

   public BlockPos getBlockPosition() {
      return this.blockPosition;
   }

   public float getXRot() {
      return this.xRot;
   }

   public float getYRot() {
      return this.yRot;
   }

   public Quaternion rotation() {
      return this.rotation;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public boolean isInitialized() {
      return this.initialized;
   }

   public boolean isDetached() {
      return this.detached;
   }

   public FluidState getFluidInCamera() {
      if (!this.initialized) {
         return Fluids.EMPTY.defaultFluidState();
      } else {
         FluidState fluidstate = this.level.getFluidState(this.blockPosition);
         return !fluidstate.isEmpty() && this.position.y >= (double)((float)this.blockPosition.getY() + fluidstate.getHeight(this.level, this.blockPosition)) ? Fluids.EMPTY.defaultFluidState() : fluidstate;
      }
   }

   public final Vector3f getLookVector() {
      return this.forwards;
   }

   public final Vector3f getUpVector() {
      return this.up;
   }

   public void reset() {
      this.level = null;
      this.entity = null;
      this.initialized = false;
   }

   public void setAnglesInternal(float yaw, float pitch) {
      this.yRot = yaw;
      this.xRot = pitch;
   }

   public net.minecraft.block.BlockState getBlockAtCamera() {
      if (!this.initialized)
         return net.minecraft.block.Blocks.AIR.defaultBlockState();
      else
         return this.level.getBlockState(this.blockPosition).getStateAtViewpoint(this.level, this.blockPosition, this.position);
   }
}
