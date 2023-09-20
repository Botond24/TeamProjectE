package net.minecraft.util.text;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;

public interface ITargetedTextComponent {
   IFormattableTextComponent resolve(@Nullable CommandSource pCommandSourceStack, @Nullable Entity pEntity, int pRecursionDepth) throws CommandSyntaxException;
}