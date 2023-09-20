package net.minecraft.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.inventory.IClearable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public class SetBlockCommand {
   private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.setblock.failed"));

   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      pDispatcher.register(Commands.literal("setblock").requires((p_198688_0_) -> {
         return p_198688_0_.hasPermission(2);
      }).then(Commands.argument("pos", BlockPosArgument.blockPos()).then(Commands.argument("block", BlockStateArgument.block()).executes((p_198682_0_) -> {
         return setBlock(p_198682_0_.getSource(), BlockPosArgument.getLoadedBlockPos(p_198682_0_, "pos"), BlockStateArgument.getBlock(p_198682_0_, "block"), SetBlockCommand.Mode.REPLACE, (Predicate<CachedBlockInfo>)null);
      }).then(Commands.literal("destroy").executes((p_198685_0_) -> {
         return setBlock(p_198685_0_.getSource(), BlockPosArgument.getLoadedBlockPos(p_198685_0_, "pos"), BlockStateArgument.getBlock(p_198685_0_, "block"), SetBlockCommand.Mode.DESTROY, (Predicate<CachedBlockInfo>)null);
      })).then(Commands.literal("keep").executes((p_198681_0_) -> {
         return setBlock(p_198681_0_.getSource(), BlockPosArgument.getLoadedBlockPos(p_198681_0_, "pos"), BlockStateArgument.getBlock(p_198681_0_, "block"), SetBlockCommand.Mode.REPLACE, (p_198687_0_) -> {
            return p_198687_0_.getLevel().isEmptyBlock(p_198687_0_.getPos());
         });
      })).then(Commands.literal("replace").executes((p_198686_0_) -> {
         return setBlock(p_198686_0_.getSource(), BlockPosArgument.getLoadedBlockPos(p_198686_0_, "pos"), BlockStateArgument.getBlock(p_198686_0_, "block"), SetBlockCommand.Mode.REPLACE, (Predicate<CachedBlockInfo>)null);
      })))));
   }

   private static int setBlock(CommandSource pSource, BlockPos pPos, BlockStateInput pState, SetBlockCommand.Mode pMode, @Nullable Predicate<CachedBlockInfo> pPredicate) throws CommandSyntaxException {
      ServerWorld serverworld = pSource.getLevel();
      if (pPredicate != null && !pPredicate.test(new CachedBlockInfo(serverworld, pPos, true))) {
         throw ERROR_FAILED.create();
      } else {
         boolean flag;
         if (pMode == SetBlockCommand.Mode.DESTROY) {
            serverworld.destroyBlock(pPos, true);
            flag = !pState.getState().isAir() || !serverworld.getBlockState(pPos).isAir();
         } else {
            TileEntity tileentity = serverworld.getBlockEntity(pPos);
            IClearable.tryClear(tileentity);
            flag = true;
         }

         if (flag && !pState.place(serverworld, pPos, 2)) {
            throw ERROR_FAILED.create();
         } else {
            serverworld.blockUpdated(pPos, pState.getState().getBlock());
            pSource.sendSuccess(new TranslationTextComponent("commands.setblock.success", pPos.getX(), pPos.getY(), pPos.getZ()), true);
            return 1;
         }
      }
   }

   public interface IFilter {
      @Nullable
      BlockStateInput filter(MutableBoundingBox p_filter_1_, BlockPos p_filter_2_, BlockStateInput p_filter_3_, ServerWorld p_filter_4_);
   }

   public static enum Mode {
      REPLACE,
      DESTROY;
   }
}