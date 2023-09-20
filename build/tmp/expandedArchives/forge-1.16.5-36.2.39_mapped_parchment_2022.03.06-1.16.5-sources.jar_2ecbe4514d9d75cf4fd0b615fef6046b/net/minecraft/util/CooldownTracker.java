package net.minecraft.util;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CooldownTracker {
   private final Map<Item, CooldownTracker.Cooldown> cooldowns = Maps.newHashMap();
   private int tickCount;

   public boolean isOnCooldown(Item pItem) {
      return this.getCooldownPercent(pItem, 0.0F) > 0.0F;
   }

   public float getCooldownPercent(Item pItem, float pPartialTicks) {
      CooldownTracker.Cooldown cooldowntracker$cooldown = this.cooldowns.get(pItem);
      if (cooldowntracker$cooldown != null) {
         float f = (float)(cooldowntracker$cooldown.endTime - cooldowntracker$cooldown.startTime);
         float f1 = (float)cooldowntracker$cooldown.endTime - ((float)this.tickCount + pPartialTicks);
         return MathHelper.clamp(f1 / f, 0.0F, 1.0F);
      } else {
         return 0.0F;
      }
   }

   public void tick() {
      ++this.tickCount;
      if (!this.cooldowns.isEmpty()) {
         Iterator<Entry<Item, CooldownTracker.Cooldown>> iterator = this.cooldowns.entrySet().iterator();

         while(iterator.hasNext()) {
            Entry<Item, CooldownTracker.Cooldown> entry = iterator.next();
            if ((entry.getValue()).endTime <= this.tickCount) {
               iterator.remove();
               this.onCooldownEnded(entry.getKey());
            }
         }
      }

   }

   public void addCooldown(Item pItem, int pTicks) {
      this.cooldowns.put(pItem, new CooldownTracker.Cooldown(this.tickCount, this.tickCount + pTicks));
      this.onCooldownStarted(pItem, pTicks);
   }

   @OnlyIn(Dist.CLIENT)
   public void removeCooldown(Item pItem) {
      this.cooldowns.remove(pItem);
      this.onCooldownEnded(pItem);
   }

   protected void onCooldownStarted(Item pItem, int pTicks) {
   }

   protected void onCooldownEnded(Item pItem) {
   }

   class Cooldown {
      private final int startTime;
      private final int endTime;

      private Cooldown(int pStartTime, int pEndTime) {
         this.startTime = pStartTime;
         this.endTime = pEndTime;
      }
   }
}