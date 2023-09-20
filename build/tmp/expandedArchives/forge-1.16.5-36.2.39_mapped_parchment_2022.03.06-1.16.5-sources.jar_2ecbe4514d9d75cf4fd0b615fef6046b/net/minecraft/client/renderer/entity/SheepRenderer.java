package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.entity.layers.SheepWoolLayer;
import net.minecraft.client.renderer.entity.model.SheepModel;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SheepRenderer extends MobRenderer<SheepEntity, SheepModel<SheepEntity>> {
   private static final ResourceLocation SHEEP_LOCATION = new ResourceLocation("textures/entity/sheep/sheep.png");

   public SheepRenderer(EntityRendererManager p_i47195_1_) {
      super(p_i47195_1_, new SheepModel<>(), 0.7F);
      this.addLayer(new SheepWoolLayer(this));
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(SheepEntity pEntity) {
      return SHEEP_LOCATION;
   }
}