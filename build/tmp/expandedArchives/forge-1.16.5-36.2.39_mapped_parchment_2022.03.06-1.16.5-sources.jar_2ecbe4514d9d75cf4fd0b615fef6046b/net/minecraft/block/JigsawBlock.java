package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.JigsawTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.jigsaw.JigsawOrientation;
import net.minecraft.world.gen.feature.template.Template;

public class JigsawBlock extends Block implements ITileEntityProvider {
   public static final EnumProperty<JigsawOrientation> ORIENTATION = BlockStateProperties.ORIENTATION;

   public JigsawBlock(AbstractBlock.Properties p_i49981_1_) {
      super(p_i49981_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(ORIENTATION, JigsawOrientation.NORTH_UP));
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(ORIENTATION);
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link IBlockState#withRotation(Rotation)} whenever possible. Implementing/overriding is
    * fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRotation) {
      return pState.setValue(ORIENTATION, pRotation.rotation().rotate(pState.getValue(ORIENTATION)));
   }

   /**
    * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link IBlockState#withMirror(Mirror)} whenever possible. Implementing/overriding is fine.
    */
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      return pState.setValue(ORIENTATION, pMirror.rotation().rotate(pState.getValue(ORIENTATION)));
   }

   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      Direction direction = pContext.getClickedFace();
      Direction direction1;
      if (direction.getAxis() == Direction.Axis.Y) {
         direction1 = pContext.getHorizontalDirection().getOpposite();
      } else {
         direction1 = Direction.UP;
      }

      return this.defaultBlockState().setValue(ORIENTATION, JigsawOrientation.fromFrontAndTop(direction, direction1));
   }

   @Nullable
   public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
      return new JigsawTileEntity();
   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      if (tileentity instanceof JigsawTileEntity && pPlayer.canUseGameMasterBlocks()) {
         pPlayer.openJigsawBlock((JigsawTileEntity)tileentity);
         return ActionResultType.sidedSuccess(pLevel.isClientSide);
      } else {
         return ActionResultType.PASS;
      }
   }

   public static boolean canAttach(Template.BlockInfo pInfo, Template.BlockInfo pInfo2) {
      Direction direction = getFrontFacing(pInfo.state);
      Direction direction1 = getFrontFacing(pInfo2.state);
      Direction direction2 = getTopFacing(pInfo.state);
      Direction direction3 = getTopFacing(pInfo2.state);
      JigsawTileEntity.OrientationType jigsawtileentity$orientationtype = JigsawTileEntity.OrientationType.byName(pInfo.nbt.getString("joint")).orElseGet(() -> {
         return direction.getAxis().isHorizontal() ? JigsawTileEntity.OrientationType.ALIGNED : JigsawTileEntity.OrientationType.ROLLABLE;
      });
      boolean flag = jigsawtileentity$orientationtype == JigsawTileEntity.OrientationType.ROLLABLE;
      return direction == direction1.getOpposite() && (flag || direction2 == direction3) && pInfo.nbt.getString("target").equals(pInfo2.nbt.getString("name"));
   }

   /**
    * This represents the face that the puzzle piece is on. To connect: 2 jigsaws must have their puzzle piece face
    * facing each other.
    */
   public static Direction getFrontFacing(BlockState pState) {
      return pState.getValue(ORIENTATION).front();
   }

   /**
    * This represents the face that the line connector is on. To connect, if the OrientationType is ALIGNED, the two
    * lines must be in the same direction. (Their textures will form one straight line)
    */
   public static Direction getTopFacing(BlockState pState) {
      return pState.getValue(ORIENTATION).top();
   }
}