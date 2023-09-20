package net.minecraft.client.renderer.entity.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GenericHeadModel extends Model {
   protected final ModelRenderer head;

   public GenericHeadModel() {
      this(0, 35, 64, 64);
   }

   public GenericHeadModel(int p_i51060_1_, int p_i51060_2_, int p_i51060_3_, int p_i51060_4_) {
      super(RenderType::entityTranslucent);
      this.texWidth = p_i51060_3_;
      this.texHeight = p_i51060_4_;
      this.head = new ModelRenderer(this, p_i51060_1_, p_i51060_2_);
      this.head.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F);
      this.head.setPos(0.0F, 0.0F, 0.0F);
   }

   public void setupAnim(float p_225603_1_, float p_225603_2_, float p_225603_3_) {
      this.head.yRot = p_225603_2_ * ((float)Math.PI / 180F);
      this.head.xRot = p_225603_3_ * ((float)Math.PI / 180F);
   }

   public void renderToBuffer(MatrixStack pMatrixStack, IVertexBuilder pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
      this.head.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
   }
}