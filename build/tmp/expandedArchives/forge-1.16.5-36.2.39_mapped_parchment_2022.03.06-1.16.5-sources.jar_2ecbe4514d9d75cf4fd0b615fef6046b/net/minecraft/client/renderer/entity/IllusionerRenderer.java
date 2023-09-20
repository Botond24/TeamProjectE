package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.model.IllagerModel;
import net.minecraft.entity.monster.IllusionerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IllusionerRenderer extends IllagerRenderer<IllusionerEntity> {
   private static final ResourceLocation ILLUSIONER = new ResourceLocation("textures/entity/illager/illusioner.png");

   public IllusionerRenderer(EntityRendererManager p_i47477_1_) {
      super(p_i47477_1_, new IllagerModel<>(0.0F, 0.0F, 64, 64), 0.5F);
      this.addLayer(new HeldItemLayer<IllusionerEntity, IllagerModel<IllusionerEntity>>(this) {
         public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight, IllusionerEntity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
            if (pLivingEntity.isCastingSpell() || pLivingEntity.isAggressive()) {
               super.render(pMatrixStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pPartialTicks, pAgeInTicks, pNetHeadYaw, pHeadPitch);
            }

         }
      });
      this.model.getHat().visible = true;
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(IllusionerEntity pEntity) {
      return ILLUSIONER;
   }

   public void render(IllusionerEntity pEntity, float pEntityYaw, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight) {
      if (pEntity.isInvisible()) {
         Vector3d[] avector3d = pEntity.getIllusionOffsets(pPartialTicks);
         float f = this.getBob(pEntity, pPartialTicks);

         for(int i = 0; i < avector3d.length; ++i) {
            pMatrixStack.pushPose();
            pMatrixStack.translate(avector3d[i].x + (double)MathHelper.cos((float)i + f * 0.5F) * 0.025D, avector3d[i].y + (double)MathHelper.cos((float)i + f * 0.75F) * 0.0125D, avector3d[i].z + (double)MathHelper.cos((float)i + f * 0.7F) * 0.025D);
            super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
            pMatrixStack.popPose();
         }
      } else {
         super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
      }

   }

   protected boolean isBodyVisible(IllusionerEntity pLivingEntity) {
      return true;
   }
}