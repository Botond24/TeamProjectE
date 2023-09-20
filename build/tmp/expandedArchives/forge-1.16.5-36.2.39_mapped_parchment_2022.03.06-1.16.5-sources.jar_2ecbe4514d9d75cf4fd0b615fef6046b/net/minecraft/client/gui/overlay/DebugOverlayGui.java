package net.minecraft.client.gui.overlay;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.PlatformDescriptors;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.DataFixUtils;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.WorldEntitySpawner;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DebugOverlayGui extends AbstractGui {
   private static final Map<Heightmap.Type, String> HEIGHTMAP_NAMES = Util.make(new EnumMap<>(Heightmap.Type.class), (p_212918_0_) -> {
      p_212918_0_.put(Heightmap.Type.WORLD_SURFACE_WG, "SW");
      p_212918_0_.put(Heightmap.Type.WORLD_SURFACE, "S");
      p_212918_0_.put(Heightmap.Type.OCEAN_FLOOR_WG, "OW");
      p_212918_0_.put(Heightmap.Type.OCEAN_FLOOR, "O");
      p_212918_0_.put(Heightmap.Type.MOTION_BLOCKING, "M");
      p_212918_0_.put(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, "ML");
   });
   private final Minecraft minecraft;
   private final FontRenderer font;
   protected RayTraceResult block;
   protected RayTraceResult liquid;
   @Nullable
   private ChunkPos lastPos;
   @Nullable
   private Chunk clientChunk;
   @Nullable
   private CompletableFuture<Chunk> serverChunk;

   public DebugOverlayGui(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
      this.font = pMinecraft.font;
   }

   public void clearChunkCache() {
      this.serverChunk = null;
      this.clientChunk = null;
   }

   public void render(MatrixStack pPoseStack) {
      this.minecraft.getProfiler().push("debug");
      RenderSystem.pushMatrix();
      Entity entity = this.minecraft.getCameraEntity();
      this.block = entity.pick(20.0D, 0.0F, false);
      this.liquid = entity.pick(20.0D, 0.0F, true);
      this.drawGameInformation(pPoseStack);
      this.drawSystemInformation(pPoseStack);
      RenderSystem.popMatrix();
      if (this.minecraft.options.renderFpsChart) {
         int i = this.minecraft.getWindow().getGuiScaledWidth();
         this.drawChart(pPoseStack, this.minecraft.getFrameTimer(), 0, i / 2, true);
         IntegratedServer integratedserver = this.minecraft.getSingleplayerServer();
         if (integratedserver != null) {
            this.drawChart(pPoseStack, integratedserver.getFrameTimer(), i - Math.min(i / 2, 240), i / 2, false);
         }
      }

      this.minecraft.getProfiler().pop();
   }

   protected void drawGameInformation(MatrixStack pPoseStack) {
      List<String> list = this.getGameInformation();
      list.add("");
      boolean flag = this.minecraft.getSingleplayerServer() != null;
      list.add("Debug: Pie [shift]: " + (this.minecraft.options.renderDebugCharts ? "visible" : "hidden") + (flag ? " FPS + TPS" : " FPS") + " [alt]: " + (this.minecraft.options.renderFpsChart ? "visible" : "hidden"));
      list.add("For help: press F3 + Q");

      for(int i = 0; i < list.size(); ++i) {
         String s = list.get(i);
         if (!Strings.isNullOrEmpty(s)) {
            int j = 9;
            int k = this.font.width(s);
            int l = 2;
            int i1 = 2 + j * i;
            fill(pPoseStack, 1, i1 - 1, 2 + k + 1, i1 + j - 1, -1873784752);
            this.font.draw(pPoseStack, s, 2.0F, (float)i1, 14737632);
         }
      }

   }

   protected void drawSystemInformation(MatrixStack pPoseStack) {
      List<String> list = this.getSystemInformation();

      for(int i = 0; i < list.size(); ++i) {
         String s = list.get(i);
         if (!Strings.isNullOrEmpty(s)) {
            int j = 9;
            int k = this.font.width(s);
            int l = this.minecraft.getWindow().getGuiScaledWidth() - 2 - k;
            int i1 = 2 + j * i;
            fill(pPoseStack, l - 1, i1 - 1, l + k + 1, i1 + j - 1, -1873784752);
            this.font.draw(pPoseStack, s, (float)l, (float)i1, 14737632);
         }
      }

   }

   protected List<String> getGameInformation() {
      IntegratedServer integratedserver = this.minecraft.getSingleplayerServer();
      NetworkManager networkmanager = this.minecraft.getConnection().getConnection();
      float f = networkmanager.getAverageSentPackets();
      float f1 = networkmanager.getAverageReceivedPackets();
      String s;
      if (integratedserver != null) {
         s = String.format("Integrated server @ %.0f ms ticks, %.0f tx, %.0f rx", integratedserver.getAverageTickTime(), f, f1);
      } else {
         s = String.format("\"%s\" server, %.0f tx, %.0f rx", this.minecraft.player.getServerBrand(), f, f1);
      }

      BlockPos blockpos = this.minecraft.getCameraEntity().blockPosition();
      if (this.minecraft.showOnlyReducedInfo()) {
         return Lists.newArrayList("Minecraft " + SharedConstants.getCurrentVersion().getName() + " (" + this.minecraft.getLaunchedVersion() + "/" + ClientBrandRetriever.getClientModName() + ")", this.minecraft.fpsString, s, this.minecraft.levelRenderer.getChunkStatistics(), this.minecraft.levelRenderer.getEntityStatistics(), "P: " + this.minecraft.particleEngine.countParticles() + ". T: " + this.minecraft.level.getEntityCount(), this.minecraft.level.gatherChunkSourceStats(), "", String.format("Chunk-relative: %d %d %d", blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15));
      } else {
         Entity entity = this.minecraft.getCameraEntity();
         Direction direction = entity.getDirection();
         String s1;
         switch(direction) {
         case NORTH:
            s1 = "Towards negative Z";
            break;
         case SOUTH:
            s1 = "Towards positive Z";
            break;
         case WEST:
            s1 = "Towards negative X";
            break;
         case EAST:
            s1 = "Towards positive X";
            break;
         default:
            s1 = "Invalid";
         }

         ChunkPos chunkpos = new ChunkPos(blockpos);
         if (!Objects.equals(this.lastPos, chunkpos)) {
            this.lastPos = chunkpos;
            this.clearChunkCache();
         }

         World world = this.getLevel();
         LongSet longset = (LongSet)(world instanceof ServerWorld ? ((ServerWorld)world).getForcedChunks() : LongSets.EMPTY_SET);
         List<String> list = Lists.newArrayList("Minecraft " + SharedConstants.getCurrentVersion().getName() + " (" + this.minecraft.getLaunchedVersion() + "/" + ClientBrandRetriever.getClientModName() + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType()) + ")", this.minecraft.fpsString, s, this.minecraft.levelRenderer.getChunkStatistics(), this.minecraft.levelRenderer.getEntityStatistics(), "P: " + this.minecraft.particleEngine.countParticles() + ". T: " + this.minecraft.level.getEntityCount(), this.minecraft.level.gatherChunkSourceStats());
         String s2 = this.getServerChunkStats();
         if (s2 != null) {
            list.add(s2);
         }

         list.add(this.minecraft.level.dimension().location() + " FC: " + longset.size());
         list.add("");
         list.add(String.format(Locale.ROOT, "XYZ: %.3f / %.5f / %.3f", this.minecraft.getCameraEntity().getX(), this.minecraft.getCameraEntity().getY(), this.minecraft.getCameraEntity().getZ()));
         list.add(String.format("Block: %d %d %d", blockpos.getX(), blockpos.getY(), blockpos.getZ()));
         list.add(String.format("Chunk: %d %d %d in %d %d %d", blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15, blockpos.getX() >> 4, blockpos.getY() >> 4, blockpos.getZ() >> 4));
         list.add(String.format(Locale.ROOT, "Facing: %s (%s) (%.1f / %.1f)", direction, s1, MathHelper.wrapDegrees(entity.yRot), MathHelper.wrapDegrees(entity.xRot)));
         if (this.minecraft.level != null) {
            if (this.minecraft.level.hasChunkAt(blockpos)) {
               Chunk chunk = this.getClientChunk();
               if (chunk.isEmpty()) {
                  list.add("Waiting for chunk...");
               } else {
                  int i = this.minecraft.level.getChunkSource().getLightEngine().getRawBrightness(blockpos, 0);
                  int j = this.minecraft.level.getBrightness(LightType.SKY, blockpos);
                  int k = this.minecraft.level.getBrightness(LightType.BLOCK, blockpos);
                  list.add("Client Light: " + i + " (" + j + " sky, " + k + " block)");
                  Chunk chunk1 = this.getServerChunk();
                  if (chunk1 != null) {
                     WorldLightManager worldlightmanager = world.getChunkSource().getLightEngine();
                     list.add("Server Light: (" + worldlightmanager.getLayerListener(LightType.SKY).getLightValue(blockpos) + " sky, " + worldlightmanager.getLayerListener(LightType.BLOCK).getLightValue(blockpos) + " block)");
                  } else {
                     list.add("Server Light: (?? sky, ?? block)");
                  }

                  StringBuilder stringbuilder = new StringBuilder("CH");

                  for(Heightmap.Type heightmap$type : Heightmap.Type.values()) {
                     if (heightmap$type.sendToClient()) {
                        stringbuilder.append(" ").append(HEIGHTMAP_NAMES.get(heightmap$type)).append(": ").append(chunk.getHeight(heightmap$type, blockpos.getX(), blockpos.getZ()));
                     }
                  }

                  list.add(stringbuilder.toString());
                  stringbuilder.setLength(0);
                  stringbuilder.append("SH");

                  for(Heightmap.Type heightmap$type1 : Heightmap.Type.values()) {
                     if (heightmap$type1.keepAfterWorldgen()) {
                        stringbuilder.append(" ").append(HEIGHTMAP_NAMES.get(heightmap$type1)).append(": ");
                        if (chunk1 != null) {
                           stringbuilder.append(chunk1.getHeight(heightmap$type1, blockpos.getX(), blockpos.getZ()));
                        } else {
                           stringbuilder.append("??");
                        }
                     }
                  }

                  list.add(stringbuilder.toString());
                  if (blockpos.getY() >= 0 && blockpos.getY() < 256) {
                     list.add("Biome: " + this.minecraft.level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getKey(this.minecraft.level.getBiome(blockpos)));
                     long i1 = 0L;
                     float f2 = 0.0F;
                     if (chunk1 != null) {
                        f2 = world.getMoonBrightness();
                        i1 = chunk1.getInhabitedTime();
                     }

                     DifficultyInstance difficultyinstance = new DifficultyInstance(world.getDifficulty(), world.getDayTime(), i1, f2);
                     list.add(String.format(Locale.ROOT, "Local Difficulty: %.2f // %.2f (Day %d)", difficultyinstance.getEffectiveDifficulty(), difficultyinstance.getSpecialMultiplier(), this.minecraft.level.getDayTime() / 24000L));
                  }
               }
            } else {
               list.add("Outside of world...");
            }
         } else {
            list.add("Outside of world...");
         }

         ServerWorld serverworld = this.getServerLevel();
         if (serverworld != null) {
            WorldEntitySpawner.EntityDensityManager worldentityspawner$entitydensitymanager = serverworld.getChunkSource().getLastSpawnState();
            if (worldentityspawner$entitydensitymanager != null) {
               Object2IntMap<EntityClassification> object2intmap = worldentityspawner$entitydensitymanager.getMobCategoryCounts();
               int l = worldentityspawner$entitydensitymanager.getSpawnableChunkCount();
               list.add("SC: " + l + ", " + (String)Stream.of(EntityClassification.values()).map((p_238511_1_) -> {
                  return Character.toUpperCase(p_238511_1_.getName().charAt(0)) + ": " + object2intmap.getInt(p_238511_1_);
               }).collect(Collectors.joining(", ")));
            } else {
               list.add("SC: N/A");
            }
         }

         ShaderGroup shadergroup = this.minecraft.gameRenderer.currentEffect();
         if (shadergroup != null) {
            list.add("Shader: " + shadergroup.getName());
         }

         list.add(this.minecraft.getSoundManager().getDebugString() + String.format(" (Mood %d%%)", Math.round(this.minecraft.player.getCurrentMood() * 100.0F)));
         return list;
      }
   }

   @Nullable
   private ServerWorld getServerLevel() {
      IntegratedServer integratedserver = this.minecraft.getSingleplayerServer();
      return integratedserver != null ? integratedserver.getLevel(this.minecraft.level.dimension()) : null;
   }

   @Nullable
   private String getServerChunkStats() {
      ServerWorld serverworld = this.getServerLevel();
      return serverworld != null ? serverworld.gatherChunkSourceStats() : null;
   }

   private World getLevel() {
      return DataFixUtils.orElse(Optional.ofNullable(this.minecraft.getSingleplayerServer()).flatMap((p_212917_1_) -> {
         return Optional.ofNullable(p_212917_1_.getLevel(this.minecraft.level.dimension()));
      }), this.minecraft.level);
   }

   @Nullable
   private Chunk getServerChunk() {
      if (this.serverChunk == null) {
         ServerWorld serverworld = this.getServerLevel();
         if (serverworld != null) {
            this.serverChunk = serverworld.getChunkSource().getChunkFuture(this.lastPos.x, this.lastPos.z, ChunkStatus.FULL, false).thenApply((p_222802_0_) -> {
               return p_222802_0_.map((p_222803_0_) -> {
                  return (Chunk)p_222803_0_;
               }, (p_222801_0_) -> {
                  return null;
               });
            });
         }

         if (this.serverChunk == null) {
            this.serverChunk = CompletableFuture.completedFuture(this.getClientChunk());
         }
      }

      return this.serverChunk.getNow((Chunk)null);
   }

   private Chunk getClientChunk() {
      if (this.clientChunk == null) {
         this.clientChunk = this.minecraft.level.getChunk(this.lastPos.x, this.lastPos.z);
      }

      return this.clientChunk;
   }

   protected List<String> getSystemInformation() {
      long i = Runtime.getRuntime().maxMemory();
      long j = Runtime.getRuntime().totalMemory();
      long k = Runtime.getRuntime().freeMemory();
      long l = j - k;
      List<String> list = Lists.newArrayList(String.format("Java: %s %dbit", System.getProperty("java.version"), this.minecraft.is64Bit() ? 64 : 32), String.format("Mem: % 2d%% %03d/%03dMB", l * 100L / i, bytesToMegabytes(l), bytesToMegabytes(i)), String.format("Allocated: % 2d%% %03dMB", j * 100L / i, bytesToMegabytes(j)), "", String.format("CPU: %s", PlatformDescriptors.getCpuInfo()), "", String.format("Display: %dx%d (%s)", Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), PlatformDescriptors.getVendor()), PlatformDescriptors.getRenderer(), PlatformDescriptors.getOpenGLVersion());
      if (this.minecraft.showOnlyReducedInfo()) {
         return list;
      } else {
         if (this.block.getType() == RayTraceResult.Type.BLOCK) {
            BlockPos blockpos = ((BlockRayTraceResult)this.block).getBlockPos();
            BlockState blockstate = this.minecraft.level.getBlockState(blockpos);
            list.add("");
            list.add(TextFormatting.UNDERLINE + "Targeted Block: " + blockpos.getX() + ", " + blockpos.getY() + ", " + blockpos.getZ());
            list.add(String.valueOf((Object)Registry.BLOCK.getKey(blockstate.getBlock())));

            for(Entry<Property<?>, Comparable<?>> entry : blockstate.getValues().entrySet()) {
               list.add(this.getPropertyValueString(entry));
            }

            for(ResourceLocation resourcelocation : blockstate.getBlock().getTags()) {
               list.add("#" + resourcelocation);
            }
         }

         if (this.liquid.getType() == RayTraceResult.Type.BLOCK) {
            BlockPos blockpos1 = ((BlockRayTraceResult)this.liquid).getBlockPos();
            FluidState fluidstate = this.minecraft.level.getFluidState(blockpos1);
            list.add("");
            list.add(TextFormatting.UNDERLINE + "Targeted Fluid: " + blockpos1.getX() + ", " + blockpos1.getY() + ", " + blockpos1.getZ());
            list.add(String.valueOf((Object)Registry.FLUID.getKey(fluidstate.getType())));

            for(Entry<Property<?>, Comparable<?>> entry1 : fluidstate.getValues().entrySet()) {
               list.add(this.getPropertyValueString(entry1));
            }

            for(ResourceLocation resourcelocation1 : fluidstate.getType().getTags()) {
               list.add("#" + resourcelocation1);
            }
         }

         Entity entity = this.minecraft.crosshairPickEntity;
         if (entity != null) {
            list.add("");
            list.add(TextFormatting.UNDERLINE + "Targeted Entity");
            list.add(String.valueOf((Object)Registry.ENTITY_TYPE.getKey(entity.getType())));
            entity.getType().getTags().forEach(t -> list.add("#" + t));
         }

         return list;
      }
   }

   private String getPropertyValueString(Entry<Property<?>, Comparable<?>> pEntry) {
      Property<?> property = pEntry.getKey();
      Comparable<?> comparable = pEntry.getValue();
      String s = Util.getPropertyName(property, comparable);
      if (Boolean.TRUE.equals(comparable)) {
         s = TextFormatting.GREEN + s;
      } else if (Boolean.FALSE.equals(comparable)) {
         s = TextFormatting.RED + s;
      }

      return property.getName() + ": " + s;
   }

   private void drawChart(MatrixStack pPoseStack, FrameTimer pFrameTimer, int p_238509_3_, int p_238509_4_, boolean p_238509_5_) {
      RenderSystem.disableDepthTest();
      int i = pFrameTimer.getLogStart();
      int j = pFrameTimer.getLogEnd();
      long[] along = pFrameTimer.getLog();
      int l = p_238509_3_;
      int i1 = Math.max(0, along.length - p_238509_4_);
      int j1 = along.length - i1;
      int lvt_9_1_ = pFrameTimer.wrapIndex(i + i1);
      long k1 = 0L;
      int l1 = Integer.MAX_VALUE;
      int i2 = Integer.MIN_VALUE;

      for(int j2 = 0; j2 < j1; ++j2) {
         int k2 = (int)(along[pFrameTimer.wrapIndex(lvt_9_1_ + j2)] / 1000000L);
         l1 = Math.min(l1, k2);
         i2 = Math.max(i2, k2);
         k1 += (long)k2;
      }

      int k4 = this.minecraft.getWindow().getGuiScaledHeight();
      fill(pPoseStack, p_238509_3_, k4 - 60, p_238509_3_ + j1, k4, -1873784752);
      BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
      RenderSystem.enableBlend();
      RenderSystem.disableTexture();
      RenderSystem.defaultBlendFunc();
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);

      for(Matrix4f matrix4f = TransformationMatrix.identity().getMatrix(); lvt_9_1_ != j; lvt_9_1_ = pFrameTimer.wrapIndex(lvt_9_1_ + 1)) {
         int l2 = pFrameTimer.scaleSampleTo(along[lvt_9_1_], p_238509_5_ ? 30 : 60, p_238509_5_ ? 60 : 20);
         int i3 = p_238509_5_ ? 100 : 60;
         int j3 = this.getSampleColor(MathHelper.clamp(l2, 0, i3), 0, i3 / 2, i3);
         int k3 = j3 >> 24 & 255;
         int l3 = j3 >> 16 & 255;
         int i4 = j3 >> 8 & 255;
         int j4 = j3 & 255;
         bufferbuilder.vertex(matrix4f, (float)(l + 1), (float)k4, 0.0F).color(l3, i4, j4, k3).endVertex();
         bufferbuilder.vertex(matrix4f, (float)(l + 1), (float)(k4 - l2 + 1), 0.0F).color(l3, i4, j4, k3).endVertex();
         bufferbuilder.vertex(matrix4f, (float)l, (float)(k4 - l2 + 1), 0.0F).color(l3, i4, j4, k3).endVertex();
         bufferbuilder.vertex(matrix4f, (float)l, (float)k4, 0.0F).color(l3, i4, j4, k3).endVertex();
         ++l;
      }

      bufferbuilder.end();
      WorldVertexBufferUploader.end(bufferbuilder);
      RenderSystem.enableTexture();
      RenderSystem.disableBlend();
      if (p_238509_5_) {
         fill(pPoseStack, p_238509_3_ + 1, k4 - 30 + 1, p_238509_3_ + 14, k4 - 30 + 10, -1873784752);
         this.font.draw(pPoseStack, "60 FPS", (float)(p_238509_3_ + 2), (float)(k4 - 30 + 2), 14737632);
         this.hLine(pPoseStack, p_238509_3_, p_238509_3_ + j1 - 1, k4 - 30, -1);
         fill(pPoseStack, p_238509_3_ + 1, k4 - 60 + 1, p_238509_3_ + 14, k4 - 60 + 10, -1873784752);
         this.font.draw(pPoseStack, "30 FPS", (float)(p_238509_3_ + 2), (float)(k4 - 60 + 2), 14737632);
         this.hLine(pPoseStack, p_238509_3_, p_238509_3_ + j1 - 1, k4 - 60, -1);
      } else {
         fill(pPoseStack, p_238509_3_ + 1, k4 - 60 + 1, p_238509_3_ + 14, k4 - 60 + 10, -1873784752);
         this.font.draw(pPoseStack, "20 TPS", (float)(p_238509_3_ + 2), (float)(k4 - 60 + 2), 14737632);
         this.hLine(pPoseStack, p_238509_3_, p_238509_3_ + j1 - 1, k4 - 60, -1);
      }

      this.hLine(pPoseStack, p_238509_3_, p_238509_3_ + j1 - 1, k4 - 1, -1);
      this.vLine(pPoseStack, p_238509_3_, k4 - 60, k4, -1);
      this.vLine(pPoseStack, p_238509_3_ + j1 - 1, k4 - 60, k4, -1);
      if (p_238509_5_ && this.minecraft.options.framerateLimit > 0 && this.minecraft.options.framerateLimit <= 250) {
         this.hLine(pPoseStack, p_238509_3_, p_238509_3_ + j1 - 1, k4 - 1 - (int)(1800.0D / (double)this.minecraft.options.framerateLimit), -16711681);
      }

      String s = l1 + " ms min";
      String s1 = k1 / (long)j1 + " ms avg";
      String s2 = i2 + " ms max";
      this.font.drawShadow(pPoseStack, s, (float)(p_238509_3_ + 2), (float)(k4 - 60 - 9), 14737632);
      this.font.drawShadow(pPoseStack, s1, (float)(p_238509_3_ + j1 / 2 - this.font.width(s1) / 2), (float)(k4 - 60 - 9), 14737632);
      this.font.drawShadow(pPoseStack, s2, (float)(p_238509_3_ + j1 - this.font.width(s2)), (float)(k4 - 60 - 9), 14737632);
      RenderSystem.enableDepthTest();
   }

   private int getSampleColor(int pHeight, int pHeightMin, int pHeightMid, int pHeightMax) {
      return pHeight < pHeightMid ? this.colorLerp(-16711936, -256, (float)pHeight / (float)pHeightMid) : this.colorLerp(-256, -65536, (float)(pHeight - pHeightMid) / (float)(pHeightMax - pHeightMid));
   }

   private int colorLerp(int pCol1, int pCol2, float pFactor) {
      int i = pCol1 >> 24 & 255;
      int j = pCol1 >> 16 & 255;
      int k = pCol1 >> 8 & 255;
      int l = pCol1 & 255;
      int i1 = pCol2 >> 24 & 255;
      int j1 = pCol2 >> 16 & 255;
      int k1 = pCol2 >> 8 & 255;
      int l1 = pCol2 & 255;
      int i2 = MathHelper.clamp((int)MathHelper.lerp(pFactor, (float)i, (float)i1), 0, 255);
      int j2 = MathHelper.clamp((int)MathHelper.lerp(pFactor, (float)j, (float)j1), 0, 255);
      int k2 = MathHelper.clamp((int)MathHelper.lerp(pFactor, (float)k, (float)k1), 0, 255);
      int l2 = MathHelper.clamp((int)MathHelper.lerp(pFactor, (float)l, (float)l1), 0, 255);
      return i2 << 24 | j2 << 16 | k2 << 8 | l2;
   }

   private static long bytesToMegabytes(long pBytes) {
      return pBytes / 1024L / 1024L;
   }
}
