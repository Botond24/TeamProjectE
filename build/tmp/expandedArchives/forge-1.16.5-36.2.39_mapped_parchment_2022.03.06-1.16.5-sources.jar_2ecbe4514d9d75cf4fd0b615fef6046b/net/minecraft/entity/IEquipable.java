package net.minecraft.entity;

import javax.annotation.Nullable;
import net.minecraft.util.SoundCategory;

public interface IEquipable {
   boolean isSaddleable();

   void equipSaddle(@Nullable SoundCategory pSource);

   boolean isSaddled();
}