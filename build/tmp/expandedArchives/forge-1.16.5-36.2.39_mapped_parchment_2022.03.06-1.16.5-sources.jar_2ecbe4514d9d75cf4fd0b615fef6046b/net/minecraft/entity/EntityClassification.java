package net.minecraft.entity;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.util.IStringSerializable;

public enum EntityClassification implements IStringSerializable, net.minecraftforge.common.IExtensibleEnum {
   MONSTER("monster", 70, false, false, 128),
   CREATURE("creature", 10, true, true, 128),
   AMBIENT("ambient", 15, true, false, 128),
   WATER_CREATURE("water_creature", 5, true, false, 128),
   WATER_AMBIENT("water_ambient", 20, true, false, 64),
   MISC("misc", -1, true, true, 128);

   public static final Codec<EntityClassification> CODEC = net.minecraftforge.common.IExtensibleEnum.createCodecForExtensibleEnum(EntityClassification::values, EntityClassification::byName);
   private static final Map<String, EntityClassification> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(EntityClassification::getName, (p_220362_0_) -> {
      return p_220362_0_;
   }));
   private final int max;
   private final boolean isFriendly;
   private final boolean isPersistent;
   private final String name;
   private final int noDespawnDistance = 32;
   private final int despawnDistance;

   private EntityClassification(String pName, int pMax, boolean pIsFriendly, boolean pIsPersistent, int pDespawnDistance) {
      this.name = pName;
      this.max = pMax;
      this.isFriendly = pIsFriendly;
      this.isPersistent = pIsPersistent;
      this.despawnDistance = pDespawnDistance;
   }

   public String getName() {
      return this.name;
   }

   public String getSerializedName() {
      return this.name;
   }

   public static EntityClassification byName(String p_233670_0_) {
      return BY_NAME.get(p_233670_0_);
   }

   public int getMaxInstancesPerChunk() {
      return this.max;
   }

   /**
    * Gets whether or not this creature type is peaceful.
    */
   public boolean isFriendly() {
      return this.isFriendly;
   }

   /**
    * Return whether this creature type is an animal.
    */
   public boolean isPersistent() {
      return this.isPersistent;
   }

   public static EntityClassification create(String name, String id, int maxNumberOfCreatureIn, boolean isPeacefulCreatureIn, boolean isAnimalIn, int despawnDistance) {
      throw new IllegalStateException("Enum not extended");
   }

   @Override
   @Deprecated
   public void init() {
      BY_NAME.put(this.getName(), this);
   }

   public int getDespawnDistance() {
      return this.despawnDistance;
   }

   public int getNoDespawnDistance() {
      return 32;
   }
}
