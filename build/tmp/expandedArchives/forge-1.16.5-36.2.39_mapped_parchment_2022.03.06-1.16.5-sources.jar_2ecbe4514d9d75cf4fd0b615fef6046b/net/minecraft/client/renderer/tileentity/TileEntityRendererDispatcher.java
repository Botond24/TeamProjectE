package net.minecraft.client.renderer.tileentity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.model.ShulkerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TileEntityRendererDispatcher {
   private final Map<TileEntityType<?>, TileEntityRenderer<?>> renderers = Maps.newHashMap();
   public static final TileEntityRendererDispatcher instance = new TileEntityRendererDispatcher();
   private final BufferBuilder singleRenderBuffer = new BufferBuilder(256);
   public FontRenderer font;
   public TextureManager textureManager;
   public World level;
   public ActiveRenderInfo camera;
   public RayTraceResult cameraHitResult;

   private TileEntityRendererDispatcher() {
      this.register(TileEntityType.SIGN, new SignTileEntityRenderer(this));
      this.register(TileEntityType.MOB_SPAWNER, new MobSpawnerTileEntityRenderer(this));
      this.register(TileEntityType.PISTON, new PistonTileEntityRenderer(this));
      this.register(TileEntityType.CHEST, new ChestTileEntityRenderer<>(this));
      this.register(TileEntityType.ENDER_CHEST, new ChestTileEntityRenderer<>(this));
      this.register(TileEntityType.TRAPPED_CHEST, new ChestTileEntityRenderer<>(this));
      this.register(TileEntityType.ENCHANTING_TABLE, new EnchantmentTableTileEntityRenderer(this));
      this.register(TileEntityType.LECTERN, new LecternTileEntityRenderer(this));
      this.register(TileEntityType.END_PORTAL, new EndPortalTileEntityRenderer<>(this));
      this.register(TileEntityType.END_GATEWAY, new EndGatewayTileEntityRenderer(this));
      this.register(TileEntityType.BEACON, new BeaconTileEntityRenderer(this));
      this.register(TileEntityType.SKULL, new SkullTileEntityRenderer(this));
      this.register(TileEntityType.BANNER, new BannerTileEntityRenderer(this));
      this.register(TileEntityType.STRUCTURE_BLOCK, new StructureTileEntityRenderer(this));
      this.register(TileEntityType.SHULKER_BOX, new ShulkerBoxTileEntityRenderer(new ShulkerModel(), this));
      this.register(TileEntityType.BED, new BedTileEntityRenderer(this));
      this.register(TileEntityType.CONDUIT, new ConduitTileEntityRenderer(this));
      this.register(TileEntityType.BELL, new BellTileEntityRenderer(this));
      this.register(TileEntityType.CAMPFIRE, new CampfireTileEntityRenderer(this));
   }

   private <E extends TileEntity> void register(TileEntityType<E> p_228854_1_, TileEntityRenderer<E> p_228854_2_) {
      this.renderers.put(p_228854_1_, p_228854_2_);
   }

   @Nullable
   public <E extends TileEntity> TileEntityRenderer<E> getRenderer(E pBlockEntity) {
      return (TileEntityRenderer<E>)this.renderers.get(pBlockEntity.getType());
   }

   public void prepare(World p_217665_1_, TextureManager p_217665_2_, FontRenderer p_217665_3_, ActiveRenderInfo p_217665_4_, RayTraceResult p_217665_5_) {
      if (this.level != p_217665_1_) {
         this.setLevel(p_217665_1_);
      }

      this.textureManager = p_217665_2_;
      this.camera = p_217665_4_;
      this.font = p_217665_3_;
      this.cameraHitResult = p_217665_5_;
   }

   public <E extends TileEntity> void render(E pBlockEntity, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer) {
      if (Vector3d.atCenterOf(pBlockEntity.getBlockPos()).closerThan(this.camera.getPosition(), pBlockEntity.getViewDistance())) {
         TileEntityRenderer<E> tileentityrenderer = this.getRenderer(pBlockEntity);
         if (tileentityrenderer != null) {
            if (pBlockEntity.hasLevel() && pBlockEntity.getType().isValid(pBlockEntity.getBlockState().getBlock())) {
               tryRender(pBlockEntity, () -> {
                  setupAndRender(tileentityrenderer, pBlockEntity, pPartialTicks, pMatrixStack, pBuffer);
               });
            }
         }
      }
   }

   private static <T extends TileEntity> void setupAndRender(TileEntityRenderer<T> pRenderer, T pBlockEntity, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer) {
      World world = pBlockEntity.getLevel();
      int i;
      if (world != null) {
         i = WorldRenderer.getLightColor(world, pBlockEntity.getBlockPos());
      } else {
         i = 15728880;
      }

      pRenderer.render(pBlockEntity, pPartialTicks, pMatrixStack, pBuffer, i, OverlayTexture.NO_OVERLAY);
   }

   /**
    * Returns true if no renderer found, false if render completed
    */
   public <E extends TileEntity> boolean renderItem(E pBlockEntity, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, int pCombinedOverlay) {
      TileEntityRenderer<E> tileentityrenderer = this.getRenderer(pBlockEntity);
      if (tileentityrenderer == null) {
         return true;
      } else {
         tryRender(pBlockEntity, () -> {
            tileentityrenderer.render(pBlockEntity, 0.0F, pMatrixStack, pBuffer, pCombinedLight, pCombinedOverlay);
         });
         return false;
      }
   }

   private static void tryRender(TileEntity pBlockEntity, Runnable pRunnable) {
      try {
         pRunnable.run();
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering Block Entity");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Block Entity Details");
         pBlockEntity.fillCrashReportCategory(crashreportcategory);
         throw new ReportedException(crashreport);
      }
   }

   public void setLevel(@Nullable World pLevel) {
      this.level = pLevel;
      if (pLevel == null) {
         this.camera = null;
      }

   }

   public FontRenderer getFont() {
      return this.font;
   }

   //Internal, Do not call Use ClientRegistry.
   public synchronized <T extends TileEntity> void setSpecialRendererInternal(TileEntityType<T> tileEntityType, TileEntityRenderer<? super T> specialRenderer) {
      this.renderers.put(tileEntityType, specialRenderer);
   }
}
