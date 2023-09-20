package net.minecraft.world.storage;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

public class MapFrame {
   private final BlockPos pos;
   private final int rotation;
   private final int entityId;

   public MapFrame(BlockPos pPos, int pRotation, int pEntityId) {
      this.pos = pPos;
      this.rotation = pRotation;
      this.entityId = pEntityId;
   }

   public static MapFrame load(CompoundNBT pCompoundTag) {
      BlockPos blockpos = NBTUtil.readBlockPos(pCompoundTag.getCompound("Pos"));
      int i = pCompoundTag.getInt("Rotation");
      int j = pCompoundTag.getInt("EntityId");
      return new MapFrame(blockpos, i, j);
   }

   public CompoundNBT save() {
      CompoundNBT compoundnbt = new CompoundNBT();
      compoundnbt.put("Pos", NBTUtil.writeBlockPos(this.pos));
      compoundnbt.putInt("Rotation", this.rotation);
      compoundnbt.putInt("EntityId", this.entityId);
      return compoundnbt;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public int getRotation() {
      return this.rotation;
   }

   public int getEntityId() {
      return this.entityId;
   }

   public String getId() {
      return frameId(this.pos);
   }

   public static String frameId(BlockPos pPos) {
      return "frame-" + pPos.getX() + "," + pPos.getY() + "," + pPos.getZ();
   }
}