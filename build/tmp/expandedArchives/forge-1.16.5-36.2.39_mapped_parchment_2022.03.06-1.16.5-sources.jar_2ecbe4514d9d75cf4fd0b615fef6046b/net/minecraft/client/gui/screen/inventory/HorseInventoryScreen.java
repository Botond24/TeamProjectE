package net.minecraft.client.gui.screen.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.entity.passive.horse.AbstractChestedHorseEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.HorseInventoryContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HorseInventoryScreen extends ContainerScreen<HorseInventoryContainer> {
   private static final ResourceLocation HORSE_INVENTORY_LOCATION = new ResourceLocation("textures/gui/container/horse.png");
   /** The EntityHorse whose inventory is currently being accessed. */
   private final AbstractHorseEntity horse;
   /** The mouse x-position recorded during the last rendered frame. */
   private float xMouse;
   /** The mouse y-position recorded during the last renderered frame. */
   private float yMouse;

   public HorseInventoryScreen(HorseInventoryContainer pHorseInventoryMenu, PlayerInventory pPlayerInventory, AbstractHorseEntity pHorse) {
      super(pHorseInventoryMenu, pPlayerInventory, pHorse.getDisplayName());
      this.horse = pHorse;
      this.passEvents = false;
   }

   protected void renderBg(MatrixStack pMatrixStack, float pPartialTicks, int pX, int pY) {
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.minecraft.getTextureManager().bind(HORSE_INVENTORY_LOCATION);
      int i = (this.width - this.imageWidth) / 2;
      int j = (this.height - this.imageHeight) / 2;
      this.blit(pMatrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
      if (this.horse instanceof AbstractChestedHorseEntity) {
         AbstractChestedHorseEntity abstractchestedhorseentity = (AbstractChestedHorseEntity)this.horse;
         if (abstractchestedhorseentity.hasChest()) {
            this.blit(pMatrixStack, i + 79, j + 17, 0, this.imageHeight, abstractchestedhorseentity.getInventoryColumns() * 18, 54);
         }
      }

      if (this.horse.isSaddleable()) {
         this.blit(pMatrixStack, i + 7, j + 35 - 18, 18, this.imageHeight + 54, 18, 18);
      }

      if (this.horse.canWearArmor()) {
         if (this.horse instanceof LlamaEntity) {
            this.blit(pMatrixStack, i + 7, j + 35, 36, this.imageHeight + 54, 18, 18);
         } else {
            this.blit(pMatrixStack, i + 7, j + 35, 0, this.imageHeight + 54, 18, 18);
         }
      }

      InventoryScreen.renderEntityInInventory(i + 51, j + 60, 17, (float)(i + 51) - this.xMouse, (float)(j + 75 - 50) - this.yMouse, this.horse);
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.renderBackground(pMatrixStack);
      this.xMouse = (float)pMouseX;
      this.yMouse = (float)pMouseY;
      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      this.renderTooltip(pMatrixStack, pMouseX, pMouseY);
   }
}