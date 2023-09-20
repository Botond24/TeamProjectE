package net.minecraft.client.renderer;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effects;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class GameRenderer implements IResourceManagerReloadListener, AutoCloseable {
   private static final ResourceLocation NAUSEA_LOCATION = new ResourceLocation("textures/misc/nausea.png");
   private static final Logger LOGGER = LogManager.getLogger();
   private final Minecraft minecraft;
   private final IResourceManager resourceManager;
   private final Random random = new Random();
   private float renderDistance;
   public final FirstPersonRenderer itemInHandRenderer;
   private final MapItemRenderer mapRenderer;
   private final RenderTypeBuffers renderBuffers;
   private int tick;
   private float fov;
   private float oldFov;
   private float darkenWorldAmount;
   private float darkenWorldAmountO;
   private boolean renderHand = true;
   private boolean renderBlockOutline = true;
   private long lastScreenshotAttempt;
   private long lastActiveTime = Util.getMillis();
   private final LightTexture lightTexture;
   private final OverlayTexture overlayTexture = new OverlayTexture();
   private boolean panoramicMode;
   private float zoom = 1.0F;
   private float zoomX;
   private float zoomY;
   @Nullable
   private ItemStack itemActivationItem;
   private int itemActivationTicks;
   private float itemActivationOffX;
   private float itemActivationOffY;
   @Nullable
   private ShaderGroup postEffect;
   private static final ResourceLocation[] EFFECTS = new ResourceLocation[]{new ResourceLocation("shaders/post/notch.json"), new ResourceLocation("shaders/post/fxaa.json"), new ResourceLocation("shaders/post/art.json"), new ResourceLocation("shaders/post/bumpy.json"), new ResourceLocation("shaders/post/blobs2.json"), new ResourceLocation("shaders/post/pencil.json"), new ResourceLocation("shaders/post/color_convolve.json"), new ResourceLocation("shaders/post/deconverge.json"), new ResourceLocation("shaders/post/flip.json"), new ResourceLocation("shaders/post/invert.json"), new ResourceLocation("shaders/post/ntsc.json"), new ResourceLocation("shaders/post/outline.json"), new ResourceLocation("shaders/post/phosphor.json"), new ResourceLocation("shaders/post/scan_pincushion.json"), new ResourceLocation("shaders/post/sobel.json"), new ResourceLocation("shaders/post/bits.json"), new ResourceLocation("shaders/post/desaturate.json"), new ResourceLocation("shaders/post/green.json"), new ResourceLocation("shaders/post/blur.json"), new ResourceLocation("shaders/post/wobble.json"), new ResourceLocation("shaders/post/blobs.json"), new ResourceLocation("shaders/post/antialias.json"), new ResourceLocation("shaders/post/creeper.json"), new ResourceLocation("shaders/post/spider.json")};
   public static final int EFFECT_NONE = EFFECTS.length;
   private int effectIndex = EFFECT_NONE;
   private boolean effectActive;
   private final ActiveRenderInfo mainCamera = new ActiveRenderInfo();

   public GameRenderer(Minecraft p_i225966_1_, IResourceManager p_i225966_2_, RenderTypeBuffers p_i225966_3_) {
      this.minecraft = p_i225966_1_;
      this.resourceManager = p_i225966_2_;
      this.itemInHandRenderer = p_i225966_1_.getItemInHandRenderer();
      this.mapRenderer = new MapItemRenderer(p_i225966_1_.getTextureManager());
      this.lightTexture = new LightTexture(this, p_i225966_1_);
      this.renderBuffers = p_i225966_3_;
      this.postEffect = null;
   }

   public void close() {
      this.lightTexture.close();
      this.mapRenderer.close();
      this.overlayTexture.close();
      this.shutdownEffect();
   }

   public void shutdownEffect() {
      if (this.postEffect != null) {
         this.postEffect.close();
      }

      this.postEffect = null;
      this.effectIndex = EFFECT_NONE;
   }

   public void togglePostEffect() {
      this.effectActive = !this.effectActive;
   }

   /**
    * What shader to use when spectating this entity
    */
   public void checkEntityPostEffect(@Nullable Entity pEntity) {
      if (this.postEffect != null) {
         this.postEffect.close();
      }

      this.postEffect = null;
      if (pEntity instanceof CreeperEntity) {
         this.loadEffect(new ResourceLocation("shaders/post/creeper.json"));
      } else if (pEntity instanceof SpiderEntity) {
         this.loadEffect(new ResourceLocation("shaders/post/spider.json"));
      } else if (pEntity instanceof EndermanEntity) {
         this.loadEffect(new ResourceLocation("shaders/post/invert.json"));
      } else {
         net.minecraftforge.client.ForgeHooksClient.loadEntityShader(pEntity, this);
      }

   }

   public void loadEffect(ResourceLocation pResourceLocation) {
      if (this.postEffect != null) {
         this.postEffect.close();
      }

      try {
         this.postEffect = new ShaderGroup(this.minecraft.getTextureManager(), this.resourceManager, this.minecraft.getMainRenderTarget(), pResourceLocation);
         this.postEffect.resize(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
         this.effectActive = true;
      } catch (IOException ioexception) {
         LOGGER.warn("Failed to load shader: {}", pResourceLocation, ioexception);
         this.effectIndex = EFFECT_NONE;
         this.effectActive = false;
      } catch (JsonSyntaxException jsonsyntaxexception) {
         LOGGER.warn("Failed to parse shader: {}", pResourceLocation, jsonsyntaxexception);
         this.effectIndex = EFFECT_NONE;
         this.effectActive = false;
      }

   }

   public void onResourceManagerReload(IResourceManager pResourceManager) {
      if (this.postEffect != null) {
         this.postEffect.close();
      }

      this.postEffect = null;
      if (this.effectIndex == EFFECT_NONE) {
         this.checkEntityPostEffect(this.minecraft.getCameraEntity());
      } else {
         this.loadEffect(EFFECTS[this.effectIndex]);
      }

   }

   /**
    * Updates the entity renderer
    */
   public void tick() {
      this.tickFov();
      this.lightTexture.tick();
      if (this.minecraft.getCameraEntity() == null) {
         this.minecraft.setCameraEntity(this.minecraft.player);
      }

      this.mainCamera.tick();
      ++this.tick;
      this.itemInHandRenderer.tick();
      this.minecraft.levelRenderer.tickRain(this.mainCamera);
      this.darkenWorldAmountO = this.darkenWorldAmount;
      if (this.minecraft.gui.getBossOverlay().shouldDarkenScreen()) {
         this.darkenWorldAmount += 0.05F;
         if (this.darkenWorldAmount > 1.0F) {
            this.darkenWorldAmount = 1.0F;
         }
      } else if (this.darkenWorldAmount > 0.0F) {
         this.darkenWorldAmount -= 0.0125F;
      }

      if (this.itemActivationTicks > 0) {
         --this.itemActivationTicks;
         if (this.itemActivationTicks == 0) {
            this.itemActivationItem = null;
         }
      }

   }

   @Nullable
   public ShaderGroup currentEffect() {
      return this.postEffect;
   }

   public void resize(int pWidth, int pHeight) {
      if (this.postEffect != null) {
         this.postEffect.resize(pWidth, pHeight);
      }

      this.minecraft.levelRenderer.resize(pWidth, pHeight);
   }

   /**
    * Gets the block or object that is being moused over.
    */
   public void pick(float pPartialTicks) {
      Entity entity = this.minecraft.getCameraEntity();
      if (entity != null) {
         if (this.minecraft.level != null) {
            this.minecraft.getProfiler().push("pick");
            this.minecraft.crosshairPickEntity = null;
            double d0 = (double)this.minecraft.gameMode.getPickRange();
            this.minecraft.hitResult = entity.pick(d0, pPartialTicks, false);
            Vector3d vector3d = entity.getEyePosition(pPartialTicks);
            boolean flag = false;
            int i = 3;
            double d1 = d0;
            if (this.minecraft.gameMode.hasFarPickRange()) {
               d1 = 6.0D;
               d0 = d1;
            } else {
               if (d0 > 3.0D) {
                  flag = true;
               }

               d0 = d0;
            }

            d1 = d1 * d1;
            if (this.minecraft.hitResult != null) {
               d1 = this.minecraft.hitResult.getLocation().distanceToSqr(vector3d);
            }

            Vector3d vector3d1 = entity.getViewVector(1.0F);
            Vector3d vector3d2 = vector3d.add(vector3d1.x * d0, vector3d1.y * d0, vector3d1.z * d0);
            float f = 1.0F;
            AxisAlignedBB axisalignedbb = entity.getBoundingBox().expandTowards(vector3d1.scale(d0)).inflate(1.0D, 1.0D, 1.0D);
            EntityRayTraceResult entityraytraceresult = ProjectileHelper.getEntityHitResult(entity, vector3d, vector3d2, axisalignedbb, (p_215312_0_) -> {
               return !p_215312_0_.isSpectator() && p_215312_0_.isPickable();
            }, d1);
            if (entityraytraceresult != null) {
               Entity entity1 = entityraytraceresult.getEntity();
               Vector3d vector3d3 = entityraytraceresult.getLocation();
               double d2 = vector3d.distanceToSqr(vector3d3);
               if (flag && d2 > 9.0D) {
                  this.minecraft.hitResult = BlockRayTraceResult.miss(vector3d3, Direction.getNearest(vector3d1.x, vector3d1.y, vector3d1.z), new BlockPos(vector3d3));
               } else if (d2 < d1 || this.minecraft.hitResult == null) {
                  this.minecraft.hitResult = entityraytraceresult;
                  if (entity1 instanceof LivingEntity || entity1 instanceof ItemFrameEntity) {
                     this.minecraft.crosshairPickEntity = entity1;
                  }
               }
            }

            this.minecraft.getProfiler().pop();
         }
      }
   }

   /**
    * Update FOV modifier hand
    */
   private void tickFov() {
      float f = 1.0F;
      if (this.minecraft.getCameraEntity() instanceof AbstractClientPlayerEntity) {
         AbstractClientPlayerEntity abstractclientplayerentity = (AbstractClientPlayerEntity)this.minecraft.getCameraEntity();
         f = abstractclientplayerentity.getFieldOfViewModifier();
      }

      this.oldFov = this.fov;
      this.fov += (f - this.fov) * 0.5F;
      if (this.fov > 1.5F) {
         this.fov = 1.5F;
      }

      if (this.fov < 0.1F) {
         this.fov = 0.1F;
      }

   }

   private double getFov(ActiveRenderInfo pActiveRenderInfo, float pPartialTicks, boolean pUseFOVSetting) {
      if (this.panoramicMode) {
         return 90.0D;
      } else {
         double d0 = 70.0D;
         if (pUseFOVSetting) {
            d0 = this.minecraft.options.fov;
            d0 = d0 * (double)MathHelper.lerp(pPartialTicks, this.oldFov, this.fov);
         }

         if (pActiveRenderInfo.getEntity() instanceof LivingEntity && ((LivingEntity)pActiveRenderInfo.getEntity()).isDeadOrDying()) {
            float f = Math.min((float)((LivingEntity)pActiveRenderInfo.getEntity()).deathTime + pPartialTicks, 20.0F);
            d0 /= (double)((1.0F - 500.0F / (f + 500.0F)) * 2.0F + 1.0F);
         }

         FluidState fluidstate = pActiveRenderInfo.getFluidInCamera();
         if (!fluidstate.isEmpty()) {
            d0 = d0 * 60.0D / 70.0D;
         }

         return net.minecraftforge.client.ForgeHooksClient.getFOVModifier(this, pActiveRenderInfo, pPartialTicks, d0);
      }
   }

   private void bobHurt(MatrixStack pMatrixStack, float pPartialTicks) {
      if (this.minecraft.getCameraEntity() instanceof LivingEntity) {
         LivingEntity livingentity = (LivingEntity)this.minecraft.getCameraEntity();
         float f = (float)livingentity.hurtTime - pPartialTicks;
         if (livingentity.isDeadOrDying()) {
            float f1 = Math.min((float)livingentity.deathTime + pPartialTicks, 20.0F);
            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(40.0F - 8000.0F / (f1 + 200.0F)));
         }

         if (f < 0.0F) {
            return;
         }

         f = f / (float)livingentity.hurtDuration;
         f = MathHelper.sin(f * f * f * f * (float)Math.PI);
         float f2 = livingentity.hurtDir;
         pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(-f2));
         pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(-f * 14.0F));
         pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f2));
      }

   }

   private void bobView(MatrixStack pMatrixStack, float pPartialTicks) {
      if (this.minecraft.getCameraEntity() instanceof PlayerEntity) {
         PlayerEntity playerentity = (PlayerEntity)this.minecraft.getCameraEntity();
         float f = playerentity.walkDist - playerentity.walkDistO;
         float f1 = -(playerentity.walkDist + f * pPartialTicks);
         float f2 = MathHelper.lerp(pPartialTicks, playerentity.oBob, playerentity.bob);
         pMatrixStack.translate((double)(MathHelper.sin(f1 * (float)Math.PI) * f2 * 0.5F), (double)(-Math.abs(MathHelper.cos(f1 * (float)Math.PI) * f2)), 0.0D);
         pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(MathHelper.sin(f1 * (float)Math.PI) * f2 * 3.0F));
         pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(Math.abs(MathHelper.cos(f1 * (float)Math.PI - 0.2F) * f2) * 5.0F));
      }
   }

   private void renderItemInHand(MatrixStack pMatrixStack, ActiveRenderInfo pActiveRenderInfo, float pPartialTicks) {
      if (!this.panoramicMode) {
         this.resetProjectionMatrix(this.getProjectionMatrix(pActiveRenderInfo, pPartialTicks, false));
         MatrixStack.Entry matrixstack$entry = pMatrixStack.last();
         matrixstack$entry.pose().setIdentity();
         matrixstack$entry.normal().setIdentity();
         pMatrixStack.pushPose();
         this.bobHurt(pMatrixStack, pPartialTicks);
         if (this.minecraft.options.bobView) {
            this.bobView(pMatrixStack, pPartialTicks);
         }

         boolean flag = this.minecraft.getCameraEntity() instanceof LivingEntity && ((LivingEntity)this.minecraft.getCameraEntity()).isSleeping();
         if (this.minecraft.options.getCameraType().isFirstPerson() && !flag && !this.minecraft.options.hideGui && this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
            this.lightTexture.turnOnLightLayer();
            this.itemInHandRenderer.renderHandsWithItems(pPartialTicks, pMatrixStack, this.renderBuffers.bufferSource(), this.minecraft.player, this.minecraft.getEntityRenderDispatcher().getPackedLightCoords(this.minecraft.player, pPartialTicks));
            this.lightTexture.turnOffLightLayer();
         }

         pMatrixStack.popPose();
         if (this.minecraft.options.getCameraType().isFirstPerson() && !flag) {
            OverlayRenderer.renderScreenEffect(this.minecraft, pMatrixStack);
            this.bobHurt(pMatrixStack, pPartialTicks);
         }

         if (this.minecraft.options.bobView) {
            this.bobView(pMatrixStack, pPartialTicks);
         }

      }
   }

   public void resetProjectionMatrix(Matrix4f pMatrix) {
      RenderSystem.matrixMode(5889);
      RenderSystem.loadIdentity();
      RenderSystem.multMatrix(pMatrix);
      RenderSystem.matrixMode(5888);
   }

   public Matrix4f getProjectionMatrix(ActiveRenderInfo p_228382_1_, float p_228382_2_, boolean p_228382_3_) {
      MatrixStack matrixstack = new MatrixStack();
      matrixstack.last().pose().setIdentity();
      if (this.zoom != 1.0F) {
         matrixstack.translate((double)this.zoomX, (double)(-this.zoomY), 0.0D);
         matrixstack.scale(this.zoom, this.zoom, 1.0F);
      }

      matrixstack.last().pose().multiply(Matrix4f.perspective(this.getFov(p_228382_1_, p_228382_2_, p_228382_3_), (float)this.minecraft.getWindow().getWidth() / (float)this.minecraft.getWindow().getHeight(), 0.05F, this.renderDistance * 4.0F));
      return matrixstack.last().pose();
   }

   public static float getNightVisionScale(LivingEntity pLivingEntity, float pNanoTime) {
      int i = pLivingEntity.getEffect(Effects.NIGHT_VISION).getDuration();
      return i > 200 ? 1.0F : 0.7F + MathHelper.sin(((float)i - pNanoTime) * (float)Math.PI * 0.2F) * 0.3F;
   }

   public void render(float pPartialTicks, long pNanoTime, boolean pRenderLevel) {
      if (!this.minecraft.isWindowActive() && this.minecraft.options.pauseOnLostFocus && (!this.minecraft.options.touchscreen || !this.minecraft.mouseHandler.isRightPressed())) {
         if (Util.getMillis() - this.lastActiveTime > 500L) {
            this.minecraft.pauseGame(false);
         }
      } else {
         this.lastActiveTime = Util.getMillis();
      }

      if (!this.minecraft.noRender) {
         int i = (int)(this.minecraft.mouseHandler.xpos() * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth());
         int j = (int)(this.minecraft.mouseHandler.ypos() * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight());
         RenderSystem.viewport(0, 0, this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
         if (pRenderLevel && this.minecraft.level != null) {
            this.minecraft.getProfiler().push("level");
            this.renderLevel(pPartialTicks, pNanoTime, new MatrixStack());
            if (this.minecraft.hasSingleplayerServer() && this.lastScreenshotAttempt < Util.getMillis() - 1000L) {
               this.lastScreenshotAttempt = Util.getMillis();
               if (!this.minecraft.getSingleplayerServer().hasWorldScreenshot()) {
                  this.takeAutoScreenshot();
               }
            }

            this.minecraft.levelRenderer.doEntityOutline();
            if (this.postEffect != null && this.effectActive) {
               RenderSystem.disableBlend();
               RenderSystem.disableDepthTest();
               RenderSystem.disableAlphaTest();
               RenderSystem.enableTexture();
               RenderSystem.matrixMode(5890);
               RenderSystem.pushMatrix();
               RenderSystem.loadIdentity();
               this.postEffect.process(pPartialTicks);
               RenderSystem.popMatrix();
               RenderSystem.enableTexture(); //FORGE: Fix MC-194675
            }

            this.minecraft.getMainRenderTarget().bindWrite(true);
         }

         MainWindow mainwindow = this.minecraft.getWindow();
         RenderSystem.clear(256, Minecraft.ON_OSX);
         RenderSystem.matrixMode(5889);
         RenderSystem.loadIdentity();
         RenderSystem.ortho(0.0D, (double)mainwindow.getWidth() / mainwindow.getGuiScale(), (double)mainwindow.getHeight() / mainwindow.getGuiScale(), 0.0D, 1000.0D, net.minecraftforge.client.ForgeHooksClient.getGuiFarPlane());
         RenderSystem.matrixMode(5888);
         RenderSystem.loadIdentity();
         RenderSystem.translatef(0.0F, 0.0F, 1000.0F - net.minecraftforge.client.ForgeHooksClient.getGuiFarPlane());
         RenderHelper.setupFor3DItems();
         MatrixStack matrixstack = new MatrixStack();
         if (pRenderLevel && this.minecraft.level != null) {
            this.minecraft.getProfiler().popPush("gui");
            if (this.minecraft.player != null) {
               float f = MathHelper.lerp(pPartialTicks, this.minecraft.player.oPortalTime, this.minecraft.player.portalTime);
               if (f > 0.0F && this.minecraft.player.hasEffect(Effects.CONFUSION) && this.minecraft.options.screenEffectScale < 1.0F) {
                  this.renderConfusionOverlay(f * (1.0F - this.minecraft.options.screenEffectScale));
               }
            }

            if (!this.minecraft.options.hideGui || this.minecraft.screen != null) {
               RenderSystem.defaultAlphaFunc();
               this.renderItemActivationAnimation(this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight(), pPartialTicks);
               this.minecraft.gui.render(matrixstack, pPartialTicks);
               RenderSystem.clear(256, Minecraft.ON_OSX);
            }

            this.minecraft.getProfiler().pop();
         }

         if (this.minecraft.overlay != null) {
            try {
               this.minecraft.overlay.render(matrixstack, i, j, this.minecraft.getDeltaFrameTime());
            } catch (Throwable throwable1) {
               CrashReport crashreport = CrashReport.forThrowable(throwable1, "Rendering overlay");
               CrashReportCategory crashreportcategory = crashreport.addCategory("Overlay render details");
               crashreportcategory.setDetail("Overlay name", () -> {
                  return this.minecraft.overlay.getClass().getCanonicalName();
               });
               throw new ReportedException(crashreport);
            }
         } else if (this.minecraft.screen != null) {
            try {
               net.minecraftforge.client.ForgeHooksClient.drawScreen(this.minecraft.screen, matrixstack, i, j, this.minecraft.getDeltaFrameTime());
            } catch (Throwable throwable) {
               CrashReport crashreport1 = CrashReport.forThrowable(throwable, "Rendering screen");
               CrashReportCategory crashreportcategory1 = crashreport1.addCategory("Screen render details");
               crashreportcategory1.setDetail("Screen name", () -> {
                  return this.minecraft.screen.getClass().getCanonicalName();
               });
               crashreportcategory1.setDetail("Mouse location", () -> {
                  return String.format(Locale.ROOT, "Scaled: (%d, %d). Absolute: (%f, %f)", i, j, this.minecraft.mouseHandler.xpos(), this.minecraft.mouseHandler.ypos());
               });
               crashreportcategory1.setDetail("Screen size", () -> {
                  return String.format(Locale.ROOT, "Scaled: (%d, %d). Absolute: (%d, %d). Scale factor of %f", this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight(), this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight(), this.minecraft.getWindow().getGuiScale());
               });
               throw new ReportedException(crashreport1);
            }
         }

      }
   }

   private void takeAutoScreenshot() {
      if (this.minecraft.levelRenderer.countRenderedChunks() > 10 && this.minecraft.levelRenderer.hasRenderedAllChunks() && !this.minecraft.getSingleplayerServer().hasWorldScreenshot()) {
         NativeImage nativeimage = ScreenShotHelper.takeScreenshot(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight(), this.minecraft.getMainRenderTarget());
         Util.ioPool().execute(() -> {
            int i = nativeimage.getWidth();
            int j = nativeimage.getHeight();
            int k = 0;
            int l = 0;
            if (i > j) {
               k = (i - j) / 2;
               i = j;
            } else {
               l = (j - i) / 2;
               j = i;
            }

            try (NativeImage nativeimage1 = new NativeImage(64, 64, false)) {
               nativeimage.resizeSubRectTo(k, l, i, j, nativeimage1);
               nativeimage1.writeToFile(this.minecraft.getSingleplayerServer().getWorldScreenshotFile());
            } catch (IOException ioexception) {
               LOGGER.warn("Couldn't save auto screenshot", (Throwable)ioexception);
            } finally {
               nativeimage.close();
            }

         });
      }

   }

   private boolean shouldRenderBlockOutline() {
      if (!this.renderBlockOutline) {
         return false;
      } else {
         Entity entity = this.minecraft.getCameraEntity();
         boolean flag = entity instanceof PlayerEntity && !this.minecraft.options.hideGui;
         if (flag && !((PlayerEntity)entity).abilities.mayBuild) {
            ItemStack itemstack = ((LivingEntity)entity).getMainHandItem();
            RayTraceResult raytraceresult = this.minecraft.hitResult;
            if (raytraceresult != null && raytraceresult.getType() == RayTraceResult.Type.BLOCK) {
               BlockPos blockpos = ((BlockRayTraceResult)raytraceresult).getBlockPos();
               BlockState blockstate = this.minecraft.level.getBlockState(blockpos);
               if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
                  flag = blockstate.getMenuProvider(this.minecraft.level, blockpos) != null;
               } else {
                  CachedBlockInfo cachedblockinfo = new CachedBlockInfo(this.minecraft.level, blockpos, false);
                  flag = !itemstack.isEmpty() && (itemstack.hasAdventureModeBreakTagForBlock(this.minecraft.level.getTagManager(), cachedblockinfo) || itemstack.hasAdventureModePlaceTagForBlock(this.minecraft.level.getTagManager(), cachedblockinfo));
               }
            }
         }

         return flag;
      }
   }

   public void renderLevel(float pPartialTicks, long pFinishTimeNano, MatrixStack pMatrixStack) {
      this.lightTexture.updateLightTexture(pPartialTicks);
      if (this.minecraft.getCameraEntity() == null) {
         this.minecraft.setCameraEntity(this.minecraft.player);
      }

      this.pick(pPartialTicks);
      this.minecraft.getProfiler().push("center");
      boolean flag = this.shouldRenderBlockOutline();
      this.minecraft.getProfiler().popPush("camera");
      ActiveRenderInfo activerenderinfo = this.mainCamera;
      this.renderDistance = (float)(this.minecraft.options.renderDistance * 16);
      MatrixStack matrixstack = new MatrixStack();
      matrixstack.last().pose().multiply(this.getProjectionMatrix(activerenderinfo, pPartialTicks, true));
      this.bobHurt(matrixstack, pPartialTicks);
      if (this.minecraft.options.bobView) {
         this.bobView(matrixstack, pPartialTicks);
      }

      float f = MathHelper.lerp(pPartialTicks, this.minecraft.player.oPortalTime, this.minecraft.player.portalTime) * this.minecraft.options.screenEffectScale * this.minecraft.options.screenEffectScale;
      if (f > 0.0F) {
         int i = this.minecraft.player.hasEffect(Effects.CONFUSION) ? 7 : 20;
         float f1 = 5.0F / (f * f + 5.0F) - f * 0.04F;
         f1 = f1 * f1;
         Vector3f vector3f = new Vector3f(0.0F, MathHelper.SQRT_OF_TWO / 2.0F, MathHelper.SQRT_OF_TWO / 2.0F);
         matrixstack.mulPose(vector3f.rotationDegrees(((float)this.tick + pPartialTicks) * (float)i));
         matrixstack.scale(1.0F / f1, 1.0F, 1.0F);
         float f2 = -((float)this.tick + pPartialTicks) * (float)i;
         matrixstack.mulPose(vector3f.rotationDegrees(f2));
      }

      Matrix4f matrix4f = matrixstack.last().pose();
      this.resetProjectionMatrix(matrix4f);
      activerenderinfo.setup(this.minecraft.level, (Entity)(this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity()), !this.minecraft.options.getCameraType().isFirstPerson(), this.minecraft.options.getCameraType().isMirrored(), pPartialTicks);

      net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup cameraSetup = net.minecraftforge.client.ForgeHooksClient.onCameraSetup(this, activerenderinfo, pPartialTicks);
      activerenderinfo.setAnglesInternal(cameraSetup.getYaw(), cameraSetup.getPitch());
      pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(cameraSetup.getRoll()));

      pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(activerenderinfo.getXRot()));
      pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(activerenderinfo.getYRot() + 180.0F));
      this.minecraft.levelRenderer.renderLevel(pMatrixStack, pPartialTicks, pFinishTimeNano, flag, activerenderinfo, this, this.lightTexture, matrix4f);
      this.minecraft.getProfiler().popPush("forge_render_last");
      net.minecraftforge.client.ForgeHooksClient.dispatchRenderLast(this.minecraft.levelRenderer, pMatrixStack, pPartialTicks, matrix4f, pFinishTimeNano);
      this.minecraft.getProfiler().popPush("hand");
      if (this.renderHand) {
         RenderSystem.clear(256, Minecraft.ON_OSX);
         this.renderItemInHand(pMatrixStack, activerenderinfo, pPartialTicks);
      }

      this.minecraft.getProfiler().pop();
   }

   public void resetData() {
      this.itemActivationItem = null;
      this.mapRenderer.resetData();
      this.mainCamera.reset();
   }

   public MapItemRenderer getMapRenderer() {
      return this.mapRenderer;
   }

   public void displayItemActivation(ItemStack pStack) {
      this.itemActivationItem = pStack;
      this.itemActivationTicks = 40;
      this.itemActivationOffX = this.random.nextFloat() * 2.0F - 1.0F;
      this.itemActivationOffY = this.random.nextFloat() * 2.0F - 1.0F;
   }

   private void renderItemActivationAnimation(int pWidthsp, int pHeightScaled, float pPartialTicks) {
      if (this.itemActivationItem != null && this.itemActivationTicks > 0) {
         int i = 40 - this.itemActivationTicks;
         float f = ((float)i + pPartialTicks) / 40.0F;
         float f1 = f * f;
         float f2 = f * f1;
         float f3 = 10.25F * f2 * f1 - 24.95F * f1 * f1 + 25.5F * f2 - 13.8F * f1 + 4.0F * f;
         float f4 = f3 * (float)Math.PI;
         float f5 = this.itemActivationOffX * (float)(pWidthsp / 4);
         float f6 = this.itemActivationOffY * (float)(pHeightScaled / 4);
         RenderSystem.enableAlphaTest();
         RenderSystem.pushMatrix();
         RenderSystem.pushLightingAttributes();
         RenderSystem.enableDepthTest();
         RenderSystem.disableCull();
         MatrixStack matrixstack = new MatrixStack();
         matrixstack.pushPose();
         matrixstack.translate((double)((float)(pWidthsp / 2) + f5 * MathHelper.abs(MathHelper.sin(f4 * 2.0F))), (double)((float)(pHeightScaled / 2) + f6 * MathHelper.abs(MathHelper.sin(f4 * 2.0F))), -50.0D);
         float f7 = 50.0F + 175.0F * MathHelper.sin(f4);
         matrixstack.scale(f7, -f7, f7);
         matrixstack.mulPose(Vector3f.YP.rotationDegrees(900.0F * MathHelper.abs(MathHelper.sin(f4))));
         matrixstack.mulPose(Vector3f.XP.rotationDegrees(6.0F * MathHelper.cos(f * 8.0F)));
         matrixstack.mulPose(Vector3f.ZP.rotationDegrees(6.0F * MathHelper.cos(f * 8.0F)));
         IRenderTypeBuffer.Impl irendertypebuffer$impl = this.renderBuffers.bufferSource();
         this.minecraft.getItemRenderer().renderStatic(this.itemActivationItem, ItemCameraTransforms.TransformType.FIXED, 15728880, OverlayTexture.NO_OVERLAY, matrixstack, irendertypebuffer$impl);
         matrixstack.popPose();
         irendertypebuffer$impl.endBatch();
         RenderSystem.popAttributes();
         RenderSystem.popMatrix();
         RenderSystem.enableCull();
         RenderSystem.disableDepthTest();
      }
   }

   private void renderConfusionOverlay(float p_243497_1_) {
      int i = this.minecraft.getWindow().getGuiScaledWidth();
      int j = this.minecraft.getWindow().getGuiScaledHeight();
      double d0 = MathHelper.lerp((double)p_243497_1_, 2.0D, 1.0D);
      float f = 0.2F * p_243497_1_;
      float f1 = 0.4F * p_243497_1_;
      float f2 = 0.2F * p_243497_1_;
      double d1 = (double)i * d0;
      double d2 = (double)j * d0;
      double d3 = ((double)i - d1) / 2.0D;
      double d4 = ((double)j - d2) / 2.0D;
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.enableBlend();
      RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
      RenderSystem.color4f(f, f1, f2, 1.0F);
      this.minecraft.getTextureManager().bind(NAUSEA_LOCATION);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuilder();
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
      bufferbuilder.vertex(d3, d4 + d2, -90.0D).uv(0.0F, 1.0F).endVertex();
      bufferbuilder.vertex(d3 + d1, d4 + d2, -90.0D).uv(1.0F, 1.0F).endVertex();
      bufferbuilder.vertex(d3 + d1, d4, -90.0D).uv(1.0F, 0.0F).endVertex();
      bufferbuilder.vertex(d3, d4, -90.0D).uv(0.0F, 0.0F).endVertex();
      tessellator.end();
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
   }

   public float getDarkenWorldAmount(float pPartialTicks) {
      return MathHelper.lerp(pPartialTicks, this.darkenWorldAmountO, this.darkenWorldAmount);
   }

   public float getRenderDistance() {
      return this.renderDistance;
   }

   public ActiveRenderInfo getMainCamera() {
      return this.mainCamera;
   }

   public LightTexture lightTexture() {
      return this.lightTexture;
   }

   public OverlayTexture overlayTexture() {
      return this.overlayTexture;
   }

   @Override
   public net.minecraftforge.resource.IResourceType getResourceType() {
      return net.minecraftforge.resource.VanillaResourceType.SHADERS;
   }
}
