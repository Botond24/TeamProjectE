package net.minecraft.client.gui;

import com.google.common.collect.Ordering;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collection;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class DisplayEffectsScreen<T extends Container> extends ContainerScreen<T> {
   /** True if there is some potion effect to display */
   protected boolean doRenderEffects;

   public DisplayEffectsScreen(T p_i51091_1_, PlayerInventory p_i51091_2_, ITextComponent p_i51091_3_) {
      super(p_i51091_1_, p_i51091_2_, p_i51091_3_);
   }

   protected void init() {
      super.init();
      this.checkEffectRendering();
   }

   protected void checkEffectRendering() {
      if (this.minecraft.player.getActiveEffects().isEmpty()) {
         this.leftPos = (this.width - this.imageWidth) / 2;
         this.doRenderEffects = false;
      } else {
         if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.PotionShiftEvent(this)))
            this.leftPos = (this.width - this.imageWidth) / 2;
         else
         this.leftPos = 160 + (this.width - this.imageWidth - 200) / 2;
         this.doRenderEffects = true;
      }

   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      if (this.doRenderEffects) {
         this.renderEffects(pMatrixStack);
      }

   }

   private void renderEffects(MatrixStack pPoseStack) {
      int i = this.leftPos - 124;
      Collection<EffectInstance> collection = this.minecraft.player.getActiveEffects();
      if (!collection.isEmpty()) {
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         int j = 33;
         if (collection.size() > 5) {
            j = 132 / (collection.size() - 1);
         }

         Iterable<EffectInstance> iterable = collection.stream().filter(effectInstance -> effectInstance.shouldRender()).sorted().collect(java.util.stream.Collectors.toList());
         this.renderBackgrounds(pPoseStack, i, j, iterable);
         this.renderIcons(pPoseStack, i, j, iterable);
         this.renderLabels(pPoseStack, i, j, iterable);
      }
   }

   private void renderBackgrounds(MatrixStack pPoseStack, int pRenderX, int pYOffset, Iterable<EffectInstance> pEffects) {
      this.minecraft.getTextureManager().bind(INVENTORY_LOCATION);
      int i = this.topPos;

      for(EffectInstance effectinstance : pEffects) {
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         this.blit(pPoseStack, pRenderX, i, 0, 166, 140, 32);
         i += pYOffset;
      }

   }

   private void renderIcons(MatrixStack pPoseStack, int pRenderX, int pYOffset, Iterable<EffectInstance> pEffects) {
      PotionSpriteUploader potionspriteuploader = this.minecraft.getMobEffectTextures();
      int i = this.topPos;

      for(EffectInstance effectinstance : pEffects) {
         Effect effect = effectinstance.getEffect();
         TextureAtlasSprite textureatlassprite = potionspriteuploader.get(effect);
         this.minecraft.getTextureManager().bind(textureatlassprite.atlas().location());
         blit(pPoseStack, pRenderX + 6, i + 7, this.getBlitOffset(), 18, 18, textureatlassprite);
         i += pYOffset;
      }

   }

   private void renderLabels(MatrixStack pPoseStack, int pRenderX, int pYOffset, Iterable<EffectInstance> pEffects) {
      int i = this.topPos;

      for(EffectInstance effectinstance : pEffects) {
         effectinstance.renderInventoryEffect(this, pPoseStack, pRenderX, i, this.getBlitOffset());
         if (!effectinstance.shouldRenderInvText()) {
            i += pYOffset;
            continue;
         }
         String s = I18n.get(effectinstance.getEffect().getDescriptionId());
         if (effectinstance.getAmplifier() >= 1 && effectinstance.getAmplifier() <= 9) {
            s = s + ' ' + I18n.get("enchantment.level." + (effectinstance.getAmplifier() + 1));
         }

         this.font.drawShadow(pPoseStack, s, (float)(pRenderX + 10 + 18), (float)(i + 6), 16777215);
         String s1 = EffectUtils.formatDuration(effectinstance, 1.0F);
         this.font.drawShadow(pPoseStack, s1, (float)(pRenderX + 10 + 18), (float)(i + 6 + 10), 8355711);
         i += pYOffset;
      }

   }
}
