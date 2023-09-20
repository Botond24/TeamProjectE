package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class RightClickBlockWithItemTrigger extends AbstractCriterionTrigger<RightClickBlockWithItemTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("item_used_on_block");

   public ResourceLocation getId() {
      return ID;
   }

   public RightClickBlockWithItemTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      LocationPredicate locationpredicate = LocationPredicate.fromJson(pJson.get("location"));
      ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("item"));
      return new RightClickBlockWithItemTrigger.Instance(pEntityPredicate, locationpredicate, itempredicate);
   }

   public void trigger(ServerPlayerEntity pPlayer, BlockPos pPos, ItemStack pStack) {
      BlockState blockstate = pPlayer.getLevel().getBlockState(pPos);
      this.trigger(pPlayer, (p_226694_4_) -> {
         return p_226694_4_.matches(blockstate, pPlayer.getLevel(), pPos, pStack);
      });
   }

   public static class Instance extends CriterionInstance {
      private final LocationPredicate location;
      private final ItemPredicate item;

      public Instance(EntityPredicate.AndPredicate p_i231602_1_, LocationPredicate p_i231602_2_, ItemPredicate p_i231602_3_) {
         super(RightClickBlockWithItemTrigger.ID, p_i231602_1_);
         this.location = p_i231602_2_;
         this.item = p_i231602_3_;
      }

      public static RightClickBlockWithItemTrigger.Instance itemUsedOnBlock(LocationPredicate.Builder pLocationBuilder, ItemPredicate.Builder pStackBuilder) {
         return new RightClickBlockWithItemTrigger.Instance(EntityPredicate.AndPredicate.ANY, pLocationBuilder.build(), pStackBuilder.build());
      }

      public boolean matches(BlockState pState, ServerWorld pLevel, BlockPos pPos, ItemStack pStack) {
         return !this.location.matches(pLevel, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D) ? false : this.item.matches(pStack);
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("location", this.location.serializeToJson());
         jsonobject.add("item", this.item.serializeToJson());
         return jsonobject;
      }
   }
}