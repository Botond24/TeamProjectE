package net.minecraft.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WrittenBookItem extends Item {
   public WrittenBookItem(Item.Properties p_i48454_1_) {
      super(p_i48454_1_);
   }

   public static boolean makeSureTagIsValid(@Nullable CompoundNBT pCompoundTag) {
      if (!WritableBookItem.makeSureTagIsValid(pCompoundTag)) {
         return false;
      } else if (!pCompoundTag.contains("title", 8)) {
         return false;
      } else {
         String s = pCompoundTag.getString("title");
         return s.length() > 32 ? false : pCompoundTag.contains("author", 8);
      }
   }

   /**
    * Gets the generation of the book (how many times it has been cloned)
    */
   public static int getGeneration(ItemStack pBookStack) {
      return pBookStack.getTag().getInt("generation");
   }

   /**
    * Gets the page count of the book
    */
   public static int getPageCount(ItemStack pBookSTack) {
      CompoundNBT compoundnbt = pBookSTack.getTag();
      return compoundnbt != null ? compoundnbt.getList("pages", 8).size() : 0;
   }

   /**
    * Gets the title name of the book
    */
   public ITextComponent getName(ItemStack pStack) {
      if (pStack.hasTag()) {
         CompoundNBT compoundnbt = pStack.getTag();
         String s = compoundnbt.getString("title");
         if (!StringUtils.isNullOrEmpty(s)) {
            return new StringTextComponent(s);
         }
      }

      return super.getName(pStack);
   }

   /**
    * allows items to add custom lines of information to the mouseover description
    */
   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(ItemStack pStack, @Nullable World pLevel, List<ITextComponent> pTooltip, ITooltipFlag pFlag) {
      if (pStack.hasTag()) {
         CompoundNBT compoundnbt = pStack.getTag();
         String s = compoundnbt.getString("author");
         if (!StringUtils.isNullOrEmpty(s)) {
            pTooltip.add((new TranslationTextComponent("book.byAuthor", s)).withStyle(TextFormatting.GRAY));
         }

         pTooltip.add((new TranslationTextComponent("book.generation." + compoundnbt.getInt("generation"))).withStyle(TextFormatting.GRAY));
      }

   }

   /**
    * Called when this item is used when targetting a Block
    */
   public ActionResultType useOn(ItemUseContext pContext) {
      World world = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      BlockState blockstate = world.getBlockState(blockpos);
      if (blockstate.is(Blocks.LECTERN)) {
         return LecternBlock.tryPlaceBook(world, blockpos, blockstate, pContext.getItemInHand()) ? ActionResultType.sidedSuccess(world.isClientSide) : ActionResultType.PASS;
      } else {
         return ActionResultType.PASS;
      }
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      pPlayer.openItemGui(itemstack, pHand);
      pPlayer.awardStat(Stats.ITEM_USED.get(this));
      return ActionResult.sidedSuccess(itemstack, pLevel.isClientSide());
   }

   public static boolean resolveBookComponents(ItemStack pBookStack, @Nullable CommandSource pResolvingSource, @Nullable PlayerEntity pResolvingPlayer) {
      CompoundNBT compoundnbt = pBookStack.getTag();
      if (compoundnbt != null && !compoundnbt.getBoolean("resolved")) {
         compoundnbt.putBoolean("resolved", true);
         if (!makeSureTagIsValid(compoundnbt)) {
            return false;
         } else {
            ListNBT listnbt = compoundnbt.getList("pages", 8);

            for(int i = 0; i < listnbt.size(); ++i) {
               String s = listnbt.getString(i);

               ITextComponent itextcomponent;
               try {
                  itextcomponent = ITextComponent.Serializer.fromJsonLenient(s);
                  itextcomponent = TextComponentUtils.updateForEntity(pResolvingSource, itextcomponent, pResolvingPlayer, 0);
               } catch (Exception exception) {
                  itextcomponent = new StringTextComponent(s);
               }

               listnbt.set(i, (INBT)StringNBT.valueOf(ITextComponent.Serializer.toJson(itextcomponent)));
            }

            compoundnbt.put("pages", listnbt);
            return true;
         }
      } else {
         return false;
      }
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
}