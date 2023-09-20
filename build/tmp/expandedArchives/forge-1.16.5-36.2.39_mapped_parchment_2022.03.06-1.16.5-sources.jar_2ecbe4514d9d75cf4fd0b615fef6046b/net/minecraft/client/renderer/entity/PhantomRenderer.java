package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.layers.PhantomEyesLayer;
import net.minecraft.client.renderer.entity.model.PhantomModel;
import net.minecraft.entity.monster.PhantomEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PhantomRenderer extends MobRenderer<PhantomEntity, PhantomModel<PhantomEntity>> {
   private static final ResourceLocation PHANTOM_LOCATION = new ResourceLocation("textures/entity/phantom.png");

   public PhantomRenderer(EntityRendererManager p_i48829_1_) {
      super(p_i48829_1_, new PhantomModel<>(), 0.75F);
      this.addLayer(new PhantomEyesLayer<>(this));
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(PhantomEntity pEntity) {
      return PHANTOM_LOCATION;
   }

   protected void scale(PhantomEntity pLivingEntity, MatrixStack pMatrixStack, float pPartialTickTime) {
      int i = pLivingEntity.getPhantomSize();
      float f = 1.0F + 0.15F * (float)i;
      pMatrixStack.scale(f, f, f);
      pMatrixStack.translate(0.0D, 1.3125D, 0.1875D);
   }

   protected void setupRotations(PhantomEntity pEntityLiving, MatrixStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
      super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
      pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(pEntityLiving.xRot));
   }
}