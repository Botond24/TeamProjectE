package net.minecraft.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.command.arguments.ItemInput;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;

public class GiveCommand {
   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      pDispatcher.register(Commands.literal("give").requires((p_198496_0_) -> {
         return p_198496_0_.hasPermission(2);
      }).then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("item", ItemArgument.item()).executes((p_198493_0_) -> {
         return giveItem(p_198493_0_.getSource(), ItemArgument.getItem(p_198493_0_, "item"), EntityArgument.getPlayers(p_198493_0_, "targets"), 1);
      }).then(Commands.argument("count", IntegerArgumentType.integer(1)).executes((p_198495_0_) -> {
         return giveItem(p_198495_0_.getSource(), ItemArgument.getItem(p_198495_0_, "item"), EntityArgument.getPlayers(p_198495_0_, "targets"), IntegerArgumentType.getInteger(p_198495_0_, "count"));
      })))));
   }

   private static int giveItem(CommandSource pSource, ItemInput pItem, Collection<ServerPlayerEntity> pTargets, int pCount) throws CommandSyntaxException {
      for(ServerPlayerEntity serverplayerentity : pTargets) {
         int i = pCount;

         while(i > 0) {
            int j = Math.min(pItem.getItem().getMaxStackSize(), i);
            i -= j;
            ItemStack itemstack = pItem.createItemStack(j, false);
            boolean flag = serverplayerentity.inventory.add(itemstack);
            if (flag && itemstack.isEmpty()) {
               itemstack.setCount(1);
               ItemEntity itementity1 = serverplayerentity.drop(itemstack, false);
               if (itementity1 != null) {
                  itementity1.makeFakeItem();
               }

               serverplayerentity.level.playSound((PlayerEntity)null, serverplayerentity.getX(), serverplayerentity.getY(), serverplayerentity.getZ(), SoundEvents.ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((serverplayerentity.getRandom().nextFloat() - serverplayerentity.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
               serverplayerentity.inventoryMenu.broadcastChanges();
            } else {
               ItemEntity itementity = serverplayerentity.drop(itemstack, false);
               if (itementity != null) {
                  itementity.setNoPickUpDelay();
                  itementity.setOwner(serverplayerentity.getUUID());
               }
            }
         }
      }

      if (pTargets.size() == 1) {
         pSource.sendSuccess(new TranslationTextComponent("commands.give.success.single", pCount, pItem.createItemStack(pCount, false).getDisplayName(), pTargets.iterator().next().getDisplayName()), true);
      } else {
         pSource.sendSuccess(new TranslationTextComponent("commands.give.success.single", pCount, pItem.createItemStack(pCount, false).getDisplayName(), pTargets.size()), true);
      }

      return pTargets.size();
   }
}