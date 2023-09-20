package net.minecraft.entity;

import net.minecraft.util.SoundCategory;

@Deprecated // Forge: Use IForgeShearable
public interface IShearable {
   @Deprecated // Forge: Use IForgeShearable
   void shear(SoundCategory pCategory);

   @Deprecated // Forge: Use IForgeShearable
   boolean readyForShearing();
}
