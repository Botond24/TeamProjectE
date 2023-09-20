package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.loot.LootContext;
import net.minecraft.util.ResourceLocation;

public class BredAnimalsTrigger extends AbstractCriterionTrigger<BredAnimalsTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("bred_animals");

   public ResourceLocation getId() {
      return ID;
   }

   public BredAnimalsTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      EntityPredicate.AndPredicate entitypredicate$andpredicate = EntityPredicate.AndPredicate.fromJson(pJson, "parent", pConditionsParser);
      EntityPredicate.AndPredicate entitypredicate$andpredicate1 = EntityPredicate.AndPredicate.fromJson(pJson, "partner", pConditionsParser);
      EntityPredicate.AndPredicate entitypredicate$andpredicate2 = EntityPredicate.AndPredicate.fromJson(pJson, "child", pConditionsParser);
      return new BredAnimalsTrigger.Instance(pEntityPredicate, entitypredicate$andpredicate, entitypredicate$andpredicate1, entitypredicate$andpredicate2);
   }

   public void trigger(ServerPlayerEntity pPlayer, AnimalEntity pParent, AnimalEntity pPartner, @Nullable AgeableEntity pChild) {
      LootContext lootcontext = EntityPredicate.createContext(pPlayer, pParent);
      LootContext lootcontext1 = EntityPredicate.createContext(pPlayer, pPartner);
      LootContext lootcontext2 = pChild != null ? EntityPredicate.createContext(pPlayer, pChild) : null;
      this.trigger(pPlayer, (p_233510_3_) -> {
         return p_233510_3_.matches(lootcontext, lootcontext1, lootcontext2);
      });
   }

   public static class Instance extends CriterionInstance {
      private final EntityPredicate.AndPredicate parent;
      private final EntityPredicate.AndPredicate partner;
      private final EntityPredicate.AndPredicate child;

      public Instance(EntityPredicate.AndPredicate pPlayer, EntityPredicate.AndPredicate pParent, EntityPredicate.AndPredicate pPartner, EntityPredicate.AndPredicate pChild) {
         super(BredAnimalsTrigger.ID, pPlayer);
         this.parent = pParent;
         this.partner = pPartner;
         this.child = pChild;
      }

      public static BredAnimalsTrigger.Instance bredAnimals() {
         return new BredAnimalsTrigger.Instance(EntityPredicate.AndPredicate.ANY, EntityPredicate.AndPredicate.ANY, EntityPredicate.AndPredicate.ANY, EntityPredicate.AndPredicate.ANY);
      }

      public static BredAnimalsTrigger.Instance bredAnimals(EntityPredicate.Builder pBuilder) {
         return new BredAnimalsTrigger.Instance(EntityPredicate.AndPredicate.ANY, EntityPredicate.AndPredicate.ANY, EntityPredicate.AndPredicate.ANY, EntityPredicate.AndPredicate.wrap(pBuilder.build()));
      }

      public static BredAnimalsTrigger.Instance bredAnimals(EntityPredicate pParent, EntityPredicate pPartner, EntityPredicate pChild) {
         return new BredAnimalsTrigger.Instance(EntityPredicate.AndPredicate.ANY, EntityPredicate.AndPredicate.wrap(pParent), EntityPredicate.AndPredicate.wrap(pPartner), EntityPredicate.AndPredicate.wrap(pChild));
      }

      public boolean matches(LootContext pParentContext, LootContext pPartnerContext, @Nullable LootContext pChildContext) {
         if (this.child == EntityPredicate.AndPredicate.ANY || pChildContext != null && this.child.matches(pChildContext)) {
            return this.parent.matches(pParentContext) && this.partner.matches(pPartnerContext) || this.parent.matches(pPartnerContext) && this.partner.matches(pParentContext);
         } else {
            return false;
         }
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("parent", this.parent.toJson(pConditions));
         jsonobject.add("partner", this.partner.toJson(pConditions));
         jsonobject.add("child", this.child.toJson(pConditions));
         return jsonobject;
      }
   }
}