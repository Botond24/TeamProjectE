package net.minecraft.client.renderer.entity.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LeashKnotModel<T extends Entity> extends SegmentedModel<T> {
   private final ModelRenderer knot;

   public LeashKnotModel() {
      this.texWidth = 32;
      this.texHeight = 32;
      this.knot = new ModelRenderer(this, 0, 0);
      this.knot.addBox(-3.0F, -6.0F, -3.0F, 6.0F, 8.0F, 6.0F, 0.0F);
      this.knot.setPos(0.0F, 0.0F, 0.0F);
   }

   public Iterable<ModelRenderer> parts() {
      return ImmutableList.of(this.knot);
   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      this.knot.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
      this.knot.xRot = pHeadPitch * ((float)Math.PI / 180F);
   }
}