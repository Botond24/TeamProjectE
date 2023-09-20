package net.minecraft.block;

import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.EndPortalTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EndPortalBlock extends ContainerBlock {
   protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);

   public EndPortalBlock(AbstractBlock.Properties p_i48406_1_) {
      super(p_i48406_1_);
   }

   public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
      return new EndPortalTileEntity();
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return SHAPE;
   }

   public void entityInside(BlockState pState, World pLevel, BlockPos pPos, Entity pEntity) {
      if (pLevel instanceof ServerWorld && !pEntity.isPassenger() && !pEntity.isVehicle() && pEntity.canChangeDimensions() && VoxelShapes.joinIsNotEmpty(VoxelShapes.create(pEntity.getBoundingBox().move((double)(-pPos.getX()), (double)(-pPos.getY()), (double)(-pPos.getZ()))), pState.getShape(pLevel, pPos), IBooleanFunction.AND)) {
         RegistryKey<World> registrykey = pLevel.dimension() == World.END ? World.OVERWORLD : World.END;
         ServerWorld serverworld = ((ServerWorld)pLevel).getServer().getLevel(registrykey);
         if (serverworld == null) {
            return;
         }

         pEntity.changeDimension(serverworld);
      }

   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
    * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
    * of whether the block can receive random update ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
      double d0 = (double)pPos.getX() + pRand.nextDouble();
      double d1 = (double)pPos.getY() + 0.8D;
      double d2 = (double)pPos.getZ() + pRand.nextDouble();
      pLevel.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
   }

   public ItemStack getCloneItemStack(IBlockReader pLevel, BlockPos pPos, BlockState pState) {
      return ItemStack.EMPTY;
   }

   public boolean canBeReplaced(BlockState pState, Fluid pFluid) {
      return false;
   }
}