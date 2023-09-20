package net.minecraft.client.renderer.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallBannerBlock;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.item.DyeColor;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BannerTileEntityRenderer extends TileEntityRenderer<BannerTileEntity> {
   private final ModelRenderer flag = makeFlag();
   private final ModelRenderer pole = new ModelRenderer(64, 64, 44, 0);
   private final ModelRenderer bar;

   public BannerTileEntityRenderer(TileEntityRendererDispatcher p_i226002_1_) {
      super(p_i226002_1_);
      this.pole.addBox(-1.0F, -30.0F, -1.0F, 2.0F, 42.0F, 2.0F, 0.0F);
      this.bar = new ModelRenderer(64, 64, 0, 42);
      this.bar.addBox(-10.0F, -32.0F, -1.0F, 20.0F, 2.0F, 2.0F, 0.0F);
   }

   public static ModelRenderer makeFlag() {
      ModelRenderer modelrenderer = new ModelRenderer(64, 64, 0, 0);
      modelrenderer.addBox(-10.0F, 0.0F, -2.0F, 20.0F, 40.0F, 1.0F, 0.0F);
      return modelrenderer;
   }

   public void render(BannerTileEntity pBlockEntity, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, int pCombinedOverlay) {
      List<Pair<BannerPattern, DyeColor>> list = pBlockEntity.getPatterns();
      if (list != null) {
         float f = 0.6666667F;
         boolean flag = pBlockEntity.getLevel() == null;
         pMatrixStack.pushPose();
         long i;
         if (flag) {
            i = 0L;
            pMatrixStack.translate(0.5D, 0.5D, 0.5D);
            this.pole.visible = true;
         } else {
            i = pBlockEntity.getLevel().getGameTime();
            BlockState blockstate = pBlockEntity.getBlockState();
            if (blockstate.getBlock() instanceof BannerBlock) {
               pMatrixStack.translate(0.5D, 0.5D, 0.5D);
               float f1 = (float)(-blockstate.getValue(BannerBlock.ROTATION) * 360) / 16.0F;
               pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f1));
               this.pole.visible = true;
            } else {
               pMatrixStack.translate(0.5D, (double)-0.16666667F, 0.5D);
               float f3 = -blockstate.getValue(WallBannerBlock.FACING).toYRot();
               pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f3));
               pMatrixStack.translate(0.0D, -0.3125D, -0.4375D);
               this.pole.visible = false;
            }
         }

         pMatrixStack.pushPose();
         pMatrixStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
         IVertexBuilder ivertexbuilder = ModelBakery.BANNER_BASE.buffer(pBuffer, RenderType::entitySolid);
         this.pole.render(pMatrixStack, ivertexbuilder, pCombinedLight, pCombinedOverlay);
         this.bar.render(pMatrixStack, ivertexbuilder, pCombinedLight, pCombinedOverlay);
         BlockPos blockpos = pBlockEntity.getBlockPos();
         float f2 = ((float)Math.floorMod((long)(blockpos.getX() * 7 + blockpos.getY() * 9 + blockpos.getZ() * 13) + i, 100L) + pPartialTicks) / 100.0F;
         this.flag.xRot = (-0.0125F + 0.01F * MathHelper.cos(((float)Math.PI * 2F) * f2)) * (float)Math.PI;
         this.flag.y = -32.0F;
         renderPatterns(pMatrixStack, pBuffer, pCombinedLight, pCombinedOverlay, this.flag, ModelBakery.BANNER_BASE, true, list);
         pMatrixStack.popPose();
         pMatrixStack.popPose();
      }
   }

   public static void renderPatterns(MatrixStack p_230180_0_, IRenderTypeBuffer p_230180_1_, int p_230180_2_, int p_230180_3_, ModelRenderer p_230180_4_, RenderMaterial p_230180_5_, boolean p_230180_6_, List<Pair<BannerPattern, DyeColor>> p_230180_7_) {
      renderPatterns(p_230180_0_, p_230180_1_, p_230180_2_, p_230180_3_, p_230180_4_, p_230180_5_, p_230180_6_, p_230180_7_, false);
   }

   public static void renderPatterns(MatrixStack p_241717_0_, IRenderTypeBuffer p_241717_1_, int p_241717_2_, int p_241717_3_, ModelRenderer p_241717_4_, RenderMaterial p_241717_5_, boolean p_241717_6_, List<Pair<BannerPattern, DyeColor>> p_241717_7_, boolean p_241717_8_) {
      p_241717_4_.render(p_241717_0_, p_241717_5_.buffer(p_241717_1_, RenderType::entitySolid, p_241717_8_), p_241717_2_, p_241717_3_);

      for(int i = 0; i < 17 && i < p_241717_7_.size(); ++i) {
         Pair<BannerPattern, DyeColor> pair = p_241717_7_.get(i);
         float[] afloat = pair.getSecond().getTextureDiffuseColors();
         RenderMaterial rendermaterial = new RenderMaterial(p_241717_6_ ? Atlases.BANNER_SHEET : Atlases.SHIELD_SHEET, pair.getFirst().location(p_241717_6_));
         p_241717_4_.render(p_241717_0_, rendermaterial.buffer(p_241717_1_, RenderType::entityNoOutline), p_241717_2_, p_241717_3_, afloat[0], afloat[1], afloat[2], 1.0F);
      }

   }
}