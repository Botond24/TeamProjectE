package net.minecraft.command.impl.data;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.arguments.NBTPathArgument;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.text.ITextComponent;

public interface IDataAccessor {
   void setData(CompoundNBT pOther) throws CommandSyntaxException;

   CompoundNBT getData() throws CommandSyntaxException;

   ITextComponent getModifiedSuccess();

   /**
    * Gets the message used as a result of querying the given NBT (both for /data get and /data get path)
    */
   ITextComponent getPrintSuccess(INBT pNbt);

   /**
    * Gets the message used as a result of querying the given path with a scale.
    */
   ITextComponent getPrintSuccess(NBTPathArgument.NBTPath pPath, double pScale, int pValue);
}