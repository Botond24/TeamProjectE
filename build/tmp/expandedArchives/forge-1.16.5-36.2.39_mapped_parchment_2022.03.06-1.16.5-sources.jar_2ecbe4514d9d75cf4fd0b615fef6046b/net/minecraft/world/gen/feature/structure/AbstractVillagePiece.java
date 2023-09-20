package net.minecraft.world.gen.feature.structure;

import com.google.common.collect.Lists;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Random;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.jigsaw.EmptyJigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.JigsawJunction;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.template.TemplateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AbstractVillagePiece extends StructurePiece {
   private static final Logger LOGGER = LogManager.getLogger();
   protected final JigsawPiece element;
   protected BlockPos position;
   private final int groundLevelDelta;
   protected final Rotation rotation;
   private final List<JigsawJunction> junctions = Lists.newArrayList();
   private final TemplateManager structureManager;

   public AbstractVillagePiece(TemplateManager pStructureManager, JigsawPiece pElement, BlockPos pPosition, int pGroundLevelDelta, Rotation pRotation, MutableBoundingBox pBox) {
      super(IStructurePieceType.JIGSAW, 0);
      this.structureManager = pStructureManager;
      this.element = pElement;
      this.position = pPosition;
      this.groundLevelDelta = pGroundLevelDelta;
      this.rotation = pRotation;
      this.boundingBox = pBox;
   }

   public AbstractVillagePiece(TemplateManager p_i242037_1_, CompoundNBT p_i242037_2_) {
      super(IStructurePieceType.JIGSAW, p_i242037_2_);
      this.structureManager = p_i242037_1_;
      this.position = new BlockPos(p_i242037_2_.getInt("PosX"), p_i242037_2_.getInt("PosY"), p_i242037_2_.getInt("PosZ"));
      this.groundLevelDelta = p_i242037_2_.getInt("ground_level_delta");
      this.element = JigsawPiece.CODEC.parse(NBTDynamicOps.INSTANCE, p_i242037_2_.getCompound("pool_element")).resultOrPartial(LOGGER::error).orElse(EmptyJigsawPiece.INSTANCE);
      this.rotation = Rotation.valueOf(p_i242037_2_.getString("rotation"));
      this.boundingBox = this.element.getBoundingBox(p_i242037_1_, this.position, this.rotation);
      ListNBT listnbt = p_i242037_2_.getList("junctions", 10);
      this.junctions.clear();
      listnbt.forEach((p_214827_1_) -> {
         this.junctions.add(JigsawJunction.deserialize(new Dynamic<>(NBTDynamicOps.INSTANCE, p_214827_1_)));
      });
   }

   protected void addAdditionalSaveData(CompoundNBT p_143011_1_) {
      p_143011_1_.putInt("PosX", this.position.getX());
      p_143011_1_.putInt("PosY", this.position.getY());
      p_143011_1_.putInt("PosZ", this.position.getZ());
      p_143011_1_.putInt("ground_level_delta", this.groundLevelDelta);
      JigsawPiece.CODEC.encodeStart(NBTDynamicOps.INSTANCE, this.element).resultOrPartial(LOGGER::error).ifPresent((p_237002_1_) -> {
         p_143011_1_.put("pool_element", p_237002_1_);
      });
      p_143011_1_.putString("rotation", this.rotation.name());
      ListNBT listnbt = new ListNBT();

      for(JigsawJunction jigsawjunction : this.junctions) {
         listnbt.add(jigsawjunction.serialize(NBTDynamicOps.INSTANCE).getValue());
      }

      p_143011_1_.put("junctions", listnbt);
   }

   public boolean postProcess(ISeedReader pLevel, StructureManager pStructureManager, ChunkGenerator pChunkGenerator, Random pRandom, MutableBoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
      return this.place(pLevel, pStructureManager, pChunkGenerator, pRandom, pBox, pPos, false);
   }

   public boolean place(ISeedReader pLevel, StructureManager pStructureManager, ChunkGenerator pChunkGenerator, Random pRandom, MutableBoundingBox pBox, BlockPos pPos, boolean pKeepJigsaws) {
      return this.element.place(this.structureManager, pLevel, pStructureManager, pChunkGenerator, this.position, pPos, this.rotation, pBox, pRandom, pKeepJigsaws);
   }

   public void move(int pX, int pY, int pZ) {
      super.move(pX, pY, pZ);
      this.position = this.position.offset(pX, pY, pZ);
   }

   public Rotation getRotation() {
      return this.rotation;
   }

   public String toString() {
      return String.format("<%s | %s | %s | %s>", this.getClass().getSimpleName(), this.position, this.rotation, this.element);
   }

   public JigsawPiece getElement() {
      return this.element;
   }

   public BlockPos getPosition() {
      return this.position;
   }

   public int getGroundLevelDelta() {
      return this.groundLevelDelta;
   }

   public void addJunction(JigsawJunction pJunction) {
      this.junctions.add(pJunction);
   }

   public List<JigsawJunction> getJunctions() {
      return this.junctions;
   }
}