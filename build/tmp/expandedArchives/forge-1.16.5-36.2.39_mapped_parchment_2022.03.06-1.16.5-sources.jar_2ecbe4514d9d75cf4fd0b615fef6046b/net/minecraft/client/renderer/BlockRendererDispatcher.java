package net.minecraft.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.Random;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockRendererDispatcher implements IResourceManagerReloadListener {
   private final BlockModelShapes blockModelShaper;
   private final BlockModelRenderer modelRenderer;
   private final FluidBlockRenderer liquidBlockRenderer;
   private final Random random = new Random();
   private final BlockColors blockColors;

   public BlockRendererDispatcher(BlockModelShapes p_i46577_1_, BlockColors p_i46577_2_) {
      this.blockModelShaper = p_i46577_1_;
      this.blockColors = p_i46577_2_;
      this.modelRenderer = new net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer(this.blockColors);
      this.liquidBlockRenderer = new FluidBlockRenderer();
   }

   public BlockModelShapes getBlockModelShaper() {
      return this.blockModelShaper;
   }

   @Deprecated //Forge: Model parameter
   public void renderBreakingTexture(BlockState pBlockState, BlockPos pPos, IBlockDisplayReader pLightReader, MatrixStack pMatrixStack, IVertexBuilder pVertexBuilder) {
       renderBlockDamage(pBlockState,pPos, pLightReader, pMatrixStack, pVertexBuilder, net.minecraftforge.client.model.data.EmptyModelData.INSTANCE);
   }
   public void renderBlockDamage(BlockState blockStateIn, BlockPos posIn, IBlockDisplayReader lightReaderIn, MatrixStack matrixStackIn, IVertexBuilder vertexBuilderIn, net.minecraftforge.client.model.data.IModelData modelData) {
      if (blockStateIn.getRenderShape() == BlockRenderType.MODEL) {
         IBakedModel ibakedmodel = this.blockModelShaper.getBlockModel(blockStateIn);
         long i = blockStateIn.getSeed(posIn);
         this.modelRenderer.renderModel(lightReaderIn, ibakedmodel, blockStateIn, posIn, matrixStackIn, vertexBuilderIn, true, this.random, i, OverlayTexture.NO_OVERLAY, modelData);
      }
   }

   @Deprecated //Forge: Model parameter
   public boolean renderBatched(BlockState pBlockState, BlockPos pPos, IBlockDisplayReader pLightReader, MatrixStack pMatrixStack, IVertexBuilder pVertexBuilder, boolean pCheckSides, Random pRand) {
       return renderModel(pBlockState, pPos, pLightReader, pMatrixStack, pVertexBuilder, pCheckSides, pRand, net.minecraftforge.client.model.data.EmptyModelData.INSTANCE);
   }
   public boolean renderModel(BlockState blockStateIn, BlockPos posIn, IBlockDisplayReader lightReaderIn, MatrixStack matrixStackIn, IVertexBuilder vertexBuilderIn, boolean checkSides, Random rand, net.minecraftforge.client.model.data.IModelData modelData) {
      try {
         BlockRenderType blockrendertype = blockStateIn.getRenderShape();
         return blockrendertype != BlockRenderType.MODEL ? false : this.modelRenderer.renderModel(lightReaderIn, this.getBlockModel(blockStateIn), blockStateIn, posIn, matrixStackIn, vertexBuilderIn, checkSides, rand, blockStateIn.getSeed(posIn), OverlayTexture.NO_OVERLAY, modelData);
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Tesselating block in world");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Block being tesselated");
         CrashReportCategory.populateBlockDetails(crashreportcategory, posIn, blockStateIn);
         throw new ReportedException(crashreport);
      }
   }

   public boolean renderLiquid(BlockPos pPos, IBlockDisplayReader pLightReader, IVertexBuilder pVertexBuilder, FluidState pFluidState) {
      try {
         return this.liquidBlockRenderer.tesselate(pLightReader, pPos, pVertexBuilder, pFluidState);
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Tesselating liquid in world");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Block being tesselated");
         CrashReportCategory.populateBlockDetails(crashreportcategory, pPos, (BlockState)null);
         throw new ReportedException(crashreport);
      }
   }

   public BlockModelRenderer getModelRenderer() {
      return this.modelRenderer;
   }

   public IBakedModel getBlockModel(BlockState pState) {
      return this.blockModelShaper.getBlockModel(pState);
   }

   @Deprecated //Forge: Model parameter
   public void renderSingleBlock(BlockState pBlockState, MatrixStack pMatrixStack, IRenderTypeBuffer pBufferType, int pCombinedLight, int pCombinedOverlay) {
      renderBlock(pBlockState, pMatrixStack, pBufferType, pCombinedLight, pCombinedOverlay, net.minecraftforge.client.model.data.EmptyModelData.INSTANCE);
   }
   public void renderBlock(BlockState pBlockState, MatrixStack pMatrixStack, IRenderTypeBuffer pBufferType, int pCombinedLight, int pCombinedOverlay, net.minecraftforge.client.model.data.IModelData modelData) {
      BlockRenderType blockrendertype = pBlockState.getRenderShape();
      if (blockrendertype != BlockRenderType.INVISIBLE) {
         switch(blockrendertype) {
         case MODEL:
            IBakedModel ibakedmodel = this.getBlockModel(pBlockState);
            int i = this.blockColors.getColor(pBlockState, (IBlockDisplayReader)null, (BlockPos)null, 0);
            float f = (float)(i >> 16 & 255) / 255.0F;
            float f1 = (float)(i >> 8 & 255) / 255.0F;
            float f2 = (float)(i & 255) / 255.0F;
            this.modelRenderer.renderModel(pMatrixStack.last(), pBufferType.getBuffer(RenderTypeLookup.getRenderType(pBlockState, false)), pBlockState, ibakedmodel, f, f1, f2, pCombinedLight, pCombinedOverlay, modelData);
            break;
         case ENTITYBLOCK_ANIMATED:
            ItemStack stack = new ItemStack(pBlockState.getBlock());
            stack.getItem().getItemStackTileEntityRenderer().renderByItem(stack, ItemCameraTransforms.TransformType.NONE, pMatrixStack, pBufferType, pCombinedLight, pCombinedOverlay);
         }

      }
   }

   public void onResourceManagerReload(IResourceManager pResourceManager) {
      this.liquidBlockRenderer.setupSprites();
   }

   @Override
   public net.minecraftforge.resource.IResourceType getResourceType() {
      return net.minecraftforge.resource.VanillaResourceType.MODELS;
   }
}
