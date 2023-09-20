package net.minecraft.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * DataPack reload listener that reads loot tables from the ResourceManager and stores them.
 * 
 * @see LootTable
 */
public class LootTableManager extends JsonReloadListener {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = LootSerializers.createLootTableSerializer().create();
   private Map<ResourceLocation, LootTable> tables = ImmutableMap.of();
   private final LootPredicateManager predicateManager;

   public LootTableManager(LootPredicateManager pPredicateManager) {
      super(GSON, "loot_tables");
      this.predicateManager = pPredicateManager;
   }

   /**
    * Get a LootTable by its ID. Returns the empty loot table if no such table exists.
    */
   public LootTable get(ResourceLocation pLootTableId) {
      return this.tables.getOrDefault(pLootTableId, LootTable.EMPTY);
   }

   protected void apply(Map<ResourceLocation, JsonElement> pObject, IResourceManager pResourceManager, IProfiler pProfiler) {
      Builder<ResourceLocation, LootTable> builder = ImmutableMap.builder();
      JsonElement jsonelement = pObject.remove(LootTables.EMPTY);
      if (jsonelement != null) {
         LOGGER.warn("Datapack tried to redefine {} loot table, ignoring", (Object)LootTables.EMPTY);
      }

      pObject.forEach((p_237403_1_, p_237403_2_) -> {
         try (net.minecraft.resources.IResource res = pResourceManager.getResource(getPreparedPath(p_237403_1_));){
            LootTable loottable = net.minecraftforge.common.ForgeHooks.loadLootTable(GSON, p_237403_1_, p_237403_2_, res == null || !res.getSourceName().equals("Default"), this);
            builder.put(p_237403_1_, loottable);
         } catch (Exception exception) {
            LOGGER.error("Couldn't parse loot table {}", p_237403_1_, exception);
         }

      });
      builder.put(LootTables.EMPTY, LootTable.EMPTY);
      ImmutableMap<ResourceLocation, LootTable> immutablemap = builder.build();
      ValidationTracker validationtracker = new ValidationTracker(LootParameterSets.ALL_PARAMS, this.predicateManager::get, immutablemap::get);
      immutablemap.forEach((p_227509_1_, p_227509_2_) -> {
         validate(validationtracker, p_227509_1_, p_227509_2_);
      });
      validationtracker.getProblems().forEach((p_215303_0_, p_215303_1_) -> {
         LOGGER.warn("Found validation problem in " + p_215303_0_ + ": " + p_215303_1_);
      });
      this.tables = immutablemap;
   }

   /**
    * Validate the given LootTable with the given ID using the given ValidationContext.
    */
   public static void validate(ValidationTracker pValidator, ResourceLocation pId, LootTable pLootTable) {
      pLootTable.validate(pValidator.setParams(pLootTable.getParamSet()).enterTable("{" + pId + "}", pId));
   }

   public static JsonElement serialize(LootTable pLootTable) {
      return GSON.toJsonTree(pLootTable);
   }

   /**
    * Get all known LootTable IDs.
    */
   public Set<ResourceLocation> getIds() {
      return this.tables.keySet();
   }
}
