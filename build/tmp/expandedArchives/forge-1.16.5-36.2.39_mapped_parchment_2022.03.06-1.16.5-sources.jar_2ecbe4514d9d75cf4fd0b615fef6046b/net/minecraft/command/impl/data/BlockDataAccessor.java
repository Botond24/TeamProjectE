package net.minecraft.command.impl.data;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.NBTPathArgument;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class BlockDataAccessor implements IDataAccessor {
   private static final SimpleCommandExceptionType ERROR_NOT_A_BLOCK_ENTITY = new SimpleCommandExceptionType(new TranslationTextComponent("commands.data.block.invalid"));
   public static final Function<String, DataCommand.IDataProvider> PROVIDER = (p_218923_0_) -> {
      return new DataCommand.IDataProvider() {
         /**
          * Creates an accessor based on the command context. This should only refer to arguments registered in {@link
          * createArgument}.
          */
         public IDataAccessor access(CommandContext<CommandSource> pContext) throws CommandSyntaxException {
            BlockPos blockpos = BlockPosArgument.getLoadedBlockPos(pContext, p_218923_0_ + "Pos");
            TileEntity tileentity = ((CommandSource)pContext.getSource()).getLevel().getBlockEntity(blockpos);
            if (tileentity == null) {
               throw BlockDataAccessor.ERROR_NOT_A_BLOCK_ENTITY.create();
            } else {
               return new BlockDataAccessor(tileentity, blockpos);
            }
         }

         /**
          * Creates an argument used for accessing data related to this type of thing, including a literal to
          * distinguish from other types.
          */
         public ArgumentBuilder<CommandSource, ?> wrap(ArgumentBuilder<CommandSource, ?> pBuilder, Function<ArgumentBuilder<CommandSource, ?>, ArgumentBuilder<CommandSource, ?>> pAction) {
            return pBuilder.then(Commands.literal("block").then((ArgumentBuilder)pAction.apply(Commands.argument(p_218923_0_ + "Pos", BlockPosArgument.blockPos()))));
         }
      };
   };
   private final TileEntity entity;
   private final BlockPos pos;

   public BlockDataAccessor(TileEntity p_i47918_1_, BlockPos p_i47918_2_) {
      this.entity = p_i47918_1_;
      this.pos = p_i47918_2_;
   }

   public void setData(CompoundNBT pOther) {
      pOther.putInt("x", this.pos.getX());
      pOther.putInt("y", this.pos.getY());
      pOther.putInt("z", this.pos.getZ());
      BlockState blockstate = this.entity.getLevel().getBlockState(this.pos);
      this.entity.load(blockstate, pOther);
      this.entity.setChanged();
      this.entity.getLevel().sendBlockUpdated(this.pos, blockstate, blockstate, 3);
   }

   public CompoundNBT getData() {
      return this.entity.save(new CompoundNBT());
   }

   public ITextComponent getModifiedSuccess() {
      return new TranslationTextComponent("commands.data.block.modified", this.pos.getX(), this.pos.getY(), this.pos.getZ());
   }

   /**
    * Gets the message used as a result of querying the given NBT (both for /data get and /data get path)
    */
   public ITextComponent getPrintSuccess(INBT pNbt) {
      return new TranslationTextComponent("commands.data.block.query", this.pos.getX(), this.pos.getY(), this.pos.getZ(), pNbt.getPrettyDisplay());
   }

   /**
    * Gets the message used as a result of querying the given path with a scale.
    */
   public ITextComponent getPrintSuccess(NBTPathArgument.NBTPath pPath, double pScale, int pValue) {
      return new TranslationTextComponent("commands.data.block.get", pPath, this.pos.getX(), this.pos.getY(), this.pos.getZ(), String.format(Locale.ROOT, "%.2f", pScale), pValue);
   }
}