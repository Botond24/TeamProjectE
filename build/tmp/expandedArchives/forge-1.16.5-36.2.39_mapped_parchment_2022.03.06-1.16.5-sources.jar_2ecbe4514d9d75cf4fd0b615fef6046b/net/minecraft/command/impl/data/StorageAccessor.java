package net.minecraft.command.impl.data;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.NBTPathArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.storage.CommandStorage;

public class StorageAccessor implements IDataAccessor {
   private static final SuggestionProvider<CommandSource> SUGGEST_STORAGE = (p_229838_0_, p_229838_1_) -> {
      return ISuggestionProvider.suggestResource(getGlobalTags(p_229838_0_).keys(), p_229838_1_);
   };
   public static final Function<String, DataCommand.IDataProvider> PROVIDER = (p_229839_0_) -> {
      return new DataCommand.IDataProvider() {
         /**
          * Creates an accessor based on the command context. This should only refer to arguments registered in {@link
          * createArgument}.
          */
         public IDataAccessor access(CommandContext<CommandSource> pContext) {
            return new StorageAccessor(StorageAccessor.getGlobalTags(pContext), ResourceLocationArgument.getId(pContext, p_229839_0_));
         }

         /**
          * Creates an argument used for accessing data related to this type of thing, including a literal to
          * distinguish from other types.
          */
         public ArgumentBuilder<CommandSource, ?> wrap(ArgumentBuilder<CommandSource, ?> pBuilder, Function<ArgumentBuilder<CommandSource, ?>, ArgumentBuilder<CommandSource, ?>> pAction) {
            return pBuilder.then(Commands.literal("storage").then((ArgumentBuilder)pAction.apply(Commands.argument(p_229839_0_, ResourceLocationArgument.id()).suggests(StorageAccessor.SUGGEST_STORAGE))));
         }
      };
   };
   private final CommandStorage storage;
   private final ResourceLocation id;

   private static CommandStorage getGlobalTags(CommandContext<CommandSource> p_229840_0_) {
      return p_229840_0_.getSource().getServer().getCommandStorage();
   }

   private StorageAccessor(CommandStorage p_i226092_1_, ResourceLocation p_i226092_2_) {
      this.storage = p_i226092_1_;
      this.id = p_i226092_2_;
   }

   public void setData(CompoundNBT pOther) {
      this.storage.set(this.id, pOther);
   }

   public CompoundNBT getData() {
      return this.storage.get(this.id);
   }

   public ITextComponent getModifiedSuccess() {
      return new TranslationTextComponent("commands.data.storage.modified", this.id);
   }

   /**
    * Gets the message used as a result of querying the given NBT (both for /data get and /data get path)
    */
   public ITextComponent getPrintSuccess(INBT pNbt) {
      return new TranslationTextComponent("commands.data.storage.query", this.id, pNbt.getPrettyDisplay());
   }

   /**
    * Gets the message used as a result of querying the given path with a scale.
    */
   public ITextComponent getPrintSuccess(NBTPathArgument.NBTPath pPath, double pScale, int pValue) {
      return new TranslationTextComponent("commands.data.storage.get", pPath, this.id, String.format(Locale.ROOT, "%.2f", pScale), pValue);
   }
}