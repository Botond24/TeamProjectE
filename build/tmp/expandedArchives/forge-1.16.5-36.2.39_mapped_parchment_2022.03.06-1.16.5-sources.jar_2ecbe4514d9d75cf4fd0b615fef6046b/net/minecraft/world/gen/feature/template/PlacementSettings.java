package net.minecraft.world.gen.feature.template;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.MutableBoundingBox;

public class PlacementSettings {
   private Mirror mirror = Mirror.NONE;
   private Rotation rotation = Rotation.NONE;
   private BlockPos rotationPivot = BlockPos.ZERO;
   private boolean ignoreEntities;
   @Nullable
   private ChunkPos chunkPos;
   @Nullable
   private MutableBoundingBox boundingBox;
   private boolean keepLiquids = true;
   @Nullable
   private Random random;
   @Nullable
   private int palette;
   private final List<StructureProcessor> processors = Lists.newArrayList();
   private boolean knownShape;
   private boolean finalizeEntities;

   public PlacementSettings copy() {
      PlacementSettings placementsettings = new PlacementSettings();
      placementsettings.mirror = this.mirror;
      placementsettings.rotation = this.rotation;
      placementsettings.rotationPivot = this.rotationPivot;
      placementsettings.ignoreEntities = this.ignoreEntities;
      placementsettings.chunkPos = this.chunkPos;
      placementsettings.boundingBox = this.boundingBox;
      placementsettings.keepLiquids = this.keepLiquids;
      placementsettings.random = this.random;
      placementsettings.palette = this.palette;
      placementsettings.processors.addAll(this.processors);
      placementsettings.knownShape = this.knownShape;
      placementsettings.finalizeEntities = this.finalizeEntities;
      return placementsettings;
   }

   public PlacementSettings setMirror(Mirror pMirror) {
      this.mirror = pMirror;
      return this;
   }

   public PlacementSettings setRotation(Rotation pRotation) {
      this.rotation = pRotation;
      return this;
   }

   public PlacementSettings setRotationPivot(BlockPos pCenter) {
      this.rotationPivot = pCenter;
      return this;
   }

   public PlacementSettings setIgnoreEntities(boolean pIgnoreEntities) {
      this.ignoreEntities = pIgnoreEntities;
      return this;
   }

   public PlacementSettings setChunkPos(ChunkPos p_186218_1_) {
      this.chunkPos = p_186218_1_;
      return this;
   }

   public PlacementSettings setBoundingBox(MutableBoundingBox pBoundingBox) {
      this.boundingBox = pBoundingBox;
      return this;
   }

   public PlacementSettings setRandom(@Nullable Random pRandom) {
      this.random = pRandom;
      return this;
   }

   public PlacementSettings setKnownShape(boolean pKnownShape) {
      this.knownShape = pKnownShape;
      return this;
   }

   public PlacementSettings clearProcessors() {
      this.processors.clear();
      return this;
   }

   public PlacementSettings addProcessor(StructureProcessor pStructureProcessor) {
      this.processors.add(pStructureProcessor);
      return this;
   }

   public PlacementSettings popProcessor(StructureProcessor pStructureProcessor) {
      this.processors.remove(pStructureProcessor);
      return this;
   }

   public Mirror getMirror() {
      return this.mirror;
   }

   public Rotation getRotation() {
      return this.rotation;
   }

   public BlockPos getRotationPivot() {
      return this.rotationPivot;
   }

   public Random getRandom(@Nullable BlockPos pSeed) {
      if (this.random != null) {
         return this.random;
      } else {
         return pSeed == null ? new Random(Util.getMillis()) : new Random(MathHelper.getSeed(pSeed));
      }
   }

   public boolean isIgnoreEntities() {
      return this.ignoreEntities;
   }

   @Nullable
   public MutableBoundingBox getBoundingBox() {
      if (this.boundingBox == null && this.chunkPos != null) {
         this.updateBoundingBoxFromChunkPos();
      }

      return this.boundingBox;
   }

   public boolean getKnownShape() {
      return this.knownShape;
   }

   public List<StructureProcessor> getProcessors() {
      return this.processors;
   }

   void updateBoundingBoxFromChunkPos() {
      if (this.chunkPos != null) {
         this.boundingBox = this.calculateBoundingBox(this.chunkPos);
      }

   }

   public boolean shouldKeepLiquids() {
      return this.keepLiquids;
   }

   public Template.Palette getRandomPalette(List<Template.Palette> pPalettes, @Nullable BlockPos pPos) {
      int i = pPalettes.size();
      if (i == 0) {
         throw new IllegalStateException("No palettes");
      } else {
         return pPalettes.get(this.getRandom(pPos).nextInt(i));
      }
   }

   @Nullable
   private MutableBoundingBox calculateBoundingBox(@Nullable ChunkPos p_186216_1_) {
      if (p_186216_1_ == null) {
         return this.boundingBox;
      } else {
         int i = p_186216_1_.x * 16;
         int j = p_186216_1_.z * 16;
         return new MutableBoundingBox(i, 0, j, i + 16 - 1, 255, j + 16 - 1);
      }
   }

   public PlacementSettings setFinalizeEntities(boolean pFinalizeEntities) {
      this.finalizeEntities = pFinalizeEntities;
      return this;
   }

   public boolean shouldFinalizeEntities() {
      return this.finalizeEntities;
   }
}