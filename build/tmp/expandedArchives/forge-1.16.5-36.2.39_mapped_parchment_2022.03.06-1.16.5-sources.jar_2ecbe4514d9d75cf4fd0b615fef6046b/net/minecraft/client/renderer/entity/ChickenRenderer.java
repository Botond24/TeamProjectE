package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.entity.model.ChickenModel;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChickenRenderer extends MobRenderer<ChickenEntity, ChickenModel<ChickenEntity>> {
   private static final ResourceLocation CHICKEN_LOCATION = new ResourceLocation("textures/entity/chicken.png");

   public ChickenRenderer(EntityRendererManager p_i47211_1_) {
      super(p_i47211_1_, new ChickenModel<>(), 0.3F);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(ChickenEntity pEntity) {
      return CHICKEN_LOCATION;
   }

   /**
    * Defines what float the third param in setRotationAngles of ModelBase is
    */
   protected float getBob(ChickenEntity pLivingBase, float pPartialTicks) {
      float f = MathHelper.lerp(pPartialTicks, pLivingBase.oFlap, pLivingBase.flap);
      float f1 = MathHelper.lerp(pPartialTicks, pLivingBase.oFlapSpeed, pLivingBase.flapSpeed);
      return (MathHelper.sin(f) + 1.0F) * f1;
   }
}