package net.minecraft.client.renderer.entity.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TridentModel extends Model {
   public static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/trident.png");
   private final ModelRenderer pole = new ModelRenderer(32, 32, 0, 6);

   public TridentModel() {
      super(RenderType::entitySolid);
      this.pole.addBox(-0.5F, 2.0F, -0.5F, 1.0F, 25.0F, 1.0F, 0.0F);
      ModelRenderer modelrenderer = new ModelRenderer(32, 32, 4, 0);
      modelrenderer.addBox(-1.5F, 0.0F, -0.5F, 3.0F, 2.0F, 1.0F);
      this.pole.addChild(modelrenderer);
      ModelRenderer modelrenderer1 = new ModelRenderer(32, 32, 4, 3);
      modelrenderer1.addBox(-2.5F, -3.0F, -0.5F, 1.0F, 4.0F, 1.0F);
      this.pole.addChild(modelrenderer1);
      ModelRenderer modelrenderer2 = new ModelRenderer(32, 32, 0, 0);
      modelrenderer2.addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F, 0.0F);
      this.pole.addChild(modelrenderer2);
      ModelRenderer modelrenderer3 = new ModelRenderer(32, 32, 4, 3);
      modelrenderer3.mirror = true;
      modelrenderer3.addBox(1.5F, -3.0F, -0.5F, 1.0F, 4.0F, 1.0F);
      this.pole.addChild(modelrenderer3);
   }

   public void renderToBuffer(MatrixStack pMatrixStack, IVertexBuilder pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
      this.pole.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
   }
}