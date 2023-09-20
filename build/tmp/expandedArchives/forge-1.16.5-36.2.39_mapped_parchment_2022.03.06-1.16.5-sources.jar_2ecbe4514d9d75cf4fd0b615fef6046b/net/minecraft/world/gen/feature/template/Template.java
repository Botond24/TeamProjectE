package net.minecraft.world.gen.feature.template;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.IClearable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.Rotation;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.shapes.BitSetVoxelShapePart;
import net.minecraft.util.math.shapes.VoxelShapePart;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.EmptyBlockReader;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class Template {
   private final List<Template.Palette> palettes = Lists.newArrayList();
   private final List<Template.EntityInfo> entityInfoList = Lists.newArrayList();
   private BlockPos size = BlockPos.ZERO;
   private String author = "?";

   public BlockPos getSize() {
      return this.size;
   }

   public void setAuthor(String pAuthor) {
      this.author = pAuthor;
   }

   public String getAuthor() {
      return this.author;
   }

   public void fillFromWorld(World p_186254_1_, BlockPos p_186254_2_, BlockPos p_186254_3_, boolean p_186254_4_, @Nullable Block p_186254_5_) {
      if (p_186254_3_.getX() >= 1 && p_186254_3_.getY() >= 1 && p_186254_3_.getZ() >= 1) {
         BlockPos blockpos = p_186254_2_.offset(p_186254_3_).offset(-1, -1, -1);
         List<Template.BlockInfo> list = Lists.newArrayList();
         List<Template.BlockInfo> list1 = Lists.newArrayList();
         List<Template.BlockInfo> list2 = Lists.newArrayList();
         BlockPos blockpos1 = new BlockPos(Math.min(p_186254_2_.getX(), blockpos.getX()), Math.min(p_186254_2_.getY(), blockpos.getY()), Math.min(p_186254_2_.getZ(), blockpos.getZ()));
         BlockPos blockpos2 = new BlockPos(Math.max(p_186254_2_.getX(), blockpos.getX()), Math.max(p_186254_2_.getY(), blockpos.getY()), Math.max(p_186254_2_.getZ(), blockpos.getZ()));
         this.size = p_186254_3_;

         for(BlockPos blockpos3 : BlockPos.betweenClosed(blockpos1, blockpos2)) {
            BlockPos blockpos4 = blockpos3.subtract(blockpos1);
            BlockState blockstate = p_186254_1_.getBlockState(blockpos3);
            if (p_186254_5_ == null || p_186254_5_ != blockstate.getBlock()) {
               TileEntity tileentity = p_186254_1_.getBlockEntity(blockpos3);
               Template.BlockInfo template$blockinfo;
               if (tileentity != null) {
                  CompoundNBT compoundnbt = tileentity.save(new CompoundNBT());
                  compoundnbt.remove("x");
                  compoundnbt.remove("y");
                  compoundnbt.remove("z");
                  template$blockinfo = new Template.BlockInfo(blockpos4, blockstate, compoundnbt.copy());
               } else {
                  template$blockinfo = new Template.BlockInfo(blockpos4, blockstate, (CompoundNBT)null);
               }

               addToLists(template$blockinfo, list, list1, list2);
            }
         }

         List<Template.BlockInfo> list3 = buildInfoList(list, list1, list2);
         this.palettes.clear();
         this.palettes.add(new Template.Palette(list3));
         if (p_186254_4_) {
            this.fillEntityList(p_186254_1_, blockpos1, blockpos2.offset(1, 1, 1));
         } else {
            this.entityInfoList.clear();
         }

      }
   }

   private static void addToLists(Template.BlockInfo pBlockInfo, List<Template.BlockInfo> pSpecialBlocks, List<Template.BlockInfo> pBlocksWithTag, List<Template.BlockInfo> pNormalBlocks) {
      if (pBlockInfo.nbt != null) {
         pBlocksWithTag.add(pBlockInfo);
      } else if (!pBlockInfo.state.getBlock().hasDynamicShape() && pBlockInfo.state.isCollisionShapeFullBlock(EmptyBlockReader.INSTANCE, BlockPos.ZERO)) {
         pSpecialBlocks.add(pBlockInfo);
      } else {
         pNormalBlocks.add(pBlockInfo);
      }

   }

   private static List<Template.BlockInfo> buildInfoList(List<Template.BlockInfo> pSpecialBlocks, List<Template.BlockInfo> pBlocksWithTag, List<Template.BlockInfo> pNormalBlocks) {
      Comparator<Template.BlockInfo> comparator = Comparator.<Template.BlockInfo>comparingInt((p_237154_0_) -> {
         return p_237154_0_.pos.getY();
      }).thenComparingInt((p_237153_0_) -> {
         return p_237153_0_.pos.getX();
      }).thenComparingInt((p_237148_0_) -> {
         return p_237148_0_.pos.getZ();
      });
      pSpecialBlocks.sort(comparator);
      pNormalBlocks.sort(comparator);
      pBlocksWithTag.sort(comparator);
      List<Template.BlockInfo> list = Lists.newArrayList();
      list.addAll(pSpecialBlocks);
      list.addAll(pNormalBlocks);
      list.addAll(pBlocksWithTag);
      return list;
   }

   /**
    * takes blocks from the world and puts the data them into this template
    */
   private void fillEntityList(World pLevel, BlockPos pStartPos, BlockPos pEndPos) {
      List<Entity> list = pLevel.getEntitiesOfClass(Entity.class, new AxisAlignedBB(pStartPos, pEndPos), (p_237142_0_) -> {
         return !(p_237142_0_ instanceof PlayerEntity);
      });
      this.entityInfoList.clear();

      for(Entity entity : list) {
         Vector3d vector3d = new Vector3d(entity.getX() - (double)pStartPos.getX(), entity.getY() - (double)pStartPos.getY(), entity.getZ() - (double)pStartPos.getZ());
         CompoundNBT compoundnbt = new CompoundNBT();
         entity.save(compoundnbt);
         BlockPos blockpos;
         if (entity instanceof PaintingEntity) {
            blockpos = ((PaintingEntity)entity).getPos().subtract(pStartPos);
         } else {
            blockpos = new BlockPos(vector3d);
         }

         this.entityInfoList.add(new Template.EntityInfo(vector3d, blockpos, compoundnbt.copy()));
      }

   }

   public List<Template.BlockInfo> filterBlocks(BlockPos pPos, PlacementSettings pSettings, Block pBlock) {
      return this.filterBlocks(pPos, pSettings, pBlock, true);
   }

   public List<Template.BlockInfo> filterBlocks(BlockPos pPos, PlacementSettings pSettings, Block pBlock, boolean pRelativePosition) {
      List<Template.BlockInfo> list = Lists.newArrayList();
      MutableBoundingBox mutableboundingbox = pSettings.getBoundingBox();
      if (this.palettes.isEmpty()) {
         return Collections.emptyList();
      } else {
         for(Template.BlockInfo template$blockinfo : pSettings.getRandomPalette(this.palettes, pPos).blocks(pBlock)) {
            BlockPos blockpos = pRelativePosition ? calculateRelativePosition(pSettings, template$blockinfo.pos).offset(pPos) : template$blockinfo.pos;
            if (mutableboundingbox == null || mutableboundingbox.isInside(blockpos)) {
               list.add(new Template.BlockInfo(blockpos, template$blockinfo.state.rotate(pSettings.getRotation()), template$blockinfo.nbt));
            }
         }

         return list;
      }
   }

   public BlockPos calculateConnectedPosition(PlacementSettings pDecorator, BlockPos p_186262_2_, PlacementSettings pSettings, BlockPos p_186262_4_) {
      BlockPos blockpos = calculateRelativePosition(pDecorator, p_186262_2_);
      BlockPos blockpos1 = calculateRelativePosition(pSettings, p_186262_4_);
      return blockpos.subtract(blockpos1);
   }

   public static BlockPos calculateRelativePosition(PlacementSettings pDecorator, BlockPos pPos) {
      return transform(pPos, pDecorator.getMirror(), pDecorator.getRotation(), pDecorator.getRotationPivot());
   }

   public static Vector3d transformedVec3d(PlacementSettings placementIn, Vector3d pos) {
      return transform(pos, placementIn.getMirror(), placementIn.getRotation(), placementIn.getRotationPivot());
   }

   public void placeInWorldChunk(IServerWorld p_237144_1_, BlockPos p_237144_2_, PlacementSettings p_237144_3_, Random p_237144_4_) {
      p_237144_3_.updateBoundingBoxFromChunkPos();
      this.placeInWorld(p_237144_1_, p_237144_2_, p_237144_3_, p_237144_4_);
   }

   public void placeInWorld(IServerWorld p_237152_1_, BlockPos p_237152_2_, PlacementSettings p_237152_3_, Random p_237152_4_) {
      this.placeInWorld(p_237152_1_, p_237152_2_, p_237152_2_, p_237152_3_, p_237152_4_, 2);
   }

   public boolean placeInWorld(IServerWorld pServerLevel, BlockPos p_237146_2_, BlockPos p_237146_3_, PlacementSettings pSettings, Random pRandom, int pFlags) {
      if (this.palettes.isEmpty()) {
         return false;
      } else {
         List<Template.BlockInfo> list = pSettings.getRandomPalette(this.palettes, p_237146_2_).blocks();
         if ((!list.isEmpty() || !pSettings.isIgnoreEntities() && !this.entityInfoList.isEmpty()) && this.size.getX() >= 1 && this.size.getY() >= 1 && this.size.getZ() >= 1) {
            MutableBoundingBox mutableboundingbox = pSettings.getBoundingBox();
            List<BlockPos> list1 = Lists.newArrayListWithCapacity(pSettings.shouldKeepLiquids() ? list.size() : 0);
            List<Pair<BlockPos, CompoundNBT>> list2 = Lists.newArrayListWithCapacity(list.size());
            int i = Integer.MAX_VALUE;
            int j = Integer.MAX_VALUE;
            int k = Integer.MAX_VALUE;
            int l = Integer.MIN_VALUE;
            int i1 = Integer.MIN_VALUE;
            int j1 = Integer.MIN_VALUE;

            for(Template.BlockInfo template$blockinfo : processBlockInfos(pServerLevel, p_237146_2_, p_237146_3_, pSettings, list, this)) {
               BlockPos blockpos = template$blockinfo.pos;
               if (mutableboundingbox == null || mutableboundingbox.isInside(blockpos)) {
                  FluidState fluidstate = pSettings.shouldKeepLiquids() ? pServerLevel.getFluidState(blockpos) : null;
                  BlockState blockstate = template$blockinfo.state.mirror(pSettings.getMirror()).rotate(pSettings.getRotation());
                  if (template$blockinfo.nbt != null) {
                     TileEntity tileentity = pServerLevel.getBlockEntity(blockpos);
                     IClearable.tryClear(tileentity);
                     pServerLevel.setBlock(blockpos, Blocks.BARRIER.defaultBlockState(), 20);
                  }

                  if (pServerLevel.setBlock(blockpos, blockstate, pFlags)) {
                     i = Math.min(i, blockpos.getX());
                     j = Math.min(j, blockpos.getY());
                     k = Math.min(k, blockpos.getZ());
                     l = Math.max(l, blockpos.getX());
                     i1 = Math.max(i1, blockpos.getY());
                     j1 = Math.max(j1, blockpos.getZ());
                     list2.add(Pair.of(blockpos, template$blockinfo.nbt));
                     if (template$blockinfo.nbt != null) {
                        TileEntity tileentity1 = pServerLevel.getBlockEntity(blockpos);
                        if (tileentity1 != null) {
                           template$blockinfo.nbt.putInt("x", blockpos.getX());
                           template$blockinfo.nbt.putInt("y", blockpos.getY());
                           template$blockinfo.nbt.putInt("z", blockpos.getZ());
                           if (tileentity1 instanceof LockableLootTileEntity) {
                              template$blockinfo.nbt.putLong("LootTableSeed", pRandom.nextLong());
                           }

                           tileentity1.load(template$blockinfo.state, template$blockinfo.nbt);
                           tileentity1.mirror(pSettings.getMirror());
                           tileentity1.rotate(pSettings.getRotation());
                        }
                     }

                     if (fluidstate != null && blockstate.getBlock() instanceof ILiquidContainer) {
                        ((ILiquidContainer)blockstate.getBlock()).placeLiquid(pServerLevel, blockpos, blockstate, fluidstate);
                        if (!fluidstate.isSource()) {
                           list1.add(blockpos);
                        }
                     }
                  }
               }
            }

            boolean flag = true;
            Direction[] adirection = new Direction[]{Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

            while(flag && !list1.isEmpty()) {
               flag = false;
               Iterator<BlockPos> iterator = list1.iterator();

               while(iterator.hasNext()) {
                  BlockPos blockpos2 = iterator.next();
                  BlockPos blockpos3 = blockpos2;
                  FluidState fluidstate2 = pServerLevel.getFluidState(blockpos2);

                  for(int k1 = 0; k1 < adirection.length && !fluidstate2.isSource(); ++k1) {
                     BlockPos blockpos1 = blockpos3.relative(adirection[k1]);
                     FluidState fluidstate1 = pServerLevel.getFluidState(blockpos1);
                     if (fluidstate1.getHeight(pServerLevel, blockpos1) > fluidstate2.getHeight(pServerLevel, blockpos3) || fluidstate1.isSource() && !fluidstate2.isSource()) {
                        fluidstate2 = fluidstate1;
                        blockpos3 = blockpos1;
                     }
                  }

                  if (fluidstate2.isSource()) {
                     BlockState blockstate2 = pServerLevel.getBlockState(blockpos2);
                     Block block = blockstate2.getBlock();
                     if (block instanceof ILiquidContainer) {
                        ((ILiquidContainer)block).placeLiquid(pServerLevel, blockpos2, blockstate2, fluidstate2);
                        flag = true;
                        iterator.remove();
                     }
                  }
               }
            }

            if (i <= l) {
               if (!pSettings.getKnownShape()) {
                  VoxelShapePart voxelshapepart = new BitSetVoxelShapePart(l - i + 1, i1 - j + 1, j1 - k + 1);
                  int l1 = i;
                  int i2 = j;
                  int j2 = k;

                  for(Pair<BlockPos, CompoundNBT> pair1 : list2) {
                     BlockPos blockpos5 = pair1.getFirst();
                     voxelshapepart.setFull(blockpos5.getX() - l1, blockpos5.getY() - i2, blockpos5.getZ() - j2, true, true);
                  }

                  updateShapeAtEdge(pServerLevel, pFlags, voxelshapepart, l1, i2, j2);
               }

               for(Pair<BlockPos, CompoundNBT> pair : list2) {
                  BlockPos blockpos4 = pair.getFirst();
                  if (!pSettings.getKnownShape()) {
                     BlockState blockstate1 = pServerLevel.getBlockState(blockpos4);
                     BlockState blockstate3 = Block.updateFromNeighbourShapes(blockstate1, pServerLevel, blockpos4);
                     if (blockstate1 != blockstate3) {
                        pServerLevel.setBlock(blockpos4, blockstate3, pFlags & -2 | 16);
                     }

                     pServerLevel.blockUpdated(blockpos4, blockstate3.getBlock());
                  }

                  if (pair.getSecond() != null) {
                     TileEntity tileentity2 = pServerLevel.getBlockEntity(blockpos4);
                     if (tileentity2 != null) {
                        tileentity2.setChanged();
                     }
                  }
               }
            }

            if (!pSettings.isIgnoreEntities()) {
               this.addEntitiesToWorld(pServerLevel, p_237146_2_, pSettings);
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public static void updateShapeAtEdge(IWorld pLevel, int p_222857_1_, VoxelShapePart pVoxelShapePart, int pX, int pY, int pZ) {
      pVoxelShapePart.forAllFaces((p_237141_5_, p_237141_6_, p_237141_7_, p_237141_8_) -> {
         BlockPos blockpos = new BlockPos(pX + p_237141_6_, pY + p_237141_7_, pZ + p_237141_8_);
         BlockPos blockpos1 = blockpos.relative(p_237141_5_);
         BlockState blockstate = pLevel.getBlockState(blockpos);
         BlockState blockstate1 = pLevel.getBlockState(blockpos1);
         BlockState blockstate2 = blockstate.updateShape(p_237141_5_, blockstate1, pLevel, blockpos, blockpos1);
         if (blockstate != blockstate2) {
            pLevel.setBlock(blockpos, blockstate2, p_222857_1_ & -2);
         }

         BlockState blockstate3 = blockstate1.updateShape(p_237141_5_.getOpposite(), blockstate2, pLevel, blockpos1, blockpos);
         if (blockstate1 != blockstate3) {
            pLevel.setBlock(blockpos1, blockstate3, p_222857_1_ & -2);
         }

      });
   }

   @Deprecated //Use Forge version
   public static List<Template.BlockInfo> processBlockInfos(IWorld pLevel, BlockPos p_237145_1_, BlockPos p_237145_2_, PlacementSettings pSettings, List<Template.BlockInfo> pBlockInfoList) {
      return processBlockInfos(pLevel, p_237145_1_, p_237145_2_, pSettings, pBlockInfoList, null);
   }

   public static List<Template.BlockInfo> processBlockInfos(IWorld pLevel, BlockPos p_237145_1_, BlockPos p_237145_2_, PlacementSettings pSettings, List<Template.BlockInfo> pBlockInfoList, @Nullable Template template) {
      List<Template.BlockInfo> list = Lists.newArrayList();

      for(Template.BlockInfo template$blockinfo : pBlockInfoList) {
         BlockPos blockpos = calculateRelativePosition(pSettings, template$blockinfo.pos).offset(p_237145_1_);
         Template.BlockInfo template$blockinfo1 = new Template.BlockInfo(blockpos, template$blockinfo.state, template$blockinfo.nbt != null ? template$blockinfo.nbt.copy() : null);

         for(Iterator<StructureProcessor> iterator = pSettings.getProcessors().iterator(); template$blockinfo1 != null && iterator.hasNext(); template$blockinfo1 = iterator.next().process(pLevel, p_237145_1_, p_237145_2_, template$blockinfo, template$blockinfo1, pSettings, template)) {
         }

         if (template$blockinfo1 != null) {
            list.add(template$blockinfo1);
         }
      }

      return list;
   }

   public static List<Template.EntityInfo> processEntityInfos(@Nullable Template template, IWorld p_215387_0_, BlockPos p_215387_1_, PlacementSettings p_215387_2_, List<Template.EntityInfo> p_215387_3_) {
      List<Template.EntityInfo> list = Lists.newArrayList();
      for(Template.EntityInfo entityInfo : p_215387_3_) {
         Vector3d pos = transformedVec3d(p_215387_2_, entityInfo.pos).add(Vector3d.atLowerCornerOf(p_215387_1_));
         BlockPos blockpos = calculateRelativePosition(p_215387_2_, entityInfo.blockPos).offset(p_215387_1_);
         Template.EntityInfo info = new Template.EntityInfo(pos, blockpos, entityInfo.nbt);
         for (StructureProcessor proc : p_215387_2_.getProcessors()) {
            info = proc.processEntity(p_215387_0_, p_215387_1_, entityInfo, info, p_215387_2_, template);
            if (info == null)
               break;
         }
         if (info != null)
            list.add(info);
      }
      return list;
   }

   private void addEntitiesToWorld(IServerWorld pServerLevel, BlockPos p_237143_2_, PlacementSettings placementIn) {
      for(Template.EntityInfo template$entityinfo : processEntityInfos(this, pServerLevel, p_237143_2_, placementIn, this.entityInfoList)) {
         BlockPos blockpos = transform(template$entityinfo.blockPos, placementIn.getMirror(), placementIn.getRotation(), placementIn.getRotationPivot()).offset(p_237143_2_);
         blockpos = template$entityinfo.blockPos; // FORGE: Position will have already been transformed by processEntityInfos
         if (placementIn.getBoundingBox() == null || placementIn.getBoundingBox().isInside(blockpos)) {
            CompoundNBT compoundnbt = template$entityinfo.nbt.copy();
            Vector3d vector3d1 = template$entityinfo.pos; // FORGE: Position will have already been transformed by processEntityInfos
            ListNBT listnbt = new ListNBT();
            listnbt.add(DoubleNBT.valueOf(vector3d1.x));
            listnbt.add(DoubleNBT.valueOf(vector3d1.y));
            listnbt.add(DoubleNBT.valueOf(vector3d1.z));
            compoundnbt.put("Pos", listnbt);
            compoundnbt.remove("UUID");
            createEntityIgnoreException(pServerLevel, compoundnbt).ifPresent((p_242927_6_) -> {
               float f = p_242927_6_.mirror(placementIn.getMirror());
               f = f + (p_242927_6_.yRot - p_242927_6_.rotate(placementIn.getRotation()));
               p_242927_6_.moveTo(vector3d1.x, vector3d1.y, vector3d1.z, f, p_242927_6_.xRot);
               if (placementIn.shouldFinalizeEntities() && p_242927_6_ instanceof MobEntity) {
                  ((MobEntity)p_242927_6_).finalizeSpawn(pServerLevel, pServerLevel.getCurrentDifficultyAt(new BlockPos(vector3d1)), SpawnReason.STRUCTURE, (ILivingEntityData)null, compoundnbt);
               }

               pServerLevel.addFreshEntityWithPassengers(p_242927_6_);
            });
         }
      }

   }

   private static Optional<Entity> createEntityIgnoreException(IServerWorld pLevel, CompoundNBT pTag) {
      try {
         return EntityType.create(pTag, pLevel.getLevel());
      } catch (Exception exception) {
         return Optional.empty();
      }
   }

   public BlockPos getSize(Rotation p_186257_1_) {
      switch(p_186257_1_) {
      case COUNTERCLOCKWISE_90:
      case CLOCKWISE_90:
         return new BlockPos(this.size.getZ(), this.size.getY(), this.size.getX());
      default:
         return this.size;
      }
   }

   public static BlockPos transform(BlockPos pTargetPos, Mirror pMirror, Rotation pRotation, BlockPos pOffset) {
      int i = pTargetPos.getX();
      int j = pTargetPos.getY();
      int k = pTargetPos.getZ();
      boolean flag = true;
      switch(pMirror) {
      case LEFT_RIGHT:
         k = -k;
         break;
      case FRONT_BACK:
         i = -i;
         break;
      default:
         flag = false;
      }

      int l = pOffset.getX();
      int i1 = pOffset.getZ();
      switch(pRotation) {
      case COUNTERCLOCKWISE_90:
         return new BlockPos(l - i1 + k, j, l + i1 - i);
      case CLOCKWISE_90:
         return new BlockPos(l + i1 - k, j, i1 - l + i);
      case CLOCKWISE_180:
         return new BlockPos(l + l - i, j, i1 + i1 - k);
      default:
         return flag ? new BlockPos(i, j, k) : pTargetPos;
      }
   }

   public static Vector3d transform(Vector3d pTarget, Mirror pMirror, Rotation pRotation, BlockPos pCenterOffset) {
      double d0 = pTarget.x;
      double d1 = pTarget.y;
      double d2 = pTarget.z;
      boolean flag = true;
      switch(pMirror) {
      case LEFT_RIGHT:
         d2 = 1.0D - d2;
         break;
      case FRONT_BACK:
         d0 = 1.0D - d0;
         break;
      default:
         flag = false;
      }

      int i = pCenterOffset.getX();
      int j = pCenterOffset.getZ();
      switch(pRotation) {
      case COUNTERCLOCKWISE_90:
         return new Vector3d((double)(i - j) + d2, d1, (double)(i + j + 1) - d0);
      case CLOCKWISE_90:
         return new Vector3d((double)(i + j + 1) - d2, d1, (double)(j - i) + d0);
      case CLOCKWISE_180:
         return new Vector3d((double)(i + i + 1) - d0, d1, (double)(j + j + 1) - d2);
      default:
         return flag ? new Vector3d(d0, d1, d2) : pTarget;
      }
   }

   public BlockPos getZeroPositionWithTransform(BlockPos pTargetPos, Mirror pMirror, Rotation pRotation) {
      return getZeroPositionWithTransform(pTargetPos, pMirror, pRotation, this.getSize().getX(), this.getSize().getZ());
   }

   public static BlockPos getZeroPositionWithTransform(BlockPos pPos, Mirror pMirror, Rotation pRotation, int pSizeX, int pSizeZ) {
      --pSizeX;
      --pSizeZ;
      int i = pMirror == Mirror.FRONT_BACK ? pSizeX : 0;
      int j = pMirror == Mirror.LEFT_RIGHT ? pSizeZ : 0;
      BlockPos blockpos = pPos;
      switch(pRotation) {
      case COUNTERCLOCKWISE_90:
         blockpos = pPos.offset(j, 0, pSizeX - i);
         break;
      case CLOCKWISE_90:
         blockpos = pPos.offset(pSizeZ - j, 0, i);
         break;
      case CLOCKWISE_180:
         blockpos = pPos.offset(pSizeX - i, 0, pSizeZ - j);
         break;
      case NONE:
         blockpos = pPos.offset(i, 0, j);
      }

      return blockpos;
   }

   public MutableBoundingBox getBoundingBox(PlacementSettings pSettings, BlockPos pStartPos) {
      return this.getBoundingBox(pStartPos, pSettings.getRotation(), pSettings.getRotationPivot(), pSettings.getMirror());
   }

   public MutableBoundingBox getBoundingBox(BlockPos pStartPos, Rotation pRotation, BlockPos pPivotPos, Mirror pMirror) {
      BlockPos blockpos = this.getSize(pRotation);
      int i = pPivotPos.getX();
      int j = pPivotPos.getZ();
      int k = blockpos.getX() - 1;
      int l = blockpos.getY() - 1;
      int i1 = blockpos.getZ() - 1;
      MutableBoundingBox mutableboundingbox = new MutableBoundingBox(0, 0, 0, 0, 0, 0);
      switch(pRotation) {
      case COUNTERCLOCKWISE_90:
         mutableboundingbox = new MutableBoundingBox(i - j, 0, i + j - i1, i - j + k, l, i + j);
         break;
      case CLOCKWISE_90:
         mutableboundingbox = new MutableBoundingBox(i + j - k, 0, j - i, i + j, l, j - i + i1);
         break;
      case CLOCKWISE_180:
         mutableboundingbox = new MutableBoundingBox(i + i - k, 0, j + j - i1, i + i, l, j + j);
         break;
      case NONE:
         mutableboundingbox = new MutableBoundingBox(0, 0, 0, k, l, i1);
      }

      switch(pMirror) {
      case LEFT_RIGHT:
         this.mirrorAABB(pRotation, i1, k, mutableboundingbox, Direction.NORTH, Direction.SOUTH);
         break;
      case FRONT_BACK:
         this.mirrorAABB(pRotation, k, i1, mutableboundingbox, Direction.WEST, Direction.EAST);
      case NONE:
      }

      mutableboundingbox.move(pStartPos.getX(), pStartPos.getY(), pStartPos.getZ());
      return mutableboundingbox;
   }

   private void mirrorAABB(Rotation p_215385_1_, int p_215385_2_, int p_215385_3_, MutableBoundingBox p_215385_4_, Direction p_215385_5_, Direction p_215385_6_) {
      BlockPos blockpos = BlockPos.ZERO;
      if (p_215385_1_ != Rotation.CLOCKWISE_90 && p_215385_1_ != Rotation.COUNTERCLOCKWISE_90) {
         if (p_215385_1_ == Rotation.CLOCKWISE_180) {
            blockpos = blockpos.relative(p_215385_6_, p_215385_2_);
         } else {
            blockpos = blockpos.relative(p_215385_5_, p_215385_2_);
         }
      } else {
         blockpos = blockpos.relative(p_215385_1_.rotate(p_215385_5_), p_215385_3_);
      }

      p_215385_4_.move(blockpos.getX(), 0, blockpos.getZ());
   }

   public CompoundNBT save(CompoundNBT pTag) {
      if (this.palettes.isEmpty()) {
         pTag.put("blocks", new ListNBT());
         pTag.put("palette", new ListNBT());
      } else {
         List<Template.BasicPalette> list = Lists.newArrayList();
         Template.BasicPalette template$basicpalette = new Template.BasicPalette();
         list.add(template$basicpalette);

         for(int i = 1; i < this.palettes.size(); ++i) {
            list.add(new Template.BasicPalette());
         }

         ListNBT listnbt1 = new ListNBT();
         List<Template.BlockInfo> list1 = this.palettes.get(0).blocks();

         for(int j = 0; j < list1.size(); ++j) {
            Template.BlockInfo template$blockinfo = list1.get(j);
            CompoundNBT compoundnbt = new CompoundNBT();
            compoundnbt.put("pos", this.newIntegerList(template$blockinfo.pos.getX(), template$blockinfo.pos.getY(), template$blockinfo.pos.getZ()));
            int k = template$basicpalette.idFor(template$blockinfo.state);
            compoundnbt.putInt("state", k);
            if (template$blockinfo.nbt != null) {
               compoundnbt.put("nbt", template$blockinfo.nbt);
            }

            listnbt1.add(compoundnbt);

            for(int l = 1; l < this.palettes.size(); ++l) {
               Template.BasicPalette template$basicpalette1 = list.get(l);
               template$basicpalette1.addMapping((this.palettes.get(l).blocks().get(j)).state, k);
            }
         }

         pTag.put("blocks", listnbt1);
         if (list.size() == 1) {
            ListNBT listnbt2 = new ListNBT();

            for(BlockState blockstate : template$basicpalette) {
               listnbt2.add(NBTUtil.writeBlockState(blockstate));
            }

            pTag.put("palette", listnbt2);
         } else {
            ListNBT listnbt3 = new ListNBT();

            for(Template.BasicPalette template$basicpalette2 : list) {
               ListNBT listnbt4 = new ListNBT();

               for(BlockState blockstate1 : template$basicpalette2) {
                  listnbt4.add(NBTUtil.writeBlockState(blockstate1));
               }

               listnbt3.add(listnbt4);
            }

            pTag.put("palettes", listnbt3);
         }
      }

      ListNBT listnbt = new ListNBT();

      for(Template.EntityInfo template$entityinfo : this.entityInfoList) {
         CompoundNBT compoundnbt1 = new CompoundNBT();
         compoundnbt1.put("pos", this.newDoubleList(template$entityinfo.pos.x, template$entityinfo.pos.y, template$entityinfo.pos.z));
         compoundnbt1.put("blockPos", this.newIntegerList(template$entityinfo.blockPos.getX(), template$entityinfo.blockPos.getY(), template$entityinfo.blockPos.getZ()));
         if (template$entityinfo.nbt != null) {
            compoundnbt1.put("nbt", template$entityinfo.nbt);
         }

         listnbt.add(compoundnbt1);
      }

      pTag.put("entities", listnbt);
      pTag.put("size", this.newIntegerList(this.size.getX(), this.size.getY(), this.size.getZ()));
      pTag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
      return pTag;
   }

   public void load(CompoundNBT pTag) {
      this.palettes.clear();
      this.entityInfoList.clear();
      ListNBT listnbt = pTag.getList("size", 3);
      this.size = new BlockPos(listnbt.getInt(0), listnbt.getInt(1), listnbt.getInt(2));
      ListNBT listnbt1 = pTag.getList("blocks", 10);
      if (pTag.contains("palettes", 9)) {
         ListNBT listnbt2 = pTag.getList("palettes", 9);

         for(int i = 0; i < listnbt2.size(); ++i) {
            this.loadPalette(listnbt2.getList(i), listnbt1);
         }
      } else {
         this.loadPalette(pTag.getList("palette", 10), listnbt1);
      }

      ListNBT listnbt5 = pTag.getList("entities", 10);

      for(int j = 0; j < listnbt5.size(); ++j) {
         CompoundNBT compoundnbt = listnbt5.getCompound(j);
         ListNBT listnbt3 = compoundnbt.getList("pos", 6);
         Vector3d vector3d = new Vector3d(listnbt3.getDouble(0), listnbt3.getDouble(1), listnbt3.getDouble(2));
         ListNBT listnbt4 = compoundnbt.getList("blockPos", 3);
         BlockPos blockpos = new BlockPos(listnbt4.getInt(0), listnbt4.getInt(1), listnbt4.getInt(2));
         if (compoundnbt.contains("nbt")) {
            CompoundNBT compoundnbt1 = compoundnbt.getCompound("nbt");
            this.entityInfoList.add(new Template.EntityInfo(vector3d, blockpos, compoundnbt1));
         }
      }

   }

   private void loadPalette(ListNBT pPalletesNBT, ListNBT pBlocksNBT) {
      Template.BasicPalette template$basicpalette = new Template.BasicPalette();

      for(int i = 0; i < pPalletesNBT.size(); ++i) {
         template$basicpalette.addMapping(NBTUtil.readBlockState(pPalletesNBT.getCompound(i)), i);
      }

      List<Template.BlockInfo> list2 = Lists.newArrayList();
      List<Template.BlockInfo> list = Lists.newArrayList();
      List<Template.BlockInfo> list1 = Lists.newArrayList();

      for(int j = 0; j < pBlocksNBT.size(); ++j) {
         CompoundNBT compoundnbt = pBlocksNBT.getCompound(j);
         ListNBT listnbt = compoundnbt.getList("pos", 3);
         BlockPos blockpos = new BlockPos(listnbt.getInt(0), listnbt.getInt(1), listnbt.getInt(2));
         BlockState blockstate = template$basicpalette.stateFor(compoundnbt.getInt("state"));
         CompoundNBT compoundnbt1;
         if (compoundnbt.contains("nbt")) {
            compoundnbt1 = compoundnbt.getCompound("nbt");
         } else {
            compoundnbt1 = null;
         }

         Template.BlockInfo template$blockinfo = new Template.BlockInfo(blockpos, blockstate, compoundnbt1);
         addToLists(template$blockinfo, list2, list, list1);
      }

      List<Template.BlockInfo> list3 = buildInfoList(list2, list, list1);
      this.palettes.add(new Template.Palette(list3));
   }

   private ListNBT newIntegerList(int... pValues) {
      ListNBT listnbt = new ListNBT();

      for(int i : pValues) {
         listnbt.add(IntNBT.valueOf(i));
      }

      return listnbt;
   }

   private ListNBT newDoubleList(double... pValues) {
      ListNBT listnbt = new ListNBT();

      for(double d0 : pValues) {
         listnbt.add(DoubleNBT.valueOf(d0));
      }

      return listnbt;
   }

   static class BasicPalette implements Iterable<BlockState> {
      public static final BlockState DEFAULT_BLOCK_STATE = Blocks.AIR.defaultBlockState();
      private final ObjectIntIdentityMap<BlockState> ids = new ObjectIntIdentityMap<>(16);
      private int lastId;

      private BasicPalette() {
      }

      public int idFor(BlockState pState) {
         int i = this.ids.getId(pState);
         if (i == -1) {
            i = this.lastId++;
            this.ids.addMapping(pState, i);
         }

         return i;
      }

      @Nullable
      public BlockState stateFor(int pId) {
         BlockState blockstate = this.ids.byId(pId);
         return blockstate == null ? DEFAULT_BLOCK_STATE : blockstate;
      }

      public Iterator<BlockState> iterator() {
         return this.ids.iterator();
      }

      public void addMapping(BlockState pState, int pId) {
         this.ids.addMapping(pState, pId);
      }
   }

   public static class BlockInfo {
      public final BlockPos pos;
      public final BlockState state;
      public final CompoundNBT nbt;

      public BlockInfo(BlockPos pPos, BlockState pState, @Nullable CompoundNBT pNbt) {
         this.pos = pPos;
         this.state = pState;
         this.nbt = pNbt;
      }

      public String toString() {
         return String.format("<StructureBlockInfo | %s | %s | %s>", this.pos, this.state, this.nbt);
      }
   }

   public static class EntityInfo {
      public final Vector3d pos;
      public final BlockPos blockPos;
      public final CompoundNBT nbt;

      public EntityInfo(Vector3d pPos, BlockPos pBlockPos, CompoundNBT pNbt) {
         this.pos = pPos;
         this.blockPos = pBlockPos;
         this.nbt = pNbt;
      }
   }

   public static final class Palette {
      private final List<Template.BlockInfo> blocks;
      private final Map<Block, List<Template.BlockInfo>> cache = Maps.newHashMap();

      private Palette(List<Template.BlockInfo> pBlocks) {
         this.blocks = pBlocks;
      }

      public List<Template.BlockInfo> blocks() {
         return this.blocks;
      }

      public List<Template.BlockInfo> blocks(Block pBlock) {
         return this.cache.computeIfAbsent(pBlock, (p_237160_1_) -> {
            return this.blocks.stream().filter((p_237159_1_) -> {
               return p_237159_1_.state.is(p_237160_1_);
            }).collect(Collectors.toList());
         });
      }
   }
}
