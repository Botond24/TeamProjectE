package net.minecraft.stats;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class StatType<T> extends net.minecraftforge.registries.ForgeRegistryEntry<StatType<?>> implements Iterable<Stat<T>> {
   private final Registry<T> registry;
   private final Map<T, Stat<T>> map = new IdentityHashMap<>();
   @Nullable
   @OnlyIn(Dist.CLIENT)
   private ITextComponent displayName;

   public StatType(Registry<T> pRegistry) {
      this.registry = pRegistry;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean contains(T pStat) {
      return this.map.containsKey(pStat);
   }

   public Stat<T> get(T pStat, IStatFormatter pFormatter) {
      return this.map.computeIfAbsent(pStat, (p_199075_2_) -> {
         return new Stat<>(this, p_199075_2_, pFormatter);
      });
   }

   public Registry<T> getRegistry() {
      return this.registry;
   }

   public Iterator<Stat<T>> iterator() {
      return this.map.values().iterator();
   }

   public Stat<T> get(T pStat) {
      return this.get(pStat, IStatFormatter.DEFAULT);
   }

   @OnlyIn(Dist.CLIENT)
   public String getTranslationKey() {
      return "stat_type." + Registry.STAT_TYPE.getKey(this).toString().replace(':', '.');
   }

   @OnlyIn(Dist.CLIENT)
   public ITextComponent getDisplayName() {
      if (this.displayName == null) {
         this.displayName = new TranslationTextComponent(this.getTranslationKey());
      }

      return this.displayName;
   }
}
