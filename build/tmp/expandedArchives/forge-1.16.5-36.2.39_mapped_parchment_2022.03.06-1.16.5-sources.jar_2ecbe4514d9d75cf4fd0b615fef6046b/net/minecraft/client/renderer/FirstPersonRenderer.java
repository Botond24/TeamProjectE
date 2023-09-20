package net.minecraft.client.renderer;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FirstPersonRenderer {
   private static final RenderType MAP_BACKGROUND = RenderType.text(new ResourceLocation("textures/map/map_background.png"));
   private static final RenderType MAP_BACKGROUND_CHECKERBOARD = RenderType.text(new ResourceLocation("textures/map/map_background_checkerboard.png"));
   private final Minecraft minecraft;
   private ItemStack mainHandItem = ItemStack.EMPTY;
   private ItemStack offHandItem = ItemStack.EMPTY;
   private float mainHandHeight;
   private float oMainHandHeight;
   private float offHandHeight;
   private float oOffHandHeight;
   private final EntityRendererManager entityRenderDispatcher;
   private final ItemRenderer itemRenderer;

   public FirstPersonRenderer(Minecraft p_i1247_1_) {
      this.minecraft = p_i1247_1_;
      this.entityRenderDispatcher = p_i1247_1_.getEntityRenderDispatcher();
      this.itemRenderer = p_i1247_1_.getItemRenderer();
   }

   public void renderItem(LivingEntity pLivingEntity, ItemStack pItemStack, ItemCameraTransforms.TransformType pTransformType, boolean pLeftHand, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight) {
      if (!pItemStack.isEmpty()) {
         this.itemRenderer.renderStatic(pLivingEntity, pItemStack, pTransformType, pLeftHand, pMatrixStack, pBuffer, pLivingEntity.level, pCombinedLight, OverlayTexture.NO_OVERLAY);
      }
   }

   /**
    * Return the angle to render the Map
    */
   private float calculateMapTilt(float pPitch) {
      float f = 1.0F - pPitch / 45.0F + 0.1F;
      f = MathHelper.clamp(f, 0.0F, 1.0F);
      return -MathHelper.cos(f * (float)Math.PI) * 0.5F + 0.5F;
   }

   private void renderMapHand(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, HandSide pSide) {
      this.minecraft.getTextureManager().bind(this.minecraft.player.getSkinTextureLocation());
      PlayerRenderer playerrenderer = (PlayerRenderer)this.entityRenderDispatcher.<AbstractClientPlayerEntity>getRenderer(this.minecraft.player);
      pMatrixStack.pushPose();
      float f = pSide == HandSide.RIGHT ? 1.0F : -1.0F;
      pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(92.0F));
      pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(45.0F));
      pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(f * -41.0F));
      pMatrixStack.translate((double)(f * 0.3F), (double)-1.1F, (double)0.45F);
      if (pSide == HandSide.RIGHT) {
         playerrenderer.renderRightHand(pMatrixStack, pBuffer, pCombinedLight, this.minecraft.player);
      } else {
         playerrenderer.renderLeftHand(pMatrixStack, pBuffer, pCombinedLight, this.minecraft.player);
      }

      pMatrixStack.popPose();
   }

   private void renderOneHandedMap(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, float pEquippedProgress, HandSide pHand, float pSwingProgress, ItemStack pStack) {
      float f = pHand == HandSide.RIGHT ? 1.0F : -1.0F;
      pMatrixStack.translate((double)(f * 0.125F), -0.125D, 0.0D);
      if (!this.minecraft.player.isInvisible()) {
         pMatrixStack.pushPose();
         pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(f * 10.0F));
         this.renderPlayerArm(pMatrixStack, pBuffer, pCombinedLight, pEquippedProgress, pSwingProgress, pHand);
         pMatrixStack.popPose();
      }

      pMatrixStack.pushPose();
      pMatrixStack.translate((double)(f * 0.51F), (double)(-0.08F + pEquippedProgress * -1.2F), -0.75D);
      float f1 = MathHelper.sqrt(pSwingProgress);
      float f2 = MathHelper.sin(f1 * (float)Math.PI);
      float f3 = -0.5F * f2;
      float f4 = 0.4F * MathHelper.sin(f1 * ((float)Math.PI * 2F));
      float f5 = -0.3F * MathHelper.sin(pSwingProgress * (float)Math.PI);
      pMatrixStack.translate((double)(f * f3), (double)(f4 - 0.3F * f2), (double)f5);
      pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(f2 * -45.0F));
      pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f * f2 * -30.0F));
      this.renderMap(pMatrixStack, pBuffer, pCombinedLight, pStack);
      pMatrixStack.popPose();
   }

   private void renderTwoHandedMap(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, float pPitch, float pEquippedProgress, float pSwingProgress) {
      float f = MathHelper.sqrt(pSwingProgress);
      float f1 = -0.2F * MathHelper.sin(pSwingProgress * (float)Math.PI);
      float f2 = -0.4F * MathHelper.sin(f * (float)Math.PI);
      pMatrixStack.translate(0.0D, (double)(-f1 / 2.0F), (double)f2);
      float f3 = this.calculateMapTilt(pPitch);
      pMatrixStack.translate(0.0D, (double)(0.04F + pEquippedProgress * -1.2F + f3 * -0.5F), (double)-0.72F);
      pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(f3 * -85.0F));
      if (!this.minecraft.player.isInvisible()) {
         pMatrixStack.pushPose();
         pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
         this.renderMapHand(pMatrixStack, pBuffer, pCombinedLight, HandSide.RIGHT);
         this.renderMapHand(pMatrixStack, pBuffer, pCombinedLight, HandSide.LEFT);
         pMatrixStack.popPose();
      }

      float f4 = MathHelper.sin(f * (float)Math.PI);
      pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(f4 * 20.0F));
      pMatrixStack.scale(2.0F, 2.0F, 2.0F);
      this.renderMap(pMatrixStack, pBuffer, pCombinedLight, this.mainHandItem);
   }

   private void renderMap(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, ItemStack pStack) {
      pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
      pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
      pMatrixStack.scale(0.38F, 0.38F, 0.38F);
      pMatrixStack.translate(-0.5D, -0.5D, 0.0D);
      pMatrixStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
      MapData mapdata = FilledMapItem.getOrCreateSavedData(pStack, this.minecraft.level);
      IVertexBuilder ivertexbuilder = pBuffer.getBuffer(mapdata == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD);
      Matrix4f matrix4f = pMatrixStack.last().pose();
      ivertexbuilder.vertex(matrix4f, -7.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(pCombinedLight).endVertex();
      ivertexbuilder.vertex(matrix4f, 135.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(pCombinedLight).endVertex();
      ivertexbuilder.vertex(matrix4f, 135.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(pCombinedLight).endVertex();
      ivertexbuilder.vertex(matrix4f, -7.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(pCombinedLight).endVertex();
      if (mapdata != null) {
         this.minecraft.gameRenderer.getMapRenderer().render(pMatrixStack, pBuffer, mapdata, false, pCombinedLight);
      }

   }

   private void renderPlayerArm(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, float pEquippedProgress, float pSwingProgress, HandSide pSide) {
      boolean flag = pSide != HandSide.LEFT;
      float f = flag ? 1.0F : -1.0F;
      float f1 = MathHelper.sqrt(pSwingProgress);
      float f2 = -0.3F * MathHelper.sin(f1 * (float)Math.PI);
      float f3 = 0.4F * MathHelper.sin(f1 * ((float)Math.PI * 2F));
      float f4 = -0.4F * MathHelper.sin(pSwingProgress * (float)Math.PI);
      pMatrixStack.translate((double)(f * (f2 + 0.64000005F)), (double)(f3 + -0.6F + pEquippedProgress * -0.6F), (double)(f4 + -0.71999997F));
      pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f * 45.0F));
      float f5 = MathHelper.sin(pSwingProgress * pSwingProgress * (float)Math.PI);
      float f6 = MathHelper.sin(f1 * (float)Math.PI);
      pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f * f6 * 70.0F));
      pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(f * f5 * -20.0F));
      AbstractClientPlayerEntity abstractclientplayerentity = this.minecraft.player;
      this.minecraft.getTextureManager().bind(abstractclientplayerentity.getSkinTextureLocation());
      pMatrixStack.translate((double)(f * -1.0F), (double)3.6F, 3.5D);
      pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(f * 120.0F));
      pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(200.0F));
      pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f * -135.0F));
      pMatrixStack.translate((double)(f * 5.6F), 0.0D, 0.0D);
      PlayerRenderer playerrenderer = (PlayerRenderer)this.entityRenderDispatcher.<AbstractClientPlayerEntity>getRenderer(abstractclientplayerentity);
      if (flag) {
         playerrenderer.renderRightHand(pMatrixStack, pBuffer, pCombinedLight, abstractclientplayerentity);
      } else {
         playerrenderer.renderLeftHand(pMatrixStack, pBuffer, pCombinedLight, abstractclientplayerentity);
      }

   }

   private void applyEatTransform(MatrixStack pMatrixStack, float pPartialTicks, HandSide pHand, ItemStack pStack) {
      float f = (float)this.minecraft.player.getUseItemRemainingTicks() - pPartialTicks + 1.0F;
      float f1 = f / (float)pStack.getUseDuration();
      if (f1 < 0.8F) {
         float f2 = MathHelper.abs(MathHelper.cos(f / 4.0F * (float)Math.PI) * 0.1F);
         pMatrixStack.translate(0.0D, (double)f2, 0.0D);
      }

      float f3 = 1.0F - (float)Math.pow((double)f1, 27.0D);
      int i = pHand == HandSide.RIGHT ? 1 : -1;
      pMatrixStack.translate((double)(f3 * 0.6F * (float)i), (double)(f3 * -0.5F), (double)(f3 * 0.0F));
      pMatrixStack.mulPose(Vector3f.YP.rotationDegrees((float)i * f3 * 90.0F));
      pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(f3 * 10.0F));
      pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees((float)i * f3 * 30.0F));
   }

   private void applyItemArmAttackTransform(MatrixStack pMatrixStack, HandSide pHand, float pSwingProgress) {
      int i = pHand == HandSide.RIGHT ? 1 : -1;
      float f = MathHelper.sin(pSwingProgress * pSwingProgress * (float)Math.PI);
      pMatrixStack.mulPose(Vector3f.YP.rotationDegrees((float)i * (45.0F + f * -20.0F)));
      float f1 = MathHelper.sin(MathHelper.sqrt(pSwingProgress) * (float)Math.PI);
      pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees((float)i * f1 * -20.0F));
      pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(f1 * -80.0F));
      pMatrixStack.mulPose(Vector3f.YP.rotationDegrees((float)i * -45.0F));
   }

   private void applyItemArmTransform(MatrixStack pMatrixStack, HandSide pHand, float pEquippedProg) {
      int i = pHand == HandSide.RIGHT ? 1 : -1;
      pMatrixStack.translate((double)((float)i * 0.56F), (double)(-0.52F + pEquippedProg * -0.6F), (double)-0.72F);
   }

   public void renderHandsWithItems(float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer.Impl pBuffer, ClientPlayerEntity pPlayerEntity, int pCombinedLight) {
      float f = pPlayerEntity.getAttackAnim(pPartialTicks);
      Hand hand = MoreObjects.firstNonNull(pPlayerEntity.swingingArm, Hand.MAIN_HAND);
      float f1 = MathHelper.lerp(pPartialTicks, pPlayerEntity.xRotO, pPlayerEntity.xRot);
      boolean flag = true;
      boolean flag1 = true;
      if (pPlayerEntity.isUsingItem()) {
         ItemStack itemstack = pPlayerEntity.getUseItem();
         if (itemstack.getItem() instanceof net.minecraft.item.ShootableItem) {
            flag = pPlayerEntity.getUsedItemHand() == Hand.MAIN_HAND;
            flag1 = !flag;
         }

         Hand hand1 = pPlayerEntity.getUsedItemHand();
         if (hand1 == Hand.MAIN_HAND) {
            ItemStack itemstack1 = pPlayerEntity.getOffhandItem();
            if (itemstack1.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemstack1)) {
               flag1 = false;
            }
         }
      } else {
         ItemStack itemstack2 = pPlayerEntity.getMainHandItem();
         ItemStack itemstack3 = pPlayerEntity.getOffhandItem();
         if (itemstack2.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemstack2)) {
            flag1 = !flag;
         }

         if (itemstack3.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemstack3)) {
            flag = !itemstack2.isEmpty();
            flag1 = !flag;
         }
      }

      float f3 = MathHelper.lerp(pPartialTicks, pPlayerEntity.xBobO, pPlayerEntity.xBob);
      float f4 = MathHelper.lerp(pPartialTicks, pPlayerEntity.yBobO, pPlayerEntity.yBob);
      pMatrixStack.mulPose(Vector3f.XP.rotationDegrees((pPlayerEntity.getViewXRot(pPartialTicks) - f3) * 0.1F));
      pMatrixStack.mulPose(Vector3f.YP.rotationDegrees((pPlayerEntity.getViewYRot(pPartialTicks) - f4) * 0.1F));
      if (flag) {
         float f5 = hand == Hand.MAIN_HAND ? f : 0.0F;
         float f2 = 1.0F - MathHelper.lerp(pPartialTicks, this.oMainHandHeight, this.mainHandHeight);
         if(!net.minecraftforge.client.ForgeHooksClient.renderSpecificFirstPersonHand(Hand.MAIN_HAND, pMatrixStack, pBuffer, pCombinedLight, pPartialTicks, f1, f5, f2, this.mainHandItem))
         this.renderArmWithItem(pPlayerEntity, pPartialTicks, f1, Hand.MAIN_HAND, f5, this.mainHandItem, f2, pMatrixStack, pBuffer, pCombinedLight);
      }

      if (flag1) {
         float f6 = hand == Hand.OFF_HAND ? f : 0.0F;
         float f7 = 1.0F - MathHelper.lerp(pPartialTicks, this.oOffHandHeight, this.offHandHeight);
         if(!net.minecraftforge.client.ForgeHooksClient.renderSpecificFirstPersonHand(Hand.OFF_HAND, pMatrixStack, pBuffer, pCombinedLight, pPartialTicks, f1, f6, f7, this.offHandItem))
         this.renderArmWithItem(pPlayerEntity, pPartialTicks, f1, Hand.OFF_HAND, f6, this.offHandItem, f7, pMatrixStack, pBuffer, pCombinedLight);
      }

      pBuffer.endBatch();
   }

   private void renderArmWithItem(AbstractClientPlayerEntity pPlayer, float pPartialTicks, float pPitch, Hand pHand, float pSwingProgress, ItemStack pStack, float pEquippedProgress, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight) {
      boolean flag = pHand == Hand.MAIN_HAND;
      HandSide handside = flag ? pPlayer.getMainArm() : pPlayer.getMainArm().getOpposite();
      pMatrixStack.pushPose();
      if (pStack.isEmpty()) {
         if (flag && !pPlayer.isInvisible()) {
            this.renderPlayerArm(pMatrixStack, pBuffer, pCombinedLight, pEquippedProgress, pSwingProgress, handside);
         }
      } else if (pStack.getItem() == Items.FILLED_MAP) {
         if (flag && this.offHandItem.isEmpty()) {
            this.renderTwoHandedMap(pMatrixStack, pBuffer, pCombinedLight, pPitch, pEquippedProgress, pSwingProgress);
         } else {
            this.renderOneHandedMap(pMatrixStack, pBuffer, pCombinedLight, pEquippedProgress, handside, pSwingProgress, pStack);
         }
      } else if (pStack.getItem() == Items.CROSSBOW) {
         boolean flag1 = CrossbowItem.isCharged(pStack);
         boolean flag2 = handside == HandSide.RIGHT;
         int i = flag2 ? 1 : -1;
         if (pPlayer.isUsingItem() && pPlayer.getUseItemRemainingTicks() > 0 && pPlayer.getUsedItemHand() == pHand) {
            this.applyItemArmTransform(pMatrixStack, handside, pEquippedProgress);
            pMatrixStack.translate((double)((float)i * -0.4785682F), (double)-0.094387F, (double)0.05731531F);
            pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(-11.935F));
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees((float)i * 65.3F));
            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees((float)i * -9.785F));
            float f9 = (float)pStack.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - pPartialTicks + 1.0F);
            float f13 = f9 / (float)CrossbowItem.getChargeDuration(pStack);
            if (f13 > 1.0F) {
               f13 = 1.0F;
            }

            if (f13 > 0.1F) {
               float f16 = MathHelper.sin((f9 - 0.1F) * 1.3F);
               float f3 = f13 - 0.1F;
               float f4 = f16 * f3;
               pMatrixStack.translate((double)(f4 * 0.0F), (double)(f4 * 0.004F), (double)(f4 * 0.0F));
            }

            pMatrixStack.translate((double)(f13 * 0.0F), (double)(f13 * 0.0F), (double)(f13 * 0.04F));
            pMatrixStack.scale(1.0F, 1.0F, 1.0F + f13 * 0.2F);
            pMatrixStack.mulPose(Vector3f.YN.rotationDegrees((float)i * 45.0F));
         } else {
            float f = -0.4F * MathHelper.sin(MathHelper.sqrt(pSwingProgress) * (float)Math.PI);
            float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt(pSwingProgress) * ((float)Math.PI * 2F));
            float f2 = -0.2F * MathHelper.sin(pSwingProgress * (float)Math.PI);
            pMatrixStack.translate((double)((float)i * f), (double)f1, (double)f2);
            this.applyItemArmTransform(pMatrixStack, handside, pEquippedProgress);
            this.applyItemArmAttackTransform(pMatrixStack, handside, pSwingProgress);
            if (flag1 && pSwingProgress < 0.001F) {
               pMatrixStack.translate((double)((float)i * -0.641864F), 0.0D, 0.0D);
               pMatrixStack.mulPose(Vector3f.YP.rotationDegrees((float)i * 10.0F));
            }
         }

         this.renderItem(pPlayer, pStack, flag2 ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !flag2, pMatrixStack, pBuffer, pCombinedLight);
      } else {
         boolean flag3 = handside == HandSide.RIGHT;
         if (pPlayer.isUsingItem() && pPlayer.getUseItemRemainingTicks() > 0 && pPlayer.getUsedItemHand() == pHand) {
            int k = flag3 ? 1 : -1;
            switch(pStack.getUseAnimation()) {
            case NONE:
               this.applyItemArmTransform(pMatrixStack, handside, pEquippedProgress);
               break;
            case EAT:
            case DRINK:
               this.applyEatTransform(pMatrixStack, pPartialTicks, handside, pStack);
               this.applyItemArmTransform(pMatrixStack, handside, pEquippedProgress);
               break;
            case BLOCK:
               this.applyItemArmTransform(pMatrixStack, handside, pEquippedProgress);
               break;
            case BOW:
               this.applyItemArmTransform(pMatrixStack, handside, pEquippedProgress);
               pMatrixStack.translate((double)((float)k * -0.2785682F), (double)0.18344387F, (double)0.15731531F);
               pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(-13.935F));
               pMatrixStack.mulPose(Vector3f.YP.rotationDegrees((float)k * 35.3F));
               pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees((float)k * -9.785F));
               float f8 = (float)pStack.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - pPartialTicks + 1.0F);
               float f12 = f8 / 20.0F;
               f12 = (f12 * f12 + f12 * 2.0F) / 3.0F;
               if (f12 > 1.0F) {
                  f12 = 1.0F;
               }

               if (f12 > 0.1F) {
                  float f15 = MathHelper.sin((f8 - 0.1F) * 1.3F);
                  float f18 = f12 - 0.1F;
                  float f20 = f15 * f18;
                  pMatrixStack.translate((double)(f20 * 0.0F), (double)(f20 * 0.004F), (double)(f20 * 0.0F));
               }

               pMatrixStack.translate((double)(f12 * 0.0F), (double)(f12 * 0.0F), (double)(f12 * 0.04F));
               pMatrixStack.scale(1.0F, 1.0F, 1.0F + f12 * 0.2F);
               pMatrixStack.mulPose(Vector3f.YN.rotationDegrees((float)k * 45.0F));
               break;
            case SPEAR:
               this.applyItemArmTransform(pMatrixStack, handside, pEquippedProgress);
               pMatrixStack.translate((double)((float)k * -0.5F), (double)0.7F, (double)0.1F);
               pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(-55.0F));
               pMatrixStack.mulPose(Vector3f.YP.rotationDegrees((float)k * 35.3F));
               pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees((float)k * -9.785F));
               float f7 = (float)pStack.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - pPartialTicks + 1.0F);
               float f11 = f7 / 10.0F;
               if (f11 > 1.0F) {
                  f11 = 1.0F;
               }

               if (f11 > 0.1F) {
                  float f14 = MathHelper.sin((f7 - 0.1F) * 1.3F);
                  float f17 = f11 - 0.1F;
                  float f19 = f14 * f17;
                  pMatrixStack.translate((double)(f19 * 0.0F), (double)(f19 * 0.004F), (double)(f19 * 0.0F));
               }

               pMatrixStack.translate(0.0D, 0.0D, (double)(f11 * 0.2F));
               pMatrixStack.scale(1.0F, 1.0F, 1.0F + f11 * 0.2F);
               pMatrixStack.mulPose(Vector3f.YN.rotationDegrees((float)k * 45.0F));
            }
         } else if (pPlayer.isAutoSpinAttack()) {
            this.applyItemArmTransform(pMatrixStack, handside, pEquippedProgress);
            int j = flag3 ? 1 : -1;
            pMatrixStack.translate((double)((float)j * -0.4F), (double)0.8F, (double)0.3F);
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees((float)j * 65.0F));
            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees((float)j * -85.0F));
         } else {
            float f5 = -0.4F * MathHelper.sin(MathHelper.sqrt(pSwingProgress) * (float)Math.PI);
            float f6 = 0.2F * MathHelper.sin(MathHelper.sqrt(pSwingProgress) * ((float)Math.PI * 2F));
            float f10 = -0.2F * MathHelper.sin(pSwingProgress * (float)Math.PI);
            int l = flag3 ? 1 : -1;
            pMatrixStack.translate((double)((float)l * f5), (double)f6, (double)f10);
            this.applyItemArmTransform(pMatrixStack, handside, pEquippedProgress);
            this.applyItemArmAttackTransform(pMatrixStack, handside, pSwingProgress);
         }

         this.renderItem(pPlayer, pStack, flag3 ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !flag3, pMatrixStack, pBuffer, pCombinedLight);
      }

      pMatrixStack.popPose();
   }

   public void tick() {
      this.oMainHandHeight = this.mainHandHeight;
      this.oOffHandHeight = this.offHandHeight;
      ClientPlayerEntity clientplayerentity = this.minecraft.player;
      ItemStack itemstack = clientplayerentity.getMainHandItem();
      ItemStack itemstack1 = clientplayerentity.getOffhandItem();
      if (ItemStack.matches(this.mainHandItem, itemstack)) {
         this.mainHandItem = itemstack;
      }

      if (ItemStack.matches(this.offHandItem, itemstack1)) {
         this.offHandItem = itemstack1;
      }

      if (clientplayerentity.isHandsBusy()) {
         this.mainHandHeight = MathHelper.clamp(this.mainHandHeight - 0.4F, 0.0F, 1.0F);
         this.offHandHeight = MathHelper.clamp(this.offHandHeight - 0.4F, 0.0F, 1.0F);
      } else {
         float f = clientplayerentity.getAttackStrengthScale(1.0F);
         boolean requipM = net.minecraftforge.client.ForgeHooksClient.shouldCauseReequipAnimation(this.mainHandItem, itemstack, clientplayerentity.inventory.selected);
         boolean requipO = net.minecraftforge.client.ForgeHooksClient.shouldCauseReequipAnimation(this.offHandItem, itemstack1, -1);

         if (!requipM && this.mainHandItem != itemstack)
            this.mainHandItem = itemstack;
         if (!requipO && this.offHandItem != itemstack1)
            this.offHandItem = itemstack1;

         this.mainHandHeight += MathHelper.clamp((!requipM ? f * f * f : 0.0F) - this.mainHandHeight, -0.4F, 0.4F);
         this.offHandHeight += MathHelper.clamp((float)(!requipO ? 1 : 0) - this.offHandHeight, -0.4F, 0.4F);
      }

      if (this.mainHandHeight < 0.1F) {
         this.mainHandItem = itemstack;
      }

      if (this.offHandHeight < 0.1F) {
         this.offHandItem = itemstack1;
      }

   }

   public void itemUsed(Hand pHand) {
      if (pHand == Hand.MAIN_HAND) {
         this.mainHandHeight = 0.0F;
      } else {
         this.offHandHeight = 0.0F;
      }

   }
}
