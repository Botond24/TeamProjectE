package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.SheepModel;
import net.minecraft.client.renderer.entity.model.SheepWoolModel;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SheepWoolLayer extends LayerRenderer<SheepEntity, SheepModel<SheepEntity>> {
   private static final ResourceLocation SHEEP_FUR_LOCATION = new ResourceLocation("textures/entity/sheep/sheep_fur.png");
   private final SheepWoolModel<SheepEntity> model = new SheepWoolModel<>();

   public SheepWoolLayer(IEntityRenderer<SheepEntity, SheepModel<SheepEntity>> p_i50925_1_) {
      super(p_i50925_1_);
   }

   public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight, SheepEntity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      if (!pLivingEntity.isSheared() && !pLivingEntity.isInvisible()) {
         float f;
         float f1;
         float f2;
         if (pLivingEntity.hasCustomName() && "jeb_".equals(pLivingEntity.getName().getContents())) {
            int i1 = 25;
            int i = pLivingEntity.tickCount / 25 + pLivingEntity.getId();
            int j = DyeColor.values().length;
            int k = i % j;
            int l = (i + 1) % j;
            float f3 = ((float)(pLivingEntity.tickCount % 25) + pPartialTicks) / 25.0F;
            float[] afloat1 = SheepEntity.getColorArray(DyeColor.byId(k));
            float[] afloat2 = SheepEntity.getColorArray(DyeColor.byId(l));
            f = afloat1[0] * (1.0F - f3) + afloat2[0] * f3;
            f1 = afloat1[1] * (1.0F - f3) + afloat2[1] * f3;
            f2 = afloat1[2] * (1.0F - f3) + afloat2[2] * f3;
         } else {
            float[] afloat = SheepEntity.getColorArray(pLivingEntity.getColor());
            f = afloat[0];
            f1 = afloat[1];
            f2 = afloat[2];
         }

         coloredCutoutModelCopyLayerRender(this.getParentModel(), this.model, SHEEP_FUR_LOCATION, pMatrixStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch, pPartialTicks, f, f1, f2);
      }
   }
}