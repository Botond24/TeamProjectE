package net.minecraft.client.renderer.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.BookModel;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.tileentity.EnchantingTableTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EnchantmentTableTileEntityRenderer extends TileEntityRenderer<EnchantingTableTileEntity> {
   /** The texture for the book above the enchantment table. */
   public static final RenderMaterial BOOK_LOCATION = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, new ResourceLocation("entity/enchanting_table_book"));
   private final BookModel bookModel = new BookModel();

   public EnchantmentTableTileEntityRenderer(TileEntityRendererDispatcher p_i226010_1_) {
      super(p_i226010_1_);
   }

   public void render(EnchantingTableTileEntity pBlockEntity, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, int pCombinedOverlay) {
      pMatrixStack.pushPose();
      pMatrixStack.translate(0.5D, 0.75D, 0.5D);
      float f = (float)pBlockEntity.time + pPartialTicks;
      pMatrixStack.translate(0.0D, (double)(0.1F + MathHelper.sin(f * 0.1F) * 0.01F), 0.0D);

      float f1;
      for(f1 = pBlockEntity.rot - pBlockEntity.oRot; f1 >= (float)Math.PI; f1 -= ((float)Math.PI * 2F)) {
      }

      while(f1 < -(float)Math.PI) {
         f1 += ((float)Math.PI * 2F);
      }

      float f2 = pBlockEntity.oRot + f1 * pPartialTicks;
      pMatrixStack.mulPose(Vector3f.YP.rotation(-f2));
      pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(80.0F));
      float f3 = MathHelper.lerp(pPartialTicks, pBlockEntity.oFlip, pBlockEntity.flip);
      float f4 = MathHelper.frac(f3 + 0.25F) * 1.6F - 0.3F;
      float f5 = MathHelper.frac(f3 + 0.75F) * 1.6F - 0.3F;
      float f6 = MathHelper.lerp(pPartialTicks, pBlockEntity.oOpen, pBlockEntity.open);
      this.bookModel.setupAnim(f, MathHelper.clamp(f4, 0.0F, 1.0F), MathHelper.clamp(f5, 0.0F, 1.0F), f6);
      IVertexBuilder ivertexbuilder = BOOK_LOCATION.buffer(pBuffer, RenderType::entitySolid);
      this.bookModel.render(pMatrixStack, ivertexbuilder, pCombinedLight, pCombinedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
      pMatrixStack.popPose();
   }
}