package net.minecraft.entity.monster;

import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class GiantEntity extends MonsterEntity {
   public GiantEntity(EntityType<? extends GiantEntity> p_i50205_1_, World p_i50205_2_) {
      super(p_i50205_1_, p_i50205_2_);
   }

   protected float getStandingEyeHeight(Pose pPose, EntitySize pSize) {
      return 10.440001F;
   }

   public static AttributeModifierMap.MutableAttribute createAttributes() {
      return MonsterEntity.createMonsterAttributes().add(Attributes.MAX_HEALTH, 100.0D).add(Attributes.MOVEMENT_SPEED, 0.5D).add(Attributes.ATTACK_DAMAGE, 50.0D);
   }

   public float getWalkTargetValue(BlockPos pPos, IWorldReader pLevel) {
      return pLevel.getBrightness(pPos) - 0.5F;
   }
}