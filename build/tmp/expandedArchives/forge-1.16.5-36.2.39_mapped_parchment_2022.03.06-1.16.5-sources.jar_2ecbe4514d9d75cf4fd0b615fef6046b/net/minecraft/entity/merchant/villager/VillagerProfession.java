package net.minecraft.entity.merchant.villager;

import com.google.common.collect.ImmutableSet;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.PointOfInterestType;

public class VillagerProfession extends net.minecraftforge.registries.ForgeRegistryEntry<VillagerProfession> {
   public static final VillagerProfession NONE = register("none", PointOfInterestType.UNEMPLOYED, (SoundEvent)null);
   public static final VillagerProfession ARMORER = register("armorer", PointOfInterestType.ARMORER, SoundEvents.VILLAGER_WORK_ARMORER);
   public static final VillagerProfession BUTCHER = register("butcher", PointOfInterestType.BUTCHER, SoundEvents.VILLAGER_WORK_BUTCHER);
   public static final VillagerProfession CARTOGRAPHER = register("cartographer", PointOfInterestType.CARTOGRAPHER, SoundEvents.VILLAGER_WORK_CARTOGRAPHER);
   public static final VillagerProfession CLERIC = register("cleric", PointOfInterestType.CLERIC, SoundEvents.VILLAGER_WORK_CLERIC);
   public static final VillagerProfession FARMER = register("farmer", PointOfInterestType.FARMER, ImmutableSet.of(Items.WHEAT, Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.BONE_MEAL), ImmutableSet.of(Blocks.FARMLAND), SoundEvents.VILLAGER_WORK_FARMER);
   public static final VillagerProfession FISHERMAN = register("fisherman", PointOfInterestType.FISHERMAN, SoundEvents.VILLAGER_WORK_FISHERMAN);
   public static final VillagerProfession FLETCHER = register("fletcher", PointOfInterestType.FLETCHER, SoundEvents.VILLAGER_WORK_FLETCHER);
   public static final VillagerProfession LEATHERWORKER = register("leatherworker", PointOfInterestType.LEATHERWORKER, SoundEvents.VILLAGER_WORK_LEATHERWORKER);
   public static final VillagerProfession LIBRARIAN = register("librarian", PointOfInterestType.LIBRARIAN, SoundEvents.VILLAGER_WORK_LIBRARIAN);
   public static final VillagerProfession MASON = register("mason", PointOfInterestType.MASON, SoundEvents.VILLAGER_WORK_MASON);
   public static final VillagerProfession NITWIT = register("nitwit", PointOfInterestType.NITWIT, (SoundEvent)null);
   public static final VillagerProfession SHEPHERD = register("shepherd", PointOfInterestType.SHEPHERD, SoundEvents.VILLAGER_WORK_SHEPHERD);
   public static final VillagerProfession TOOLSMITH = register("toolsmith", PointOfInterestType.TOOLSMITH, SoundEvents.VILLAGER_WORK_TOOLSMITH);
   public static final VillagerProfession WEAPONSMITH = register("weaponsmith", PointOfInterestType.WEAPONSMITH, SoundEvents.VILLAGER_WORK_WEAPONSMITH);
   private final String name;
   private final PointOfInterestType jobPoiType;
   /** Defines items villagers of this profession can pick up and use. */
   private final ImmutableSet<Item> requestedItems;
   /** World blocks this profession interracts with. */
   private final ImmutableSet<Block> secondaryPoi;
   @Nullable
   private final SoundEvent workSound;

   public VillagerProfession(String pName, PointOfInterestType pJobPoiType, ImmutableSet<Item> pRequestedItems, ImmutableSet<Block> pSecondaryPoi, @Nullable SoundEvent pWorkSound) {
      this.name = pName;
      this.jobPoiType = pJobPoiType;
      this.requestedItems = pRequestedItems;
      this.secondaryPoi = pSecondaryPoi;
      this.workSound = pWorkSound;
   }

   public PointOfInterestType getJobPoiType() {
      return this.jobPoiType;
   }

   /**
    * @return A shared static immutable set of the specific items this profession can handle.
    */
   public ImmutableSet<Item> getRequestedItems() {
      return this.requestedItems;
   }

   /**
    * @return A shared static immutable set of the world blocks this profession interracts with beside job site block.
    */
   public ImmutableSet<Block> getSecondaryPoi() {
      return this.secondaryPoi;
   }

   @Nullable
   public SoundEvent getWorkSound() {
      return this.workSound;
   }

   public String toString() {
      return this.name;
   }

   static VillagerProfession register(String pName, PointOfInterestType pJobPoiType, @Nullable SoundEvent pWorkSound) {
      return register(pName, pJobPoiType, ImmutableSet.of(), ImmutableSet.of(), pWorkSound);
   }

   static VillagerProfession register(String pName, PointOfInterestType pJobPoiType, ImmutableSet<Item> pRequestedItems, ImmutableSet<Block> pSecondaryPoi, @Nullable SoundEvent pWorkSound) {
      return Registry.register(Registry.VILLAGER_PROFESSION, new ResourceLocation(pName), new VillagerProfession(pName, pJobPoiType, pRequestedItems, pSecondaryPoi, pWorkSound));
   }
}
