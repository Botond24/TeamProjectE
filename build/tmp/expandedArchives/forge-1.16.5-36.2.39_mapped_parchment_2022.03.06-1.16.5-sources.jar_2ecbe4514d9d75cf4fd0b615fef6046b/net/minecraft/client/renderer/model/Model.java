package net.minecraft.client.renderer.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class Model implements Consumer<ModelRenderer> {
   protected final Function<ResourceLocation, RenderType> renderType;
   public int texWidth = 64;
   public int texHeight = 32;

   public Model(Function<ResourceLocation, RenderType> p_i225947_1_) {
      this.renderType = p_i225947_1_;
   }

   public void accept(ModelRenderer p_accept_1_) {
   }

   public final RenderType renderType(ResourceLocation pLocation) {
      return this.renderType.apply(pLocation);
   }

   public abstract void renderToBuffer(MatrixStack pMatrixStack, IVertexBuilder pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha);
}