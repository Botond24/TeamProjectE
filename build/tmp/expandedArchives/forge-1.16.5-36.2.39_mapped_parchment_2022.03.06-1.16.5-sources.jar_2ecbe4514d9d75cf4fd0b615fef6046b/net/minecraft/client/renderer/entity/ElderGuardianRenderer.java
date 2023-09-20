package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.monster.ElderGuardianEntity;
import net.minecraft.entity.monster.GuardianEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ElderGuardianRenderer extends GuardianRenderer {
   public static final ResourceLocation GUARDIAN_ELDER_LOCATION = new ResourceLocation("textures/entity/guardian_elder.png");

   public ElderGuardianRenderer(EntityRendererManager p_i47209_1_) {
      super(p_i47209_1_, 1.2F);
   }

   protected void scale(GuardianEntity pLivingEntity, MatrixStack pMatrixStack, float pPartialTickTime) {
      pMatrixStack.scale(ElderGuardianEntity.ELDER_SIZE_SCALE, ElderGuardianEntity.ELDER_SIZE_SCALE, ElderGuardianEntity.ELDER_SIZE_SCALE);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(GuardianEntity pEntity) {
      return GUARDIAN_ELDER_LOCATION;
   }
}