package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.PufferFishBigModel;
import net.minecraft.client.renderer.entity.model.PufferFishMediumModel;
import net.minecraft.client.renderer.entity.model.PufferFishSmallModel;
import net.minecraft.entity.passive.fish.PufferfishEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PufferfishRenderer extends MobRenderer<PufferfishEntity, EntityModel<PufferfishEntity>> {
   private static final ResourceLocation PUFFER_LOCATION = new ResourceLocation("textures/entity/fish/pufferfish.png");
   private int puffStateO;
   private final PufferFishSmallModel<PufferfishEntity> small = new PufferFishSmallModel<>();
   private final PufferFishMediumModel<PufferfishEntity> mid = new PufferFishMediumModel<>();
   private final PufferFishBigModel<PufferfishEntity> big = new PufferFishBigModel<>();

   public PufferfishRenderer(EntityRendererManager p_i48863_1_) {
      super(p_i48863_1_, new PufferFishBigModel<>(), 0.2F);
      this.puffStateO = 3;
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(PufferfishEntity pEntity) {
      return PUFFER_LOCATION;
   }

   public void render(PufferfishEntity pEntity, float pEntityYaw, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight) {
      int i = pEntity.getPuffState();
      if (i != this.puffStateO) {
         if (i == 0) {
            this.model = this.small;
         } else if (i == 1) {
            this.model = this.mid;
         } else {
            this.model = this.big;
         }
      }

      this.puffStateO = i;
      this.shadowRadius = 0.1F + 0.1F * (float)i;
      super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
   }

   protected void setupRotations(PufferfishEntity pEntityLiving, MatrixStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
      pMatrixStack.translate(0.0D, (double)(MathHelper.cos(pAgeInTicks * 0.05F) * 0.08F), 0.0D);
      super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
   }
}