package net.minecraft.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Locale;
import java.util.Set;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Convert any empty maps into explorer maps that lead to a structure that is nearest to the current {@linkplain
 * LootContextParams.ORIGIN}, if present.
 */
public class ExplorationMap extends LootFunction {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final Structure<?> DEFAULT_FEATURE = Structure.BURIED_TREASURE;
   public static final MapDecoration.Type DEFAULT_DECORATION = MapDecoration.Type.MANSION;
   private final Structure<?> destination;
   private final MapDecoration.Type mapDecoration;
   private final byte zoom;
   private final int searchRadius;
   private final boolean skipKnownStructures;

   private ExplorationMap(ILootCondition[] pConditions, Structure<?> pDestination, MapDecoration.Type pMapDecoration, byte pZoom, int pSearchRadius, boolean pSkipKnownStructures) {
      super(pConditions);
      this.destination = pDestination;
      this.mapDecoration = pMapDecoration;
      this.zoom = pZoom;
      this.searchRadius = pSearchRadius;
      this.skipKnownStructures = pSkipKnownStructures;
   }

   public LootFunctionType getType() {
      return LootFunctionManager.EXPLORATION_MAP;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootParameter<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootParameters.ORIGIN);
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      if (pStack.getItem() != Items.MAP) {
         return pStack;
      } else {
         Vector3d vector3d = pContext.getParamOrNull(LootParameters.ORIGIN);
         if (vector3d != null) {
            ServerWorld serverworld = pContext.getLevel();
            BlockPos blockpos = serverworld.findNearestMapFeature(this.destination, new BlockPos(vector3d), this.searchRadius, this.skipKnownStructures);
            if (blockpos != null) {
               ItemStack itemstack = FilledMapItem.create(serverworld, blockpos.getX(), blockpos.getZ(), this.zoom, true, true);
               FilledMapItem.renderBiomePreviewMap(serverworld, itemstack);
               MapData.addTargetDecoration(itemstack, blockpos, "+", this.mapDecoration);
               itemstack.setHoverName(new TranslationTextComponent("filled_map." + this.destination.getFeatureName().toLowerCase(Locale.ROOT)));
               return itemstack;
            }
         }

         return pStack;
      }
   }

   public static ExplorationMap.Builder makeExplorationMap() {
      return new ExplorationMap.Builder();
   }

   public static class Builder extends LootFunction.Builder<ExplorationMap.Builder> {
      private Structure<?> destination = ExplorationMap.DEFAULT_FEATURE;
      private MapDecoration.Type mapDecoration = ExplorationMap.DEFAULT_DECORATION;
      private byte zoom = 2;
      private int searchRadius = 50;
      private boolean skipKnownStructures = true;

      protected ExplorationMap.Builder getThis() {
         return this;
      }

      public ExplorationMap.Builder setDestination(Structure<?> p_237427_1_) {
         this.destination = p_237427_1_;
         return this;
      }

      public ExplorationMap.Builder setMapDecoration(MapDecoration.Type p_216064_1_) {
         this.mapDecoration = p_216064_1_;
         return this;
      }

      public ExplorationMap.Builder setZoom(byte p_216062_1_) {
         this.zoom = p_216062_1_;
         return this;
      }

      public ExplorationMap.Builder setSkipKnownStructures(boolean p_216063_1_) {
         this.skipKnownStructures = p_216063_1_;
         return this;
      }

      public ILootFunction build() {
         return new ExplorationMap(this.getConditions(), this.destination, this.mapDecoration, this.zoom, this.searchRadius, this.skipKnownStructures);
      }
   }

   public static class Serializer extends LootFunction.Serializer<ExplorationMap> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, ExplorationMap pValue, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pValue, pSerializationContext);
         if (!pValue.destination.equals(ExplorationMap.DEFAULT_FEATURE)) {
            pJson.add("destination", pSerializationContext.serialize(pValue.destination.getFeatureName()));
         }

         if (pValue.mapDecoration != ExplorationMap.DEFAULT_DECORATION) {
            pJson.add("decoration", pSerializationContext.serialize(pValue.mapDecoration.toString().toLowerCase(Locale.ROOT)));
         }

         if (pValue.zoom != 2) {
            pJson.addProperty("zoom", pValue.zoom);
         }

         if (pValue.searchRadius != 50) {
            pJson.addProperty("search_radius", pValue.searchRadius);
         }

         if (!pValue.skipKnownStructures) {
            pJson.addProperty("skip_existing_chunks", pValue.skipKnownStructures);
         }

      }

      public ExplorationMap deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, ILootCondition[] pConditions) {
         Structure<?> structure = readStructure(pObject);
         String s = pObject.has("decoration") ? JSONUtils.getAsString(pObject, "decoration") : "mansion";
         MapDecoration.Type mapdecoration$type = ExplorationMap.DEFAULT_DECORATION;

         try {
            mapdecoration$type = MapDecoration.Type.valueOf(s.toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException illegalargumentexception) {
            ExplorationMap.LOGGER.error("Error while parsing loot table decoration entry. Found {}. Defaulting to " + ExplorationMap.DEFAULT_DECORATION, (Object)s);
         }

         byte b0 = JSONUtils.getAsByte(pObject, "zoom", (byte)2);
         int i = JSONUtils.getAsInt(pObject, "search_radius", 50);
         boolean flag = JSONUtils.getAsBoolean(pObject, "skip_existing_chunks", true);
         return new ExplorationMap(pConditions, structure, mapdecoration$type, b0, i, flag);
      }

      private static Structure<?> readStructure(JsonObject p_237428_0_) {
         if (p_237428_0_.has("destination")) {
            String s = JSONUtils.getAsString(p_237428_0_, "destination");
            Structure<?> structure = Structure.STRUCTURES_REGISTRY.get(s.toLowerCase(Locale.ROOT));
            if (structure != null) {
               return structure;
            }
         }

         return ExplorationMap.DEFAULT_FEATURE;
      }
   }
}