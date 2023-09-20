package net.minecraft.pathfinding;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.Region;

public abstract class NodeProcessor {
   protected Region level;
   protected MobEntity mob;
   protected final Int2ObjectMap<PathPoint> nodes = new Int2ObjectOpenHashMap<>();
   protected int entityWidth;
   protected int entityHeight;
   protected int entityDepth;
   protected boolean canPassDoors;
   protected boolean canOpenDoors;
   protected boolean canFloat;

   public void prepare(Region p_225578_1_, MobEntity p_225578_2_) {
      this.level = p_225578_1_;
      this.mob = p_225578_2_;
      this.nodes.clear();
      this.entityWidth = MathHelper.floor(p_225578_2_.getBbWidth() + 1.0F);
      this.entityHeight = MathHelper.floor(p_225578_2_.getBbHeight() + 1.0F);
      this.entityDepth = MathHelper.floor(p_225578_2_.getBbWidth() + 1.0F);
   }

   /**
    * This method is called when all nodes have been processed and PathEntity is created.
    * {@link net.minecraft.world.pathfinder.WalkNodeProcessor WalkNodeProcessor} uses this to change its field {@link
    * net.minecraft.world.pathfinder.WalkNodeProcessor#avoidsWater avoidsWater}
    */
   public void done() {
      this.level = null;
      this.mob = null;
   }

   protected PathPoint getNode(BlockPos p_237223_1_) {
      return this.getNode(p_237223_1_.getX(), p_237223_1_.getY(), p_237223_1_.getZ());
   }

   /**
    * Returns a mapped point or creates and adds one
    */
   protected PathPoint getNode(int pX, int pY, int pZ) {
      return this.nodes.computeIfAbsent(PathPoint.createHash(pX, pY, pZ), (p_215743_3_) -> {
         return new PathPoint(pX, pY, pZ);
      });
   }

   public abstract PathPoint getStart();

   public abstract FlaggedPathPoint getGoal(double p_224768_1_, double p_224768_3_, double p_224768_5_);

   public abstract int getNeighbors(PathPoint[] p_222859_1_, PathPoint p_222859_2_);

   /**
    * Returns the significant (e.g LAVA if the entity were half in lava) node type at the location taking the
    * surroundings and the entity size in account
    */
   public abstract PathNodeType getBlockPathType(IBlockReader pBlockaccess, int pX, int pY, int pZ, MobEntity pEntityliving, int pXSize, int pYSize, int pZSize, boolean pCanBreakDoors, boolean pCanEnterDoors);

   /**
    * Returns the node type at the specified postion taking the block below into account
    */
   public abstract PathNodeType getBlockPathType(IBlockReader pLevel, int pX, int pY, int pZ);

   public void setCanPassDoors(boolean pCanEnterDoors) {
      this.canPassDoors = pCanEnterDoors;
   }

   public void setCanOpenDoors(boolean pCanOpenDoors) {
      this.canOpenDoors = pCanOpenDoors;
   }

   public void setCanFloat(boolean pCanSwim) {
      this.canFloat = pCanSwim;
   }

   public boolean canPassDoors() {
      return this.canPassDoors;
   }

   public boolean canOpenDoors() {
      return this.canOpenDoors;
   }

   public boolean canFloat() {
      return this.canFloat;
   }
}