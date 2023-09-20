package net.minecraft.client.renderer.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.state.properties.PistonType;
import net.minecraft.tileentity.PistonTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PistonTileEntityRenderer extends TileEntityRenderer<PistonTileEntity> {
   private BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

   public PistonTileEntityRenderer(TileEntityRendererDispatcher p_i226012_1_) {
      super(p_i226012_1_);
   }

   public void render(PistonTileEntity pBlockEntity, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, int pCombinedOverlay) {
      World world = pBlockEntity.getLevel();
      if (world != null) {
         BlockPos blockpos = pBlockEntity.getBlockPos().relative(pBlockEntity.getMovementDirection().getOpposite());
         BlockState blockstate = pBlockEntity.getMovedState();
         if (!blockstate.isAir()) {
            BlockModelRenderer.enableCaching();
            pMatrixStack.pushPose();
            pMatrixStack.translate((double)pBlockEntity.getXOff(pPartialTicks), (double)pBlockEntity.getYOff(pPartialTicks), (double)pBlockEntity.getZOff(pPartialTicks));
            if (blockstate.is(Blocks.PISTON_HEAD) && pBlockEntity.getProgress(pPartialTicks) <= 4.0F) {
               blockstate = blockstate.setValue(PistonHeadBlock.SHORT, Boolean.valueOf(pBlockEntity.getProgress(pPartialTicks) <= 0.5F));
               this.renderBlock(blockpos, blockstate, pMatrixStack, pBuffer, world, false, pCombinedOverlay);
            } else if (pBlockEntity.isSourcePiston() && !pBlockEntity.isExtending()) {
               PistonType pistontype = blockstate.is(Blocks.STICKY_PISTON) ? PistonType.STICKY : PistonType.DEFAULT;
               BlockState blockstate1 = Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.TYPE, pistontype).setValue(PistonHeadBlock.FACING, blockstate.getValue(PistonBlock.FACING));
               blockstate1 = blockstate1.setValue(PistonHeadBlock.SHORT, Boolean.valueOf(pBlockEntity.getProgress(pPartialTicks) >= 0.5F));
               this.renderBlock(blockpos, blockstate1, pMatrixStack, pBuffer, world, false, pCombinedOverlay);
               BlockPos blockpos1 = blockpos.relative(pBlockEntity.getMovementDirection());
               pMatrixStack.popPose();
               pMatrixStack.pushPose();
               blockstate = blockstate.setValue(PistonBlock.EXTENDED, Boolean.valueOf(true));
               this.renderBlock(blockpos1, blockstate, pMatrixStack, pBuffer, world, true, pCombinedOverlay);
            } else {
               this.renderBlock(blockpos, blockstate, pMatrixStack, pBuffer, world, false, pCombinedOverlay);
            }

            pMatrixStack.popPose();
            BlockModelRenderer.clearCache();
         }
      }
   }

   private void renderBlock(BlockPos p_228876_1_, BlockState p_228876_2_, MatrixStack p_228876_3_, IRenderTypeBuffer p_228876_4_, World p_228876_5_, boolean p_228876_6_, int p_228876_7_) {
      net.minecraftforge.client.ForgeHooksClient.renderPistonMovedBlocks(p_228876_1_, p_228876_2_, p_228876_3_, p_228876_4_, p_228876_5_, p_228876_6_, p_228876_7_, blockRenderer == null ? blockRenderer = Minecraft.getInstance().getBlockRenderer() : blockRenderer);
      if(false) {
      RenderType rendertype = RenderTypeLookup.getMovingBlockRenderType(p_228876_2_);
      IVertexBuilder ivertexbuilder = p_228876_4_.getBuffer(rendertype);
      this.blockRenderer.getModelRenderer().tesselateBlock(p_228876_5_, this.blockRenderer.getBlockModel(p_228876_2_), p_228876_2_, p_228876_1_, p_228876_3_, ivertexbuilder, p_228876_6_, new Random(), p_228876_2_.getSeed(p_228876_1_), p_228876_7_);
      }
   }
}
