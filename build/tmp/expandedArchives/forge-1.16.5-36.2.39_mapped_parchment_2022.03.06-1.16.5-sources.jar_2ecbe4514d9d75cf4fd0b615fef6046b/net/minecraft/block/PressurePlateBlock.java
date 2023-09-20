package net.minecraft.block;

import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class PressurePlateBlock extends AbstractPressurePlateBlock {
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   private final PressurePlateBlock.Sensitivity sensitivity;

   public PressurePlateBlock(PressurePlateBlock.Sensitivity pSensitivity, AbstractBlock.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, Boolean.valueOf(false)));
      this.sensitivity = pSensitivity;
   }

   protected int getSignalForState(BlockState pState) {
      return pState.getValue(POWERED) ? 15 : 0;
   }

   protected BlockState setSignalForState(BlockState pState, int pStrength) {
      return pState.setValue(POWERED, Boolean.valueOf(pStrength > 0));
   }

   protected void playOnSound(IWorld pLevel, BlockPos pPos) {
      if (this.material != Material.WOOD && this.material != Material.NETHER_WOOD) {
         pLevel.playSound((PlayerEntity)null, pPos, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.6F);
      } else {
         pLevel.playSound((PlayerEntity)null, pPos, SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.8F);
      }

   }

   protected void playOffSound(IWorld pLevel, BlockPos pPos) {
      if (this.material != Material.WOOD && this.material != Material.NETHER_WOOD) {
         pLevel.playSound((PlayerEntity)null, pPos, SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.5F);
      } else {
         pLevel.playSound((PlayerEntity)null, pPos, SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.7F);
      }

   }

   protected int getSignalStrength(World pLevel, BlockPos pPos) {
      AxisAlignedBB axisalignedbb = TOUCH_AABB.move(pPos);
      List<? extends Entity> list;
      switch(this.sensitivity) {
      case EVERYTHING:
         list = pLevel.getEntities((Entity)null, axisalignedbb);
         break;
      case MOBS:
         list = pLevel.getEntitiesOfClass(LivingEntity.class, axisalignedbb);
         break;
      default:
         return 0;
      }

      if (!list.isEmpty()) {
         for(Entity entity : list) {
            if (!entity.isIgnoringBlockTriggers()) {
               return 15;
            }
         }
      }

      return 0;
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(POWERED);
   }

   public static enum Sensitivity {
      EVERYTHING,
      MOBS;
   }
}