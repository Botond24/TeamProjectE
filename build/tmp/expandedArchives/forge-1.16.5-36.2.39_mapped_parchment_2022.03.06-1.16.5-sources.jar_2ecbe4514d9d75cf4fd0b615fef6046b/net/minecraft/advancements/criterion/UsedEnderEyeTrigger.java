package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class UsedEnderEyeTrigger extends AbstractCriterionTrigger<UsedEnderEyeTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("used_ender_eye");

   public ResourceLocation getId() {
      return ID;
   }

   public UsedEnderEyeTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      MinMaxBounds.FloatBound minmaxbounds$floatbound = MinMaxBounds.FloatBound.fromJson(pJson.get("distance"));
      return new UsedEnderEyeTrigger.Instance(pEntityPredicate, minmaxbounds$floatbound);
   }

   public void trigger(ServerPlayerEntity pPlayer, BlockPos pPos) {
      double d0 = pPlayer.getX() - (double)pPos.getX();
      double d1 = pPlayer.getZ() - (double)pPos.getZ();
      double d2 = d0 * d0 + d1 * d1;
      this.trigger(pPlayer, (p_227325_2_) -> {
         return p_227325_2_.matches(d2);
      });
   }

   public static class Instance extends CriterionInstance {
      private final MinMaxBounds.FloatBound level;

      public Instance(EntityPredicate.AndPredicate p_i232030_1_, MinMaxBounds.FloatBound p_i232030_2_) {
         super(UsedEnderEyeTrigger.ID, p_i232030_1_);
         this.level = p_i232030_2_;
      }

      public boolean matches(double pDistanceSq) {
         return this.level.matchesSqr(pDistanceSq);
      }
   }
}