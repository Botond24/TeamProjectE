package net.minecraft.command.arguments;

import com.google.common.collect.Maps;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;

public class SuggestionProviders {
   private static final Map<ResourceLocation, SuggestionProvider<ISuggestionProvider>> PROVIDERS_BY_NAME = Maps.newHashMap();
   private static final ResourceLocation DEFAULT_NAME = new ResourceLocation("ask_server");
   public static final SuggestionProvider<ISuggestionProvider> ASK_SERVER = register(DEFAULT_NAME, (p_197500_0_, p_197500_1_) -> {
      return p_197500_0_.getSource().customSuggestion(p_197500_0_, p_197500_1_);
   });
   public static final SuggestionProvider<CommandSource> ALL_RECIPES = register(new ResourceLocation("all_recipes"), (p_197501_0_, p_197501_1_) -> {
      return ISuggestionProvider.suggestResource(p_197501_0_.getSource().getRecipeNames(), p_197501_1_);
   });
   public static final SuggestionProvider<CommandSource> AVAILABLE_SOUNDS = register(new ResourceLocation("available_sounds"), (p_197495_0_, p_197495_1_) -> {
      return ISuggestionProvider.suggestResource(p_197495_0_.getSource().getAvailableSoundEvents(), p_197495_1_);
   });
   public static final SuggestionProvider<CommandSource> AVAILABLE_BIOMES = register(new ResourceLocation("available_biomes"), (p_239577_0_, p_239577_1_) -> {
      return ISuggestionProvider.suggestResource(p_239577_0_.getSource().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).keySet(), p_239577_1_);
   });
   public static final SuggestionProvider<CommandSource> SUMMONABLE_ENTITIES = register(new ResourceLocation("summonable_entities"), (p_201210_0_, p_201210_1_) -> {
      return ISuggestionProvider.suggestResource(Registry.ENTITY_TYPE.stream().filter(EntityType::canSummon), p_201210_1_, EntityType::getKey, (p_201209_0_) -> {
         return new TranslationTextComponent(Util.makeDescriptionId("entity", EntityType.getKey(p_201209_0_)));
      });
   });

   public static <S extends ISuggestionProvider> SuggestionProvider<S> register(ResourceLocation pName, SuggestionProvider<ISuggestionProvider> pProvider) {
      if (PROVIDERS_BY_NAME.containsKey(pName)) {
         throw new IllegalArgumentException("A command suggestion provider is already registered with the name " + pName);
      } else {
         PROVIDERS_BY_NAME.put(pName, pProvider);
         return (SuggestionProvider<S>)new SuggestionProviders.Wrapper(pName, pProvider);
      }
   }

   public static SuggestionProvider<ISuggestionProvider> getProvider(ResourceLocation pName) {
      return PROVIDERS_BY_NAME.getOrDefault(pName, ASK_SERVER);
   }

   /**
    * Gets the ID for the given provider. If the provider is not a wrapped one created via {@link #register}, then it
    * returns {@link #ASK_SERVER_ID} instead, as there is no known ID but ASK_SERVER always works.
    */
   public static ResourceLocation getName(SuggestionProvider<ISuggestionProvider> pProvider) {
      return pProvider instanceof SuggestionProviders.Wrapper ? ((SuggestionProviders.Wrapper)pProvider).name : DEFAULT_NAME;
   }

   /**
    * Checks to make sure that the given suggestion provider is a wrapped one that was created via {@link #register}. If
    * not, returns {@link #ASK_SERVER}. Needed because custom providers don't have a known ID to send to the client, but
    * ASK_SERVER always works.
    */
   public static SuggestionProvider<ISuggestionProvider> safelySwap(SuggestionProvider<ISuggestionProvider> pProvider) {
      return pProvider instanceof SuggestionProviders.Wrapper ? pProvider : ASK_SERVER;
   }

   public static class Wrapper implements SuggestionProvider<ISuggestionProvider> {
      private final SuggestionProvider<ISuggestionProvider> delegate;
      private final ResourceLocation name;

      public Wrapper(ResourceLocation pName, SuggestionProvider<ISuggestionProvider> pDelegate) {
         this.delegate = pDelegate;
         this.name = pName;
      }

      public CompletableFuture<Suggestions> getSuggestions(CommandContext<ISuggestionProvider> p_getSuggestions_1_, SuggestionsBuilder p_getSuggestions_2_) throws CommandSyntaxException {
         return this.delegate.getSuggestions(p_getSuggestions_1_, p_getSuggestions_2_);
      }
   }
}