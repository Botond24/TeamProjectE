package net.minecraft.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FireworkStarItem extends Item {
   public FireworkStarItem(Item.Properties p_i48496_1_) {
      super(p_i48496_1_);
   }

   /**
    * allows items to add custom lines of information to the mouseover description
    */
   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(ItemStack pStack, @Nullable World pLevel, List<ITextComponent> pTooltip, ITooltipFlag pFlag) {
      CompoundNBT compoundnbt = pStack.getTagElement("Explosion");
      if (compoundnbt != null) {
         appendHoverText(compoundnbt, pTooltip);
      }

   }

   @OnlyIn(Dist.CLIENT)
   public static void appendHoverText(CompoundNBT pCompound, List<ITextComponent> pTooltipComponents) {
      FireworkRocketItem.Shape fireworkrocketitem$shape = FireworkRocketItem.Shape.byId(pCompound.getByte("Type"));
      pTooltipComponents.add((new TranslationTextComponent("item.minecraft.firework_star.shape." + fireworkrocketitem$shape.getName())).withStyle(TextFormatting.GRAY));
      int[] aint = pCompound.getIntArray("Colors");
      if (aint.length > 0) {
         pTooltipComponents.add(appendColors((new StringTextComponent("")).withStyle(TextFormatting.GRAY), aint));
      }

      int[] aint1 = pCompound.getIntArray("FadeColors");
      if (aint1.length > 0) {
         pTooltipComponents.add(appendColors((new TranslationTextComponent("item.minecraft.firework_star.fade_to")).append(" ").withStyle(TextFormatting.GRAY), aint1));
      }

      if (pCompound.getBoolean("Trail")) {
         pTooltipComponents.add((new TranslationTextComponent("item.minecraft.firework_star.trail")).withStyle(TextFormatting.GRAY));
      }

      if (pCompound.getBoolean("Flicker")) {
         pTooltipComponents.add((new TranslationTextComponent("item.minecraft.firework_star.flicker")).withStyle(TextFormatting.GRAY));
      }

   }

   @OnlyIn(Dist.CLIENT)
   private static ITextComponent appendColors(IFormattableTextComponent pTooltipComponent, int[] pColors) {
      for(int i = 0; i < pColors.length; ++i) {
         if (i > 0) {
            pTooltipComponent.append(", ");
         }

         pTooltipComponent.append(getColorName(pColors[i]));
      }

      return pTooltipComponent;
   }

   @OnlyIn(Dist.CLIENT)
   private static ITextComponent getColorName(int pColor) {
      DyeColor dyecolor = DyeColor.byFireworkColor(pColor);
      return dyecolor == null ? new TranslationTextComponent("item.minecraft.firework_star.custom_color") : new TranslationTextComponent("item.minecraft.firework_star." + dyecolor.getName());
   }
}