package net.minecraft.pathfinding;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.Region;
import net.minecraft.world.World;

public abstract class PathNavigator {
   protected final MobEntity mob;
   protected final World level;
   @Nullable
   protected Path path;
   protected double speedModifier;
   protected int tick;
   protected int lastStuckCheck;
   protected Vector3d lastStuckCheckPos = Vector3d.ZERO;
   protected Vector3i timeoutCachedNode = Vector3i.ZERO;
   protected long timeoutTimer;
   protected long lastTimeoutCheck;
   protected double timeoutLimit;
   protected float maxDistanceToWaypoint = 0.5F;
   protected boolean hasDelayedRecomputation;
   protected long timeLastRecompute;
   protected NodeProcessor nodeEvaluator;
   private BlockPos targetPos;
   /** Distance in which a path point counts as target-reaching */
   private int reachRange;
   private float maxVisitedNodesMultiplier = 1.0F;
   private final PathFinder pathFinder;
   private boolean isStuck;

   public PathNavigator(MobEntity pMob, World pLevel) {
      this.mob = pMob;
      this.level = pLevel;
      int i = MathHelper.floor(pMob.getAttributeValue(Attributes.FOLLOW_RANGE) * 16.0D);
      this.pathFinder = this.createPathFinder(i);
   }

   public void resetMaxVisitedNodesMultiplier() {
      this.maxVisitedNodesMultiplier = 1.0F;
   }

   public void setMaxVisitedNodesMultiplier(float pMultiplier) {
      this.maxVisitedNodesMultiplier = pMultiplier;
   }

   public BlockPos getTargetPos() {
      return this.targetPos;
   }

   protected abstract PathFinder createPathFinder(int pMaxVisitedNodes);

   /**
    * Sets the speed
    */
   public void setSpeedModifier(double pSpeed) {
      this.speedModifier = pSpeed;
   }

   /**
    * Returns true if path can be changed by {@link net.minecraft.pathfinding.PathNavigate#onUpdateNavigation()
    * onUpdateNavigation()}
    */
   public boolean hasDelayedRecomputation() {
      return this.hasDelayedRecomputation;
   }

   public void recomputePath() {
      if (this.level.getGameTime() - this.timeLastRecompute > 20L) {
         if (this.targetPos != null) {
            this.path = null;
            this.path = this.createPath(this.targetPos, this.reachRange);
            this.timeLastRecompute = this.level.getGameTime();
            this.hasDelayedRecomputation = false;
         }
      } else {
         this.hasDelayedRecomputation = true;
      }

   }

   /**
    * Returns path to given BlockPos
    */
   @Nullable
   public final Path createPath(double pX, double pY, double pZ, int pAccuracy) {
      return this.createPath(new BlockPos(pX, pY, pZ), pAccuracy);
   }

   /**
    * Returns a path to one of the elements of the stream or null
    */
   @Nullable
   public Path createPath(Stream<BlockPos> pTargets, int pAccuracy) {
      return this.createPath(pTargets.collect(Collectors.toSet()), 8, false, pAccuracy);
   }

   @Nullable
   public Path createPath(Set<BlockPos> pPositions, int pDistance) {
      return this.createPath(pPositions, 8, false, pDistance);
   }

   /**
    * Returns path to given BlockPos
    */
   @Nullable
   public Path createPath(BlockPos pPos, int pAccuracy) {
      return this.createPath(ImmutableSet.of(pPos), 8, false, pAccuracy);
   }

   /**
    * Returns a path to the given entity or null
    */
   @Nullable
   public Path createPath(Entity pEntity, int p_75494_2_) {
      return this.createPath(ImmutableSet.of(pEntity.blockPosition()), 16, true, p_75494_2_);
   }

   /**
    * Returns a path to one of the given targets or null
    */
   @Nullable
   protected Path createPath(Set<BlockPos> pTargets, int pRegionOffset, boolean pOffsetUpward, int pAccuracy) {
      if (pTargets.isEmpty()) {
         return null;
      } else if (this.mob.getY() < 0.0D) {
         return null;
      } else if (!this.canUpdatePath()) {
         return null;
      } else if (this.path != null && !this.path.isDone() && pTargets.contains(this.targetPos)) {
         return this.path;
      } else {
         this.level.getProfiler().push("pathfind");
         float f = (float)this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
         BlockPos blockpos = pOffsetUpward ? this.mob.blockPosition().above() : this.mob.blockPosition();
         int i = (int)(f + (float)pRegionOffset);
         Region region = new Region(this.level, blockpos.offset(-i, -i, -i), blockpos.offset(i, i, i));
         Path path = this.pathFinder.findPath(region, this.mob, pTargets, f, pAccuracy, this.maxVisitedNodesMultiplier);
         this.level.getProfiler().pop();
         if (path != null && path.getTarget() != null) {
            this.targetPos = path.getTarget();
            this.reachRange = pAccuracy;
            this.resetStuckTimeout();
         }

         return path;
      }
   }

   /**
    * Try to find and set a path to XYZ. Returns true if successful. Args : x, y, z, speed
    */
   public boolean moveTo(double pX, double pY, double pZ, double pSpeed) {
      return this.moveTo(this.createPath(pX, pY, pZ, 1), pSpeed);
   }

   /**
    * Try to find and set a path to EntityLiving. Returns true if successful. Args : entity, speed
    */
   public boolean moveTo(Entity pEntity, double pSpeed) {
      Path path = this.createPath(pEntity, 1);
      return path != null && this.moveTo(path, pSpeed);
   }

   /**
    * Sets a new path. If it's diferent from the old path. Checks to adjust path for sun avoiding, and stores start
    * coords. Args : path, speed
    */
   public boolean moveTo(@Nullable Path pPathentity, double pSpeed) {
      if (pPathentity == null) {
         this.path = null;
         return false;
      } else {
         if (!pPathentity.sameAs(this.path)) {
            this.path = pPathentity;
         }

         if (this.isDone()) {
            return false;
         } else {
            this.trimPath();
            if (this.path.getNodeCount() <= 0) {
               return false;
            } else {
               this.speedModifier = pSpeed;
               Vector3d vector3d = this.getTempMobPos();
               this.lastStuckCheck = this.tick;
               this.lastStuckCheckPos = vector3d;
               return true;
            }
         }
      }
   }

   /**
    * gets the actively used PathEntity
    */
   @Nullable
   public Path getPath() {
      return this.path;
   }

   public void tick() {
      ++this.tick;
      if (this.hasDelayedRecomputation) {
         this.recomputePath();
      }

      if (!this.isDone()) {
         if (this.canUpdatePath()) {
            this.followThePath();
         } else if (this.path != null && !this.path.isDone()) {
            Vector3d vector3d = this.getTempMobPos();
            Vector3d vector3d1 = this.path.getNextEntityPos(this.mob);
            if (vector3d.y > vector3d1.y && !this.mob.isOnGround() && MathHelper.floor(vector3d.x) == MathHelper.floor(vector3d1.x) && MathHelper.floor(vector3d.z) == MathHelper.floor(vector3d1.z)) {
               this.path.advance();
            }
         }

         DebugPacketSender.sendPathFindingPacket(this.level, this.mob, this.path, this.maxDistanceToWaypoint);
         if (!this.isDone()) {
            Vector3d vector3d2 = this.path.getNextEntityPos(this.mob);
            BlockPos blockpos = new BlockPos(vector3d2);
            this.mob.getMoveControl().setWantedPosition(vector3d2.x, this.level.getBlockState(blockpos.below()).isAir() ? vector3d2.y : WalkNodeProcessor.getFloorLevel(this.level, blockpos), vector3d2.z, this.speedModifier);
         }
      }
   }

   protected void followThePath() {
      Vector3d vector3d = this.getTempMobPos();
      this.maxDistanceToWaypoint = this.mob.getBbWidth() > 0.75F ? this.mob.getBbWidth() / 2.0F : 0.75F - this.mob.getBbWidth() / 2.0F;
      Vector3i vector3i = this.path.getNextNodePos();
      double d0 = Math.abs(this.mob.getX() - ((double)vector3i.getX() + (this.mob.getBbWidth() + 1) / 2D)); //Forge: Fix MC-94054
      double d1 = Math.abs(this.mob.getY() - (double)vector3i.getY());
      double d2 = Math.abs(this.mob.getZ() - ((double)vector3i.getZ() + (this.mob.getBbWidth() + 1) / 2D)); //Forge: Fix MC-94054
      boolean flag = d0 <= (double)this.maxDistanceToWaypoint && d2 <= (double)this.maxDistanceToWaypoint && d1 < 1.0D; //Forge: Fix MC-94054
      if (flag || this.mob.canCutCorner(this.path.getNextNode().type) && this.shouldTargetNextNodeInDirection(vector3d)) {
         this.path.advance();
      }

      this.doStuckDetection(vector3d);
   }

   private boolean shouldTargetNextNodeInDirection(Vector3d p_234112_1_) {
      if (this.path.getNextNodeIndex() + 1 >= this.path.getNodeCount()) {
         return false;
      } else {
         Vector3d vector3d = Vector3d.atBottomCenterOf(this.path.getNextNodePos());
         if (!p_234112_1_.closerThan(vector3d, 2.0D)) {
            return false;
         } else {
            Vector3d vector3d1 = Vector3d.atBottomCenterOf(this.path.getNodePos(this.path.getNextNodeIndex() + 1));
            Vector3d vector3d2 = vector3d1.subtract(vector3d);
            Vector3d vector3d3 = p_234112_1_.subtract(vector3d);
            return vector3d2.dot(vector3d3) > 0.0D;
         }
      }
   }

   /**
    * Checks if entity haven't been moved when last checked and if so, clears current {@link
    * net.minecraft.pathfinding.PathEntity}
    */
   protected void doStuckDetection(Vector3d pPositionVec3) {
      if (this.tick - this.lastStuckCheck > 100) {
         if (pPositionVec3.distanceToSqr(this.lastStuckCheckPos) < 2.25D) {
            this.isStuck = true;
            this.stop();
         } else {
            this.isStuck = false;
         }

         this.lastStuckCheck = this.tick;
         this.lastStuckCheckPos = pPositionVec3;
      }

      if (this.path != null && !this.path.isDone()) {
         Vector3i vector3i = this.path.getNextNodePos();
         if (vector3i.equals(this.timeoutCachedNode)) {
            this.timeoutTimer += Util.getMillis() - this.lastTimeoutCheck;
         } else {
            this.timeoutCachedNode = vector3i;
            double d0 = pPositionVec3.distanceTo(Vector3d.atBottomCenterOf(this.timeoutCachedNode));
            this.timeoutLimit = this.mob.getSpeed() > 0.0F ? d0 / (double)this.mob.getSpeed() * 1000.0D : 0.0D;
         }

         if (this.timeoutLimit > 0.0D && (double)this.timeoutTimer > this.timeoutLimit * 3.0D) {
            this.timeoutPath();
         }

         this.lastTimeoutCheck = Util.getMillis();
      }

   }

   private void timeoutPath() {
      this.resetStuckTimeout();
      this.stop();
   }

   private void resetStuckTimeout() {
      this.timeoutCachedNode = Vector3i.ZERO;
      this.timeoutTimer = 0L;
      this.timeoutLimit = 0.0D;
      this.isStuck = false;
   }

   /**
    * If null path or reached the end
    */
   public boolean isDone() {
      return this.path == null || this.path.isDone();
   }

   public boolean isInProgress() {
      return !this.isDone();
   }

   /**
    * sets active PathEntity to null
    */
   public void stop() {
      this.path = null;
   }

   protected abstract Vector3d getTempMobPos();

   /**
    * If on ground or swimming and can swim
    */
   protected abstract boolean canUpdatePath();

   /**
    * Returns true if the entity is in water or lava, false otherwise
    */
   protected boolean isInLiquid() {
      return this.mob.isInWaterOrBubble() || this.mob.isInLava();
   }

   /**
    * Trims path data from the end to the first sun covered block
    */
   protected void trimPath() {
      if (this.path != null) {
         for(int i = 0; i < this.path.getNodeCount(); ++i) {
            PathPoint pathpoint = this.path.getNode(i);
            PathPoint pathpoint1 = i + 1 < this.path.getNodeCount() ? this.path.getNode(i + 1) : null;
            BlockState blockstate = this.level.getBlockState(new BlockPos(pathpoint.x, pathpoint.y, pathpoint.z));
            if (blockstate.is(Blocks.CAULDRON)) {
               this.path.replaceNode(i, pathpoint.cloneAndMove(pathpoint.x, pathpoint.y + 1, pathpoint.z));
               if (pathpoint1 != null && pathpoint.y >= pathpoint1.y) {
                  this.path.replaceNode(i + 1, pathpoint.cloneAndMove(pathpoint1.x, pathpoint.y + 1, pathpoint1.z));
               }
            }
         }

      }
   }

   /**
    * Checks if the specified entity can safely walk to the specified location.
    */
   protected abstract boolean canMoveDirectly(Vector3d pPosVec31, Vector3d pPosVec32, int pSizeX, int pSizeY, int pSizeZ);

   public boolean isStableDestination(BlockPos pPos) {
      BlockPos blockpos = pPos.below();
      return this.level.getBlockState(blockpos).isSolidRender(this.level, blockpos);
   }

   public NodeProcessor getNodeEvaluator() {
      return this.nodeEvaluator;
   }

   public void setCanFloat(boolean pCanSwim) {
      this.nodeEvaluator.setCanFloat(pCanSwim);
   }

   public boolean canFloat() {
      return this.nodeEvaluator.canFloat();
   }

   public void recomputePath(BlockPos pPos) {
      if (this.path != null && !this.path.isDone() && this.path.getNodeCount() != 0) {
         PathPoint pathpoint = this.path.getEndNode();
         Vector3d vector3d = new Vector3d(((double)pathpoint.x + this.mob.getX()) / 2.0D, ((double)pathpoint.y + this.mob.getY()) / 2.0D, ((double)pathpoint.z + this.mob.getZ()) / 2.0D);
         if (pPos.closerThan(vector3d, (double)(this.path.getNodeCount() - this.path.getNextNodeIndex()))) {
            this.recomputePath();
         }

      }
   }

   public boolean isStuck() {
      return this.isStuck;
   }
}
