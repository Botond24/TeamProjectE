package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

public class ShotCrossbowTrigger extends AbstractCriterionTrigger<ShotCrossbowTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("shot_crossbow");

   public ResourceLocation getId() {
      return ID;
   }

   public ShotCrossbowTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("item"));
      return new ShotCrossbowTrigger.Instance(pEntityPredicate, itempredicate);
   }

   public void trigger(ServerPlayerEntity pShooter, ItemStack pStack) {
      this.trigger(pShooter, (p_227037_1_) -> {
         return p_227037_1_.matches(pStack);
      });
   }

   public static class Instance extends CriterionInstance {
      private final ItemPredicate item;

      public Instance(EntityPredicate.AndPredicate p_i231880_1_, ItemPredicate p_i231880_2_) {
         super(ShotCrossbowTrigger.ID, p_i231880_1_);
         this.item = p_i231880_2_;
      }

      public static ShotCrossbowTrigger.Instance shotCrossbow(IItemProvider pItemProvider) {
         return new ShotCrossbowTrigger.Instance(EntityPredicate.AndPredicate.ANY, ItemPredicate.Builder.item().of(pItemProvider).build());
      }

      public boolean matches(ItemStack pStack) {
         return this.item.matches(pStack);
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("item", this.item.serializeToJson());
         return jsonobject;
      }
   }
}