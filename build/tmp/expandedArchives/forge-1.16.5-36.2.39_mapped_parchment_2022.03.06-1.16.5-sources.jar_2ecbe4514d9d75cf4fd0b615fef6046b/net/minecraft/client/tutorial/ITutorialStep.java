package net.minecraft.client.tutorial;

import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ITutorialStep {
   default void clear() {
   }

   default void tick() {
   }

   /**
    * Handles the player movement
    */
   default void onInput(MovementInput pInput) {
   }

   default void onMouse(double pVelocityX, double pVelocityY) {
   }

   /**
    * Handles blocks and entities hovering
    */
   default void onLookAt(ClientWorld pLevel, RayTraceResult pResult) {
   }

   /**
    * Called when a player hits block to destroy it.
    */
   default void onDestroyBlock(ClientWorld pLevel, BlockPos pPos, BlockState pState, float pDiggingStage) {
   }

   /**
    * Called when the player opens his inventory
    */
   default void onOpenInventory() {
   }

   /**
    * Called when the player pick up an ItemStack
    */
   default void onGetItem(ItemStack pStack) {
   }
}