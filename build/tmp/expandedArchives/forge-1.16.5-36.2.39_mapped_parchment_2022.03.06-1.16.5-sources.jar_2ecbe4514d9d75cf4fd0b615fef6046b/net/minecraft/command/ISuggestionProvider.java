package net.minecraft.command;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.World;

public interface ISuggestionProvider {
   Collection<String> getOnlinePlayerNames();

   default Collection<String> getSelectedEntities() {
      return Collections.emptyList();
   }

   Collection<String> getAllTeams();

   Collection<ResourceLocation> getAvailableSoundEvents();

   Stream<ResourceLocation> getRecipeNames();

   CompletableFuture<Suggestions> customSuggestion(CommandContext<ISuggestionProvider> pContext, SuggestionsBuilder pSuggestionsBuilder);

   default Collection<ISuggestionProvider.Coordinates> getRelevantCoordinates() {
      return Collections.singleton(ISuggestionProvider.Coordinates.DEFAULT_GLOBAL);
   }

   default Collection<ISuggestionProvider.Coordinates> getAbsoluteCoordinates() {
      return Collections.singleton(ISuggestionProvider.Coordinates.DEFAULT_GLOBAL);
   }

   Set<RegistryKey<World>> levels();

   DynamicRegistries registryAccess();

   boolean hasPermission(int pLevel);

   static <T> void filterResources(Iterable<T> pResources, String pInput, Function<T, ResourceLocation> pLocationFunction, Consumer<T> pResourceConsumer) {
      boolean flag = pInput.indexOf(58) > -1;

      for(T t : pResources) {
         ResourceLocation resourcelocation = pLocationFunction.apply(t);
         if (flag) {
            String s = resourcelocation.toString();
            if (matchesSubStr(pInput, s)) {
               pResourceConsumer.accept(t);
            }
         } else if (matchesSubStr(pInput, resourcelocation.getNamespace()) || resourcelocation.getNamespace().equals("minecraft") && matchesSubStr(pInput, resourcelocation.getPath())) {
            pResourceConsumer.accept(t);
         }
      }

   }

   static <T> void filterResources(Iterable<T> pResources, String pRemaining, String pPrefix, Function<T, ResourceLocation> pLocationFunction, Consumer<T> pResourceConsumer) {
      if (pRemaining.isEmpty()) {
         pResources.forEach(pResourceConsumer);
      } else {
         String s = Strings.commonPrefix(pRemaining, pPrefix);
         if (!s.isEmpty()) {
            String s1 = pRemaining.substring(s.length());
            filterResources(pResources, s1, pLocationFunction, pResourceConsumer);
         }
      }

   }

   static CompletableFuture<Suggestions> suggestResource(Iterable<ResourceLocation> pResources, SuggestionsBuilder pBuilder, String pPrefix) {
      String s = pBuilder.getRemaining().toLowerCase(Locale.ROOT);
      filterResources(pResources, s, pPrefix, (p_210519_0_) -> {
         return p_210519_0_;
      }, (p_210518_2_) -> {
         pBuilder.suggest(pPrefix + p_210518_2_);
      });
      return pBuilder.buildFuture();
   }

   static CompletableFuture<Suggestions> suggestResource(Iterable<ResourceLocation> pResources, SuggestionsBuilder pBuilder) {
      String s = pBuilder.getRemaining().toLowerCase(Locale.ROOT);
      filterResources(pResources, s, (p_210517_0_) -> {
         return p_210517_0_;
      }, (p_210513_1_) -> {
         pBuilder.suggest(p_210513_1_.toString());
      });
      return pBuilder.buildFuture();
   }

   static <T> CompletableFuture<Suggestions> suggestResource(Iterable<T> pResources, SuggestionsBuilder pBuilder, Function<T, ResourceLocation> pLocationFunction, Function<T, Message> pSuggestionFunction) {
      String s = pBuilder.getRemaining().toLowerCase(Locale.ROOT);
      filterResources(pResources, s, pLocationFunction, (p_210515_3_) -> {
         pBuilder.suggest(pLocationFunction.apply(p_210515_3_).toString(), pSuggestionFunction.apply(p_210515_3_));
      });
      return pBuilder.buildFuture();
   }

   static CompletableFuture<Suggestions> suggestResource(Stream<ResourceLocation> pResourceLocations, SuggestionsBuilder pBuilder) {
      return suggestResource(pResourceLocations::iterator, pBuilder);
   }

   static <T> CompletableFuture<Suggestions> suggestResource(Stream<T> pResources, SuggestionsBuilder pBuilder, Function<T, ResourceLocation> pLocationFunction, Function<T, Message> pSuggestionFunction) {
      return suggestResource(pResources::iterator, pBuilder, pLocationFunction, pSuggestionFunction);
   }

   static CompletableFuture<Suggestions> suggestCoordinates(String pRemaining, Collection<ISuggestionProvider.Coordinates> pCoordinates, SuggestionsBuilder pBuilder, Predicate<String> pVaidator) {
      List<String> list = Lists.newArrayList();
      if (Strings.isNullOrEmpty(pRemaining)) {
         for(ISuggestionProvider.Coordinates isuggestionprovider$coordinates : pCoordinates) {
            String s = isuggestionprovider$coordinates.x + " " + isuggestionprovider$coordinates.y + " " + isuggestionprovider$coordinates.z;
            if (pVaidator.test(s)) {
               list.add(isuggestionprovider$coordinates.x);
               list.add(isuggestionprovider$coordinates.x + " " + isuggestionprovider$coordinates.y);
               list.add(s);
            }
         }
      } else {
         String[] astring = pRemaining.split(" ");
         if (astring.length == 1) {
            for(ISuggestionProvider.Coordinates isuggestionprovider$coordinates1 : pCoordinates) {
               String s1 = astring[0] + " " + isuggestionprovider$coordinates1.y + " " + isuggestionprovider$coordinates1.z;
               if (pVaidator.test(s1)) {
                  list.add(astring[0] + " " + isuggestionprovider$coordinates1.y);
                  list.add(s1);
               }
            }
         } else if (astring.length == 2) {
            for(ISuggestionProvider.Coordinates isuggestionprovider$coordinates2 : pCoordinates) {
               String s2 = astring[0] + " " + astring[1] + " " + isuggestionprovider$coordinates2.z;
               if (pVaidator.test(s2)) {
                  list.add(s2);
               }
            }
         }
      }

      return suggest(list, pBuilder);
   }

   static CompletableFuture<Suggestions> suggest2DCoordinates(String pRemaining, Collection<ISuggestionProvider.Coordinates> pCoordinates, SuggestionsBuilder pBuilder, Predicate<String> pValidator) {
      List<String> list = Lists.newArrayList();
      if (Strings.isNullOrEmpty(pRemaining)) {
         for(ISuggestionProvider.Coordinates isuggestionprovider$coordinates : pCoordinates) {
            String s = isuggestionprovider$coordinates.x + " " + isuggestionprovider$coordinates.z;
            if (pValidator.test(s)) {
               list.add(isuggestionprovider$coordinates.x);
               list.add(s);
            }
         }
      } else {
         String[] astring = pRemaining.split(" ");
         if (astring.length == 1) {
            for(ISuggestionProvider.Coordinates isuggestionprovider$coordinates1 : pCoordinates) {
               String s1 = astring[0] + " " + isuggestionprovider$coordinates1.z;
               if (pValidator.test(s1)) {
                  list.add(s1);
               }
            }
         }
      }

      return suggest(list, pBuilder);
   }

   static CompletableFuture<Suggestions> suggest(Iterable<String> pStrings, SuggestionsBuilder pBuilder) {
      String s = pBuilder.getRemaining().toLowerCase(Locale.ROOT);

      for(String s1 : pStrings) {
         if (matchesSubStr(s, s1.toLowerCase(Locale.ROOT))) {
            pBuilder.suggest(s1);
         }
      }

      return pBuilder.buildFuture();
   }

   static CompletableFuture<Suggestions> suggest(Stream<String> pStrings, SuggestionsBuilder pBuilder) {
      String s = pBuilder.getRemaining().toLowerCase(Locale.ROOT);
      pStrings.filter((p_197007_1_) -> {
         return matchesSubStr(s, p_197007_1_.toLowerCase(Locale.ROOT));
      }).forEach(pBuilder::suggest);
      return pBuilder.buildFuture();
   }

   static CompletableFuture<Suggestions> suggest(String[] pStrings, SuggestionsBuilder pBuilder) {
      String s = pBuilder.getRemaining().toLowerCase(Locale.ROOT);

      for(String s1 : pStrings) {
         if (matchesSubStr(s, s1.toLowerCase(Locale.ROOT))) {
            pBuilder.suggest(s1);
         }
      }

      return pBuilder.buildFuture();
   }

   static boolean matchesSubStr(String pInput, String pSubstring) {
      for(int i = 0; !pSubstring.startsWith(pInput, i); ++i) {
         i = pSubstring.indexOf(95, i);
         if (i < 0) {
            return false;
         }
      }

      return true;
   }

   public static class Coordinates {
      public static final ISuggestionProvider.Coordinates DEFAULT_LOCAL = new ISuggestionProvider.Coordinates("^", "^", "^");
      public static final ISuggestionProvider.Coordinates DEFAULT_GLOBAL = new ISuggestionProvider.Coordinates("~", "~", "~");
      public final String x;
      public final String y;
      public final String z;

      public Coordinates(String pX, String pY, String pZ) {
         this.x = pX;
         this.y = pY;
         this.z = pZ;
      }
   }
}