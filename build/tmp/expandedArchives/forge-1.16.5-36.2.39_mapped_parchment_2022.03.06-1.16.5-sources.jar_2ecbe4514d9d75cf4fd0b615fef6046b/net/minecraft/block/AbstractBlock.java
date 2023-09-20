package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.Property;
import net.minecraft.state.StateHolder;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.BlockVoxelShape;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.EmptyBlockReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

//TODO, Delegates are weird here now, because Block extends this.
public abstract class AbstractBlock extends net.minecraftforge.registries.ForgeRegistryEntry<Block> {
   protected static final Direction[] UPDATE_SHAPE_ORDER = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.DOWN, Direction.UP};
   protected final Material material;
   protected final boolean hasCollision;
   protected final float explosionResistance;
   /** Whether this blocks receives random ticks */
   protected final boolean isRandomlyTicking;
   protected final SoundType soundType;
   /** Determines how much velocity is maintained while moving on top of this block */
   protected final float friction;
   protected final float speedFactor;
   protected final float jumpFactor;
   protected final boolean dynamicShape;
   protected final AbstractBlock.Properties properties;
   @Nullable
   protected ResourceLocation drops;

   public AbstractBlock(AbstractBlock.Properties pProperties) {
      this.material = pProperties.material;
      this.hasCollision = pProperties.hasCollision;
      this.drops = pProperties.drops;
      this.explosionResistance = pProperties.explosionResistance;
      this.isRandomlyTicking = pProperties.isRandomlyTicking;
      this.soundType = pProperties.soundType;
      this.friction = pProperties.friction;
      this.speedFactor = pProperties.speedFactor;
      this.jumpFactor = pProperties.jumpFactor;
      this.dynamicShape = pProperties.dynamicShape;
      this.properties = pProperties;
      final ResourceLocation lootTableCache = pProperties.drops;
      this.lootTableSupplier = lootTableCache != null ? () -> lootTableCache : pProperties.lootTableSupplier != null ? pProperties.lootTableSupplier : () -> new ResourceLocation(this.getRegistryName().getNamespace(), "blocks/" + this.getRegistryName().getPath());
   }

   /**
    * performs updates on diagonal neighbors of the target position and passes in the flags. The flags can be referenced
    * from the docs for {@link IWorldWriter#setBlockState(IBlockState, BlockPos, int)}.
    */
   @Deprecated
   public void updateIndirectNeighbourShapes(BlockState pState, IWorld pLevel, BlockPos pPos, int pFlags, int pRecursionLeft) {
   }

   @Deprecated
   public boolean isPathfindable(BlockState pState, IBlockReader pLevel, BlockPos pPos, PathType pType) {
      switch(pType) {
      case LAND:
         return !pState.isCollisionShapeFullBlock(pLevel, pPos);
      case WATER:
         return pLevel.getFluidState(pPos).is(FluidTags.WATER);
      case AIR:
         return !pState.isCollisionShapeFullBlock(pLevel, pPos);
      default:
         return false;
      }
   }

   /**
    * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific face passed in.
    */
   @Deprecated
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      return pState;
   }

   @Deprecated
   @OnlyIn(Dist.CLIENT)
   public boolean skipRendering(BlockState pState, BlockState pAdjacentBlockState, Direction pSide) {
      return false;
   }

   @Deprecated
   public void neighborChanged(BlockState pState, World pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      DebugPacketSender.sendNeighborsUpdatePacket(pLevel, pPos);
   }

   @Deprecated
   public void onPlace(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
   }

   @Deprecated
   public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (pState.hasTileEntity() && (!pState.is(pNewState.getBlock()) || !pNewState.hasTileEntity())) {
         pLevel.removeBlockEntity(pPos);
      }

   }

   @Deprecated
   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      return ActionResultType.PASS;
   }

   /**
    * Called on server when World#addBlockEvent is called. If server returns true, then also called on the client. On
    * the Server, this may perform additional changes to the world, like pistons replacing the block with an extended
    * base. On the client, the update may involve replacing tile entities or effects such as sounds or particles
    * @deprecated call via {@link IBlockState#onBlockEventReceived(World,BlockPos,int,int)} whenever possible.
    * Implementing/overriding is fine.
    */
   @Deprecated
   public boolean triggerEvent(BlockState pState, World pLevel, BlockPos pPos, int pId, int pParam) {
      return false;
   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link IBlockState#getRenderType()} whenever possible. Implementing/overriding is fine.
    */
   @Deprecated
   public BlockRenderType getRenderShape(BlockState pState) {
      return BlockRenderType.MODEL;
   }

   @Deprecated
   public boolean useShapeForLightOcclusion(BlockState pState) {
      return false;
   }

   /**
    * Can this block provide power. Only wire currently seems to have this change based on its state.
    * @deprecated call via {@link IBlockState#canProvidePower()} whenever possible. Implementing/overriding is fine.
    */
   @Deprecated
   public boolean isSignalSource(BlockState pState) {
      return false;
   }

   /**
    * @deprecated call via {@link IBlockState#getMobilityFlag()} whenever possible. Implementing/overriding is fine.
    */
   @Deprecated
   public PushReaction getPistonPushReaction(BlockState pState) {
      return this.material.getPushReaction();
   }

   @Deprecated
   public FluidState getFluidState(BlockState pState) {
      return Fluids.EMPTY.defaultFluidState();
   }

   /**
    * @deprecated call via {@link IBlockState#hasComparatorInputOverride()} whenever possible. Implementing/overriding
    * is fine.
    */
   @Deprecated
   public boolean hasAnalogOutputSignal(BlockState pState) {
      return false;
   }

   /**
    * Get the OffsetType for this Block. Determines if the model is rendered slightly offset.
    */
   public AbstractBlock.OffsetType getOffsetType() {
      return AbstractBlock.OffsetType.NONE;
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link IBlockState#withRotation(Rotation)} whenever possible. Implementing/overriding is
    * fine.
    */
   @Deprecated
   public BlockState rotate(BlockState pState, Rotation pRotation) {
      return pState;
   }

   /**
    * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link IBlockState#withMirror(Mirror)} whenever possible. Implementing/overriding is fine.
    */
   @Deprecated
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      return pState;
   }

   @Deprecated
   public boolean canBeReplaced(BlockState pState, BlockItemUseContext pUseContext) {
      return pState.getMaterial().isReplaceable() && (pUseContext.getItemInHand().isEmpty() || pUseContext.getItemInHand().getItem() != this.asItem());
   }

   @Deprecated
   public boolean canBeReplaced(BlockState pState, Fluid pFluid) {
      return this.material.isReplaceable() || !this.material.isSolid();
   }

   @Deprecated
   public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
      ResourceLocation resourcelocation = this.getLootTable();
      if (resourcelocation == LootTables.EMPTY) {
         return Collections.emptyList();
      } else {
         LootContext lootcontext = pBuilder.withParameter(LootParameters.BLOCK_STATE, pState).create(LootParameterSets.BLOCK);
         ServerWorld serverworld = lootcontext.getLevel();
         LootTable loottable = serverworld.getServer().getLootTables().get(resourcelocation);
         return loottable.getRandomItems(lootcontext);
      }
   }

   /**
    * Return a random long to be passed to {@link IBakedModel#getQuads}, used for random model rotations
    */
   @Deprecated
   @OnlyIn(Dist.CLIENT)
   public long getSeed(BlockState pState, BlockPos pPos) {
      return MathHelper.getSeed(pPos);
   }

   @Deprecated
   public VoxelShape getOcclusionShape(BlockState pState, IBlockReader pLevel, BlockPos pPos) {
      return pState.getShape(pLevel, pPos);
   }

   @Deprecated
   public VoxelShape getBlockSupportShape(BlockState pState, IBlockReader pReader, BlockPos pPos) {
      return this.getCollisionShape(pState, pReader, pPos, ISelectionContext.empty());
   }

   @Deprecated
   public VoxelShape getInteractionShape(BlockState pState, IBlockReader pLevel, BlockPos pPos) {
      return VoxelShapes.empty();
   }

   @Deprecated
   public int getLightBlock(BlockState pState, IBlockReader pLevel, BlockPos pPos) {
      if (pState.isSolidRender(pLevel, pPos)) {
         return pLevel.getMaxLightLevel();
      } else {
         return pState.propagatesSkylightDown(pLevel, pPos) ? 0 : 1;
      }
   }

   @Nullable
   @Deprecated
   public INamedContainerProvider getMenuProvider(BlockState pState, World pLevel, BlockPos pPos) {
      return null;
   }

   @Deprecated
   public boolean canSurvive(BlockState pState, IWorldReader pLevel, BlockPos pPos) {
      return true;
   }

   @Deprecated
   @OnlyIn(Dist.CLIENT)
   public float getShadeBrightness(BlockState pState, IBlockReader pLevel, BlockPos pPos) {
      return pState.isCollisionShapeFullBlock(pLevel, pPos) ? 0.2F : 1.0F;
   }

   /**
    * @deprecated call via {@link IBlockState#getComparatorInputOverride(World,BlockPos)} whenever possible.
    * Implementing/overriding is fine.
    */
   @Deprecated
   public int getAnalogOutputSignal(BlockState pBlockState, World pLevel, BlockPos pPos) {
      return 0;
   }

   @Deprecated
   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return VoxelShapes.block();
   }

   @Deprecated
   public VoxelShape getCollisionShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return this.hasCollision ? pState.getShape(pLevel, pPos) : VoxelShapes.empty();
   }

   @Deprecated
   public VoxelShape getVisualShape(BlockState pState, IBlockReader pReader, BlockPos pPos, ISelectionContext pContext) {
      return this.getCollisionShape(pState, pReader, pPos, pContext);
   }

   /**
    * Performs a random tick on a block.
    */
   @Deprecated
   public void randomTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
      this.tick(pState, pLevel, pPos, pRandom);
   }

   @Deprecated
   public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
   }

   /**
    * Get the hardness of this Block relative to the ability of the given player
    * @deprecated call via {@link IBlockState#getPlayerRelativeBlockHardness(EntityPlayer,World,BlockPos)} whenever
    * possible. Implementing/overriding is fine.
    */
   @Deprecated
   public float getDestroyProgress(BlockState pState, PlayerEntity pPlayer, IBlockReader pLevel, BlockPos pPos) {
      float f = pState.getDestroySpeed(pLevel, pPos);
      if (f == -1.0F) {
         return 0.0F;
      } else {
         int i = net.minecraftforge.common.ForgeHooks.canHarvestBlock(pState, pPlayer, pLevel, pPos) ? 30 : 100;
         return pPlayer.getDigSpeed(pState, pPos) / f / (float)i;
      }
   }

   /**
    * Perform side-effects from block dropping, such as creating silverfish
    */
   @Deprecated
   public void spawnAfterBreak(BlockState pState, ServerWorld pLevel, BlockPos pPos, ItemStack pStack) {
   }

   @Deprecated
   public void attack(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer) {
   }

   /**
    * @deprecated call via {@link IBlockState#getWeakPower(IBlockAccess,BlockPos,EnumFacing)} whenever possible.
    * Implementing/overriding is fine.
    */
   @Deprecated
   public int getSignal(BlockState pBlockState, IBlockReader pBlockAccess, BlockPos pPos, Direction pSide) {
      return 0;
   }

   @Deprecated
   public void entityInside(BlockState pState, World pLevel, BlockPos pPos, Entity pEntity) {
   }

   /**
    * @deprecated call via {@link IBlockState#getStrongPower(IBlockAccess,BlockPos,EnumFacing)} whenever possible.
    * Implementing/overriding is fine.
    */
   @Deprecated
   public int getDirectSignal(BlockState pBlockState, IBlockReader pBlockAccess, BlockPos pPos, Direction pSide) {
      return 0;
   }

   @Deprecated //Forge: Use state.hasTileEntity()
   public final boolean isEntityBlock() {
      return this instanceof ITileEntityProvider;
   }

   public final ResourceLocation getLootTable() {
      if (this.drops == null) {
         this.drops = this.lootTableSupplier.get();
      }

      return this.drops;
   }

   @Deprecated
   public void onProjectileHit(World pLevel, BlockState pState, BlockRayTraceResult pHit, ProjectileEntity pProjectile) {
   }

   public abstract Item asItem();

   protected abstract Block asBlock();

   public MaterialColor defaultMaterialColor() {
      return this.properties.materialColor.apply(this.asBlock().defaultBlockState());
   }

   protected boolean isAir(BlockState state) {
      return ((AbstractBlockState)state).isAir;
   }

   /* ======================================== FORGE START ===================================== */
   private final java.util.function.Supplier<ResourceLocation> lootTableSupplier;
   /* ========================================= FORGE END ====================================== */

   public abstract static class AbstractBlockState extends StateHolder<Block, BlockState> {
      private final int lightEmission;
      private final boolean useShapeForLightOcclusion;
      private final boolean isAir;
      private final Material material;
      private final MaterialColor materialColor;
      private final float destroySpeed;
      private final boolean requiresCorrectToolForDrops;
      private final boolean canOcclude;
      private final AbstractBlock.IPositionPredicate isRedstoneConductor;
      private final AbstractBlock.IPositionPredicate isSuffocating;
      private final AbstractBlock.IPositionPredicate isViewBlocking;
      private final AbstractBlock.IPositionPredicate hasPostProcess;
      private final AbstractBlock.IPositionPredicate emissiveRendering;
      @Nullable
      protected AbstractBlock.AbstractBlockState.Cache cache;

      protected AbstractBlockState(Block pOwner, ImmutableMap<Property<?>, Comparable<?>> pValues, MapCodec<BlockState> pPropertiesCodec) {
         super(pOwner, pValues, pPropertiesCodec);
         AbstractBlock.Properties abstractblock$properties = pOwner.properties;
         this.lightEmission = abstractblock$properties.lightEmission.applyAsInt(this.asState());
         this.useShapeForLightOcclusion = pOwner.useShapeForLightOcclusion(this.asState());
         this.isAir = abstractblock$properties.isAir;
         this.material = abstractblock$properties.material;
         this.materialColor = abstractblock$properties.materialColor.apply(this.asState());
         this.destroySpeed = abstractblock$properties.destroyTime;
         this.requiresCorrectToolForDrops = abstractblock$properties.requiresCorrectToolForDrops;
         this.canOcclude = abstractblock$properties.canOcclude;
         this.isRedstoneConductor = abstractblock$properties.isRedstoneConductor;
         this.isSuffocating = abstractblock$properties.isSuffocating;
         this.isViewBlocking = abstractblock$properties.isViewBlocking;
         this.hasPostProcess = abstractblock$properties.hasPostProcess;
         this.emissiveRendering = abstractblock$properties.emissiveRendering;
      }

      public void initCache() {
         if (!this.getBlock().hasDynamicShape()) {
            this.cache = new AbstractBlock.AbstractBlockState.Cache(this.asState());
         }

      }

      public Block getBlock() {
         return this.owner;
      }

      public Material getMaterial() {
         return this.material;
      }

      public boolean isValidSpawn(IBlockReader pLevel, BlockPos pPos, EntityType<?> pEntityType) {
         return this.getBlock().properties.isValidSpawn.test(this.asState(), pLevel, pPos, pEntityType);
      }

      public boolean propagatesSkylightDown(IBlockReader pLevel, BlockPos pPos) {
         return this.cache != null ? this.cache.propagatesSkylightDown : this.getBlock().propagatesSkylightDown(this.asState(), pLevel, pPos);
      }

      public int getLightBlock(IBlockReader pLevel, BlockPos pPos) {
         return this.cache != null ? this.cache.lightBlock : this.getBlock().getLightBlock(this.asState(), pLevel, pPos);
      }

      public VoxelShape getFaceOcclusionShape(IBlockReader pLevel, BlockPos pPos, Direction pDirection) {
         return this.cache != null && this.cache.occlusionShapes != null ? this.cache.occlusionShapes[pDirection.ordinal()] : VoxelShapes.getFaceShape(this.getOcclusionShape(pLevel, pPos), pDirection);
      }

      public VoxelShape getOcclusionShape(IBlockReader pLevel, BlockPos pPos) {
         return this.getBlock().getOcclusionShape(this.asState(), pLevel, pPos);
      }

      public boolean hasLargeCollisionShape() {
         return this.cache == null || this.cache.largeCollisionShape;
      }

      public boolean useShapeForLightOcclusion() {
         return this.useShapeForLightOcclusion;
      }

      /** @deprecated use {@link BlockState#getLightValue(IBlockReader, BlockPos)} */
      @Deprecated
      public int getLightEmission() {
         return this.lightEmission;
      }

      /** @deprecated use {@link BlockState#isAir(IBlockReader, BlockPos)} until 1.17, at which point this method will be undreprecated. See https://github.com/MinecraftForge/MinecraftForge/issues/7409 for more details */
      @Deprecated
      public boolean isAir() {
         return this.getBlock().isAir((BlockState)this);
      }

      public MaterialColor getMapColor(IBlockReader pLevel, BlockPos pPos) {
         return this.materialColor;
      }

      /** @deprecated use {@link BlockState#rotate(IWorld, BlockPos, Rotation)} */
      /**
       * @return the blockstate with the given rotation. If inapplicable, returns itself.
       */
      @Deprecated
      public BlockState rotate(Rotation pRotation) {
         return this.getBlock().rotate(this.asState(), pRotation);
      }

      /**
       * @return the blockstate mirrored in the given way. If inapplicable, returns itself.
       */
      public BlockState mirror(Mirror pMirror) {
         return this.getBlock().mirror(this.asState(), pMirror);
      }

      public BlockRenderType getRenderShape() {
         return this.getBlock().getRenderShape(this.asState());
      }

      @OnlyIn(Dist.CLIENT)
      public boolean emissiveRendering(IBlockReader pLevel, BlockPos pPos) {
         return this.emissiveRendering.test(this.asState(), pLevel, pPos);
      }

      @OnlyIn(Dist.CLIENT)
      public float getShadeBrightness(IBlockReader pLevel, BlockPos pPos) {
         return this.getBlock().getShadeBrightness(this.asState(), pLevel, pPos);
      }

      public boolean isRedstoneConductor(IBlockReader pLevel, BlockPos pPos) {
         return this.isRedstoneConductor.test(this.asState(), pLevel, pPos);
      }

      public boolean isSignalSource() {
         return this.getBlock().isSignalSource(this.asState());
      }

      public int getSignal(IBlockReader pLevel, BlockPos pPos, Direction pDirection) {
         return this.getBlock().getSignal(this.asState(), pLevel, pPos, pDirection);
      }

      public boolean hasAnalogOutputSignal() {
         return this.getBlock().hasAnalogOutputSignal(this.asState());
      }

      public int getAnalogOutputSignal(World pLevel, BlockPos pPos) {
         return this.getBlock().getAnalogOutputSignal(this.asState(), pLevel, pPos);
      }

      public float getDestroySpeed(IBlockReader pLevel, BlockPos pPos) {
         return this.destroySpeed;
      }

      public float getDestroyProgress(PlayerEntity pPlayer, IBlockReader pLevel, BlockPos pPos) {
         return this.getBlock().getDestroyProgress(this.asState(), pPlayer, pLevel, pPos);
      }

      public int getDirectSignal(IBlockReader pLevel, BlockPos pPos, Direction pDirection) {
         return this.getBlock().getDirectSignal(this.asState(), pLevel, pPos, pDirection);
      }

      public PushReaction getPistonPushReaction() {
         return this.getBlock().getPistonPushReaction(this.asState());
      }

      public boolean isSolidRender(IBlockReader pLevel, BlockPos pPos) {
         if (this.cache != null) {
            return this.cache.solidRender;
         } else {
            BlockState blockstate = this.asState();
            return blockstate.canOcclude() ? Block.isShapeFullBlock(blockstate.getOcclusionShape(pLevel, pPos)) : false;
         }
      }

      public boolean canOcclude() {
         return this.canOcclude;
      }

      @OnlyIn(Dist.CLIENT)
      public boolean skipRendering(BlockState pState, Direction pFace) {
         return this.getBlock().skipRendering(this.asState(), pState, pFace);
      }

      public VoxelShape getShape(IBlockReader pLevel, BlockPos pPos) {
         return this.getShape(pLevel, pPos, ISelectionContext.empty());
      }

      public VoxelShape getShape(IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
         return this.getBlock().getShape(this.asState(), pLevel, pPos, pContext);
      }

      public VoxelShape getCollisionShape(IBlockReader pLevel, BlockPos pPos) {
         return this.cache != null ? this.cache.collisionShape : this.getCollisionShape(pLevel, pPos, ISelectionContext.empty());
      }

      public VoxelShape getCollisionShape(IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
         return this.getBlock().getCollisionShape(this.asState(), pLevel, pPos, pContext);
      }

      public VoxelShape getBlockSupportShape(IBlockReader pLevel, BlockPos pPos) {
         return this.getBlock().getBlockSupportShape(this.asState(), pLevel, pPos);
      }

      public VoxelShape getVisualShape(IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
         return this.getBlock().getVisualShape(this.asState(), pLevel, pPos, pContext);
      }

      public VoxelShape getInteractionShape(IBlockReader pLevel, BlockPos pPos) {
         return this.getBlock().getInteractionShape(this.asState(), pLevel, pPos);
      }

      public final boolean entityCanStandOn(IBlockReader pLevel, BlockPos pPos, Entity pEntity) {
         return this.entityCanStandOnFace(pLevel, pPos, pEntity, Direction.UP);
      }

      /**
       * @return true if the collision box of this state covers the entire upper face of the blockspace
       */
      public final boolean entityCanStandOnFace(IBlockReader pLevel, BlockPos pPos, Entity pEntity, Direction pFace) {
         return Block.isFaceFull(this.getCollisionShape(pLevel, pPos, ISelectionContext.of(pEntity)), pFace);
      }

      public Vector3d getOffset(IBlockReader pLevel, BlockPos pPos) {
         AbstractBlock.OffsetType abstractblock$offsettype = this.getBlock().getOffsetType();
         if (abstractblock$offsettype == AbstractBlock.OffsetType.NONE) {
            return Vector3d.ZERO;
         } else {
            long i = MathHelper.getSeed(pPos.getX(), 0, pPos.getZ());
            return new Vector3d(((double)((float)(i & 15L) / 15.0F) - 0.5D) * 0.5D, abstractblock$offsettype == AbstractBlock.OffsetType.XYZ ? ((double)((float)(i >> 4 & 15L) / 15.0F) - 1.0D) * 0.2D : 0.0D, ((double)((float)(i >> 8 & 15L) / 15.0F) - 0.5D) * 0.5D);
         }
      }

      public boolean triggerEvent(World pLevel, BlockPos pPos, int pId, int pParam) {
         return this.getBlock().triggerEvent(this.asState(), pLevel, pPos, pId, pParam);
      }

      public void neighborChanged(World pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
         this.getBlock().neighborChanged(this.asState(), pLevel, pPos, pBlock, pFromPos, pIsMoving);
      }

      public final void updateNeighbourShapes(IWorld pLevel, BlockPos pPos, int pFlag) {
         this.updateNeighbourShapes(pLevel, pPos, pFlag, 512);
      }

      public final void updateNeighbourShapes(IWorld pLevel, BlockPos pPos, int pFlag, int pRecursionLeft) {
         this.getBlock();
         BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

         for(Direction direction : AbstractBlock.UPDATE_SHAPE_ORDER) {
            blockpos$mutable.setWithOffset(pPos, direction);
            BlockState blockstate = pLevel.getBlockState(blockpos$mutable);
            BlockState blockstate1 = blockstate.updateShape(direction.getOpposite(), this.asState(), pLevel, blockpos$mutable, pPos);
            Block.updateOrDestroy(blockstate, blockstate1, pLevel, blockpos$mutable, pFlag, pRecursionLeft);
         }

      }

      /**
       * Performs validations on the block state and possibly neighboring blocks to validate whether the incoming state
       * is valid to stay in the world. Currently used only by redstone wire to update itself if neighboring blocks have
       * changed and to possibly break itself.
       */
      public final void updateIndirectNeighbourShapes(IWorld pLevel, BlockPos pPos, int pFlags) {
         this.updateIndirectNeighbourShapes(pLevel, pPos, pFlags, 512);
      }

      public void updateIndirectNeighbourShapes(IWorld pLevel, BlockPos pPos, int pFlags, int pRecursionLeft) {
         this.getBlock().updateIndirectNeighbourShapes(this.asState(), pLevel, pPos, pFlags, pRecursionLeft);
      }

      public void onPlace(World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
         this.getBlock().onPlace(this.asState(), pLevel, pPos, pOldState, pIsMoving);
      }

      public void onRemove(World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
         this.getBlock().onRemove(this.asState(), pLevel, pPos, pNewState, pIsMoving);
      }

      public void tick(ServerWorld pLevel, BlockPos pPos, Random pRandom) {
         this.getBlock().tick(this.asState(), pLevel, pPos, pRandom);
      }

      public void randomTick(ServerWorld pLevel, BlockPos pPos, Random pRandom) {
         this.getBlock().randomTick(this.asState(), pLevel, pPos, pRandom);
      }

      public void entityInside(World pLevel, BlockPos pPos, Entity pEntity) {
         this.getBlock().entityInside(this.asState(), pLevel, pPos, pEntity);
      }

      public void spawnAfterBreak(ServerWorld pLevel, BlockPos pPos, ItemStack pStack) {
         this.getBlock().spawnAfterBreak(this.asState(), pLevel, pPos, pStack);
      }

      public List<ItemStack> getDrops(LootContext.Builder pBuilder) {
         return this.getBlock().getDrops(this.asState(), pBuilder);
      }

      public ActionResultType use(World pLevel, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pResult) {
         return this.getBlock().use(this.asState(), pLevel, pResult.getBlockPos(), pPlayer, pHand, pResult);
      }

      public void attack(World pLevel, BlockPos pPos, PlayerEntity pPlayer) {
         this.getBlock().attack(this.asState(), pLevel, pPos, pPlayer);
      }

      public boolean isSuffocating(IBlockReader pLevel, BlockPos pPos) {
         return this.isSuffocating.test(this.asState(), pLevel, pPos);
      }

      @OnlyIn(Dist.CLIENT)
      public boolean isViewBlocking(IBlockReader pLevel, BlockPos pPos) {
         return this.isViewBlocking.test(this.asState(), pLevel, pPos);
      }

      public BlockState updateShape(Direction pDirection, BlockState pQueried, IWorld pLevel, BlockPos pCurrentPos, BlockPos pOffsetPos) {
         return this.getBlock().updateShape(this.asState(), pDirection, pQueried, pLevel, pCurrentPos, pOffsetPos);
      }

      public boolean isPathfindable(IBlockReader pLevel, BlockPos pPos, PathType pType) {
         return this.getBlock().isPathfindable(this.asState(), pLevel, pPos, pType);
      }

      public boolean canBeReplaced(BlockItemUseContext pUseContext) {
         return this.getBlock().canBeReplaced(this.asState(), pUseContext);
      }

      public boolean canBeReplaced(Fluid pFluid) {
         return this.getBlock().canBeReplaced(this.asState(), pFluid);
      }

      public boolean canSurvive(IWorldReader pLevel, BlockPos pPos) {
         return this.getBlock().canSurvive(this.asState(), pLevel, pPos);
      }

      public boolean hasPostProcess(IBlockReader pLevel, BlockPos pPos) {
         return this.hasPostProcess.test(this.asState(), pLevel, pPos);
      }

      @Nullable
      public INamedContainerProvider getMenuProvider(World pLevel, BlockPos pPos) {
         return this.getBlock().getMenuProvider(this.asState(), pLevel, pPos);
      }

      public boolean is(ITag<Block> pTag) {
         return this.getBlock().is(pTag);
      }

      public boolean is(ITag<Block> pTag, Predicate<AbstractBlock.AbstractBlockState> pPredicate) {
         return this.getBlock().is(pTag) && pPredicate.test(this);
      }

      public boolean is(Block pTag) {
         return this.getBlock().is(pTag);
      }

      public FluidState getFluidState() {
         return this.getBlock().getFluidState(this.asState());
      }

      public boolean isRandomlyTicking() {
         return this.getBlock().isRandomlyTicking(this.asState());
      }

      @OnlyIn(Dist.CLIENT)
      public long getSeed(BlockPos pPos) {
         return this.getBlock().getSeed(this.asState(), pPos);
      }

      public SoundType getSoundType() {
         return this.getBlock().getSoundType(this.asState());
      }

      public void onProjectileHit(World pLevel, BlockState pState, BlockRayTraceResult pHit, ProjectileEntity pProjectile) {
         this.getBlock().onProjectileHit(pLevel, pState, pHit, pProjectile);
      }

      public boolean isFaceSturdy(IBlockReader pLevel, BlockPos pPos, Direction pDirection) {
         return this.isFaceSturdy(pLevel, pPos, pDirection, BlockVoxelShape.FULL);
      }

      public boolean isFaceSturdy(IBlockReader pLevel, BlockPos pPos, Direction pFace, BlockVoxelShape pSupportType) {
         return this.cache != null ? this.cache.isFaceSturdy(pFace, pSupportType) : pSupportType.isSupporting(this.asState(), pLevel, pPos, pFace);
      }

      public boolean isCollisionShapeFullBlock(IBlockReader pLevel, BlockPos pPos) {
         return this.cache != null ? this.cache.isCollisionShapeFullBlock : Block.isShapeFullBlock(this.getCollisionShape(pLevel, pPos));
      }

      protected abstract BlockState asState();

      public boolean requiresCorrectToolForDrops() {
         return this.requiresCorrectToolForDrops;
      }

      static final class Cache {
         private static final Direction[] DIRECTIONS = Direction.values();
         private static final int SUPPORT_TYPE_COUNT = BlockVoxelShape.values().length;
         protected final boolean solidRender;
         private final boolean propagatesSkylightDown;
         private final int lightBlock;
         @Nullable
         private final VoxelShape[] occlusionShapes;
         protected final VoxelShape collisionShape;
         protected final boolean largeCollisionShape;
         private final boolean[] faceSturdy;
         protected final boolean isCollisionShapeFullBlock;

         private Cache(BlockState p_i50627_1_) {
            Block block = p_i50627_1_.getBlock();
            this.solidRender = p_i50627_1_.isSolidRender(EmptyBlockReader.INSTANCE, BlockPos.ZERO);
            this.propagatesSkylightDown = block.propagatesSkylightDown(p_i50627_1_, EmptyBlockReader.INSTANCE, BlockPos.ZERO);
            this.lightBlock = block.getLightBlock(p_i50627_1_, EmptyBlockReader.INSTANCE, BlockPos.ZERO);
            if (!p_i50627_1_.canOcclude()) {
               this.occlusionShapes = null;
            } else {
               this.occlusionShapes = new VoxelShape[DIRECTIONS.length];
               VoxelShape voxelshape = block.getOcclusionShape(p_i50627_1_, EmptyBlockReader.INSTANCE, BlockPos.ZERO);

               for(Direction direction : DIRECTIONS) {
                  this.occlusionShapes[direction.ordinal()] = VoxelShapes.getFaceShape(voxelshape, direction);
               }
            }

            this.collisionShape = block.getCollisionShape(p_i50627_1_, EmptyBlockReader.INSTANCE, BlockPos.ZERO, ISelectionContext.empty());
            this.largeCollisionShape = Arrays.stream(Direction.Axis.values()).anyMatch((p_235796_1_) -> {
               return this.collisionShape.min(p_235796_1_) < 0.0D || this.collisionShape.max(p_235796_1_) > 1.0D;
            });
            this.faceSturdy = new boolean[DIRECTIONS.length * SUPPORT_TYPE_COUNT];

            for(Direction direction1 : DIRECTIONS) {
               for(BlockVoxelShape blockvoxelshape : BlockVoxelShape.values()) {
                  this.faceSturdy[getFaceSupportIndex(direction1, blockvoxelshape)] = blockvoxelshape.isSupporting(p_i50627_1_, EmptyBlockReader.INSTANCE, BlockPos.ZERO, direction1);
               }
            }

            this.isCollisionShapeFullBlock = Block.isShapeFullBlock(p_i50627_1_.getCollisionShape(EmptyBlockReader.INSTANCE, BlockPos.ZERO));
         }

         public boolean isFaceSturdy(Direction pDirection, BlockVoxelShape pSupportType) {
            return this.faceSturdy[getFaceSupportIndex(pDirection, pSupportType)];
         }

         private static int getFaceSupportIndex(Direction pDirection, BlockVoxelShape pSupportType) {
            return pDirection.ordinal() * SUPPORT_TYPE_COUNT + pSupportType.ordinal();
         }
      }
   }

   public interface IExtendedPositionPredicate<A> {
      boolean test(BlockState p_test_1_, IBlockReader p_test_2_, BlockPos p_test_3_, A p_test_4_);
   }

   public interface IPositionPredicate {
      boolean test(BlockState p_test_1_, IBlockReader p_test_2_, BlockPos p_test_3_);
   }

   public static enum OffsetType {
      NONE,
      XZ,
      XYZ;
   }

   public static class Properties {
      private Material material;
      private Function<BlockState, MaterialColor> materialColor;
      private boolean hasCollision = true;
      private SoundType soundType = SoundType.STONE;
      private ToIntFunction<BlockState> lightEmission = (p_235830_0_) -> {
         return 0;
      };
      private float explosionResistance;
      private float destroyTime;
      private boolean requiresCorrectToolForDrops;
      private boolean isRandomlyTicking;
      private float friction = 0.6F;
      private float speedFactor = 1.0F;
      private float jumpFactor = 1.0F;
      /** Sets loot table information */
      private ResourceLocation drops;
      private boolean canOcclude = true;
      private boolean isAir;
      private int harvestLevel = -1;
      private net.minecraftforge.common.ToolType harvestTool;
      private java.util.function.Supplier<ResourceLocation> lootTableSupplier;
      private AbstractBlock.IExtendedPositionPredicate<EntityType<?>> isValidSpawn = (p_235832_0_, p_235832_1_, p_235832_2_, p_235832_3_) -> {
         return p_235832_0_.isFaceSturdy(p_235832_1_, p_235832_2_, Direction.UP) && p_235832_0_.getLightValue(p_235832_1_, p_235832_2_) < 14;
      };
      private AbstractBlock.IPositionPredicate isRedstoneConductor = (p_235853_0_, p_235853_1_, p_235853_2_) -> {
         return p_235853_0_.getMaterial().isSolidBlocking() && p_235853_0_.isCollisionShapeFullBlock(p_235853_1_, p_235853_2_);
      };
      private AbstractBlock.IPositionPredicate isSuffocating = (p_235848_1_, p_235848_2_, p_235848_3_) -> {
         return this.material.blocksMotion() && p_235848_1_.isCollisionShapeFullBlock(p_235848_2_, p_235848_3_);
      };
      /** If it blocks vision on the client side. */
      private AbstractBlock.IPositionPredicate isViewBlocking = this.isSuffocating;
      private AbstractBlock.IPositionPredicate hasPostProcess = (p_235843_0_, p_235843_1_, p_235843_2_) -> {
         return false;
      };
      private AbstractBlock.IPositionPredicate emissiveRendering = (p_235831_0_, p_235831_1_, p_235831_2_) -> {
         return false;
      };
      private boolean dynamicShape;

      private Properties(Material pMaterial, MaterialColor pMaterialColor) {
         this(pMaterial, (p_235837_1_) -> {
            return pMaterialColor;
         });
      }

      private Properties(Material pMaterial, Function<BlockState, MaterialColor> pMaterialColor) {
         this.material = pMaterial;
         this.materialColor = pMaterialColor;
      }

      public static AbstractBlock.Properties of(Material pMaterial) {
         return of(pMaterial, pMaterial.getColor());
      }

      public static AbstractBlock.Properties of(Material pMaterial, DyeColor pColor) {
         return of(pMaterial, pColor.getMaterialColor());
      }

      public static AbstractBlock.Properties of(Material pMaterial, MaterialColor pMaterialColor) {
         return new AbstractBlock.Properties(pMaterial, pMaterialColor);
      }

      public static AbstractBlock.Properties of(Material pMaterial, Function<BlockState, MaterialColor> pMaterialColor) {
         return new AbstractBlock.Properties(pMaterial, pMaterialColor);
      }

      public static AbstractBlock.Properties copy(AbstractBlock pBlockBehaviour) {
         AbstractBlock.Properties abstractblock$properties = new AbstractBlock.Properties(pBlockBehaviour.material, pBlockBehaviour.properties.materialColor);
         abstractblock$properties.material = pBlockBehaviour.properties.material;
         abstractblock$properties.destroyTime = pBlockBehaviour.properties.destroyTime;
         abstractblock$properties.explosionResistance = pBlockBehaviour.properties.explosionResistance;
         abstractblock$properties.hasCollision = pBlockBehaviour.properties.hasCollision;
         abstractblock$properties.isRandomlyTicking = pBlockBehaviour.properties.isRandomlyTicking;
         abstractblock$properties.lightEmission = pBlockBehaviour.properties.lightEmission;
         abstractblock$properties.materialColor = pBlockBehaviour.properties.materialColor;
         abstractblock$properties.soundType = pBlockBehaviour.properties.soundType;
         abstractblock$properties.friction = pBlockBehaviour.properties.friction;
         abstractblock$properties.speedFactor = pBlockBehaviour.properties.speedFactor;
         abstractblock$properties.dynamicShape = pBlockBehaviour.properties.dynamicShape;
         abstractblock$properties.canOcclude = pBlockBehaviour.properties.canOcclude;
         abstractblock$properties.isAir = pBlockBehaviour.properties.isAir;
         abstractblock$properties.requiresCorrectToolForDrops = pBlockBehaviour.properties.requiresCorrectToolForDrops;
         abstractblock$properties.harvestLevel = pBlockBehaviour.properties.harvestLevel;
         abstractblock$properties.harvestTool = pBlockBehaviour.properties.harvestTool;
         return abstractblock$properties;
      }

      public AbstractBlock.Properties noCollission() {
         this.hasCollision = false;
         this.canOcclude = false;
         return this;
      }

      public AbstractBlock.Properties noOcclusion() {
         this.canOcclude = false;
         return this;
      }

      public AbstractBlock.Properties harvestLevel(int harvestLevel) {
         this.harvestLevel = harvestLevel;
         return this;
      }

      public AbstractBlock.Properties harvestTool(net.minecraftforge.common.ToolType harvestTool) {
         this.harvestTool = harvestTool;
         return this;
      }

      public int getHarvestLevel() {
         return this.harvestLevel;
      }

      public net.minecraftforge.common.ToolType getHarvestTool() {
         return this.harvestTool;
      }

      public AbstractBlock.Properties friction(float pFriction) {
         this.friction = pFriction;
         return this;
      }

      public AbstractBlock.Properties speedFactor(float pSpeedFactor) {
         this.speedFactor = pSpeedFactor;
         return this;
      }

      public AbstractBlock.Properties jumpFactor(float pJumpFactor) {
         this.jumpFactor = pJumpFactor;
         return this;
      }

      public AbstractBlock.Properties sound(SoundType pSoundType) {
         this.soundType = pSoundType;
         return this;
      }

      public AbstractBlock.Properties lightLevel(ToIntFunction<BlockState> pLightEmission) {
         this.lightEmission = pLightEmission;
         return this;
      }

      public AbstractBlock.Properties strength(float pDestroyTime, float pExplosionResistance) {
         this.destroyTime = pDestroyTime;
         this.explosionResistance = Math.max(0.0F, pExplosionResistance);
         return this;
      }

      public AbstractBlock.Properties instabreak() {
         return this.strength(0.0F);
      }

      public AbstractBlock.Properties strength(float pStrength) {
         this.strength(pStrength, pStrength);
         return this;
      }

      public AbstractBlock.Properties randomTicks() {
         this.isRandomlyTicking = true;
         return this;
      }

      public AbstractBlock.Properties dynamicShape() {
         this.dynamicShape = true;
         return this;
      }

      public AbstractBlock.Properties noDrops() {
         this.drops = LootTables.EMPTY;
         return this;
      }

      @Deprecated // FORGE: Use the variant that takes a Supplier below
      public AbstractBlock.Properties dropsLike(Block pBlock) {
         this.lootTableSupplier = () -> pBlock.delegate.get().getLootTable();
         return this;
      }

      public AbstractBlock.Properties lootFrom(java.util.function.Supplier<? extends Block> blockIn) {
          this.lootTableSupplier = () -> blockIn.get().getLootTable();
          return this;
      }

      public AbstractBlock.Properties air() {
         this.isAir = true;
         return this;
      }

      public AbstractBlock.Properties isValidSpawn(AbstractBlock.IExtendedPositionPredicate<EntityType<?>> pIsValidSpawn) {
         this.isValidSpawn = pIsValidSpawn;
         return this;
      }

      public AbstractBlock.Properties isRedstoneConductor(AbstractBlock.IPositionPredicate pIsRedstoneConductor) {
         this.isRedstoneConductor = pIsRedstoneConductor;
         return this;
      }

      public AbstractBlock.Properties isSuffocating(AbstractBlock.IPositionPredicate pIsSuffocating) {
         this.isSuffocating = pIsSuffocating;
         return this;
      }

      /**
       * If it blocks vision on the client side.
       */
      public AbstractBlock.Properties isViewBlocking(AbstractBlock.IPositionPredicate pIsViewBlocking) {
         this.isViewBlocking = pIsViewBlocking;
         return this;
      }

      public AbstractBlock.Properties hasPostProcess(AbstractBlock.IPositionPredicate pHasPostProcess) {
         this.hasPostProcess = pHasPostProcess;
         return this;
      }

      public AbstractBlock.Properties emissiveRendering(AbstractBlock.IPositionPredicate pEmissiveRendering) {
         this.emissiveRendering = pEmissiveRendering;
         return this;
      }

      public AbstractBlock.Properties requiresCorrectToolForDrops() {
         this.requiresCorrectToolForDrops = true;
         return this;
      }
   }
}
