package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.layers.TropicalFishPatternLayer;
import net.minecraft.client.renderer.entity.model.AbstractTropicalFishModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.TropicalFishAModel;
import net.minecraft.client.renderer.entity.model.TropicalFishBModel;
import net.minecraft.entity.passive.fish.TropicalFishEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TropicalFishRenderer extends MobRenderer<TropicalFishEntity, EntityModel<TropicalFishEntity>> {
   /** Breaking recompile intentionally since modelA/B incorrectly mapped. */
   private final TropicalFishAModel<TropicalFishEntity> modelA = new TropicalFishAModel<>(0.0F);
   /** Breaking recompile intentionally since modelA/B incorrectly mapped. */
   private final TropicalFishBModel<TropicalFishEntity> modelB = new TropicalFishBModel<>(0.0F);

   public TropicalFishRenderer(EntityRendererManager p_i48889_1_) {
      super(p_i48889_1_, new TropicalFishAModel<>(0.0F), 0.15F);
      this.addLayer(new TropicalFishPatternLayer(this));
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(TropicalFishEntity pEntity) {
      return pEntity.getBaseTextureLocation();
   }

   public void render(TropicalFishEntity pEntity, float pEntityYaw, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight) {
      AbstractTropicalFishModel<TropicalFishEntity> abstracttropicalfishmodel = (AbstractTropicalFishModel<TropicalFishEntity>)(pEntity.getBaseVariant() == 0 ? this.modelA : this.modelB);
      this.model = abstracttropicalfishmodel;
      float[] afloat = pEntity.getBaseColor();
      abstracttropicalfishmodel.setColor(afloat[0], afloat[1], afloat[2]);
      super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
      abstracttropicalfishmodel.setColor(1.0F, 1.0F, 1.0F);
   }

   protected void setupRotations(TropicalFishEntity pEntityLiving, MatrixStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
      super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
      float f = 4.3F * MathHelper.sin(0.6F * pAgeInTicks);
      pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f));
      if (!pEntityLiving.isInWater()) {
         pMatrixStack.translate((double)0.2F, (double)0.1F, 0.0D);
         pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
      }

   }
}