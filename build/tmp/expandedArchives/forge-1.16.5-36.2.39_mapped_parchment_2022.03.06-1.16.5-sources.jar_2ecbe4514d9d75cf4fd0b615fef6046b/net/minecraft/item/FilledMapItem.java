package net.minecraft.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FilledMapItem extends AbstractMapItem {
   public FilledMapItem(Item.Properties p_i48482_1_) {
      super(p_i48482_1_);
   }

   public static ItemStack create(World pLevel, int pLevelX, int pLevelZ, byte pScale, boolean pTrackingPosition, boolean pUnlimitedTracking) {
      ItemStack itemstack = new ItemStack(Items.FILLED_MAP);
      createAndStoreSavedData(itemstack, pLevel, pLevelX, pLevelZ, pScale, pTrackingPosition, pUnlimitedTracking, pLevel.dimension());
      return itemstack;
   }

   @Nullable
   public static MapData getSavedData(ItemStack pStack, World pLevel) {
      return pLevel.getMapData(makeKey(getMapId(pStack)));
   }

   @Nullable
   public static MapData getOrCreateSavedData(ItemStack p_195950_0_, World p_195950_1_) {
      // FORGE: Add instance method for mods to override
      Item map = p_195950_0_.getItem();
      if (map instanceof FilledMapItem) {
        return ((FilledMapItem)map).getCustomMapData(p_195950_0_, p_195950_1_);
      }
      return null;
   }

   @Nullable
   protected MapData getCustomMapData(ItemStack p_195950_0_, World p_195950_1_) {
      MapData mapdata = getSavedData(p_195950_0_, p_195950_1_);
      if (mapdata == null && p_195950_1_ instanceof ServerWorld) {
         mapdata = createAndStoreSavedData(p_195950_0_, p_195950_1_, p_195950_1_.getLevelData().getXSpawn(), p_195950_1_.getLevelData().getZSpawn(), 3, false, false, p_195950_1_.dimension());
      }

      return mapdata;
   }

   public static int getMapId(ItemStack p_195949_0_) {
      CompoundNBT compoundnbt = p_195949_0_.getTag();
      return compoundnbt != null && compoundnbt.contains("map", 99) ? compoundnbt.getInt("map") : 0;
   }

   private static MapData createAndStoreSavedData(ItemStack p_195951_0_, World p_195951_1_, int p_195951_2_, int p_195951_3_, int p_195951_4_, boolean p_195951_5_, boolean p_195951_6_, RegistryKey<World> p_195951_7_) {
      int i = p_195951_1_.getFreeMapId();
      MapData mapdata = new MapData(makeKey(i));
      mapdata.setProperties(p_195951_2_, p_195951_3_, p_195951_4_, p_195951_5_, p_195951_6_, p_195951_7_);
      p_195951_1_.setMapData(mapdata);
      p_195951_0_.getOrCreateTag().putInt("map", i);
      return mapdata;
   }

   public static String makeKey(int pMapId) {
      return "map_" + pMapId;
   }

   public void update(World pLevel, Entity pViewer, MapData pData) {
      if (pLevel.dimension() == pData.dimension && pViewer instanceof PlayerEntity) {
         int i = 1 << pData.scale;
         int j = pData.x;
         int k = pData.z;
         int l = MathHelper.floor(pViewer.getX() - (double)j) / i + 64;
         int i1 = MathHelper.floor(pViewer.getZ() - (double)k) / i + 64;
         int j1 = 128 / i;
         if (pLevel.dimensionType().hasCeiling()) {
            j1 /= 2;
         }

         MapData.MapInfo mapdata$mapinfo = pData.getHoldingPlayer((PlayerEntity)pViewer);
         ++mapdata$mapinfo.step;
         boolean flag = false;

         for(int k1 = l - j1 + 1; k1 < l + j1; ++k1) {
            if ((k1 & 15) == (mapdata$mapinfo.step & 15) || flag) {
               flag = false;
               double d0 = 0.0D;

               for(int l1 = i1 - j1 - 1; l1 < i1 + j1; ++l1) {
                  if (k1 >= 0 && l1 >= -1 && k1 < 128 && l1 < 128) {
                     int i2 = k1 - l;
                     int j2 = l1 - i1;
                     boolean flag1 = i2 * i2 + j2 * j2 > (j1 - 2) * (j1 - 2);
                     int k2 = (j / i + k1 - 64) * i;
                     int l2 = (k / i + l1 - 64) * i;
                     Multiset<MaterialColor> multiset = LinkedHashMultiset.create();
                     Chunk chunk = pLevel.getChunkAt(new BlockPos(k2, 0, l2));
                     if (!chunk.isEmpty()) {
                        ChunkPos chunkpos = chunk.getPos();
                        int i3 = k2 & 15;
                        int j3 = l2 & 15;
                        int k3 = 0;
                        double d1 = 0.0D;
                        if (pLevel.dimensionType().hasCeiling()) {
                           int l3 = k2 + l2 * 231871;
                           l3 = l3 * l3 * 31287121 + l3 * 11;
                           if ((l3 >> 20 & 1) == 0) {
                              multiset.add(Blocks.DIRT.defaultBlockState().getMapColor(pLevel, BlockPos.ZERO), 10);
                           } else {
                              multiset.add(Blocks.STONE.defaultBlockState().getMapColor(pLevel, BlockPos.ZERO), 100);
                           }

                           d1 = 100.0D;
                        } else {
                           BlockPos.Mutable blockpos$mutable1 = new BlockPos.Mutable();
                           BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

                           for(int i4 = 0; i4 < i; ++i4) {
                              for(int j4 = 0; j4 < i; ++j4) {
                                 int k4 = chunk.getHeight(Heightmap.Type.WORLD_SURFACE, i4 + i3, j4 + j3) + 1;
                                 BlockState blockstate;
                                 if (k4 <= 1) {
                                    blockstate = Blocks.BEDROCK.defaultBlockState();
                                 } else {
                                    do {
                                       --k4;
                                       blockpos$mutable1.set(chunkpos.getMinBlockX() + i4 + i3, k4, chunkpos.getMinBlockZ() + j4 + j3);
                                       blockstate = chunk.getBlockState(blockpos$mutable1);
                                    } while(blockstate.getMapColor(pLevel, blockpos$mutable1) == MaterialColor.NONE && k4 > 0);

                                    if (k4 > 0 && !blockstate.getFluidState().isEmpty()) {
                                       int l4 = k4 - 1;
                                       blockpos$mutable.set(blockpos$mutable1);

                                       BlockState blockstate1;
                                       do {
                                          blockpos$mutable.setY(l4--);
                                          blockstate1 = chunk.getBlockState(blockpos$mutable);
                                          ++k3;
                                       } while(l4 > 0 && !blockstate1.getFluidState().isEmpty());

                                       blockstate = this.getCorrectStateForFluidBlock(pLevel, blockstate, blockpos$mutable1);
                                    }
                                 }

                                 pData.checkBanners(pLevel, chunkpos.getMinBlockX() + i4 + i3, chunkpos.getMinBlockZ() + j4 + j3);
                                 d1 += (double)k4 / (double)(i * i);
                                 multiset.add(blockstate.getMapColor(pLevel, blockpos$mutable1));
                              }
                           }
                        }

                        k3 = k3 / (i * i);
                        double d2 = (d1 - d0) * 4.0D / (double)(i + 4) + ((double)(k1 + l1 & 1) - 0.5D) * 0.4D;
                        int i5 = 1;
                        if (d2 > 0.6D) {
                           i5 = 2;
                        }

                        if (d2 < -0.6D) {
                           i5 = 0;
                        }

                        MaterialColor materialcolor = Iterables.getFirst(Multisets.copyHighestCountFirst(multiset), MaterialColor.NONE);
                        if (materialcolor == MaterialColor.WATER) {
                           d2 = (double)k3 * 0.1D + (double)(k1 + l1 & 1) * 0.2D;
                           i5 = 1;
                           if (d2 < 0.5D) {
                              i5 = 2;
                           }

                           if (d2 > 0.9D) {
                              i5 = 0;
                           }
                        }

                        d0 = d1;
                        if (l1 >= 0 && i2 * i2 + j2 * j2 < j1 * j1 && (!flag1 || (k1 + l1 & 1) != 0)) {
                           byte b0 = pData.colors[k1 + l1 * 128];
                           byte b1 = (byte)(materialcolor.id * 4 + i5);
                           if (b0 != b1) {
                              pData.colors[k1 + l1 * 128] = b1;
                              pData.setDirty(k1, l1);
                              flag = true;
                           }
                        }
                     }
                  }
               }
            }
         }

      }
   }

   private BlockState getCorrectStateForFluidBlock(World pLevel, BlockState pState, BlockPos pPos) {
      FluidState fluidstate = pState.getFluidState();
      return !fluidstate.isEmpty() && !pState.isFaceSturdy(pLevel, pPos, Direction.UP) ? fluidstate.createLegacyBlock() : pState;
   }

   private static boolean isLand(Biome[] pBiomes, int pScale, int pX, int pZ) {
      return pBiomes[pX * pScale + pZ * pScale * 128 * pScale].getDepth() >= 0.0F;
   }

   public static void renderBiomePreviewMap(ServerWorld pServerLevel, ItemStack pStack) {
      MapData mapdata = getOrCreateSavedData(pStack, pServerLevel);
      if (mapdata != null) {
         if (pServerLevel.dimension() == mapdata.dimension) {
            int i = 1 << mapdata.scale;
            int j = mapdata.x;
            int k = mapdata.z;
            Biome[] abiome = new Biome[128 * i * 128 * i];

            for(int l = 0; l < 128 * i; ++l) {
               for(int i1 = 0; i1 < 128 * i; ++i1) {
                  abiome[l * 128 * i + i1] = pServerLevel.getBiome(new BlockPos((j / i - 64) * i + i1, 0, (k / i - 64) * i + l));
               }
            }

            for(int l1 = 0; l1 < 128; ++l1) {
               for(int i2 = 0; i2 < 128; ++i2) {
                  if (l1 > 0 && i2 > 0 && l1 < 127 && i2 < 127) {
                     Biome biome = abiome[l1 * i + i2 * i * 128 * i];
                     int j1 = 8;
                     if (isLand(abiome, i, l1 - 1, i2 - 1)) {
                        --j1;
                     }

                     if (isLand(abiome, i, l1 - 1, i2 + 1)) {
                        --j1;
                     }

                     if (isLand(abiome, i, l1 - 1, i2)) {
                        --j1;
                     }

                     if (isLand(abiome, i, l1 + 1, i2 - 1)) {
                        --j1;
                     }

                     if (isLand(abiome, i, l1 + 1, i2 + 1)) {
                        --j1;
                     }

                     if (isLand(abiome, i, l1 + 1, i2)) {
                        --j1;
                     }

                     if (isLand(abiome, i, l1, i2 - 1)) {
                        --j1;
                     }

                     if (isLand(abiome, i, l1, i2 + 1)) {
                        --j1;
                     }

                     int k1 = 3;
                     MaterialColor materialcolor = MaterialColor.NONE;
                     if (biome.getDepth() < 0.0F) {
                        materialcolor = MaterialColor.COLOR_ORANGE;
                        if (j1 > 7 && i2 % 2 == 0) {
                           k1 = (l1 + (int)(MathHelper.sin((float)i2 + 0.0F) * 7.0F)) / 8 % 5;
                           if (k1 == 3) {
                              k1 = 1;
                           } else if (k1 == 4) {
                              k1 = 0;
                           }
                        } else if (j1 > 7) {
                           materialcolor = MaterialColor.NONE;
                        } else if (j1 > 5) {
                           k1 = 1;
                        } else if (j1 > 3) {
                           k1 = 0;
                        } else if (j1 > 1) {
                           k1 = 0;
                        }
                     } else if (j1 > 0) {
                        materialcolor = MaterialColor.COLOR_BROWN;
                        if (j1 > 3) {
                           k1 = 1;
                        } else {
                           k1 = 3;
                        }
                     }

                     if (materialcolor != MaterialColor.NONE) {
                        mapdata.colors[l1 + i2 * 128] = (byte)(materialcolor.id * 4 + k1);
                        mapdata.setDirty(l1, i2);
                     }
                  }
               }
            }

         }
      }
   }

   /**
    * Called each tick as long the item is on a player inventory. Uses by maps to check if is on a player hand and
    * update it's contents.
    */
   public void inventoryTick(ItemStack pStack, World pLevel, Entity pEntity, int pItemSlot, boolean pIsSelected) {
      if (!pLevel.isClientSide) {
         MapData mapdata = getOrCreateSavedData(pStack, pLevel);
         if (mapdata != null) {
            if (pEntity instanceof PlayerEntity) {
               PlayerEntity playerentity = (PlayerEntity)pEntity;
               mapdata.tickCarriedBy(playerentity, pStack);
            }

            if (!mapdata.locked && (pIsSelected || pEntity instanceof PlayerEntity && ((PlayerEntity)pEntity).getOffhandItem() == pStack)) {
               this.update(pLevel, pEntity, mapdata);
            }

         }
      }
   }

   @Nullable
   public IPacket<?> getUpdatePacket(ItemStack pStack, World pLevel, PlayerEntity pPlayer) {
      return getOrCreateSavedData(pStack, pLevel).getUpdatePacket(pStack, pLevel, pPlayer);
   }

   /**
    * Called when item is crafted/smelted. Used only by maps so far.
    */
   public void onCraftedBy(ItemStack pStack, World pLevel, PlayerEntity pPlayer) {
      CompoundNBT compoundnbt = pStack.getTag();
      if (compoundnbt != null && compoundnbt.contains("map_scale_direction", 99)) {
         scaleMap(pStack, pLevel, compoundnbt.getInt("map_scale_direction"));
         compoundnbt.remove("map_scale_direction");
      } else if (compoundnbt != null && compoundnbt.contains("map_to_lock", 1) && compoundnbt.getBoolean("map_to_lock")) {
         lockMap(pLevel, pStack);
         compoundnbt.remove("map_to_lock");
      }

   }

   protected static void scaleMap(ItemStack pStack, World pLevel, int pScale) {
      MapData mapdata = getOrCreateSavedData(pStack, pLevel);
      if (mapdata != null) {
         createAndStoreSavedData(pStack, pLevel, mapdata.x, mapdata.z, MathHelper.clamp(mapdata.scale + pScale, 0, 4), mapdata.trackingPosition, mapdata.unlimitedTracking, mapdata.dimension);
      }

   }

   public static void lockMap(World pLevel, ItemStack pStack) {
      MapData mapdata = getOrCreateSavedData(pStack, pLevel);
      if (mapdata != null) {
         MapData mapdata1 = createAndStoreSavedData(pStack, pLevel, 0, 0, mapdata.scale, mapdata.trackingPosition, mapdata.unlimitedTracking, mapdata.dimension);
         mapdata1.lockData(mapdata);
      }

   }

   /**
    * allows items to add custom lines of information to the mouseover description
    */
   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(ItemStack pStack, @Nullable World pLevel, List<ITextComponent> pTooltip, ITooltipFlag pFlag) {
      MapData mapdata = pLevel == null ? null : getOrCreateSavedData(pStack, pLevel);
      if (mapdata != null && mapdata.locked) {
         pTooltip.add((new TranslationTextComponent("filled_map.locked", getMapId(pStack))).withStyle(TextFormatting.GRAY));
      }

      if (pFlag.isAdvanced()) {
         if (mapdata != null) {
            pTooltip.add((new TranslationTextComponent("filled_map.id", getMapId(pStack))).withStyle(TextFormatting.GRAY));
            pTooltip.add((new TranslationTextComponent("filled_map.scale", 1 << mapdata.scale)).withStyle(TextFormatting.GRAY));
            pTooltip.add((new TranslationTextComponent("filled_map.level", mapdata.scale, 4)).withStyle(TextFormatting.GRAY));
         } else {
            pTooltip.add((new TranslationTextComponent("filled_map.unknown")).withStyle(TextFormatting.GRAY));
         }
      }

   }

   @OnlyIn(Dist.CLIENT)
   public static int getColor(ItemStack pStack) {
      CompoundNBT compoundnbt = pStack.getTagElement("display");
      if (compoundnbt != null && compoundnbt.contains("MapColor", 99)) {
         int i = compoundnbt.getInt("MapColor");
         return -16777216 | i & 16777215;
      } else {
         return -12173266;
      }
   }

   /**
    * Called when this item is used when targetting a Block
    */
   public ActionResultType useOn(ItemUseContext pContext) {
      BlockState blockstate = pContext.getLevel().getBlockState(pContext.getClickedPos());
      if (blockstate.is(BlockTags.BANNERS)) {
         if (!pContext.getLevel().isClientSide) {
            MapData mapdata = getOrCreateSavedData(pContext.getItemInHand(), pContext.getLevel());
            mapdata.toggleBanner(pContext.getLevel(), pContext.getClickedPos());
         }

         return ActionResultType.sidedSuccess(pContext.getLevel().isClientSide);
      } else {
         return super.useOn(pContext);
      }
   }
}
