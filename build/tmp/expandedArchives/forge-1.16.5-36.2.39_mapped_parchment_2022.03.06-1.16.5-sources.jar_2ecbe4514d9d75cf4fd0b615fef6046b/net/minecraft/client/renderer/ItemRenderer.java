package net.minecraft.client.renderer;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.MatrixApplyingVertexBuilder;
import com.mojang.blaze3d.vertex.VertexBuilderUtils;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BreakableBlock;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemRenderer implements IResourceManagerReloadListener {
   public static final ResourceLocation ENCHANT_GLINT_LOCATION = new ResourceLocation("textures/misc/enchanted_item_glint.png");
   private static final Set<Item> IGNORED = Sets.newHashSet(Items.AIR);
   public float blitOffset;
   private final ItemModelMesher itemModelShaper;
   private final TextureManager textureManager;
   private final ItemColors itemColors;

   public ItemRenderer(TextureManager p_i46552_1_, ModelManager p_i46552_2_, ItemColors p_i46552_3_) {
      this.textureManager = p_i46552_1_;
      this.itemModelShaper = new net.minecraftforge.client.ItemModelMesherForge(p_i46552_2_);

      for(Item item : Registry.ITEM) {
         if (!IGNORED.contains(item)) {
            this.itemModelShaper.register(item, new ModelResourceLocation(Registry.ITEM.getKey(item), "inventory"));
         }
      }

      this.itemColors = p_i46552_3_;
   }

   public ItemModelMesher getItemModelShaper() {
      return this.itemModelShaper;
   }

   public void renderModelLists(IBakedModel pModel, ItemStack pStack, int pCombinedLight, int pCombinedOverlay, MatrixStack pMatrixStack, IVertexBuilder pBuffer) {
      Random random = new Random();
      long i = 42L;

      for(Direction direction : Direction.values()) {
         random.setSeed(42L);
         this.renderQuadList(pMatrixStack, pBuffer, pModel.getQuads((BlockState)null, direction, random), pStack, pCombinedLight, pCombinedOverlay);
      }

      random.setSeed(42L);
      this.renderQuadList(pMatrixStack, pBuffer, pModel.getQuads((BlockState)null, (Direction)null, random), pStack, pCombinedLight, pCombinedOverlay);
   }

   public void render(ItemStack pItemStack, ItemCameraTransforms.TransformType pTransformType, boolean pLeftHand, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, int pCombinedOverlay, IBakedModel pModel) {
      if (!pItemStack.isEmpty()) {
         pMatrixStack.pushPose();
         boolean flag = pTransformType == ItemCameraTransforms.TransformType.GUI || pTransformType == ItemCameraTransforms.TransformType.GROUND || pTransformType == ItemCameraTransforms.TransformType.FIXED;
         if (pItemStack.getItem() == Items.TRIDENT && flag) {
            pModel = this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft:trident#inventory"));
         }

         pModel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(pMatrixStack, pModel, pTransformType, pLeftHand);
         pMatrixStack.translate(-0.5D, -0.5D, -0.5D);
         if (!pModel.isCustomRenderer() && (pItemStack.getItem() != Items.TRIDENT || flag)) {
            boolean flag1;
            if (pTransformType != ItemCameraTransforms.TransformType.GUI && !pTransformType.firstPerson() && pItemStack.getItem() instanceof BlockItem) {
               Block block = ((BlockItem)pItemStack.getItem()).getBlock();
               flag1 = !(block instanceof BreakableBlock) && !(block instanceof StainedGlassPaneBlock);
            } else {
               flag1 = true;
            }
            if (pModel.isLayered()) { net.minecraftforge.client.ForgeHooksClient.drawItemLayered(this, pModel, pItemStack, pMatrixStack, pBuffer, pCombinedLight, pCombinedOverlay, flag1); }
            else {
            RenderType rendertype = RenderTypeLookup.getRenderType(pItemStack, flag1);
            IVertexBuilder ivertexbuilder;
            if (pItemStack.getItem() == Items.COMPASS && pItemStack.hasFoil()) {
               pMatrixStack.pushPose();
               MatrixStack.Entry matrixstack$entry = pMatrixStack.last();
               if (pTransformType == ItemCameraTransforms.TransformType.GUI) {
                  matrixstack$entry.pose().multiply(0.5F);
               } else if (pTransformType.firstPerson()) {
                  matrixstack$entry.pose().multiply(0.75F);
               }

               if (flag1) {
                  ivertexbuilder = getCompassFoilBufferDirect(pBuffer, rendertype, matrixstack$entry);
               } else {
                  ivertexbuilder = getCompassFoilBuffer(pBuffer, rendertype, matrixstack$entry);
               }

               pMatrixStack.popPose();
            } else if (flag1) {
               ivertexbuilder = getFoilBufferDirect(pBuffer, rendertype, true, pItemStack.hasFoil());
            } else {
               ivertexbuilder = getFoilBuffer(pBuffer, rendertype, true, pItemStack.hasFoil());
            }

            this.renderModelLists(pModel, pItemStack, pCombinedLight, pCombinedOverlay, pMatrixStack, ivertexbuilder);
            }
         } else {
            pItemStack.getItem().getItemStackTileEntityRenderer().renderByItem(pItemStack, pTransformType, pMatrixStack, pBuffer, pCombinedLight, pCombinedOverlay);
         }

         pMatrixStack.popPose();
      }
   }

   public static IVertexBuilder getArmorFoilBuffer(IRenderTypeBuffer pBuffer, RenderType pRenderType, boolean pNoEntity, boolean pWithGlint) {
      return pWithGlint ? VertexBuilderUtils.create(pBuffer.getBuffer(pNoEntity ? RenderType.armorGlint() : RenderType.armorEntityGlint()), pBuffer.getBuffer(pRenderType)) : pBuffer.getBuffer(pRenderType);
   }

   public static IVertexBuilder getCompassFoilBuffer(IRenderTypeBuffer pBuffer, RenderType pRenderType, MatrixStack.Entry pMatrixEntry) {
      return VertexBuilderUtils.create(new MatrixApplyingVertexBuilder(pBuffer.getBuffer(RenderType.glint()), pMatrixEntry.pose(), pMatrixEntry.normal()), pBuffer.getBuffer(pRenderType));
   }

   public static IVertexBuilder getCompassFoilBufferDirect(IRenderTypeBuffer pBuffer, RenderType pRenderType, MatrixStack.Entry pMatrixEntry) {
      return VertexBuilderUtils.create(new MatrixApplyingVertexBuilder(pBuffer.getBuffer(RenderType.glintDirect()), pMatrixEntry.pose(), pMatrixEntry.normal()), pBuffer.getBuffer(pRenderType));
   }

   public static IVertexBuilder getFoilBuffer(IRenderTypeBuffer pBuffer, RenderType pRenderType, boolean pIsItem, boolean pGlint) {
      if (pGlint) {
         return Minecraft.useShaderTransparency() && pRenderType == Atlases.translucentItemSheet() ? VertexBuilderUtils.create(pBuffer.getBuffer(RenderType.glintTranslucent()), pBuffer.getBuffer(pRenderType)) : VertexBuilderUtils.create(pBuffer.getBuffer(pIsItem ? RenderType.glint() : RenderType.entityGlint()), pBuffer.getBuffer(pRenderType));
      } else {
         return pBuffer.getBuffer(pRenderType);
      }
   }

   public static IVertexBuilder getFoilBufferDirect(IRenderTypeBuffer pBuffer, RenderType pRenderType, boolean pNoEntity, boolean pWithGlint) {
      return pWithGlint ? VertexBuilderUtils.create(pBuffer.getBuffer(pNoEntity ? RenderType.glintDirect() : RenderType.entityGlintDirect()), pBuffer.getBuffer(pRenderType)) : pBuffer.getBuffer(pRenderType);
   }

   public void renderQuadList(MatrixStack pMatrixStack, IVertexBuilder pBuffer, List<BakedQuad> pQuads, ItemStack pItemStack, int pCombinedLight, int pCombinedOverlay) {
      boolean flag = !pItemStack.isEmpty();
      MatrixStack.Entry matrixstack$entry = pMatrixStack.last();

      for(BakedQuad bakedquad : pQuads) {
         int i = -1;
         if (flag && bakedquad.isTinted()) {
            i = this.itemColors.getColor(pItemStack, bakedquad.getTintIndex());
         }

         float f = (float)(i >> 16 & 255) / 255.0F;
         float f1 = (float)(i >> 8 & 255) / 255.0F;
         float f2 = (float)(i & 255) / 255.0F;
         pBuffer.addVertexData(matrixstack$entry, bakedquad, f, f1, f2, pCombinedLight, pCombinedOverlay, true);
      }

   }

   public IBakedModel getModel(ItemStack p_184393_1_, @Nullable World p_184393_2_, @Nullable LivingEntity p_184393_3_) {
      Item item = p_184393_1_.getItem();
      IBakedModel ibakedmodel;
      if (item == Items.TRIDENT) {
         ibakedmodel = this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft:trident_in_hand#inventory"));
      } else {
         ibakedmodel = this.itemModelShaper.getItemModel(p_184393_1_);
      }

      ClientWorld clientworld = p_184393_2_ instanceof ClientWorld ? (ClientWorld)p_184393_2_ : null;
      IBakedModel ibakedmodel1 = ibakedmodel.getOverrides().resolve(ibakedmodel, p_184393_1_, clientworld, p_184393_3_);
      return ibakedmodel1 == null ? this.itemModelShaper.getModelManager().getMissingModel() : ibakedmodel1;
   }

   public void renderStatic(ItemStack p_229110_1_, ItemCameraTransforms.TransformType p_229110_2_, int p_229110_3_, int p_229110_4_, MatrixStack p_229110_5_, IRenderTypeBuffer p_229110_6_) {
      this.renderStatic((LivingEntity)null, p_229110_1_, p_229110_2_, false, p_229110_5_, p_229110_6_, (World)null, p_229110_3_, p_229110_4_);
   }

   public void renderStatic(@Nullable LivingEntity p_229109_1_, ItemStack p_229109_2_, ItemCameraTransforms.TransformType p_229109_3_, boolean p_229109_4_, MatrixStack p_229109_5_, IRenderTypeBuffer p_229109_6_, @Nullable World p_229109_7_, int p_229109_8_, int p_229109_9_) {
      if (!p_229109_2_.isEmpty()) {
         IBakedModel ibakedmodel = this.getModel(p_229109_2_, p_229109_7_, p_229109_1_);
         this.render(p_229109_2_, p_229109_3_, p_229109_4_, p_229109_5_, p_229109_6_, p_229109_8_, p_229109_9_, ibakedmodel);
      }
   }

   public void renderGuiItem(ItemStack pStack, int pX, int pY) {
      this.renderGuiItem(pStack, pX, pY, this.getModel(pStack, (World)null, (LivingEntity)null));
   }

   protected void renderGuiItem(ItemStack pStack, int pX, int pY, IBakedModel pBakedmodel) {
      RenderSystem.pushMatrix();
      this.textureManager.bind(AtlasTexture.LOCATION_BLOCKS);
      this.textureManager.getTexture(AtlasTexture.LOCATION_BLOCKS).setFilter(false, false);
      RenderSystem.enableRescaleNormal();
      RenderSystem.enableAlphaTest();
      RenderSystem.defaultAlphaFunc();
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.translatef((float)pX, (float)pY, 100.0F + this.blitOffset);
      RenderSystem.translatef(8.0F, 8.0F, 0.0F);
      RenderSystem.scalef(1.0F, -1.0F, 1.0F);
      RenderSystem.scalef(16.0F, 16.0F, 16.0F);
      MatrixStack matrixstack = new MatrixStack();
      IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();
      boolean flag = !pBakedmodel.usesBlockLight();
      if (flag) {
         RenderHelper.setupForFlatItems();
      }

      this.render(pStack, ItemCameraTransforms.TransformType.GUI, false, matrixstack, irendertypebuffer$impl, 15728880, OverlayTexture.NO_OVERLAY, pBakedmodel);
      irendertypebuffer$impl.endBatch();
      RenderSystem.enableDepthTest();
      if (flag) {
         RenderHelper.setupFor3DItems();
      }

      RenderSystem.disableAlphaTest();
      RenderSystem.disableRescaleNormal();
      RenderSystem.popMatrix();
   }

   public void renderAndDecorateItem(ItemStack pStack, int pXPosition, int pYPosition) {
      this.tryRenderGuiItem(Minecraft.getInstance().player, pStack, pXPosition, pYPosition);
   }

   public void renderAndDecorateFakeItem(ItemStack pStack, int pX, int pY) {
      this.tryRenderGuiItem((LivingEntity)null, pStack, pX, pY);
   }

   public void renderAndDecorateItem(LivingEntity p_184391_1_, ItemStack p_184391_2_, int p_184391_3_, int p_184391_4_) {
      this.tryRenderGuiItem(p_184391_1_, p_184391_2_, p_184391_3_, p_184391_4_);
   }

   private void tryRenderGuiItem(@Nullable LivingEntity p_239387_1_, ItemStack p_239387_2_, int p_239387_3_, int p_239387_4_) {
      if (!p_239387_2_.isEmpty()) {
         this.blitOffset += 50.0F;

         try {
            this.renderGuiItem(p_239387_2_, p_239387_3_, p_239387_4_, this.getModel(p_239387_2_, (World)null, p_239387_1_));
         } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
            crashreportcategory.setDetail("Item Type", () -> {
               return String.valueOf((Object)p_239387_2_.getItem());
            });
            crashreportcategory.setDetail("Registry Name", () -> String.valueOf(p_239387_2_.getItem().getRegistryName()));
            crashreportcategory.setDetail("Item Damage", () -> {
               return String.valueOf(p_239387_2_.getDamageValue());
            });
            crashreportcategory.setDetail("Item NBT", () -> {
               return String.valueOf((Object)p_239387_2_.getTag());
            });
            crashreportcategory.setDetail("Item Foil", () -> {
               return String.valueOf(p_239387_2_.hasFoil());
            });
            throw new ReportedException(crashreport);
         }

         this.blitOffset -= 50.0F;
      }
   }

   public void renderGuiItemDecorations(FontRenderer pFr, ItemStack pStack, int pXPosition, int pYPosition) {
      this.renderGuiItemDecorations(pFr, pStack, pXPosition, pYPosition, (String)null);
   }

   /**
    * Renders the stack size and/or damage bar for the given ItemStack.
    */
   public void renderGuiItemDecorations(FontRenderer pFr, ItemStack pStack, int pXPosition, int pYPosition, @Nullable String pText) {
      if (!pStack.isEmpty()) {
         MatrixStack matrixstack = new MatrixStack();
         if (pStack.getCount() != 1 || pText != null) {
            String s = pText == null ? String.valueOf(pStack.getCount()) : pText;
            matrixstack.translate(0.0D, 0.0D, (double)(this.blitOffset + 200.0F));
            IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
            pFr.drawInBatch(s, (float)(pXPosition + 19 - 2 - pFr.width(s)), (float)(pYPosition + 6 + 3), 16777215, true, matrixstack.last().pose(), irendertypebuffer$impl, false, 0, 15728880);
            irendertypebuffer$impl.endBatch();
         }

         if (pStack.getItem().showDurabilityBar(pStack)) {
            RenderSystem.disableDepthTest();
            RenderSystem.disableTexture();
            RenderSystem.disableAlphaTest();
            RenderSystem.disableBlend();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuilder();
            double health = pStack.getItem().getDurabilityForDisplay(pStack);
            int i = Math.round(13.0F - (float)health * 13.0F);
            int j = pStack.getItem().getRGBDurabilityForDisplay(pStack);
            this.fillRect(bufferbuilder, pXPosition + 2, pYPosition + 13, 13, 2, 0, 0, 0, 255);
            this.fillRect(bufferbuilder, pXPosition + 2, pYPosition + 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
            RenderSystem.enableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableTexture();
            RenderSystem.enableDepthTest();
         }

         ClientPlayerEntity clientplayerentity = Minecraft.getInstance().player;
         float f3 = clientplayerentity == null ? 0.0F : clientplayerentity.getCooldowns().getCooldownPercent(pStack.getItem(), Minecraft.getInstance().getFrameTime());
         if (f3 > 0.0F) {
            RenderSystem.disableDepthTest();
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            Tessellator tessellator1 = Tessellator.getInstance();
            BufferBuilder bufferbuilder1 = tessellator1.getBuilder();
            this.fillRect(bufferbuilder1, pXPosition, pYPosition + MathHelper.floor(16.0F * (1.0F - f3)), 16, MathHelper.ceil(16.0F * f3), 255, 255, 255, 127);
            RenderSystem.enableTexture();
            RenderSystem.enableDepthTest();
         }

      }
   }

   /**
    * Draw with the WorldRenderer
    */
   private void fillRect(BufferBuilder pRenderer, int pX, int pY, int pWidth, int pHeight, int pRed, int pGreen, int pBlue, int pAlpha) {
      pRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
      pRenderer.vertex((double)(pX + 0), (double)(pY + 0), 0.0D).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pRenderer.vertex((double)(pX + 0), (double)(pY + pHeight), 0.0D).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pRenderer.vertex((double)(pX + pWidth), (double)(pY + pHeight), 0.0D).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      pRenderer.vertex((double)(pX + pWidth), (double)(pY + 0), 0.0D).color(pRed, pGreen, pBlue, pAlpha).endVertex();
      Tessellator.getInstance().end();
   }

   public void onResourceManagerReload(IResourceManager pResourceManager) {
      this.itemModelShaper.rebuildCache();
   }

   @Override
   public net.minecraftforge.resource.IResourceType getResourceType() {
      return net.minecraftforge.resource.VanillaResourceType.MODELS;
   }
}
