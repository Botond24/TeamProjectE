package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.model.MagmaCubeModel;
import net.minecraft.entity.monster.MagmaCubeEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MagmaCubeRenderer extends MobRenderer<MagmaCubeEntity, MagmaCubeModel<MagmaCubeEntity>> {
   private static final ResourceLocation MAGMACUBE_LOCATION = new ResourceLocation("textures/entity/slime/magmacube.png");

   public MagmaCubeRenderer(EntityRendererManager p_i46159_1_) {
      super(p_i46159_1_, new MagmaCubeModel<>(), 0.25F);
   }

   protected int getBlockLightLevel(MagmaCubeEntity pEntity, BlockPos pPos) {
      return 15;
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(MagmaCubeEntity pEntity) {
      return MAGMACUBE_LOCATION;
   }

   protected void scale(MagmaCubeEntity pLivingEntity, MatrixStack pMatrixStack, float pPartialTickTime) {
      int i = pLivingEntity.getSize();
      float f = MathHelper.lerp(pPartialTickTime, pLivingEntity.oSquish, pLivingEntity.squish) / ((float)i * 0.5F + 1.0F);
      float f1 = 1.0F / (f + 1.0F);
      pMatrixStack.scale(f1 * (float)i, 1.0F / f1 * (float)i, f1 * (float)i);
   }
}