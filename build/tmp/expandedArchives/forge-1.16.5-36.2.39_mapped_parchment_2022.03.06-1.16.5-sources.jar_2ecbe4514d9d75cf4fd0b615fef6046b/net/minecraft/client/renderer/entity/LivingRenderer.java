package net.minecraft.client.renderer.entity;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public abstract class LivingRenderer<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements IEntityRenderer<T, M> {
   private static final Logger LOGGER = LogManager.getLogger();
   protected M model;
   protected final List<LayerRenderer<T, M>> layers = Lists.newArrayList();

   public LivingRenderer(EntityRendererManager p_i50965_1_, M p_i50965_2_, float p_i50965_3_) {
      super(p_i50965_1_);
      this.model = p_i50965_2_;
      this.shadowRadius = p_i50965_3_;
   }

   public final boolean addLayer(LayerRenderer<T, M> pLayer) {
      return this.layers.add(pLayer);
   }

   public M getModel() {
      return this.model;
   }

   public void render(T pEntity, float pEntityYaw, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight) {
      if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Pre<T, M>(pEntity, this, pPartialTicks, pMatrixStack, pBuffer, pPackedLight))) return;
      pMatrixStack.pushPose();
      this.model.attackTime = this.getAttackAnim(pEntity, pPartialTicks);

      boolean shouldSit = pEntity.isPassenger() && (pEntity.getVehicle() != null && pEntity.getVehicle().shouldRiderSit());
      this.model.riding = shouldSit;
      this.model.young = pEntity.isBaby();
      float f = MathHelper.rotLerp(pPartialTicks, pEntity.yBodyRotO, pEntity.yBodyRot);
      float f1 = MathHelper.rotLerp(pPartialTicks, pEntity.yHeadRotO, pEntity.yHeadRot);
      float f2 = f1 - f;
      if (shouldSit && pEntity.getVehicle() instanceof LivingEntity) {
         LivingEntity livingentity = (LivingEntity)pEntity.getVehicle();
         f = MathHelper.rotLerp(pPartialTicks, livingentity.yBodyRotO, livingentity.yBodyRot);
         f2 = f1 - f;
         float f3 = MathHelper.wrapDegrees(f2);
         if (f3 < -85.0F) {
            f3 = -85.0F;
         }

         if (f3 >= 85.0F) {
            f3 = 85.0F;
         }

         f = f1 - f3;
         if (f3 * f3 > 2500.0F) {
            f += f3 * 0.2F;
         }

         f2 = f1 - f;
      }

      float f6 = MathHelper.lerp(pPartialTicks, pEntity.xRotO, pEntity.xRot);
      if (pEntity.getPose() == Pose.SLEEPING) {
         Direction direction = pEntity.getBedOrientation();
         if (direction != null) {
            float f4 = pEntity.getEyeHeight(Pose.STANDING) - 0.1F;
            pMatrixStack.translate((double)((float)(-direction.getStepX()) * f4), 0.0D, (double)((float)(-direction.getStepZ()) * f4));
         }
      }

      float f7 = this.getBob(pEntity, pPartialTicks);
      this.setupRotations(pEntity, pMatrixStack, f7, f, pPartialTicks);
      pMatrixStack.scale(-1.0F, -1.0F, 1.0F);
      this.scale(pEntity, pMatrixStack, pPartialTicks);
      pMatrixStack.translate(0.0D, (double)-1.501F, 0.0D);
      float f8 = 0.0F;
      float f5 = 0.0F;
      if (!shouldSit && pEntity.isAlive()) {
         f8 = MathHelper.lerp(pPartialTicks, pEntity.animationSpeedOld, pEntity.animationSpeed);
         f5 = pEntity.animationPosition - pEntity.animationSpeed * (1.0F - pPartialTicks);
         if (pEntity.isBaby()) {
            f5 *= 3.0F;
         }

         if (f8 > 1.0F) {
            f8 = 1.0F;
         }
      }

      this.model.prepareMobModel(pEntity, f5, f8, pPartialTicks);
      this.model.setupAnim(pEntity, f5, f8, f7, f2, f6);
      Minecraft minecraft = Minecraft.getInstance();
      boolean flag = this.isBodyVisible(pEntity);
      boolean flag1 = !flag && !pEntity.isInvisibleTo(minecraft.player);
      boolean flag2 = minecraft.shouldEntityAppearGlowing(pEntity);
      RenderType rendertype = this.getRenderType(pEntity, flag, flag1, flag2);
      if (rendertype != null) {
         IVertexBuilder ivertexbuilder = pBuffer.getBuffer(rendertype);
         int i = getOverlayCoords(pEntity, this.getWhiteOverlayProgress(pEntity, pPartialTicks));
         this.model.renderToBuffer(pMatrixStack, ivertexbuilder, pPackedLight, i, 1.0F, 1.0F, 1.0F, flag1 ? 0.15F : 1.0F);
      }

      if (!pEntity.isSpectator()) {
         for(LayerRenderer<T, M> layerrenderer : this.layers) {
            layerrenderer.render(pMatrixStack, pBuffer, pPackedLight, pEntity, f5, f8, pPartialTicks, f7, f2, f6);
         }
      }

      pMatrixStack.popPose();
      super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Post<T, M>(pEntity, this, pPartialTicks, pMatrixStack, pBuffer, pPackedLight));
   }

   @Nullable
   protected RenderType getRenderType(T p_230496_1_, boolean p_230496_2_, boolean p_230496_3_, boolean p_230496_4_) {
      ResourceLocation resourcelocation = this.getTextureLocation(p_230496_1_);
      if (p_230496_3_) {
         return RenderType.itemEntityTranslucentCull(resourcelocation);
      } else if (p_230496_2_) {
         return this.model.renderType(resourcelocation);
      } else {
         return p_230496_4_ ? RenderType.outline(resourcelocation) : null;
      }
   }

   public static int getOverlayCoords(LivingEntity pLivingEntity, float pU) {
      return OverlayTexture.pack(OverlayTexture.u(pU), OverlayTexture.v(pLivingEntity.hurtTime > 0 || pLivingEntity.deathTime > 0));
   }

   protected boolean isBodyVisible(T pLivingEntity) {
      return !pLivingEntity.isInvisible();
   }

   private static float sleepDirectionToRotation(Direction pFacing) {
      switch(pFacing) {
      case SOUTH:
         return 90.0F;
      case WEST:
         return 0.0F;
      case NORTH:
         return 270.0F;
      case EAST:
         return 180.0F;
      default:
         return 0.0F;
      }
   }

   protected boolean isShaking(T p_230495_1_) {
      return false;
   }

   protected void setupRotations(T pEntityLiving, MatrixStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
      if (this.isShaking(pEntityLiving)) {
         pRotationYaw += (float)(Math.cos((double)pEntityLiving.tickCount * 3.25D) * Math.PI * (double)0.4F);
      }

      Pose pose = pEntityLiving.getPose();
      if (pose != Pose.SLEEPING) {
         pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - pRotationYaw));
      }

      if (pEntityLiving.deathTime > 0) {
         float f = ((float)pEntityLiving.deathTime + pPartialTicks - 1.0F) / 20.0F * 1.6F;
         f = MathHelper.sqrt(f);
         if (f > 1.0F) {
            f = 1.0F;
         }

         pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(f * this.getFlipDegrees(pEntityLiving)));
      } else if (pEntityLiving.isAutoSpinAttack()) {
         pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F - pEntityLiving.xRot));
         pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(((float)pEntityLiving.tickCount + pPartialTicks) * -75.0F));
      } else if (pose == Pose.SLEEPING) {
         Direction direction = pEntityLiving.getBedOrientation();
         float f1 = direction != null ? sleepDirectionToRotation(direction) : pRotationYaw;
         pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f1));
         pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(this.getFlipDegrees(pEntityLiving)));
         pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(270.0F));
      } else if (pEntityLiving.hasCustomName() || pEntityLiving instanceof PlayerEntity) {
         String s = TextFormatting.stripFormatting(pEntityLiving.getName().getString());
         if (("Dinnerbone".equals(s) || "Grumm".equals(s)) && (!(pEntityLiving instanceof PlayerEntity) || ((PlayerEntity)pEntityLiving).isModelPartShown(PlayerModelPart.CAPE))) {
            pMatrixStack.translate(0.0D, (double)(pEntityLiving.getBbHeight() + 0.1F), 0.0D);
            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
         }
      }

   }

   /**
    * Returns where in the swing animation the living entity is (from 0 to 1).  Args : entity, partialTickTime
    */
   protected float getAttackAnim(T pLivingBase, float pPartialTickTime) {
      return pLivingBase.getAttackAnim(pPartialTickTime);
   }

   /**
    * Defines what float the third param in setRotationAngles of ModelBase is
    */
   protected float getBob(T pLivingBase, float pPartialTicks) {
      return (float)pLivingBase.tickCount + pPartialTicks;
   }

   protected float getFlipDegrees(T pLivingEntity) {
      return 90.0F;
   }

   protected float getWhiteOverlayProgress(T pLivingEntity, float pPartialTicks) {
      return 0.0F;
   }

   protected void scale(T pLivingEntity, MatrixStack pMatrixStack, float pPartialTickTime) {
   }

   protected boolean shouldShowName(T pEntity) {
      double d0 = this.entityRenderDispatcher.distanceToSqr(pEntity);
      float f = pEntity.isDiscrete() ? 32.0F : 64.0F;
      if (d0 >= (double)(f * f)) {
         return false;
      } else {
         Minecraft minecraft = Minecraft.getInstance();
         ClientPlayerEntity clientplayerentity = minecraft.player;
         boolean flag = !pEntity.isInvisibleTo(clientplayerentity);
         if (pEntity != clientplayerentity) {
            Team team = pEntity.getTeam();
            Team team1 = clientplayerentity.getTeam();
            if (team != null) {
               Team.Visible team$visible = team.getNameTagVisibility();
               switch(team$visible) {
               case ALWAYS:
                  return flag;
               case NEVER:
                  return false;
               case HIDE_FOR_OTHER_TEAMS:
                  return team1 == null ? flag : team.isAlliedTo(team1) && (team.canSeeFriendlyInvisibles() || flag);
               case HIDE_FOR_OWN_TEAM:
                  return team1 == null ? flag : !team.isAlliedTo(team1) && flag;
               default:
                  return true;
               }
            }
         }

         return Minecraft.renderNames() && pEntity != minecraft.getCameraEntity() && flag && !pEntity.isVehicle();
      }
   }
}
