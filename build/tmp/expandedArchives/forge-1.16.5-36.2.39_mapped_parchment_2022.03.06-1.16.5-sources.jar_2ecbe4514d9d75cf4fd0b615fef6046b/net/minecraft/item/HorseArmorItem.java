package net.minecraft.item;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class HorseArmorItem extends Item {
   private final int protection;
   private final ResourceLocation texture;

   /**
    * 
    * @param pProtection the given protection level of the {@code HorseArmorItem}
    * @param pIdentifier the texture path identifier for the {@code DyeableHorseArmorItem}, {@link
    * net.minecraft.world.item.HorseArmorItem}
    * @param pProperties the item properties
    */
   public HorseArmorItem(int pProtection, String pIdentifier, Item.Properties pProperties) {
      this(pProtection, new ResourceLocation("textures/entity/horse/armor/horse_armor_" + pIdentifier + ".png"), pProperties);
   }

   /**
    * 
    * @param pProtection the given protection level of the {@code HorseArmorItem}
    * @param pIdentifier the texture path identifier for the {@code DyeableHorseArmorItem}, {@link
    * net.minecraft.world.item.HorseArmorItem}
    * @param pProperties the item properties
    */
   public HorseArmorItem(int pProtection, ResourceLocation texture, Item.Properties pProperties) {
      super(pProperties);
      this.protection = pProtection;
      this.texture = texture;
   }

   @OnlyIn(Dist.CLIENT)
   public ResourceLocation getTexture() {
      return texture;
   }

   public int getProtection() {
      return this.protection;
   }
}
