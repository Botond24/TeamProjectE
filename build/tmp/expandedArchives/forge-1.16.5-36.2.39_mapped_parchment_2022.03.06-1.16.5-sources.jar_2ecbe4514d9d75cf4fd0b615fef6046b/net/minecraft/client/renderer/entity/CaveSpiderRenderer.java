package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.monster.CaveSpiderEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CaveSpiderRenderer extends SpiderRenderer<CaveSpiderEntity> {
   private static final ResourceLocation CAVE_SPIDER_LOCATION = new ResourceLocation("textures/entity/spider/cave_spider.png");

   public CaveSpiderRenderer(EntityRendererManager p_i46189_1_) {
      super(p_i46189_1_);
      this.shadowRadius *= 0.7F;
   }

   protected void scale(CaveSpiderEntity pLivingEntity, MatrixStack pMatrixStack, float pPartialTickTime) {
      pMatrixStack.scale(0.7F, 0.7F, 0.7F);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(CaveSpiderEntity pEntity) {
      return CAVE_SPIDER_LOCATION;
   }
}