package net.minecraft.advancements;

import com.google.common.base.Functions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AdvancementList {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Map<ResourceLocation, Advancement> advancements = Maps.newHashMap();
   private final Set<Advancement> roots = Sets.newLinkedHashSet();
   private final Set<Advancement> tasks = Sets.newLinkedHashSet();
   private AdvancementList.IListener listener;

   @OnlyIn(Dist.CLIENT)
   private void remove(Advancement pAdvancement) {
      for(Advancement advancement : pAdvancement.getChildren()) {
         this.remove(advancement);
      }

      LOGGER.info("Forgot about advancement {}", (Object)pAdvancement.getId());
      this.advancements.remove(pAdvancement.getId());
      if (pAdvancement.getParent() == null) {
         this.roots.remove(pAdvancement);
         if (this.listener != null) {
            this.listener.onRemoveAdvancementRoot(pAdvancement);
         }
      } else {
         this.tasks.remove(pAdvancement);
         if (this.listener != null) {
            this.listener.onRemoveAdvancementTask(pAdvancement);
         }
      }

   }

   @OnlyIn(Dist.CLIENT)
   public void remove(Set<ResourceLocation> pIds) {
      for(ResourceLocation resourcelocation : pIds) {
         Advancement advancement = this.advancements.get(resourcelocation);
         if (advancement == null) {
            LOGGER.warn("Told to remove advancement {} but I don't know what that is", (Object)resourcelocation);
         } else {
            this.remove(advancement);
         }
      }

   }

   public void add(Map<ResourceLocation, Advancement.Builder> pAdvancements) {
      Function<ResourceLocation, Advancement> function = Functions.forMap(this.advancements, (Advancement)null);

      while(!pAdvancements.isEmpty()) {
         boolean flag = false;
         Iterator<Entry<ResourceLocation, Advancement.Builder>> iterator = pAdvancements.entrySet().iterator();

         while(iterator.hasNext()) {
            Entry<ResourceLocation, Advancement.Builder> entry = iterator.next();
            ResourceLocation resourcelocation = entry.getKey();
            Advancement.Builder advancement$builder = entry.getValue();
            if (advancement$builder.canBuild(function)) {
               Advancement advancement = advancement$builder.build(resourcelocation);
               this.advancements.put(resourcelocation, advancement);
               flag = true;
               iterator.remove();
               if (advancement.getParent() == null) {
                  this.roots.add(advancement);
                  if (this.listener != null) {
                     this.listener.onAddAdvancementRoot(advancement);
                  }
               } else {
                  this.tasks.add(advancement);
                  if (this.listener != null) {
                     this.listener.onAddAdvancementTask(advancement);
                  }
               }
            }
         }

         if (!flag) {
            for(Entry<ResourceLocation, Advancement.Builder> entry1 : pAdvancements.entrySet()) {
               LOGGER.error("Couldn't load advancement {}: {}", entry1.getKey(), entry1.getValue());
            }
            break;
         }
      }

      net.minecraftforge.common.AdvancementLoadFix.buildSortedTrees(this.roots);
      LOGGER.info("Loaded {} advancements", (int)this.advancements.size());
   }

   @OnlyIn(Dist.CLIENT)
   public void clear() {
      this.advancements.clear();
      this.roots.clear();
      this.tasks.clear();
      if (this.listener != null) {
         this.listener.onAdvancementsCleared();
      }

   }

   public Iterable<Advancement> getRoots() {
      return this.roots;
   }

   public Collection<Advancement> getAllAdvancements() {
      return this.advancements.values();
   }

   @Nullable
   public Advancement get(ResourceLocation pId) {
      return this.advancements.get(pId);
   }

   @OnlyIn(Dist.CLIENT)
   public void setListener(@Nullable AdvancementList.IListener pListener) {
      this.listener = pListener;
      if (pListener != null) {
         for(Advancement advancement : this.roots) {
            pListener.onAddAdvancementRoot(advancement);
         }

         for(Advancement advancement1 : this.tasks) {
            pListener.onAddAdvancementTask(advancement1);
         }
      }

   }

   public interface IListener {
      void onAddAdvancementRoot(Advancement pAdvancement);

      @OnlyIn(Dist.CLIENT)
      void onRemoveAdvancementRoot(Advancement pAdvancement);

      void onAddAdvancementTask(Advancement pAdvancement);

      @OnlyIn(Dist.CLIENT)
      void onRemoveAdvancementTask(Advancement pAdvancement);

      @OnlyIn(Dist.CLIENT)
      void onAdvancementsCleared();
   }
}