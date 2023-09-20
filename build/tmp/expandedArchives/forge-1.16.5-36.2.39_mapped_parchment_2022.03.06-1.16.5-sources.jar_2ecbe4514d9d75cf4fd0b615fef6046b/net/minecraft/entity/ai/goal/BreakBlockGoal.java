package net.minecraft.entity.ai.goal;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;

public class BreakBlockGoal extends MoveToBlockGoal {
   private final Block blockToRemove;
   private final MobEntity removerMob;
   private int ticksSinceReachedGoal;

   public BreakBlockGoal(Block p_i48795_1_, CreatureEntity p_i48795_2_, double p_i48795_3_, int p_i48795_5_) {
      super(p_i48795_2_, p_i48795_3_, 24, p_i48795_5_);
      this.blockToRemove = p_i48795_1_;
      this.removerMob = p_i48795_2_;
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      if (!net.minecraftforge.common.ForgeHooks.canEntityDestroy(this.removerMob.level, this.blockPos, this.removerMob)) {
         return false;
      } else if (this.nextStartTick > 0) {
         --this.nextStartTick;
         return false;
      } else if (this.tryFindBlock()) {
         this.nextStartTick = 20;
         return true;
      } else {
         this.nextStartTick = this.nextStartTick(this.mob);
         return false;
      }
   }

   private boolean tryFindBlock() {
      return this.blockPos != null && this.isValidTarget(this.mob.level, this.blockPos) ? true : this.findNearestBlock();
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      super.stop();
      this.removerMob.fallDistance = 1.0F;
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      super.start();
      this.ticksSinceReachedGoal = 0;
   }

   public void playDestroyProgressSound(IWorld pLevel, BlockPos pPos) {
   }

   public void playBreakSound(World pLevel, BlockPos pPos) {
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      super.tick();
      World world = this.removerMob.level;
      BlockPos blockpos = this.removerMob.blockPosition();
      BlockPos blockpos1 = this.getPosWithBlock(blockpos, world);
      Random random = this.removerMob.getRandom();
      if (this.isReachedTarget() && blockpos1 != null) {
         if (this.ticksSinceReachedGoal > 0) {
            Vector3d vector3d = this.removerMob.getDeltaMovement();
            this.removerMob.setDeltaMovement(vector3d.x, 0.3D, vector3d.z);
            if (!world.isClientSide) {
               double d0 = 0.08D;
               ((ServerWorld)world).sendParticles(new ItemParticleData(ParticleTypes.ITEM, new ItemStack(Items.EGG)), (double)blockpos1.getX() + 0.5D, (double)blockpos1.getY() + 0.7D, (double)blockpos1.getZ() + 0.5D, 3, ((double)random.nextFloat() - 0.5D) * 0.08D, ((double)random.nextFloat() - 0.5D) * 0.08D, ((double)random.nextFloat() - 0.5D) * 0.08D, (double)0.15F);
            }
         }

         if (this.ticksSinceReachedGoal % 2 == 0) {
            Vector3d vector3d1 = this.removerMob.getDeltaMovement();
            this.removerMob.setDeltaMovement(vector3d1.x, -0.3D, vector3d1.z);
            if (this.ticksSinceReachedGoal % 6 == 0) {
               this.playDestroyProgressSound(world, this.blockPos);
            }
         }

         if (this.ticksSinceReachedGoal > 60) {
            world.removeBlock(blockpos1, false);
            if (!world.isClientSide) {
               for(int i = 0; i < 20; ++i) {
                  double d3 = random.nextGaussian() * 0.02D;
                  double d1 = random.nextGaussian() * 0.02D;
                  double d2 = random.nextGaussian() * 0.02D;
                  ((ServerWorld)world).sendParticles(ParticleTypes.POOF, (double)blockpos1.getX() + 0.5D, (double)blockpos1.getY(), (double)blockpos1.getZ() + 0.5D, 1, d3, d1, d2, (double)0.15F);
               }

               this.playBreakSound(world, blockpos1);
            }
         }

         ++this.ticksSinceReachedGoal;
      }

   }

   @Nullable
   private BlockPos getPosWithBlock(BlockPos pPos, IBlockReader pLevel) {
      if (pLevel.getBlockState(pPos).is(this.blockToRemove)) {
         return pPos;
      } else {
         BlockPos[] ablockpos = new BlockPos[]{pPos.below(), pPos.west(), pPos.east(), pPos.north(), pPos.south(), pPos.below().below()};

         for(BlockPos blockpos : ablockpos) {
            if (pLevel.getBlockState(blockpos).is(this.blockToRemove)) {
               return blockpos;
            }
         }

         return null;
      }
   }

   /**
    * Return true to set given position as destination
    */
   protected boolean isValidTarget(IWorldReader pLevel, BlockPos pPos) {
      IChunk ichunk = pLevel.getChunk(pPos.getX() >> 4, pPos.getZ() >> 4, ChunkStatus.FULL, false);
      if (ichunk == null) {
         return false;
      } else {
         return ichunk.getBlockState(pPos).is(this.blockToRemove) && ichunk.getBlockState(pPos.above()).isAir() && ichunk.getBlockState(pPos.above(2)).isAir();
      }
   }
}
