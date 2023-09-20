package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.block.WoodType;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.item.DyeColor;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TrappedChestTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Atlases {
   public static final ResourceLocation SHULKER_SHEET = new ResourceLocation("textures/atlas/shulker_boxes.png");
   public static final ResourceLocation BED_SHEET = new ResourceLocation("textures/atlas/beds.png");
   public static final ResourceLocation BANNER_SHEET = new ResourceLocation("textures/atlas/banner_patterns.png");
   public static final ResourceLocation SHIELD_SHEET = new ResourceLocation("textures/atlas/shield_patterns.png");
   public static final ResourceLocation SIGN_SHEET = new ResourceLocation("textures/atlas/signs.png");
   public static final ResourceLocation CHEST_SHEET = new ResourceLocation("textures/atlas/chest.png");
   private static final RenderType SHULKER_BOX_SHEET_TYPE = RenderType.entityCutoutNoCull(SHULKER_SHEET);
   private static final RenderType BED_SHEET_TYPE = RenderType.entitySolid(BED_SHEET);
   private static final RenderType BANNER_SHEET_TYPE = RenderType.entityNoOutline(BANNER_SHEET);
   private static final RenderType SHIELD_SHEET_TYPE = RenderType.entityNoOutline(SHIELD_SHEET);
   private static final RenderType SIGN_SHEET_TYPE = RenderType.entityCutoutNoCull(SIGN_SHEET);
   private static final RenderType CHEST_SHEET_TYPE = RenderType.entityCutout(CHEST_SHEET);
   private static final RenderType SOLID_BLOCK_SHEET = RenderType.entitySolid(AtlasTexture.LOCATION_BLOCKS);
   private static final RenderType CUTOUT_BLOCK_SHEET = RenderType.entityCutout(AtlasTexture.LOCATION_BLOCKS);
   private static final RenderType TRANSLUCENT_ITEM_CULL_BLOCK_SHEET = RenderType.itemEntityTranslucentCull(AtlasTexture.LOCATION_BLOCKS);
   private static final RenderType TRANSLUCENT_CULL_BLOCK_SHEET = RenderType.entityTranslucentCull(AtlasTexture.LOCATION_BLOCKS);
   public static final RenderMaterial DEFAULT_SHULKER_TEXTURE_LOCATION = new RenderMaterial(SHULKER_SHEET, new ResourceLocation("entity/shulker/shulker"));
   public static final List<RenderMaterial> SHULKER_TEXTURE_LOCATION = Stream.of("white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black").map((p_228777_0_) -> {
      return new RenderMaterial(SHULKER_SHEET, new ResourceLocation("entity/shulker/shulker_" + p_228777_0_));
   }).collect(ImmutableList.toImmutableList());
   public static final Map<WoodType, RenderMaterial> SIGN_MATERIALS = WoodType.values().collect(Collectors.toMap(Function.identity(), Atlases::signTexture));
   public static final RenderMaterial[] BED_TEXTURES = Arrays.stream(DyeColor.values()).sorted(Comparator.comparingInt(DyeColor::getId)).map((p_228770_0_) -> {
      return new RenderMaterial(BED_SHEET, new ResourceLocation("entity/bed/" + p_228770_0_.getName()));
   }).toArray((p_228769_0_) -> {
      return new RenderMaterial[p_228769_0_];
   });
   public static final RenderMaterial CHEST_TRAP_LOCATION = chestMaterial("trapped");
   public static final RenderMaterial CHEST_TRAP_LOCATION_LEFT = chestMaterial("trapped_left");
   public static final RenderMaterial CHEST_TRAP_LOCATION_RIGHT = chestMaterial("trapped_right");
   public static final RenderMaterial CHEST_XMAS_LOCATION = chestMaterial("christmas");
   public static final RenderMaterial CHEST_XMAS_LOCATION_LEFT = chestMaterial("christmas_left");
   public static final RenderMaterial CHEST_XMAS_LOCATION_RIGHT = chestMaterial("christmas_right");
   public static final RenderMaterial CHEST_LOCATION = chestMaterial("normal");
   public static final RenderMaterial CHEST_LOCATION_LEFT = chestMaterial("normal_left");
   public static final RenderMaterial CHEST_LOCATION_RIGHT = chestMaterial("normal_right");
   public static final RenderMaterial ENDER_CHEST_LOCATION = chestMaterial("ender");

   public static RenderType bannerSheet() {
      return BANNER_SHEET_TYPE;
   }

   public static RenderType shieldSheet() {
      return SHIELD_SHEET_TYPE;
   }

   public static RenderType bedSheet() {
      return BED_SHEET_TYPE;
   }

   public static RenderType shulkerBoxSheet() {
      return SHULKER_BOX_SHEET_TYPE;
   }

   public static RenderType signSheet() {
      return SIGN_SHEET_TYPE;
   }

   public static RenderType chestSheet() {
      return CHEST_SHEET_TYPE;
   }

   public static RenderType solidBlockSheet() {
      return SOLID_BLOCK_SHEET;
   }

   public static RenderType cutoutBlockSheet() {
      return CUTOUT_BLOCK_SHEET;
   }

   public static RenderType translucentItemSheet() {
      return TRANSLUCENT_ITEM_CULL_BLOCK_SHEET;
   }

   public static RenderType translucentCullBlockSheet() {
      return TRANSLUCENT_CULL_BLOCK_SHEET;
   }

   public static void getAllMaterials(Consumer<RenderMaterial> pMaterialConsumer) {
      pMaterialConsumer.accept(DEFAULT_SHULKER_TEXTURE_LOCATION);
      SHULKER_TEXTURE_LOCATION.forEach(pMaterialConsumer);

      for(BannerPattern bannerpattern : BannerPattern.values()) {
         pMaterialConsumer.accept(new RenderMaterial(BANNER_SHEET, bannerpattern.location(true)));
         pMaterialConsumer.accept(new RenderMaterial(SHIELD_SHEET, bannerpattern.location(false)));
      }

      SIGN_MATERIALS.values().forEach(pMaterialConsumer);

      for(RenderMaterial rendermaterial : BED_TEXTURES) {
         pMaterialConsumer.accept(rendermaterial);
      }

      pMaterialConsumer.accept(CHEST_TRAP_LOCATION);
      pMaterialConsumer.accept(CHEST_TRAP_LOCATION_LEFT);
      pMaterialConsumer.accept(CHEST_TRAP_LOCATION_RIGHT);
      pMaterialConsumer.accept(CHEST_XMAS_LOCATION);
      pMaterialConsumer.accept(CHEST_XMAS_LOCATION_LEFT);
      pMaterialConsumer.accept(CHEST_XMAS_LOCATION_RIGHT);
      pMaterialConsumer.accept(CHEST_LOCATION);
      pMaterialConsumer.accept(CHEST_LOCATION_LEFT);
      pMaterialConsumer.accept(CHEST_LOCATION_RIGHT);
      pMaterialConsumer.accept(ENDER_CHEST_LOCATION);
   }

   public static RenderMaterial signTexture(WoodType p_228773_0_) {
      ResourceLocation location = new ResourceLocation(p_228773_0_.name());
      return new RenderMaterial(SIGN_SHEET, new ResourceLocation(location.getNamespace(), "entity/signs/" + location.getPath()));
   }

   private static RenderMaterial chestMaterial(String pChestName) {
      return new RenderMaterial(CHEST_SHEET, new ResourceLocation("entity/chest/" + pChestName));
   }

   public static RenderMaterial chooseMaterial(TileEntity pBlockEntity, ChestType pChestType, boolean pHoliday) {
      if (pHoliday) {
         return chooseMaterial(pChestType, CHEST_XMAS_LOCATION, CHEST_XMAS_LOCATION_LEFT, CHEST_XMAS_LOCATION_RIGHT);
      } else if (pBlockEntity instanceof TrappedChestTileEntity) {
         return chooseMaterial(pChestType, CHEST_TRAP_LOCATION, CHEST_TRAP_LOCATION_LEFT, CHEST_TRAP_LOCATION_RIGHT);
      } else {
         return pBlockEntity instanceof EnderChestTileEntity ? ENDER_CHEST_LOCATION : chooseMaterial(pChestType, CHEST_LOCATION, CHEST_LOCATION_LEFT, CHEST_LOCATION_RIGHT);
      }
   }

   private static RenderMaterial chooseMaterial(ChestType pChestType, RenderMaterial pDoubleMaterial, RenderMaterial pLeftMaterial, RenderMaterial pRightMaterial) {
      switch(pChestType) {
      case LEFT:
         return pLeftMaterial;
      case RIGHT:
         return pRightMaterial;
      case SINGLE:
      default:
         return pDoubleMaterial;
      }
   }

   /**
    * Not threadsafe. Enqueue it in client setup.
    */
   public static void addWoodType(WoodType woodType) {
      SIGN_MATERIALS.put(woodType, signTexture(woodType));
   }
}
