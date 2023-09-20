package net.minecraft.item;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class DebugStickItem extends Item {
   public DebugStickItem(Item.Properties p_i48513_1_) {
      super(p_i48513_1_);
   }

   /**
    * Returns true if this item has an enchantment glint. By default, this returns <code>stack.isItemEnchanted()</code>,
    * but other items can override it (for instance, written books always return true).
    * 
    * Note that if you override this method, you generally want to also call the super version (on {@link Item}) to get
    * the glint for enchanted items. Of course, that is unnecessary if the overwritten version always returns true.
    */
   public boolean isFoil(ItemStack pStack) {
      return true;
   }

   public boolean canAttackBlock(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer) {
      if (!pLevel.isClientSide) {
         this.handleInteraction(pPlayer, pState, pLevel, pPos, false, pPlayer.getItemInHand(Hand.MAIN_HAND));
      }

      return false;
   }

   /**
    * Called when this item is used when targetting a Block
    */
   public ActionResultType useOn(ItemUseContext pContext) {
      PlayerEntity playerentity = pContext.getPlayer();
      World world = pContext.getLevel();
      if (!world.isClientSide && playerentity != null) {
         BlockPos blockpos = pContext.getClickedPos();
         this.handleInteraction(playerentity, world.getBlockState(blockpos), world, blockpos, true, pContext.getItemInHand());
      }

      return ActionResultType.sidedSuccess(world.isClientSide);
   }

   private void handleInteraction(PlayerEntity p_195958_1_, BlockState p_195958_2_, IWorld p_195958_3_, BlockPos p_195958_4_, boolean p_195958_5_, ItemStack p_195958_6_) {
      if (p_195958_1_.canUseGameMasterBlocks()) {
         Block block = p_195958_2_.getBlock();
         StateContainer<Block, BlockState> statecontainer = block.getStateDefinition();
         Collection<Property<?>> collection = statecontainer.getProperties();
         String s = Registry.BLOCK.getKey(block).toString();
         if (collection.isEmpty()) {
            message(p_195958_1_, new TranslationTextComponent(this.getDescriptionId() + ".empty", s));
         } else {
            CompoundNBT compoundnbt = p_195958_6_.getOrCreateTagElement("DebugProperty");
            String s1 = compoundnbt.getString(s);
            Property<?> property = statecontainer.getProperty(s1);
            if (p_195958_5_) {
               if (property == null) {
                  property = collection.iterator().next();
               }

               BlockState blockstate = cycleState(p_195958_2_, property, p_195958_1_.isSecondaryUseActive());
               p_195958_3_.setBlock(p_195958_4_, blockstate, 18);
               message(p_195958_1_, new TranslationTextComponent(this.getDescriptionId() + ".update", property.getName(), getNameHelper(blockstate, property)));
            } else {
               property = getRelative(collection, property, p_195958_1_.isSecondaryUseActive());
               String s2 = property.getName();
               compoundnbt.putString(s, s2);
               message(p_195958_1_, new TranslationTextComponent(this.getDescriptionId() + ".select", s2, getNameHelper(p_195958_2_, property)));
            }

         }
      }
   }

   private static <T extends Comparable<T>> BlockState cycleState(BlockState pState, Property<T> pProperty, boolean pBackwards) {
      return pState.setValue(pProperty, getRelative(pProperty.getPossibleValues(), pState.getValue(pProperty), pBackwards));
   }

   private static <T> T getRelative(Iterable<T> pAllowedValues, @Nullable T pCurrentValue, boolean pBackwards) {
      return (T)(pBackwards ? Util.findPreviousInIterable(pAllowedValues, pCurrentValue) : Util.findNextInIterable(pAllowedValues, pCurrentValue));
   }

   private static void message(PlayerEntity pPlayer, ITextComponent pMessageComponent) {
      ((ServerPlayerEntity)pPlayer).sendMessage(pMessageComponent, ChatType.GAME_INFO, Util.NIL_UUID);
   }

   private static <T extends Comparable<T>> String getNameHelper(BlockState pState, Property<T> pProperty) {
      return pProperty.getName(pState.getValue(pProperty));
   }
}