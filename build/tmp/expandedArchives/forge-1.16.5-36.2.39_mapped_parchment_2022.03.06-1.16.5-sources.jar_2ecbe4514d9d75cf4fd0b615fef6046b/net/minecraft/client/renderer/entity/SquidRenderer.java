package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.model.SquidModel;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SquidRenderer extends MobRenderer<SquidEntity, SquidModel<SquidEntity>> {
   private static final ResourceLocation SQUID_LOCATION = new ResourceLocation("textures/entity/squid.png");

   public SquidRenderer(EntityRendererManager p_i47192_1_) {
      super(p_i47192_1_, new SquidModel<>(), 0.7F);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(SquidEntity pEntity) {
      return SQUID_LOCATION;
   }

   protected void setupRotations(SquidEntity pEntityLiving, MatrixStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
      float f = MathHelper.lerp(pPartialTicks, pEntityLiving.xBodyRotO, pEntityLiving.xBodyRot);
      float f1 = MathHelper.lerp(pPartialTicks, pEntityLiving.zBodyRotO, pEntityLiving.zBodyRot);
      pMatrixStack.translate(0.0D, 0.5D, 0.0D);
      pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - pRotationYaw));
      pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(f));
      pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f1));
      pMatrixStack.translate(0.0D, (double)-1.2F, 0.0D);
   }

   /**
    * Defines what float the third param in setRotationAngles of ModelBase is
    */
   protected float getBob(SquidEntity pLivingBase, float pPartialTicks) {
      return MathHelper.lerp(pPartialTicks, pLivingBase.oldTentacleAngle, pLivingBase.tentacleAngle);
   }
}