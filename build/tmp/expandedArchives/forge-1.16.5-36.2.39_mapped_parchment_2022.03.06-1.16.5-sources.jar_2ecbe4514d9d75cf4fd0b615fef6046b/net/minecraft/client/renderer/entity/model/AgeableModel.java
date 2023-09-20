package net.minecraft.client.renderer.entity.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AgeableModel<E extends Entity> extends EntityModel<E> {
   private final boolean scaleHead;
   private final float yHeadOffset;
   private final float zHeadOffset;
   private final float babyHeadScale;
   private final float babyBodyScale;
   private final float bodyYOffset;

   protected AgeableModel(boolean p_i225943_1_, float p_i225943_2_, float p_i225943_3_) {
      this(p_i225943_1_, p_i225943_2_, p_i225943_3_, 2.0F, 2.0F, 24.0F);
   }

   protected AgeableModel(boolean p_i225944_1_, float p_i225944_2_, float p_i225944_3_, float p_i225944_4_, float p_i225944_5_, float p_i225944_6_) {
      this(RenderType::entityCutoutNoCull, p_i225944_1_, p_i225944_2_, p_i225944_3_, p_i225944_4_, p_i225944_5_, p_i225944_6_);
   }

   protected AgeableModel(Function<ResourceLocation, RenderType> p_i225942_1_, boolean p_i225942_2_, float p_i225942_3_, float p_i225942_4_, float p_i225942_5_, float p_i225942_6_, float p_i225942_7_) {
      super(p_i225942_1_);
      this.scaleHead = p_i225942_2_;
      this.yHeadOffset = p_i225942_3_;
      this.zHeadOffset = p_i225942_4_;
      this.babyHeadScale = p_i225942_5_;
      this.babyBodyScale = p_i225942_6_;
      this.bodyYOffset = p_i225942_7_;
   }

   protected AgeableModel() {
      this(false, 5.0F, 2.0F);
   }

   public void renderToBuffer(MatrixStack pMatrixStack, IVertexBuilder pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
      if (this.young) {
         pMatrixStack.pushPose();
         if (this.scaleHead) {
            float f = 1.5F / this.babyHeadScale;
            pMatrixStack.scale(f, f, f);
         }

         pMatrixStack.translate(0.0D, (double)(this.yHeadOffset / 16.0F), (double)(this.zHeadOffset / 16.0F));
         this.headParts().forEach((p_228230_8_) -> {
            p_228230_8_.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
         });
         pMatrixStack.popPose();
         pMatrixStack.pushPose();
         float f1 = 1.0F / this.babyBodyScale;
         pMatrixStack.scale(f1, f1, f1);
         pMatrixStack.translate(0.0D, (double)(this.bodyYOffset / 16.0F), 0.0D);
         this.bodyParts().forEach((p_228229_8_) -> {
            p_228229_8_.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
         });
         pMatrixStack.popPose();
      } else {
         this.headParts().forEach((p_228228_8_) -> {
            p_228228_8_.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
         });
         this.bodyParts().forEach((p_228227_8_) -> {
            p_228227_8_.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
         });
      }

   }

   protected abstract Iterable<ModelRenderer> headParts();

   protected abstract Iterable<ModelRenderer> bodyParts();
}