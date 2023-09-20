package net.minecraft.entity.ai.brain.sensor;

import java.util.Random;
import java.util.Set;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.server.ServerWorld;

public abstract class Sensor<E extends LivingEntity> {
   private static final Random RANDOM = new Random();
   private static final EntityPredicate TARGET_CONDITIONS = (new EntityPredicate()).range(16.0D).allowSameTeam().allowNonAttackable();
   private static final EntityPredicate TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING = (new EntityPredicate()).range(16.0D).allowSameTeam().allowNonAttackable().ignoreInvisibilityTesting();
   private final int scanRate;
   private long timeToTick;

   public Sensor(int p_i50301_1_) {
      this.scanRate = p_i50301_1_;
      this.timeToTick = (long)RANDOM.nextInt(p_i50301_1_);
   }

   public Sensor() {
      this(20);
   }

   public final void tick(ServerWorld pLevel, E pEntity) {
      if (--this.timeToTick <= 0L) {
         this.timeToTick = (long)this.scanRate;
         this.doTick(pLevel, pEntity);
      }

   }

   protected abstract void doTick(ServerWorld pLevel, E pEntity);

   public abstract Set<MemoryModuleType<?>> requires();

   protected static boolean isEntityTargetable(LivingEntity pLivingEntity, LivingEntity pTarget) {
      return pLivingEntity.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, pTarget) ? TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.test(pLivingEntity, pTarget) : TARGET_CONDITIONS.test(pLivingEntity, pTarget);
   }
}