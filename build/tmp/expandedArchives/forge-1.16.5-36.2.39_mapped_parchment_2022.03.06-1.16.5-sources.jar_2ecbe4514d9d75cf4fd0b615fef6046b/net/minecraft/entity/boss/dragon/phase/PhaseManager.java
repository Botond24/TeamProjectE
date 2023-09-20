package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PhaseManager {
   private static final Logger LOGGER = LogManager.getLogger();
   private final EnderDragonEntity dragon;
   private final IPhase[] phases = new IPhase[PhaseType.getCount()];
   private IPhase currentPhase;

   public PhaseManager(EnderDragonEntity pDragon) {
      this.dragon = pDragon;
      this.setPhase(PhaseType.HOVERING);
   }

   public void setPhase(PhaseType<?> pPhase) {
      if (this.currentPhase == null || pPhase != this.currentPhase.getPhase()) {
         if (this.currentPhase != null) {
            this.currentPhase.end();
         }

         this.currentPhase = this.getPhase(pPhase);
         if (!this.dragon.level.isClientSide) {
            this.dragon.getEntityData().set(EnderDragonEntity.DATA_PHASE, pPhase.getId());
         }

         LOGGER.debug("Dragon is now in phase {} on the {}", pPhase, this.dragon.level.isClientSide ? "client" : "server");
         this.currentPhase.begin();
      }
   }

   public IPhase getCurrentPhase() {
      return this.currentPhase;
   }

   public <T extends IPhase> T getPhase(PhaseType<T> pPhase) {
      int i = pPhase.getId();
      if (this.phases[i] == null) {
         this.phases[i] = pPhase.createInstance(this.dragon);
      }

      return (T)this.phases[i];
   }
}