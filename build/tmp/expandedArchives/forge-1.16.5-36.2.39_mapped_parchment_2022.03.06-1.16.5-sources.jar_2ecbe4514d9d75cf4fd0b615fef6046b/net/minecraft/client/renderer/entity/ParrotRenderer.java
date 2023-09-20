package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.entity.model.ParrotModel;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParrotRenderer extends MobRenderer<ParrotEntity, ParrotModel> {
   public static final ResourceLocation[] PARROT_LOCATIONS = new ResourceLocation[]{new ResourceLocation("textures/entity/parrot/parrot_red_blue.png"), new ResourceLocation("textures/entity/parrot/parrot_blue.png"), new ResourceLocation("textures/entity/parrot/parrot_green.png"), new ResourceLocation("textures/entity/parrot/parrot_yellow_blue.png"), new ResourceLocation("textures/entity/parrot/parrot_grey.png")};

   public ParrotRenderer(EntityRendererManager p_i47375_1_) {
      super(p_i47375_1_, new ParrotModel(), 0.3F);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(ParrotEntity pEntity) {
      return PARROT_LOCATIONS[pEntity.getVariant()];
   }

   /**
    * Defines what float the third param in setRotationAngles of ModelBase is
    */
   public float getBob(ParrotEntity pLivingBase, float pPartialTicks) {
      float f = MathHelper.lerp(pPartialTicks, pLivingBase.oFlap, pLivingBase.flap);
      float f1 = MathHelper.lerp(pPartialTicks, pLivingBase.oFlapSpeed, pLivingBase.flapSpeed);
      return (MathHelper.sin(f) + 1.0F) * f1;
   }
}