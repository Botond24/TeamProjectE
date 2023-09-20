package net.minecraft.pathfinding;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Path {
   private final List<PathPoint> nodes;
   private PathPoint[] openSet = new PathPoint[0];
   private PathPoint[] closedSet = new PathPoint[0];
   @OnlyIn(Dist.CLIENT)
   private Set<FlaggedPathPoint> targetNodes;
   private int nextNodeIndex;
   private final BlockPos target;
   private final float distToTarget;
   private final boolean reached;

   public Path(List<PathPoint> p_i51804_1_, BlockPos p_i51804_2_, boolean p_i51804_3_) {
      this.nodes = p_i51804_1_;
      this.target = p_i51804_2_;
      this.distToTarget = p_i51804_1_.isEmpty() ? Float.MAX_VALUE : this.nodes.get(this.nodes.size() - 1).distanceManhattan(this.target);
      this.reached = p_i51804_3_;
   }

   /**
    * Directs this path to the next point in its array
    */
   public void advance() {
      ++this.nextNodeIndex;
   }

   public boolean notStarted() {
      return this.nextNodeIndex <= 0;
   }

   /**
    * Returns true if this path has reached the end
    */
   public boolean isDone() {
      return this.nextNodeIndex >= this.nodes.size();
   }

   /**
    * returns the last PathPoint of the Array
    */
   @Nullable
   public PathPoint getEndNode() {
      return !this.nodes.isEmpty() ? this.nodes.get(this.nodes.size() - 1) : null;
   }

   /**
    * return the PathPoint located at the specified PathIndex, usually the current one
    */
   public PathPoint getNode(int pIndex) {
      return this.nodes.get(pIndex);
   }

   public void truncateNodes(int pLength) {
      if (this.nodes.size() > pLength) {
         this.nodes.subList(pLength, this.nodes.size()).clear();
      }

   }

   public void replaceNode(int pIndex, PathPoint pPoint) {
      this.nodes.set(pIndex, pPoint);
   }

   public int getNodeCount() {
      return this.nodes.size();
   }

   public int getNextNodeIndex() {
      return this.nextNodeIndex;
   }

   public void setNextNodeIndex(int pCurrentPathIndex) {
      this.nextNodeIndex = pCurrentPathIndex;
   }

   /**
    * Gets the vector of the PathPoint associated with the given index.
    */
   public Vector3d getEntityPosAtNode(Entity pEntity, int pIndex) {
      PathPoint pathpoint = this.nodes.get(pIndex);
      double d0 = (double)pathpoint.x + (double)((int)(pEntity.getBbWidth() + 1.0F)) * 0.5D;
      double d1 = (double)pathpoint.y;
      double d2 = (double)pathpoint.z + (double)((int)(pEntity.getBbWidth() + 1.0F)) * 0.5D;
      return new Vector3d(d0, d1, d2);
   }

   public BlockPos getNodePos(int p_242947_1_) {
      return this.nodes.get(p_242947_1_).asBlockPos();
   }

   /**
    * returns the current PathEntity target node as Vec3D
    */
   public Vector3d getNextEntityPos(Entity pEntity) {
      return this.getEntityPosAtNode(pEntity, this.nextNodeIndex);
   }

   public BlockPos getNextNodePos() {
      return this.nodes.get(this.nextNodeIndex).asBlockPos();
   }

   public PathPoint getNextNode() {
      return this.nodes.get(this.nextNodeIndex);
   }

   @Nullable
   public PathPoint getPreviousNode() {
      return this.nextNodeIndex > 0 ? this.nodes.get(this.nextNodeIndex - 1) : null;
   }

   /**
    * Returns true if the EntityPath are the same. Non instance related equals.
    */
   public boolean sameAs(@Nullable Path pPathentity) {
      if (pPathentity == null) {
         return false;
      } else if (pPathentity.nodes.size() != this.nodes.size()) {
         return false;
      } else {
         for(int i = 0; i < this.nodes.size(); ++i) {
            PathPoint pathpoint = this.nodes.get(i);
            PathPoint pathpoint1 = pPathentity.nodes.get(i);
            if (pathpoint.x != pathpoint1.x || pathpoint.y != pathpoint1.y || pathpoint.z != pathpoint1.z) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean canReach() {
      return this.reached;
   }

   @OnlyIn(Dist.CLIENT)
   public PathPoint[] getOpenSet() {
      return this.openSet;
   }

   @OnlyIn(Dist.CLIENT)
   public PathPoint[] getClosedSet() {
      return this.closedSet;
   }

   @OnlyIn(Dist.CLIENT)
   public static Path createFromStream(PacketBuffer pBuf) {
      boolean flag = pBuf.readBoolean();
      int i = pBuf.readInt();
      int j = pBuf.readInt();
      Set<FlaggedPathPoint> set = Sets.newHashSet();

      for(int k = 0; k < j; ++k) {
         set.add(FlaggedPathPoint.createFromStream(pBuf));
      }

      BlockPos blockpos = new BlockPos(pBuf.readInt(), pBuf.readInt(), pBuf.readInt());
      List<PathPoint> list = Lists.newArrayList();
      int l = pBuf.readInt();

      for(int i1 = 0; i1 < l; ++i1) {
         list.add(PathPoint.createFromStream(pBuf));
      }

      PathPoint[] apathpoint = new PathPoint[pBuf.readInt()];

      for(int j1 = 0; j1 < apathpoint.length; ++j1) {
         apathpoint[j1] = PathPoint.createFromStream(pBuf);
      }

      PathPoint[] apathpoint1 = new PathPoint[pBuf.readInt()];

      for(int k1 = 0; k1 < apathpoint1.length; ++k1) {
         apathpoint1[k1] = PathPoint.createFromStream(pBuf);
      }

      Path path = new Path(list, blockpos, flag);
      path.openSet = apathpoint;
      path.closedSet = apathpoint1;
      path.targetNodes = set;
      path.nextNodeIndex = i;
      return path;
   }

   public String toString() {
      return "Path(length=" + this.nodes.size() + ")";
   }

   public BlockPos getTarget() {
      return this.target;
   }

   public float getDistToTarget() {
      return this.distToTarget;
   }
}