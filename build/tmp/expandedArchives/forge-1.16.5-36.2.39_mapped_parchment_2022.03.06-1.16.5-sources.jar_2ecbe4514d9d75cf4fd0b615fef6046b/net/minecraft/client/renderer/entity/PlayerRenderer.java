package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BeeStingerLayer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.Deadmau5HeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HeadLayer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.layers.ParrotVariantLayer;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerRenderer extends LivingRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {
   public PlayerRenderer(EntityRendererManager p_i46102_1_) {
      this(p_i46102_1_, false);
   }

   public PlayerRenderer(EntityRendererManager p_i46103_1_, boolean p_i46103_2_) {
      super(p_i46103_1_, new PlayerModel<>(0.0F, p_i46103_2_), 0.5F);
      this.addLayer(new BipedArmorLayer<>(this, new BipedModel(0.5F), new BipedModel(1.0F)));
      this.addLayer(new HeldItemLayer<>(this));
      this.addLayer(new ArrowLayer<>(this));
      this.addLayer(new Deadmau5HeadLayer(this));
      this.addLayer(new CapeLayer(this));
      this.addLayer(new HeadLayer<>(this));
      this.addLayer(new ElytraLayer<>(this));
      this.addLayer(new ParrotVariantLayer<>(this));
      this.addLayer(new SpinAttackEffectLayer<>(this));
      this.addLayer(new BeeStingerLayer<>(this));
   }

   public void render(AbstractClientPlayerEntity pEntity, float pEntityYaw, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight) {
      this.setModelProperties(pEntity);
      if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderPlayerEvent.Pre(pEntity, this, pPartialTicks, pMatrixStack, pBuffer, pPackedLight))) return;
      super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderPlayerEvent.Post(pEntity, this, pPartialTicks, pMatrixStack, pBuffer, pPackedLight));
   }

   public Vector3d getRenderOffset(AbstractClientPlayerEntity pEntity, float pPartialTicks) {
      return pEntity.isCrouching() ? new Vector3d(0.0D, -0.125D, 0.0D) : super.getRenderOffset(pEntity, pPartialTicks);
   }

   private void setModelProperties(AbstractClientPlayerEntity pClientPlayer) {
      PlayerModel<AbstractClientPlayerEntity> playermodel = this.getModel();
      if (pClientPlayer.isSpectator()) {
         playermodel.setAllVisible(false);
         playermodel.head.visible = true;
         playermodel.hat.visible = true;
      } else {
         playermodel.setAllVisible(true);
         playermodel.hat.visible = pClientPlayer.isModelPartShown(PlayerModelPart.HAT);
         playermodel.jacket.visible = pClientPlayer.isModelPartShown(PlayerModelPart.JACKET);
         playermodel.leftPants.visible = pClientPlayer.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
         playermodel.rightPants.visible = pClientPlayer.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
         playermodel.leftSleeve.visible = pClientPlayer.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
         playermodel.rightSleeve.visible = pClientPlayer.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
         playermodel.crouching = pClientPlayer.isCrouching();
         BipedModel.ArmPose bipedmodel$armpose = getArmPose(pClientPlayer, Hand.MAIN_HAND);
         BipedModel.ArmPose bipedmodel$armpose1 = getArmPose(pClientPlayer, Hand.OFF_HAND);
         if (bipedmodel$armpose.isTwoHanded()) {
            bipedmodel$armpose1 = pClientPlayer.getOffhandItem().isEmpty() ? BipedModel.ArmPose.EMPTY : BipedModel.ArmPose.ITEM;
         }

         if (pClientPlayer.getMainArm() == HandSide.RIGHT) {
            playermodel.rightArmPose = bipedmodel$armpose;
            playermodel.leftArmPose = bipedmodel$armpose1;
         } else {
            playermodel.rightArmPose = bipedmodel$armpose1;
            playermodel.leftArmPose = bipedmodel$armpose;
         }
      }

   }

   private static BipedModel.ArmPose getArmPose(AbstractClientPlayerEntity p_241741_0_, Hand p_241741_1_) {
      ItemStack itemstack = p_241741_0_.getItemInHand(p_241741_1_);
      if (itemstack.isEmpty()) {
         return BipedModel.ArmPose.EMPTY;
      } else {
         if (p_241741_0_.getUsedItemHand() == p_241741_1_ && p_241741_0_.getUseItemRemainingTicks() > 0) {
            UseAction useaction = itemstack.getUseAnimation();
            if (useaction == UseAction.BLOCK) {
               return BipedModel.ArmPose.BLOCK;
            }

            if (useaction == UseAction.BOW) {
               return BipedModel.ArmPose.BOW_AND_ARROW;
            }

            if (useaction == UseAction.SPEAR) {
               return BipedModel.ArmPose.THROW_SPEAR;
            }

            if (useaction == UseAction.CROSSBOW && p_241741_1_ == p_241741_0_.getUsedItemHand()) {
               return BipedModel.ArmPose.CROSSBOW_CHARGE;
            }
         } else if (!p_241741_0_.swinging && itemstack.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemstack)) {
            return BipedModel.ArmPose.CROSSBOW_HOLD;
         }

         return BipedModel.ArmPose.ITEM;
      }
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(AbstractClientPlayerEntity pEntity) {
      return pEntity.getSkinTextureLocation();
   }

   protected void scale(AbstractClientPlayerEntity pLivingEntity, MatrixStack pMatrixStack, float pPartialTickTime) {
      float f = 0.9375F;
      pMatrixStack.scale(0.9375F, 0.9375F, 0.9375F);
   }

   protected void renderNameTag(AbstractClientPlayerEntity pEntity, ITextComponent pDisplayName, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight) {
      double d0 = this.entityRenderDispatcher.distanceToSqr(pEntity);
      pMatrixStack.pushPose();
      if (d0 < 100.0D) {
         Scoreboard scoreboard = pEntity.getScoreboard();
         ScoreObjective scoreobjective = scoreboard.getDisplayObjective(2);
         if (scoreobjective != null) {
            Score score = scoreboard.getOrCreatePlayerScore(pEntity.getScoreboardName(), scoreobjective);
            super.renderNameTag(pEntity, (new StringTextComponent(Integer.toString(score.getScore()))).append(" ").append(scoreobjective.getDisplayName()), pMatrixStack, pBuffer, pPackedLight);
            pMatrixStack.translate(0.0D, (double)(9.0F * 1.15F * 0.025F), 0.0D);
         }
      }

      super.renderNameTag(pEntity, pDisplayName, pMatrixStack, pBuffer, pPackedLight);
      pMatrixStack.popPose();
   }

   public void renderRightHand(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, AbstractClientPlayerEntity pPlayer) {
      if(!net.minecraftforge.client.ForgeHooksClient.renderSpecificFirstPersonArm(pMatrixStack, pBuffer, pCombinedLight, pPlayer, HandSide.RIGHT))
      this.renderHand(pMatrixStack, pBuffer, pCombinedLight, pPlayer, (this.model).rightArm, (this.model).rightSleeve);
   }

   public void renderLeftHand(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, AbstractClientPlayerEntity pPlayer) {
      if(!net.minecraftforge.client.ForgeHooksClient.renderSpecificFirstPersonArm(pMatrixStack, pBuffer, pCombinedLight, pPlayer, HandSide.LEFT))
      this.renderHand(pMatrixStack, pBuffer, pCombinedLight, pPlayer, (this.model).leftArm, (this.model).leftSleeve);
   }

   private void renderHand(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, AbstractClientPlayerEntity pPlayer, ModelRenderer pRendererArm, ModelRenderer pRendererArmwear) {
      PlayerModel<AbstractClientPlayerEntity> playermodel = this.getModel();
      this.setModelProperties(pPlayer);
      playermodel.attackTime = 0.0F;
      playermodel.crouching = false;
      playermodel.swimAmount = 0.0F;
      playermodel.setupAnim(pPlayer, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
      pRendererArm.xRot = 0.0F;
      pRendererArm.render(pMatrixStack, pBuffer.getBuffer(RenderType.entitySolid(pPlayer.getSkinTextureLocation())), pCombinedLight, OverlayTexture.NO_OVERLAY);
      pRendererArmwear.xRot = 0.0F;
      pRendererArmwear.render(pMatrixStack, pBuffer.getBuffer(RenderType.entityTranslucent(pPlayer.getSkinTextureLocation())), pCombinedLight, OverlayTexture.NO_OVERLAY);
   }

   protected void setupRotations(AbstractClientPlayerEntity pEntityLiving, MatrixStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
      float f = pEntityLiving.getSwimAmount(pPartialTicks);
      if (pEntityLiving.isFallFlying()) {
         super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
         float f1 = (float)pEntityLiving.getFallFlyingTicks() + pPartialTicks;
         float f2 = MathHelper.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
         if (!pEntityLiving.isAutoSpinAttack()) {
            pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(f2 * (-90.0F - pEntityLiving.xRot)));
         }

         Vector3d vector3d = pEntityLiving.getViewVector(pPartialTicks);
         Vector3d vector3d1 = pEntityLiving.getDeltaMovement();
         double d0 = Entity.getHorizontalDistanceSqr(vector3d1);
         double d1 = Entity.getHorizontalDistanceSqr(vector3d);
         if (d0 > 0.0D && d1 > 0.0D) {
            double d2 = (vector3d1.x * vector3d.x + vector3d1.z * vector3d.z) / Math.sqrt(d0 * d1);
            double d3 = vector3d1.x * vector3d.z - vector3d1.z * vector3d.x;
            pMatrixStack.mulPose(Vector3f.YP.rotation((float)(Math.signum(d3) * Math.acos(d2))));
         }
      } else if (f > 0.0F) {
         super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
         float f3 = pEntityLiving.isInWater() ? -90.0F - pEntityLiving.xRot : -90.0F;
         float f4 = MathHelper.lerp(f, 0.0F, f3);
         pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(f4));
         if (pEntityLiving.isVisuallySwimming()) {
            pMatrixStack.translate(0.0D, -1.0D, (double)0.3F);
         }
      } else {
         super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
      }

   }
}
