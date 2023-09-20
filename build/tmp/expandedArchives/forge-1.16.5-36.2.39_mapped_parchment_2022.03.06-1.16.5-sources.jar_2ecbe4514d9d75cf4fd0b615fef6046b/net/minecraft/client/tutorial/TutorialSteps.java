package net.minecraft.client.tutorial;

import java.util.function.Function;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum TutorialSteps {
   MOVEMENT("movement", MovementStep::new),
   FIND_TREE("find_tree", FindTreeStep::new),
   PUNCH_TREE("punch_tree", PunchTreeStep::new),
   OPEN_INVENTORY("open_inventory", OpenInventoryStep::new),
   CRAFT_PLANKS("craft_planks", CraftPlanksStep::new),
   NONE("none", CompletedTutorialStep::new);

   private final String name;
   private final Function<Tutorial, ? extends ITutorialStep> constructor;

   private <T extends ITutorialStep> TutorialSteps(String pName, Function<Tutorial, T> pConstructor) {
      this.name = pName;
      this.constructor = pConstructor;
   }

   public ITutorialStep create(Tutorial pTutorial) {
      return this.constructor.apply(pTutorial);
   }

   public String getName() {
      return this.name;
   }

   public static TutorialSteps getByName(String pName) {
      for(TutorialSteps tutorialsteps : values()) {
         if (tutorialsteps.name.equals(pName)) {
            return tutorialsteps;
         }
      }

      return NONE;
   }
}