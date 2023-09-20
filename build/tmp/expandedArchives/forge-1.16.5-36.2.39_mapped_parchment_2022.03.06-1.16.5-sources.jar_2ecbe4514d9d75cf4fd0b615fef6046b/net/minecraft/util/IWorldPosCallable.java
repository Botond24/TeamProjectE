package net.minecraft.util;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IWorldPosCallable {
   IWorldPosCallable NULL = new IWorldPosCallable() {
      public <T> Optional<T> evaluate(BiFunction<World, BlockPos, T> pLevelPosConsumer) {
         return Optional.empty();
      }
   };

   static IWorldPosCallable create(final World pLevel, final BlockPos pPos) {
      return new IWorldPosCallable() {
         public <T> Optional<T> evaluate(BiFunction<World, BlockPos, T> pLevelPosConsumer) {
            return Optional.of(pLevelPosConsumer.apply(pLevel, pPos));
         }
      };
   }

   <T> Optional<T> evaluate(BiFunction<World, BlockPos, T> pLevelPosConsumer);

   default <T> T evaluate(BiFunction<World, BlockPos, T> pLevelPosConsumer, T pDefaultValue) {
      return this.evaluate(pLevelPosConsumer).orElse(pDefaultValue);
   }

   default void execute(BiConsumer<World, BlockPos> pLevelPosConsumer) {
      this.evaluate((p_221487_1_, p_221487_2_) -> {
         pLevelPosConsumer.accept(p_221487_1_, p_221487_2_);
         return Optional.empty();
      });
   }
}