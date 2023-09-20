package net.minecraft.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;

/**
 * LootContext stores various context information for loot generation.
 * This includes the Level as well as any known {@link LootContextParam}s.
 */
public class LootContext {
   private final Random random;
   private final float luck;
   private final ServerWorld level;
   private final Function<ResourceLocation, LootTable> lootTables;
   private final Set<LootTable> visitedTables = Sets.newLinkedHashSet();
   private final Function<ResourceLocation, ILootCondition> conditions;
   private final Set<ILootCondition> visitedConditions = Sets.newLinkedHashSet();
   private final Map<LootParameter<?>, Object> params;
   private final Map<ResourceLocation, LootContext.IDynamicDropProvider> dynamicDrops;

   private LootContext(Random pRandom, float pLuck, ServerWorld pLevel, Function<ResourceLocation, LootTable> pLootTables, Function<ResourceLocation, ILootCondition> pConditions, Map<LootParameter<?>, Object> pParams, Map<ResourceLocation, LootContext.IDynamicDropProvider> pDynamicDrops) {
      this.random = pRandom;
      this.luck = pLuck;
      this.level = pLevel;
      this.lootTables = pLootTables;
      this.conditions = pConditions;
      this.params = ImmutableMap.copyOf(pParams);
      this.dynamicDrops = ImmutableMap.copyOf(pDynamicDrops);
   }

   /**
    * Check whether the given parameter is present in this context.
    */
   public boolean hasParam(LootParameter<?> pParameter) {
      return this.params.containsKey(pParameter);
   }

   /**
    * Add the dynamic drops for the given dynamic drops name to the given consumer.
    * If no dynamic drops provider for the given name has been registered to this LootContext, nothing is generated.
    * 
    * @see DynamicDrops
    */
   public void addDynamicDrops(ResourceLocation pName, Consumer<ItemStack> pConsumer) {
      LootContext.IDynamicDropProvider lootcontext$idynamicdropprovider = this.dynamicDrops.get(pName);
      if (lootcontext$idynamicdropprovider != null) {
         lootcontext$idynamicdropprovider.add(this, pConsumer);
      }

   }

   /**
    * Get the value of the given parameter if it is present in this context, null otherwise.
    */
   @Nullable
   public <T> T getParamOrNull(LootParameter<T> pParameter) {
      return (T)this.params.get(pParameter);
   }

   public boolean addVisitedTable(LootTable pLootTable) {
      return this.visitedTables.add(pLootTable);
   }

   public void removeVisitedTable(LootTable pLootTable) {
      this.visitedTables.remove(pLootTable);
   }

   public boolean addVisitedCondition(ILootCondition pCondition) {
      return this.visitedConditions.add(pCondition);
   }

   public void removeVisitedCondition(ILootCondition pCondition) {
      this.visitedConditions.remove(pCondition);
   }

   public LootTable getLootTable(ResourceLocation pTableId) {
      return this.lootTables.apply(pTableId);
   }

   public ILootCondition getCondition(ResourceLocation pConditionId) {
      return this.conditions.apply(pConditionId);
   }

   public Random getRandom() {
      return this.random;
   }

   /**
    * The luck value for this loot context. This is usually just the player's {@linkplain Attributes#LUCK luck value},
    * however it may be modified depending on the context of the looting.
    * When fishing for example it is increased based on the Luck of the Sea enchantment.
    */
   public float getLuck() {
      return this.luck;
   }

   public ServerWorld getLevel() {
      return this.level;
   }

   // ============================== FORGE START ==============================
   public int getLootingModifier() {
      return net.minecraftforge.common.ForgeHooks.getLootingLevel(getParamOrNull(LootParameters.THIS_ENTITY), getParamOrNull(LootParameters.KILLER_ENTITY), getParamOrNull(LootParameters.DAMAGE_SOURCE));
   }

   private ResourceLocation queriedLootTableId;

   private LootContext(Random rand, float luckIn, ServerWorld worldIn, Function<ResourceLocation, LootTable> lootTableManagerIn, Function<ResourceLocation, ILootCondition> pConditions, Map<LootParameter<?>, Object> parametersIn, Map<ResourceLocation, LootContext.IDynamicDropProvider> conditionsIn, ResourceLocation queriedLootTableId) {
      this(rand, luckIn, worldIn, lootTableManagerIn, pConditions, parametersIn, conditionsIn);
      if (queriedLootTableId != null) this.queriedLootTableId = queriedLootTableId;
   }

   public void setQueriedLootTableId(ResourceLocation queriedLootTableId) {
      if (this.queriedLootTableId == null && queriedLootTableId != null) this.queriedLootTableId = queriedLootTableId;
   }
   public ResourceLocation getQueriedLootTableId() {
      return this.queriedLootTableId == null? net.minecraftforge.common.loot.LootTableIdCondition.UNKNOWN_LOOT_TABLE : this.queriedLootTableId;
   }
   // =============================== FORGE END ===============================

   public static class Builder {
      private final ServerWorld level;
      private final Map<LootParameter<?>, Object> params = Maps.newIdentityHashMap();
      private final Map<ResourceLocation, LootContext.IDynamicDropProvider> dynamicDrops = Maps.newHashMap();
      private Random random;
      private float luck;
      private ResourceLocation queriedLootTableId; // Forge: correctly pass around loot table ID with copy constructor

      public Builder(ServerWorld pLevel) {
         this.level = pLevel;
      }

      public Builder(LootContext context) {
         this.level = context.level;
         this.params.putAll(context.params);
         this.dynamicDrops.putAll(context.dynamicDrops);
         this.random = context.random;
         this.luck = context.luck;
         this.queriedLootTableId = context.queriedLootTableId;
      }

      public LootContext.Builder withRandom(Random pRandom) {
         this.random = pRandom;
         return this;
      }

      public LootContext.Builder withOptionalRandomSeed(long pSeed) {
         if (pSeed != 0L) {
            this.random = new Random(pSeed);
         }

         return this;
      }

      public LootContext.Builder withOptionalRandomSeed(long pSeed, Random pRandom) {
         if (pSeed == 0L) {
            this.random = pRandom;
         } else {
            this.random = new Random(pSeed);
         }

         return this;
      }

      public LootContext.Builder withLuck(float pLuck) {
         this.luck = pLuck;
         return this;
      }

      public <T> LootContext.Builder withParameter(LootParameter<T> pParameter, T pValue) {
         this.params.put(pParameter, pValue);
         return this;
      }

      public <T> LootContext.Builder withOptionalParameter(LootParameter<T> pParameter, @Nullable T pValue) {
         if (pValue == null) {
            this.params.remove(pParameter);
         } else {
            this.params.put(pParameter, pValue);
         }

         return this;
      }

      /**
       * Registers a DynamicDrop to the LootContext.
       * 
       * @see LootContext.DynamicDrop
       */
      public LootContext.Builder withDynamicDrop(ResourceLocation pDynamicDropId, LootContext.IDynamicDropProvider pDynamicDrop) {
         LootContext.IDynamicDropProvider lootcontext$idynamicdropprovider = this.dynamicDrops.put(pDynamicDropId, pDynamicDrop);
         if (lootcontext$idynamicdropprovider != null) {
            throw new IllegalStateException("Duplicated dynamic drop '" + this.dynamicDrops + "'");
         } else {
            return this;
         }
      }

      public ServerWorld getLevel() {
         return this.level;
      }

      public <T> T getParameter(LootParameter<T> pParameter) {
         T t = (T)this.params.get(pParameter);
         if (t == null) {
            throw new IllegalArgumentException("No parameter " + pParameter);
         } else {
            return t;
         }
      }

      @Nullable
      public <T> T getOptionalParameter(LootParameter<T> pParameter) {
         return (T)this.params.get(pParameter);
      }

      public LootContext create(LootParameterSet pParameterSet) {
         Set<LootParameter<?>> set = Sets.difference(this.params.keySet(), pParameterSet.getAllowed());
         if (false && !set.isEmpty()) { // Forge: Allow mods to pass custom loot parameters (not part of the vanilla loot table) to the loot context.
            throw new IllegalArgumentException("Parameters not allowed in this parameter set: " + set);
         } else {
            Set<LootParameter<?>> set1 = Sets.difference(pParameterSet.getRequired(), this.params.keySet());
            if (!set1.isEmpty()) {
               throw new IllegalArgumentException("Missing required parameters: " + set1);
            } else {
               Random random = this.random;
               if (random == null) {
                  random = new Random();
               }

               MinecraftServer minecraftserver = this.level.getServer();
               return new LootContext(random, this.luck, this.level, minecraftserver.getLootTables()::get, minecraftserver.getPredicateManager()::get, this.params, this.dynamicDrops, this.queriedLootTableId);
            }
         }
      }
   }

   /**
    * Represents a type of entity that can be looked up in a {@link LootContext} using a {@link LootContextParam}.
    */
   public static enum EntityTarget {
      THIS("this", LootParameters.THIS_ENTITY),
      KILLER("killer", LootParameters.KILLER_ENTITY),
      DIRECT_KILLER("direct_killer", LootParameters.DIRECT_KILLER_ENTITY),
      KILLER_PLAYER("killer_player", LootParameters.LAST_DAMAGE_PLAYER);

      private final String name;
      private final LootParameter<? extends Entity> param;

      private EntityTarget(String pName, LootParameter<? extends Entity> pParam) {
         this.name = pName;
         this.param = pParam;
      }

      public LootParameter<? extends Entity> getParam() {
         return this.param;
      }

      public static LootContext.EntityTarget getByName(String pName) {
         for(LootContext.EntityTarget lootcontext$entitytarget : values()) {
            if (lootcontext$entitytarget.name.equals(pName)) {
               return lootcontext$entitytarget;
            }
         }

         throw new IllegalArgumentException("Invalid entity target " + pName);
      }

      public static class Serializer extends TypeAdapter<LootContext.EntityTarget> {
         public void write(JsonWriter p_write_1_, LootContext.EntityTarget p_write_2_) throws IOException {
            p_write_1_.value(p_write_2_.name);
         }

         public LootContext.EntityTarget read(JsonReader p_read_1_) throws IOException {
            return LootContext.EntityTarget.getByName(p_read_1_.nextString());
         }
      }
   }

   /**
    * DynamicDrop allows a loot generating object (e.g. a Block or Entity) to provide dynamic drops to a loot table.
    * An example of this are shulker boxes, which provide their contents as a dynamic drop source.
    * Dynamic drops are registered with a name using {@link LootContext.Builder#withDynamicDrop}.
    * 
    * These dynamic drops can then be referenced from a loot table using {@link DynamicLoot}.
    */
   @FunctionalInterface
   public interface IDynamicDropProvider {
      void add(LootContext p_add_1_, Consumer<ItemStack> p_add_2_);
   }
}
