package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.client.renderer.entity.model.WolfModel;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WolfRenderer extends MobRenderer<WolfEntity, WolfModel<WolfEntity>> {
   private static final ResourceLocation WOLF_LOCATION = new ResourceLocation("textures/entity/wolf/wolf.png");
   private static final ResourceLocation WOLF_TAME_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_tame.png");
   private static final ResourceLocation WOLF_ANGRY_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_angry.png");

   public WolfRenderer(EntityRendererManager p_i47187_1_) {
      super(p_i47187_1_, new WolfModel<>(), 0.5F);
      this.addLayer(new WolfCollarLayer(this));
   }

   /**
    * Defines what float the third param in setRotationAngles of ModelBase is
    */
   protected float getBob(WolfEntity pLivingBase, float pPartialTicks) {
      return pLivingBase.getTailAngle();
   }

   public void render(WolfEntity pEntity, float pEntityYaw, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight) {
      if (pEntity.isWet()) {
         float f = pEntity.getWetShade(pPartialTicks);
         this.model.setColor(f, f, f);
      }

      super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
      if (pEntity.isWet()) {
         this.model.setColor(1.0F, 1.0F, 1.0F);
      }

   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(WolfEntity pEntity) {
      if (pEntity.isTame()) {
         return WOLF_TAME_LOCATION;
      } else {
         return pEntity.isAngry() ? WOLF_ANGRY_LOCATION : WOLF_LOCATION;
      }
   }
}