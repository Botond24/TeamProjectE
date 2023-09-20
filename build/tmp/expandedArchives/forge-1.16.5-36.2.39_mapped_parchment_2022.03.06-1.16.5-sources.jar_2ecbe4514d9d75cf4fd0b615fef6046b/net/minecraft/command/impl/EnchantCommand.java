package net.minecraft.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EnchantmentArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TranslationTextComponent;

public class EnchantCommand {
   private static final DynamicCommandExceptionType ERROR_NOT_LIVING_ENTITY = new DynamicCommandExceptionType((p_208839_0_) -> {
      return new TranslationTextComponent("commands.enchant.failed.entity", p_208839_0_);
   });
   private static final DynamicCommandExceptionType ERROR_NO_ITEM = new DynamicCommandExceptionType((p_208835_0_) -> {
      return new TranslationTextComponent("commands.enchant.failed.itemless", p_208835_0_);
   });
   private static final DynamicCommandExceptionType ERROR_INCOMPATIBLE = new DynamicCommandExceptionType((p_208837_0_) -> {
      return new TranslationTextComponent("commands.enchant.failed.incompatible", p_208837_0_);
   });
   private static final Dynamic2CommandExceptionType ERROR_LEVEL_TOO_HIGH = new Dynamic2CommandExceptionType((p_208840_0_, p_208840_1_) -> {
      return new TranslationTextComponent("commands.enchant.failed.level", p_208840_0_, p_208840_1_);
   });
   private static final SimpleCommandExceptionType ERROR_NOTHING_HAPPENED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.enchant.failed"));

   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      pDispatcher.register(Commands.literal("enchant").requires((p_203630_0_) -> {
         return p_203630_0_.hasPermission(2);
      }).then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("enchantment", EnchantmentArgument.enchantment()).executes((p_202648_0_) -> {
         return enchant(p_202648_0_.getSource(), EntityArgument.getEntities(p_202648_0_, "targets"), EnchantmentArgument.getEnchantment(p_202648_0_, "enchantment"), 1);
      }).then(Commands.argument("level", IntegerArgumentType.integer(0)).executes((p_202650_0_) -> {
         return enchant(p_202650_0_.getSource(), EntityArgument.getEntities(p_202650_0_, "targets"), EnchantmentArgument.getEnchantment(p_202650_0_, "enchantment"), IntegerArgumentType.getInteger(p_202650_0_, "level"));
      })))));
   }

   private static int enchant(CommandSource pSource, Collection<? extends Entity> pTargets, Enchantment pEnchantment, int pLevel) throws CommandSyntaxException {
      if (pLevel > pEnchantment.getMaxLevel()) {
         throw ERROR_LEVEL_TOO_HIGH.create(pLevel, pEnchantment.getMaxLevel());
      } else {
         int i = 0;

         for(Entity entity : pTargets) {
            if (entity instanceof LivingEntity) {
               LivingEntity livingentity = (LivingEntity)entity;
               ItemStack itemstack = livingentity.getMainHandItem();
               if (!itemstack.isEmpty()) {
                  if (pEnchantment.canEnchant(itemstack) && EnchantmentHelper.isEnchantmentCompatible(EnchantmentHelper.getEnchantments(itemstack).keySet(), pEnchantment)) {
                     itemstack.enchant(pEnchantment, pLevel);
                     ++i;
                  } else if (pTargets.size() == 1) {
                     throw ERROR_INCOMPATIBLE.create(itemstack.getItem().getName(itemstack).getString());
                  }
               } else if (pTargets.size() == 1) {
                  throw ERROR_NO_ITEM.create(livingentity.getName().getString());
               }
            } else if (pTargets.size() == 1) {
               throw ERROR_NOT_LIVING_ENTITY.create(entity.getName().getString());
            }
         }

         if (i == 0) {
            throw ERROR_NOTHING_HAPPENED.create();
         } else {
            if (pTargets.size() == 1) {
               pSource.sendSuccess(new TranslationTextComponent("commands.enchant.success.single", pEnchantment.getFullname(pLevel), pTargets.iterator().next().getDisplayName()), true);
            } else {
               pSource.sendSuccess(new TranslationTextComponent("commands.enchant.success.multiple", pEnchantment.getFullname(pLevel), pTargets.size()), true);
            }

            return i;
         }
      }
   }
}