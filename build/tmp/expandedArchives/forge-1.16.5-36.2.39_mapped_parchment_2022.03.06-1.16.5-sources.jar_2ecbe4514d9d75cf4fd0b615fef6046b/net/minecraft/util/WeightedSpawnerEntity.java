package net.minecraft.util;

import net.minecraft.nbt.CompoundNBT;

public class WeightedSpawnerEntity extends WeightedRandom.Item {
   private final CompoundNBT tag;

   public WeightedSpawnerEntity() {
      super(1);
      this.tag = new CompoundNBT();
      this.tag.putString("id", "minecraft:pig");
   }

   public WeightedSpawnerEntity(CompoundNBT pTag) {
      this(pTag.contains("Weight", 99) ? pTag.getInt("Weight") : 1, pTag.getCompound("Entity"));
   }

   public WeightedSpawnerEntity(int pWeight, CompoundNBT pTag) {
      super(pWeight);
      this.tag = pTag;
      ResourceLocation resourcelocation = ResourceLocation.tryParse(pTag.getString("id"));
      if (resourcelocation != null) {
         pTag.putString("id", resourcelocation.toString());
      } else {
         pTag.putString("id", "minecraft:pig");
      }

   }

   public CompoundNBT save() {
      CompoundNBT compoundnbt = new CompoundNBT();
      compoundnbt.put("Entity", this.tag);
      compoundnbt.putInt("Weight", this.weight);
      return compoundnbt;
   }

   public CompoundNBT getTag() {
      return this.tag;
   }
}