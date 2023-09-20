package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.layers.WitchHeldItemLayer;
import net.minecraft.client.renderer.entity.model.WitchModel;
import net.minecraft.entity.monster.WitchEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitchRenderer extends MobRenderer<WitchEntity, WitchModel<WitchEntity>> {
   private static final ResourceLocation WITCH_LOCATION = new ResourceLocation("textures/entity/witch.png");

   public WitchRenderer(EntityRendererManager p_i46131_1_) {
      super(p_i46131_1_, new WitchModel<>(0.0F), 0.5F);
      this.addLayer(new WitchHeldItemLayer<>(this));
   }

   public void render(WitchEntity pEntity, float pEntityYaw, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight) {
      this.model.setHoldingItem(!pEntity.getMainHandItem().isEmpty());
      super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(WitchEntity pEntity) {
      return WITCH_LOCATION;
   }

   protected void scale(WitchEntity pLivingEntity, MatrixStack pMatrixStack, float pPartialTickTime) {
      float f = 0.9375F;
      pMatrixStack.scale(0.9375F, 0.9375F, 0.9375F);
   }
}