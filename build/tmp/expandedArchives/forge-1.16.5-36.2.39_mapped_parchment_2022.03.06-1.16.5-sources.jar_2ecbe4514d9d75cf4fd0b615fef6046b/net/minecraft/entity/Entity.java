package net.minecraft.entity;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.HoneyBlock;
import net.minecraft.block.PortalInfo;
import net.minecraft.block.PortalSize;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.PushReaction;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.INameable;
import net.minecraft.util.Mirror;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.TeleportationRepositioner;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Entity extends net.minecraftforge.common.capabilities.CapabilityProvider<Entity> implements INameable, ICommandSource, net.minecraftforge.common.extensions.IForgeEntity {
   protected static final Logger LOGGER = LogManager.getLogger();
   protected static final AtomicInteger ENTITY_COUNTER = new AtomicInteger();
   private static final List<ItemStack> EMPTY_LIST = Collections.emptyList();
   private static final AxisAlignedBB INITIAL_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
   private static double viewScale = 1.0D;
   @Deprecated // Forge: Use the getter to allow overriding in mods
   private final EntityType<?> type;
   private int id = ENTITY_COUNTER.incrementAndGet();
   public boolean blocksBuilding;
   private final List<Entity> passengers = Lists.newArrayList();
   protected int boardingCooldown;
   @Nullable
   private Entity vehicle;
   public boolean forcedLoading;
   public World level;
   public double xo;
   public double yo;
   public double zo;
   private Vector3d position;
   private BlockPos blockPosition;
   private Vector3d deltaMovement = Vector3d.ZERO;
   public float yRot;
   public float xRot;
   public float yRotO;
   public float xRotO;
   private AxisAlignedBB bb = INITIAL_AABB;
   protected boolean onGround;
   public boolean horizontalCollision;
   public boolean verticalCollision;
   public boolean hurtMarked;
   protected Vector3d stuckSpeedMultiplier = Vector3d.ZERO;
   @Deprecated //Forge: Use isAlive, remove(boolean) and revive() instead of directly accessing this field. To allow the entity to react to and better control this information.
   public boolean removed;
   public float walkDistO;
   public float walkDist;
   public float moveDist;
   public float fallDistance;
   private float nextStep = 1.0F;
   private float nextFlap = 1.0F;
   public double xOld;
   public double yOld;
   public double zOld;
   public float maxUpStep;
   public boolean noPhysics;
   public float pushthrough;
   protected final Random random = new Random();
   public int tickCount;
   private int remainingFireTicks = -this.getFireImmuneTicks();
   protected boolean wasTouchingWater;
   protected Object2DoubleMap<ITag<Fluid>> fluidHeight = new Object2DoubleArrayMap<>(2);
   protected boolean wasEyeInWater;
   @Nullable
   protected ITag<Fluid> fluidOnEyes;
   public int invulnerableTime;
   protected boolean firstTick = true;
   protected final EntityDataManager entityData;
   protected static final DataParameter<Byte> DATA_SHARED_FLAGS_ID = EntityDataManager.defineId(Entity.class, DataSerializers.BYTE);
   private static final DataParameter<Integer> DATA_AIR_SUPPLY_ID = EntityDataManager.defineId(Entity.class, DataSerializers.INT);
   private static final DataParameter<Optional<ITextComponent>> DATA_CUSTOM_NAME = EntityDataManager.defineId(Entity.class, DataSerializers.OPTIONAL_COMPONENT);
   private static final DataParameter<Boolean> DATA_CUSTOM_NAME_VISIBLE = EntityDataManager.defineId(Entity.class, DataSerializers.BOOLEAN);
   private static final DataParameter<Boolean> DATA_SILENT = EntityDataManager.defineId(Entity.class, DataSerializers.BOOLEAN);
   private static final DataParameter<Boolean> DATA_NO_GRAVITY = EntityDataManager.defineId(Entity.class, DataSerializers.BOOLEAN);
   protected static final DataParameter<Pose> DATA_POSE = EntityDataManager.defineId(Entity.class, DataSerializers.POSE);
   public boolean inChunk;
   public int xChunk;
   public int yChunk;
   public int zChunk;
   private boolean movedSinceLastChunkCheck;
   private Vector3d packetCoordinates;
   public boolean noCulling;
   public boolean hasImpulse;
   private int portalCooldown;
   protected boolean isInsidePortal;
   protected int portalTime;
   protected BlockPos portalEntrancePos;
   private boolean invulnerable;
   protected UUID uuid = MathHelper.createInsecureUUID(this.random);
   protected String stringUUID = this.uuid.toString();
   protected boolean glowing;
   private final Set<String> tags = Sets.newHashSet();
   private boolean forceChunkAddition;
   private final double[] pistonDeltas = new double[]{0.0D, 0.0D, 0.0D};
   private long pistonDeltasGameTime;
   private EntitySize dimensions;
   private float eyeHeight;

   public Entity(EntityType<?> pType, World pLevel) {
      super(Entity.class);
      this.type = pType;
      this.level = pLevel;
      this.dimensions = pType.getDimensions();
      this.position = Vector3d.ZERO;
      this.blockPosition = BlockPos.ZERO;
      this.packetCoordinates = Vector3d.ZERO;
      this.setPos(0.0D, 0.0D, 0.0D);
      this.entityData = new EntityDataManager(this);
      this.entityData.define(DATA_SHARED_FLAGS_ID, (byte)0);
      this.entityData.define(DATA_AIR_SUPPLY_ID, this.getMaxAirSupply());
      this.entityData.define(DATA_CUSTOM_NAME_VISIBLE, false);
      this.entityData.define(DATA_CUSTOM_NAME, Optional.empty());
      this.entityData.define(DATA_SILENT, false);
      this.entityData.define(DATA_NO_GRAVITY, false);
      this.entityData.define(DATA_POSE, Pose.STANDING);
      this.defineSynchedData();
      net.minecraftforge.event.entity.EntityEvent.Size sizeEvent = net.minecraftforge.event.ForgeEventFactory.getEntitySizeForge(this, Pose.STANDING, this.dimensions, this.getEyeHeight(Pose.STANDING, this.dimensions));
      this.dimensions = sizeEvent.getNewSize();
      this.eyeHeight = sizeEvent.getNewEyeHeight();
      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.EntityEvent.EntityConstructing(this));
      this.gatherCapabilities();
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isColliding(BlockPos pPos, BlockState pState) {
      VoxelShape voxelshape = pState.getCollisionShape(this.level, pPos, ISelectionContext.of(this));
      VoxelShape voxelshape1 = voxelshape.move((double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ());
      return VoxelShapes.joinIsNotEmpty(voxelshape1, VoxelShapes.create(this.getBoundingBox()), IBooleanFunction.AND);
   }

   @OnlyIn(Dist.CLIENT)
   public int getTeamColor() {
      Team team = this.getTeam();
      return team != null && team.getColor().getColor() != null ? team.getColor().getColor() : 16777215;
   }

   /**
    * Returns true if the player is in spectator mode.
    */
   public boolean isSpectator() {
      return false;
   }

   public final void unRide() {
      if (this.isVehicle()) {
         this.ejectPassengers();
      }

      if (this.isPassenger()) {
         this.stopRiding();
      }

   }

   public void setPacketCoordinates(double pX, double pY, double pZ) {
      this.setPacketCoordinates(new Vector3d(pX, pY, pZ));
   }

   public void setPacketCoordinates(Vector3d pPacketCoordinates) {
      this.packetCoordinates = pPacketCoordinates;
   }

   @OnlyIn(Dist.CLIENT)
   public Vector3d getPacketCoordinates() {
      return this.packetCoordinates;
   }

   public EntityType<?> getType() {
      return this.type;
   }

   public int getId() {
      return this.id;
   }

   public void setId(int pId) {
      this.id = pId;
   }

   public Set<String> getTags() {
      return this.tags;
   }

   public boolean addTag(String pTag) {
      return this.tags.size() >= 1024 ? false : this.tags.add(pTag);
   }

   public boolean removeTag(String pTag) {
      return this.tags.remove(pTag);
   }

   /**
    * Called by the /kill command.
    */
   public void kill() {
      this.remove();
   }

   protected abstract void defineSynchedData();

   public EntityDataManager getEntityData() {
      return this.entityData;
   }

   public boolean equals(Object p_equals_1_) {
      if (p_equals_1_ instanceof Entity) {
         return ((Entity)p_equals_1_).id == this.id;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.id;
   }

   @OnlyIn(Dist.CLIENT)
   protected void resetPos() {
      if (this.level != null) {
         for(double d0 = this.getY(); d0 > 0.0D && d0 < 256.0D; ++d0) {
            this.setPos(this.getX(), d0, this.getZ());
            if (this.level.noCollision(this)) {
               break;
            }
         }

         this.setDeltaMovement(Vector3d.ZERO);
         this.xRot = 0.0F;
      }
   }

   public void remove() {
      this.remove(false);
   }

   public void remove(boolean keepData) {
      this.removed = true;
      if (!keepData)
         this.invalidateCaps();
   }

   public void setPose(Pose pPose) {
      this.entityData.set(DATA_POSE, pPose);
   }

   public Pose getPose() {
      return this.entityData.get(DATA_POSE);
   }

   public boolean closerThan(Entity pEntity, double pDistance) {
      double d0 = pEntity.position.x - this.position.x;
      double d1 = pEntity.position.y - this.position.y;
      double d2 = pEntity.position.z - this.position.z;
      return d0 * d0 + d1 * d1 + d2 * d2 < pDistance * pDistance;
   }

   /**
    * Sets the rotation of the entity.
    */
   protected void setRot(float pYRot, float pXRot) {
      this.yRot = pYRot % 360.0F;
      this.xRot = pXRot % 360.0F;
   }

   /**
    * Sets the x,y,z of the entity from the given parameters. Also seems to set up a bounding box.
    */
   public void setPos(double pX, double pY, double pZ) {
      this.setPosRaw(pX, pY, pZ);
      this.setBoundingBox(this.dimensions.makeBoundingBox(pX, pY, pZ));
   }

   /**
    * Recomputes this entity's bounding box so that it is positioned at this entity's X/Y/Z.
    */
   protected void reapplyPosition() {
      this.setPos(this.position.x, this.position.y, this.position.z);
   }

   @OnlyIn(Dist.CLIENT)
   public void turn(double pYRot, double pXRot) {
      double d0 = pXRot * 0.15D;
      double d1 = pYRot * 0.15D;
      this.xRot = (float)((double)this.xRot + d0);
      this.yRot = (float)((double)this.yRot + d1);
      this.xRot = MathHelper.clamp(this.xRot, -90.0F, 90.0F);
      this.xRotO = (float)((double)this.xRotO + d0);
      this.yRotO = (float)((double)this.yRotO + d1);
      this.xRotO = MathHelper.clamp(this.xRotO, -90.0F, 90.0F);
      if (this.vehicle != null) {
         this.vehicle.onPassengerTurned(this);
      }

   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      if (!this.level.isClientSide) {
         this.setSharedFlag(6, this.isGlowing());
      }

      this.baseTick();
   }

   /**
    * Gets called every tick from main Entity class
    */
   public void baseTick() {
      this.level.getProfiler().push("entityBaseTick");
      if (this.isPassenger() && this.getVehicle().removed) {
         this.stopRiding();
      }

      if (this.boardingCooldown > 0) {
         --this.boardingCooldown;
      }

      this.walkDistO = this.walkDist;
      this.xRotO = this.xRot;
      this.yRotO = this.yRot;
      this.handleNetherPortal();
      if (this.canSpawnSprintParticle()) {
         this.spawnSprintParticle();
      }

      this.updateInWaterStateAndDoFluidPushing();
      this.updateFluidOnEyes();
      this.updateSwimming();
      if (this.level.isClientSide) {
         this.clearFire();
      } else if (this.remainingFireTicks > 0) {
         if (this.fireImmune()) {
            this.setRemainingFireTicks(this.remainingFireTicks - 4);
            if (this.remainingFireTicks < 0) {
               this.clearFire();
            }
         } else {
            if (this.remainingFireTicks % 20 == 0 && !this.isInLava()) {
               this.hurt(DamageSource.ON_FIRE, 1.0F);
            }

            this.setRemainingFireTicks(this.remainingFireTicks - 1);
         }
      }

      if (this.isInLava()) {
         this.lavaHurt();
         this.fallDistance *= 0.5F;
      }

      if (this.getY() < -64.0D) {
         this.outOfWorld();
      }

      if (!this.level.isClientSide) {
         this.setSharedFlag(0, this.remainingFireTicks > 0);
      }

      this.firstTick = false;
      this.level.getProfiler().pop();
   }

   public void setPortalCooldown() {
      this.portalCooldown = this.getDimensionChangingDelay();
   }

   public boolean isOnPortalCooldown() {
      return this.portalCooldown > 0;
   }

   /**
    * Decrements the counter for the remaining time until the entity may use a portal again.
    */
   protected void processPortalCooldown() {
      if (this.isOnPortalCooldown()) {
         --this.portalCooldown;
      }

   }

   /**
    * Return the amount of time this entity should stay in a portal before being transported.
    */
   public int getPortalWaitTime() {
      return 0;
   }

   /**
    * Called whenever the entity is walking inside of lava.
    */
   protected void lavaHurt() {
      if (!this.fireImmune()) {
         this.setSecondsOnFire(15);
         this.hurt(DamageSource.LAVA, 4.0F);
      }
   }

   /**
    * Sets entity to burn for x amount of seconds, cannot lower amount of existing fire.
    */
   public void setSecondsOnFire(int pSeconds) {
      int i = pSeconds * 20;
      if (this instanceof LivingEntity) {
         i = ProtectionEnchantment.getFireAfterDampener((LivingEntity)this, i);
      }

      if (this.remainingFireTicks < i) {
         this.setRemainingFireTicks(i);
      }

   }

   public void setRemainingFireTicks(int pRemainingFireTicks) {
      this.remainingFireTicks = pRemainingFireTicks;
   }

   public int getRemainingFireTicks() {
      return this.remainingFireTicks;
   }

   /**
    * Removes fire from entity.
    */
   public void clearFire() {
      this.setRemainingFireTicks(0);
   }

   /**
    * sets the dead flag. Used when you fall off the bottom of the world.
    */
   protected void outOfWorld() {
      this.remove();
   }

   /**
    * Checks if the offset position from the entity's current position has a collision with a block or a liquid.
    */
   public boolean isFree(double pX, double pY, double pZ) {
      return this.isFree(this.getBoundingBox().move(pX, pY, pZ));
   }

   /**
    * Determines if the entity has no collision with a block or a liquid within the specified bounding box.
    */
   private boolean isFree(AxisAlignedBB pBox) {
      return this.level.noCollision(this, pBox) && !this.level.containsAnyLiquid(pBox);
   }

   public void setOnGround(boolean pOnGround) {
      this.onGround = pOnGround;
   }

   public boolean isOnGround() {
      return this.onGround;
   }

   public void move(MoverType pType, Vector3d pPos) {
      if (this.noPhysics) {
         this.setBoundingBox(this.getBoundingBox().move(pPos));
         this.setLocationFromBoundingbox();
      } else {
         if (pType == MoverType.PISTON) {
            pPos = this.limitPistonMovement(pPos);
            if (pPos.equals(Vector3d.ZERO)) {
               return;
            }
         }

         this.level.getProfiler().push("move");
         if (this.stuckSpeedMultiplier.lengthSqr() > 1.0E-7D) {
            pPos = pPos.multiply(this.stuckSpeedMultiplier);
            this.stuckSpeedMultiplier = Vector3d.ZERO;
            this.setDeltaMovement(Vector3d.ZERO);
         }

         pPos = this.maybeBackOffFromEdge(pPos, pType);
         Vector3d vector3d = this.collide(pPos);
         if (vector3d.lengthSqr() > 1.0E-7D) {
            this.setBoundingBox(this.getBoundingBox().move(vector3d));
            this.setLocationFromBoundingbox();
         }

         this.level.getProfiler().pop();
         this.level.getProfiler().push("rest");
         this.horizontalCollision = !MathHelper.equal(pPos.x, vector3d.x) || !MathHelper.equal(pPos.z, vector3d.z);
         this.verticalCollision = pPos.y != vector3d.y;
         this.onGround = this.verticalCollision && pPos.y < 0.0D;
         BlockPos blockpos = this.getOnPos();
         BlockState blockstate = this.level.getBlockState(blockpos);
         this.checkFallDamage(vector3d.y, this.onGround, blockstate, blockpos);
         Vector3d vector3d1 = this.getDeltaMovement();
         if (pPos.x != vector3d.x) {
            this.setDeltaMovement(0.0D, vector3d1.y, vector3d1.z);
         }

         if (pPos.z != vector3d.z) {
            this.setDeltaMovement(vector3d1.x, vector3d1.y, 0.0D);
         }

         Block block = blockstate.getBlock();
         if (pPos.y != vector3d.y) {
            block.updateEntityAfterFallOn(this.level, this);
         }

         if (this.onGround && !this.isSteppingCarefully()) {
            block.stepOn(this.level, blockpos, this);
         }

         if (this.isMovementNoisy() && !this.isPassenger()) {
            double d0 = vector3d.x;
            double d1 = vector3d.y;
            double d2 = vector3d.z;
            if (!block.is(BlockTags.CLIMBABLE)) {
               d1 = 0.0D;
            }

            this.walkDist = (float)((double)this.walkDist + (double)MathHelper.sqrt(getHorizontalDistanceSqr(vector3d)) * 0.6D);
            this.moveDist = (float)((double)this.moveDist + (double)MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2) * 0.6D);
            if (this.moveDist > this.nextStep && !blockstate.isAir(this.level, blockpos)) {
               this.nextStep = this.nextStep();
               if (this.isInWater()) {
                  Entity entity = this.isVehicle() && this.getControllingPassenger() != null ? this.getControllingPassenger() : this;
                  float f = entity == this ? 0.35F : 0.4F;
                  Vector3d vector3d2 = entity.getDeltaMovement();
                  float f1 = MathHelper.sqrt(vector3d2.x * vector3d2.x * (double)0.2F + vector3d2.y * vector3d2.y + vector3d2.z * vector3d2.z * (double)0.2F) * f;
                  if (f1 > 1.0F) {
                     f1 = 1.0F;
                  }

                  this.playSwimSound(f1);
               } else {
                  this.playStepSound(blockpos, blockstate);
               }
            } else if (this.moveDist > this.nextFlap && this.makeFlySound() && blockstate.isAir(this.level, blockpos)) {
               this.nextFlap = this.playFlySound(this.moveDist);
            }
         }

         try {
            this.checkInsideBlocks();
         } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Checking entity block collision");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being checked for collision");
            this.fillCrashReportCategory(crashreportcategory);
            throw new ReportedException(crashreport);
         }

         float f2 = this.getBlockSpeedFactor();
         this.setDeltaMovement(this.getDeltaMovement().multiply((double)f2, 1.0D, (double)f2));
         if (BlockPos.betweenClosedStream(this.getBoundingBox().deflate(0.001D)).noneMatch((p_233572_0_) -> {
            BlockState state = level.getBlockState(p_233572_0_);
            return state.is(BlockTags.FIRE) || state.is(Blocks.LAVA) || state.isBurning(level, p_233572_0_);
         }) && this.remainingFireTicks <= 0) {
            this.setRemainingFireTicks(-this.getFireImmuneTicks());
         }

         if (this.isInWaterRainOrBubble() && this.isOnFire()) {
            this.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
            this.setRemainingFireTicks(-this.getFireImmuneTicks());
         }

         this.level.getProfiler().pop();
      }
   }

   protected BlockPos getOnPos() {
      int i = MathHelper.floor(this.position.x);
      int j = MathHelper.floor(this.position.y - (double)0.2F);
      int k = MathHelper.floor(this.position.z);
      BlockPos blockpos = new BlockPos(i, j, k);
      if (this.level.isEmptyBlock(blockpos)) {
         BlockPos blockpos1 = blockpos.below();
         BlockState blockstate = this.level.getBlockState(blockpos1);
         if (blockstate.collisionExtendsVertically(this.level, blockpos1, this)) {
            return blockpos1;
         }
      }

      return blockpos;
   }

   protected float getBlockJumpFactor() {
      float f = this.level.getBlockState(this.blockPosition()).getBlock().getJumpFactor();
      float f1 = this.level.getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getJumpFactor();
      return (double)f == 1.0D ? f1 : f;
   }

   protected float getBlockSpeedFactor() {
      Block block = this.level.getBlockState(this.blockPosition()).getBlock();
      float f = block.getSpeedFactor();
      if (block != Blocks.WATER && block != Blocks.BUBBLE_COLUMN) {
         return (double)f == 1.0D ? this.level.getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getSpeedFactor() : f;
      } else {
         return f;
      }
   }

   protected BlockPos getBlockPosBelowThatAffectsMyMovement() {
      return new BlockPos(this.position.x, this.getBoundingBox().minY - 0.5000001D, this.position.z);
   }

   protected Vector3d maybeBackOffFromEdge(Vector3d pVec, MoverType pMover) {
      return pVec;
   }

   protected Vector3d limitPistonMovement(Vector3d pPos) {
      if (pPos.lengthSqr() <= 1.0E-7D) {
         return pPos;
      } else {
         long i = this.level.getGameTime();
         if (i != this.pistonDeltasGameTime) {
            Arrays.fill(this.pistonDeltas, 0.0D);
            this.pistonDeltasGameTime = i;
         }

         if (pPos.x != 0.0D) {
            double d2 = this.applyPistonMovementRestriction(Direction.Axis.X, pPos.x);
            return Math.abs(d2) <= (double)1.0E-5F ? Vector3d.ZERO : new Vector3d(d2, 0.0D, 0.0D);
         } else if (pPos.y != 0.0D) {
            double d1 = this.applyPistonMovementRestriction(Direction.Axis.Y, pPos.y);
            return Math.abs(d1) <= (double)1.0E-5F ? Vector3d.ZERO : new Vector3d(0.0D, d1, 0.0D);
         } else if (pPos.z != 0.0D) {
            double d0 = this.applyPistonMovementRestriction(Direction.Axis.Z, pPos.z);
            return Math.abs(d0) <= (double)1.0E-5F ? Vector3d.ZERO : new Vector3d(0.0D, 0.0D, d0);
         } else {
            return Vector3d.ZERO;
         }
      }
   }

   private double applyPistonMovementRestriction(Direction.Axis pAxis, double pDistance) {
      int i = pAxis.ordinal();
      double d0 = MathHelper.clamp(pDistance + this.pistonDeltas[i], -0.51D, 0.51D);
      pDistance = d0 - this.pistonDeltas[i];
      this.pistonDeltas[i] = d0;
      return pDistance;
   }

   /**
    * Given a motion vector, return an updated vector that takes into account restrictions such as collisions (from all
    * directions) and step-up from stepHeight
    */
   private Vector3d collide(Vector3d pVec) {
      AxisAlignedBB axisalignedbb = this.getBoundingBox();
      ISelectionContext iselectioncontext = ISelectionContext.of(this);
      VoxelShape voxelshape = this.level.getWorldBorder().getCollisionShape();
      Stream<VoxelShape> stream = VoxelShapes.joinIsNotEmpty(voxelshape, VoxelShapes.create(axisalignedbb.deflate(1.0E-7D)), IBooleanFunction.AND) ? Stream.empty() : Stream.of(voxelshape);
      Stream<VoxelShape> stream1 = this.level.getEntityCollisions(this, axisalignedbb.expandTowards(pVec), (p_233561_0_) -> {
         return true;
      });
      ReuseableStream<VoxelShape> reuseablestream = new ReuseableStream<>(Stream.concat(stream1, stream));
      Vector3d vector3d = pVec.lengthSqr() == 0.0D ? pVec : collideBoundingBoxHeuristically(this, pVec, axisalignedbb, this.level, iselectioncontext, reuseablestream);
      boolean flag = pVec.x != vector3d.x;
      boolean flag1 = pVec.y != vector3d.y;
      boolean flag2 = pVec.z != vector3d.z;
      boolean flag3 = this.onGround || flag1 && pVec.y < 0.0D;
      if (this.maxUpStep > 0.0F && flag3 && (flag || flag2)) {
         Vector3d vector3d1 = collideBoundingBoxHeuristically(this, new Vector3d(pVec.x, (double)this.maxUpStep, pVec.z), axisalignedbb, this.level, iselectioncontext, reuseablestream);
         Vector3d vector3d2 = collideBoundingBoxHeuristically(this, new Vector3d(0.0D, (double)this.maxUpStep, 0.0D), axisalignedbb.expandTowards(pVec.x, 0.0D, pVec.z), this.level, iselectioncontext, reuseablestream);
         if (vector3d2.y < (double)this.maxUpStep) {
            Vector3d vector3d3 = collideBoundingBoxHeuristically(this, new Vector3d(pVec.x, 0.0D, pVec.z), axisalignedbb.move(vector3d2), this.level, iselectioncontext, reuseablestream).add(vector3d2);
            if (getHorizontalDistanceSqr(vector3d3) > getHorizontalDistanceSqr(vector3d1)) {
               vector3d1 = vector3d3;
            }
         }

         if (getHorizontalDistanceSqr(vector3d1) > getHorizontalDistanceSqr(vector3d)) {
            return vector3d1.add(collideBoundingBoxHeuristically(this, new Vector3d(0.0D, -vector3d1.y + pVec.y, 0.0D), axisalignedbb.move(vector3d1), this.level, iselectioncontext, reuseablestream));
         }
      }

      return vector3d;
   }

   public static double getHorizontalDistanceSqr(Vector3d pVector) {
      return pVector.x * pVector.x + pVector.z * pVector.z;
   }

   public static Vector3d collideBoundingBoxHeuristically(@Nullable Entity pEntity, Vector3d pVec, AxisAlignedBB pCollisionBox, World pLevel, ISelectionContext pContext, ReuseableStream<VoxelShape> pPotentialHits) {
      boolean flag = pVec.x == 0.0D;
      boolean flag1 = pVec.y == 0.0D;
      boolean flag2 = pVec.z == 0.0D;
      if ((!flag || !flag1) && (!flag || !flag2) && (!flag1 || !flag2)) {
         ReuseableStream<VoxelShape> reuseablestream = new ReuseableStream<>(Stream.concat(pPotentialHits.getStream(), pLevel.getBlockCollisions(pEntity, pCollisionBox.expandTowards(pVec))));
         return collideBoundingBoxLegacy(pVec, pCollisionBox, reuseablestream);
      } else {
         return collideBoundingBox(pVec, pCollisionBox, pLevel, pContext, pPotentialHits);
      }
   }

   public static Vector3d collideBoundingBoxLegacy(Vector3d pVec, AxisAlignedBB pCollisionBox, ReuseableStream<VoxelShape> pPotentialHits) {
      double d0 = pVec.x;
      double d1 = pVec.y;
      double d2 = pVec.z;
      if (d1 != 0.0D) {
         d1 = VoxelShapes.collide(Direction.Axis.Y, pCollisionBox, pPotentialHits.getStream(), d1);
         if (d1 != 0.0D) {
            pCollisionBox = pCollisionBox.move(0.0D, d1, 0.0D);
         }
      }

      boolean flag = Math.abs(d0) < Math.abs(d2);
      if (flag && d2 != 0.0D) {
         d2 = VoxelShapes.collide(Direction.Axis.Z, pCollisionBox, pPotentialHits.getStream(), d2);
         if (d2 != 0.0D) {
            pCollisionBox = pCollisionBox.move(0.0D, 0.0D, d2);
         }
      }

      if (d0 != 0.0D) {
         d0 = VoxelShapes.collide(Direction.Axis.X, pCollisionBox, pPotentialHits.getStream(), d0);
         if (!flag && d0 != 0.0D) {
            pCollisionBox = pCollisionBox.move(d0, 0.0D, 0.0D);
         }
      }

      if (!flag && d2 != 0.0D) {
         d2 = VoxelShapes.collide(Direction.Axis.Z, pCollisionBox, pPotentialHits.getStream(), d2);
      }

      return new Vector3d(d0, d1, d2);
   }

   public static Vector3d collideBoundingBox(Vector3d pVec, AxisAlignedBB pCollisionBox, IWorldReader pLevel, ISelectionContext pSelectionContext, ReuseableStream<VoxelShape> pPotentialHits) {
      double d0 = pVec.x;
      double d1 = pVec.y;
      double d2 = pVec.z;
      if (d1 != 0.0D) {
         d1 = VoxelShapes.collide(Direction.Axis.Y, pCollisionBox, pLevel, d1, pSelectionContext, pPotentialHits.getStream());
         if (d1 != 0.0D) {
            pCollisionBox = pCollisionBox.move(0.0D, d1, 0.0D);
         }
      }

      boolean flag = Math.abs(d0) < Math.abs(d2);
      if (flag && d2 != 0.0D) {
         d2 = VoxelShapes.collide(Direction.Axis.Z, pCollisionBox, pLevel, d2, pSelectionContext, pPotentialHits.getStream());
         if (d2 != 0.0D) {
            pCollisionBox = pCollisionBox.move(0.0D, 0.0D, d2);
         }
      }

      if (d0 != 0.0D) {
         d0 = VoxelShapes.collide(Direction.Axis.X, pCollisionBox, pLevel, d0, pSelectionContext, pPotentialHits.getStream());
         if (!flag && d0 != 0.0D) {
            pCollisionBox = pCollisionBox.move(d0, 0.0D, 0.0D);
         }
      }

      if (!flag && d2 != 0.0D) {
         d2 = VoxelShapes.collide(Direction.Axis.Z, pCollisionBox, pLevel, d2, pSelectionContext, pPotentialHits.getStream());
      }

      return new Vector3d(d0, d1, d2);
   }

   protected float nextStep() {
      return (float)((int)this.moveDist + 1);
   }

   public void setLocationFromBoundingbox() {
      AxisAlignedBB axisalignedbb = this.getBoundingBox();
      this.setPosRaw((axisalignedbb.minX + axisalignedbb.maxX) / 2.0D, axisalignedbb.minY, (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D);
      if (this.isAddedToWorld() && !this.level.isClientSide && level instanceof ServerWorld) ((ServerWorld)this.level).updateChunkPos(this); // Forge - Process chunk registration after moving.
   }

   protected SoundEvent getSwimSound() {
      return SoundEvents.GENERIC_SWIM;
   }

   protected SoundEvent getSwimSplashSound() {
      return SoundEvents.GENERIC_SPLASH;
   }

   protected SoundEvent getSwimHighSpeedSplashSound() {
      return SoundEvents.GENERIC_SPLASH;
   }

   protected void checkInsideBlocks() {
      AxisAlignedBB axisalignedbb = this.getBoundingBox();
      BlockPos blockpos = new BlockPos(axisalignedbb.minX + 0.001D, axisalignedbb.minY + 0.001D, axisalignedbb.minZ + 0.001D);
      BlockPos blockpos1 = new BlockPos(axisalignedbb.maxX - 0.001D, axisalignedbb.maxY - 0.001D, axisalignedbb.maxZ - 0.001D);
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
      if (this.level.hasChunksAt(blockpos, blockpos1)) {
         for(int i = blockpos.getX(); i <= blockpos1.getX(); ++i) {
            for(int j = blockpos.getY(); j <= blockpos1.getY(); ++j) {
               for(int k = blockpos.getZ(); k <= blockpos1.getZ(); ++k) {
                  blockpos$mutable.set(i, j, k);
                  BlockState blockstate = this.level.getBlockState(blockpos$mutable);

                  try {
                     blockstate.entityInside(this.level, blockpos$mutable, this);
                     this.onInsideBlock(blockstate);
                  } catch (Throwable throwable) {
                     CrashReport crashreport = CrashReport.forThrowable(throwable, "Colliding entity with block");
                     CrashReportCategory crashreportcategory = crashreport.addCategory("Block being collided with");
                     CrashReportCategory.populateBlockDetails(crashreportcategory, blockpos$mutable, blockstate);
                     throw new ReportedException(crashreport);
                  }
               }
            }
         }
      }

   }

   protected void onInsideBlock(BlockState pState) {
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      if (!pBlock.getMaterial().isLiquid()) {
         BlockState blockstate = this.level.getBlockState(pPos.above());
         SoundType soundtype = blockstate.is(Blocks.SNOW) ? blockstate.getSoundType(level, pPos, this) : pBlock.getSoundType(level, pPos, this);
         this.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
      }
   }

   protected void playSwimSound(float pVolume) {
      this.playSound(this.getSwimSound(), pVolume, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
   }

   protected float playFlySound(float pVolume) {
      return 0.0F;
   }

   protected boolean makeFlySound() {
      return false;
   }

   public void playSound(SoundEvent pSound, float pVolume, float pPitch) {
      if (!this.isSilent()) {
         this.level.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), pSound, this.getSoundSource(), pVolume, pPitch);
      }

   }

   /**
    * @return True if this entity will not play sounds
    */
   public boolean isSilent() {
      return this.entityData.get(DATA_SILENT);
   }

   /**
    * When set to true the entity will not play sounds.
    */
   public void setSilent(boolean pIsSilent) {
      this.entityData.set(DATA_SILENT, pIsSilent);
   }

   public boolean isNoGravity() {
      return this.entityData.get(DATA_NO_GRAVITY);
   }

   public void setNoGravity(boolean pNoGravity) {
      this.entityData.set(DATA_NO_GRAVITY, pNoGravity);
   }

   protected boolean isMovementNoisy() {
      return true;
   }

   protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
      if (pOnGround) {
         if (this.fallDistance > 0.0F) {
            pState.getBlock().fallOn(this.level, pPos, this, this.fallDistance);
         }

         this.fallDistance = 0.0F;
      } else if (pY < 0.0D) {
         this.fallDistance = (float)((double)this.fallDistance - pY);
      }

   }

   public boolean fireImmune() {
      return this.getType().fireImmune();
   }

   public boolean causeFallDamage(float pFallDistance, float pDamageMultiplier) {
      if (this.isVehicle()) {
         for(Entity entity : this.getPassengers()) {
            entity.causeFallDamage(pFallDistance, pDamageMultiplier);
         }
      }

      return false;
   }

   /**
    * Checks if this entity is inside water (if inWater field is true as a result of handleWaterMovement() returning
    * true)
    */
   public boolean isInWater() {
      return this.wasTouchingWater;
   }

   private boolean isInRain() {
      BlockPos blockpos = this.blockPosition();
      return this.level.isRainingAt(blockpos) || this.level.isRainingAt(new BlockPos((double)blockpos.getX(), this.getBoundingBox().maxY, (double)blockpos.getZ()));
   }

   private boolean isInBubbleColumn() {
      return this.level.getBlockState(this.blockPosition()).is(Blocks.BUBBLE_COLUMN);
   }

   /**
    * Checks if this entity is either in water or on an open air block in rain (used in wolves).
    */
   public boolean isInWaterOrRain() {
      return this.isInWater() || this.isInRain();
   }

   public boolean isInWaterRainOrBubble() {
      return this.isInWater() || this.isInRain() || this.isInBubbleColumn();
   }

   public boolean isInWaterOrBubble() {
      return this.isInWater() || this.isInBubbleColumn();
   }

   public boolean isUnderWater() {
      return this.wasEyeInWater && this.isInWater();
   }

   public void updateSwimming() {
      if (this.isSwimming()) {
         this.setSwimming(this.isSprinting() && this.isInWater() && !this.isPassenger());
      } else {
         this.setSwimming(this.isSprinting() && this.isUnderWater() && !this.isPassenger());
      }

   }

   protected boolean updateInWaterStateAndDoFluidPushing() {
      this.fluidHeight.clear();
      this.updateInWaterStateAndDoWaterCurrentPushing();
      double d0 = this.level.dimensionType().ultraWarm() ? 0.007D : 0.0023333333333333335D;
      boolean flag = this.updateFluidHeightAndDoFluidPushing(FluidTags.LAVA, d0);
      return this.isInWater() || flag;
   }

   void updateInWaterStateAndDoWaterCurrentPushing() {
      if (this.getVehicle() instanceof BoatEntity) {
         this.wasTouchingWater = false;
      } else if (this.updateFluidHeightAndDoFluidPushing(FluidTags.WATER, 0.014D)) {
         if (!this.wasTouchingWater && !this.firstTick) {
            this.doWaterSplashEffect();
         }

         this.fallDistance = 0.0F;
         this.wasTouchingWater = true;
         this.clearFire();
      } else {
         this.wasTouchingWater = false;
      }

   }

   private void updateFluidOnEyes() {
      this.wasEyeInWater = this.isEyeInFluid(FluidTags.WATER);
      this.fluidOnEyes = null;
      double d0 = this.getEyeY() - (double)0.11111111F;
      Entity entity = this.getVehicle();
      if (entity instanceof BoatEntity) {
         BoatEntity boatentity = (BoatEntity)entity;
         if (!boatentity.isUnderWater() && boatentity.getBoundingBox().maxY >= d0 && boatentity.getBoundingBox().minY <= d0) {
            return;
         }
      }

      BlockPos blockpos = new BlockPos(this.getX(), d0, this.getZ());
      FluidState fluidstate = this.level.getFluidState(blockpos);

      for(ITag<Fluid> itag : FluidTags.getWrappers()) {
         if (fluidstate.is(itag)) {
            double d1 = (double)((float)blockpos.getY() + fluidstate.getHeight(this.level, blockpos));
            if (d1 > d0) {
               this.fluidOnEyes = itag;
            }

            return;
         }
      }

   }

   /**
    * Plays the {@link #getSplashSound() splash sound}, and the {@link ParticleType#WATER_BUBBLE} and {@link
    * ParticleType#WATER_SPLASH} particles.
    */
   protected void doWaterSplashEffect() {
      Entity entity = this.isVehicle() && this.getControllingPassenger() != null ? this.getControllingPassenger() : this;
      float f = entity == this ? 0.2F : 0.9F;
      Vector3d vector3d = entity.getDeltaMovement();
      float f1 = MathHelper.sqrt(vector3d.x * vector3d.x * (double)0.2F + vector3d.y * vector3d.y + vector3d.z * vector3d.z * (double)0.2F) * f;
      if (f1 > 1.0F) {
         f1 = 1.0F;
      }

      if ((double)f1 < 0.25D) {
         this.playSound(this.getSwimSplashSound(), f1, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
      } else {
         this.playSound(this.getSwimHighSpeedSplashSound(), f1, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
      }

      float f2 = (float)MathHelper.floor(this.getY());

      for(int i = 0; (float)i < 1.0F + this.dimensions.width * 20.0F; ++i) {
         double d0 = (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.dimensions.width;
         double d1 = (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.dimensions.width;
         this.level.addParticle(ParticleTypes.BUBBLE, this.getX() + d0, (double)(f2 + 1.0F), this.getZ() + d1, vector3d.x, vector3d.y - this.random.nextDouble() * (double)0.2F, vector3d.z);
      }

      for(int j = 0; (float)j < 1.0F + this.dimensions.width * 20.0F; ++j) {
         double d2 = (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.dimensions.width;
         double d3 = (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.dimensions.width;
         this.level.addParticle(ParticleTypes.SPLASH, this.getX() + d2, (double)(f2 + 1.0F), this.getZ() + d3, vector3d.x, vector3d.y, vector3d.z);
      }

   }

   protected BlockState getBlockStateOn() {
      return this.level.getBlockState(this.getOnPos());
   }

   public boolean canSpawnSprintParticle() {
      return this.isSprinting() && !this.isInWater() && !this.isSpectator() && !this.isCrouching() && !this.isInLava() && this.isAlive();
   }

   protected void spawnSprintParticle() {
      int i = MathHelper.floor(this.getX());
      int j = MathHelper.floor(this.getY() - (double)0.2F);
      int k = MathHelper.floor(this.getZ());
      BlockPos blockpos = new BlockPos(i, j, k);
      BlockState blockstate = this.level.getBlockState(blockpos);
      if(!blockstate.addRunningEffects(level, blockpos, this))
      if (blockstate.getRenderShape() != BlockRenderType.INVISIBLE) {
         Vector3d vector3d = this.getDeltaMovement();
         this.level.addParticle(new BlockParticleData(ParticleTypes.BLOCK, blockstate).setPos(blockpos), this.getX() + (this.random.nextDouble() - 0.5D) * (double)this.dimensions.width, this.getY() + 0.1D, this.getZ() + (this.random.nextDouble() - 0.5D) * (double)this.dimensions.width, vector3d.x * -4.0D, 1.5D, vector3d.z * -4.0D);
      }

   }

   public boolean isEyeInFluid(ITag<Fluid> pTag) {
      return this.fluidOnEyes == pTag;
   }

   public boolean isInLava() {
      return !this.firstTick && this.fluidHeight.getDouble(FluidTags.LAVA) > 0.0D;
   }

   public void moveRelative(float pAmount, Vector3d pRelative) {
      Vector3d vector3d = getInputVector(pRelative, pAmount, this.yRot);
      this.setDeltaMovement(this.getDeltaMovement().add(vector3d));
   }

   private static Vector3d getInputVector(Vector3d pRelative, float pMotionScaler, float pFacing) {
      double d0 = pRelative.lengthSqr();
      if (d0 < 1.0E-7D) {
         return Vector3d.ZERO;
      } else {
         Vector3d vector3d = (d0 > 1.0D ? pRelative.normalize() : pRelative).scale((double)pMotionScaler);
         float f = MathHelper.sin(pFacing * ((float)Math.PI / 180F));
         float f1 = MathHelper.cos(pFacing * ((float)Math.PI / 180F));
         return new Vector3d(vector3d.x * (double)f1 - vector3d.z * (double)f, vector3d.y, vector3d.z * (double)f1 + vector3d.x * (double)f);
      }
   }

   /**
    * Gets how bright this entity is.
    */
   public float getBrightness() {
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable(this.getX(), 0.0D, this.getZ());
      if (this.level.hasChunkAt(blockpos$mutable)) {
         blockpos$mutable.setY(MathHelper.floor(this.getEyeY()));
         return this.level.getBrightness(blockpos$mutable);
      } else {
         return 0.0F;
      }
   }

   public void setLevel(World pLevel) {
      this.level = pLevel;
   }

   /**
    * Sets position and rotation, clamping and wrapping params to valid values. Used by network code.
    */
   public void absMoveTo(double pX, double pY, double pZ, float pYRot, float pXRot) {
      this.absMoveTo(pX, pY, pZ);
      this.yRot = pYRot % 360.0F;
      this.xRot = MathHelper.clamp(pXRot, -90.0F, 90.0F) % 360.0F;
      this.yRotO = this.yRot;
      this.xRotO = this.xRot;
   }

   public void absMoveTo(double pX, double pY, double pZ) {
      double d0 = MathHelper.clamp(pX, -3.0E7D, 3.0E7D);
      double d1 = MathHelper.clamp(pZ, -3.0E7D, 3.0E7D);
      this.xo = d0;
      this.yo = pY;
      this.zo = d1;
      this.setPos(d0, pY, d1);
   }

   public void moveTo(Vector3d pVec) {
      this.moveTo(pVec.x, pVec.y, pVec.z);
   }

   public void moveTo(double pX, double pY, double pZ) {
      this.moveTo(pX, pY, pZ, this.yRot, this.xRot);
   }

   public void moveTo(BlockPos pPos, float pYRot, float pXRot) {
      this.moveTo((double)pPos.getX() + 0.5D, (double)pPos.getY(), (double)pPos.getZ() + 0.5D, pYRot, pXRot);
   }

   /**
    * Sets the location and rotation of the entity in the world.
    */
   public void moveTo(double pX, double pY, double pZ, float pYRot, float pXRot) {
      this.setPosAndOldPos(pX, pY, pZ);
      this.yRot = pYRot;
      this.xRot = pXRot;
      this.reapplyPosition();
   }

   public void setPosAndOldPos(double pX, double pY, double pZ) {
      this.setPosRaw(pX, pY, pZ);
      this.xo = pX;
      this.yo = pY;
      this.zo = pZ;
      this.xOld = pX;
      this.yOld = pY;
      this.zOld = pZ;
   }

   /**
    * Returns the distance to the entity.
    */
   public float distanceTo(Entity pEntity) {
      float f = (float)(this.getX() - pEntity.getX());
      float f1 = (float)(this.getY() - pEntity.getY());
      float f2 = (float)(this.getZ() - pEntity.getZ());
      return MathHelper.sqrt(f * f + f1 * f1 + f2 * f2);
   }

   /**
    * Gets the squared distance to the position.
    */
   public double distanceToSqr(double pX, double pY, double pZ) {
      double d0 = this.getX() - pX;
      double d1 = this.getY() - pY;
      double d2 = this.getZ() - pZ;
      return d0 * d0 + d1 * d1 + d2 * d2;
   }

   /**
    * Returns the squared distance to the entity.
    */
   public double distanceToSqr(Entity pEntity) {
      return this.distanceToSqr(pEntity.position());
   }

   public double distanceToSqr(Vector3d pVec) {
      double d0 = this.getX() - pVec.x;
      double d1 = this.getY() - pVec.y;
      double d2 = this.getZ() - pVec.z;
      return d0 * d0 + d1 * d1 + d2 * d2;
   }

   /**
    * Called by a player entity when they collide with an entity
    */
   public void playerTouch(PlayerEntity pEntity) {
   }

   /**
    * Applies a velocity to the entities, to push them away from eachother.
    */
   public void push(Entity pEntity) {
      if (!this.isPassengerOfSameVehicle(pEntity)) {
         if (!pEntity.noPhysics && !this.noPhysics) {
            double d0 = pEntity.getX() - this.getX();
            double d1 = pEntity.getZ() - this.getZ();
            double d2 = MathHelper.absMax(d0, d1);
            if (d2 >= (double)0.01F) {
               d2 = (double)MathHelper.sqrt(d2);
               d0 = d0 / d2;
               d1 = d1 / d2;
               double d3 = 1.0D / d2;
               if (d3 > 1.0D) {
                  d3 = 1.0D;
               }

               d0 = d0 * d3;
               d1 = d1 * d3;
               d0 = d0 * (double)0.05F;
               d1 = d1 * (double)0.05F;
               d0 = d0 * (double)(1.0F - this.pushthrough);
               d1 = d1 * (double)(1.0F - this.pushthrough);
               if (!this.isVehicle()) {
                  this.push(-d0, 0.0D, -d1);
               }

               if (!pEntity.isVehicle()) {
                  pEntity.push(d0, 0.0D, d1);
               }
            }

         }
      }
   }

   /**
    * Adds to the current velocity of the entity, and sets {@link #isAirBorne} to true.
    */
   public void push(double pX, double pY, double pZ) {
      this.setDeltaMovement(this.getDeltaMovement().add(pX, pY, pZ));
      this.hasImpulse = true;
   }

   /**
    * Marks this entity's velocity as changed, so that it can be re-synced with the client later
    */
   protected void markHurt() {
      this.hurtMarked = true;
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (this.isInvulnerableTo(pSource)) {
         return false;
      } else {
         this.markHurt();
         return false;
      }
   }

   /**
    * interpolated look vector
    */
   public final Vector3d getViewVector(float pPartialTicks) {
      return this.calculateViewVector(this.getViewXRot(pPartialTicks), this.getViewYRot(pPartialTicks));
   }

   /**
    * Returns the current X rotation of the entity.
    */
   public float getViewXRot(float pPartialTicks) {
      return pPartialTicks == 1.0F ? this.xRot : MathHelper.lerp(pPartialTicks, this.xRotO, this.xRot);
   }

   /**
    * Returns the current Y rotation of the entity.
    */
   public float getViewYRot(float pPartialTick) {
      return pPartialTick == 1.0F ? this.yRot : MathHelper.lerp(pPartialTick, this.yRotO, this.yRot);
   }

   /**
    * Calculates the view vector using the X and Y rotation of an entity.
    */
   protected final Vector3d calculateViewVector(float pXRot, float pYRot) {
      float f = pXRot * ((float)Math.PI / 180F);
      float f1 = -pYRot * ((float)Math.PI / 180F);
      float f2 = MathHelper.cos(f1);
      float f3 = MathHelper.sin(f1);
      float f4 = MathHelper.cos(f);
      float f5 = MathHelper.sin(f);
      return new Vector3d((double)(f3 * f4), (double)(-f5), (double)(f2 * f4));
   }

   public final Vector3d getUpVector(float pPartialTicks) {
      return this.calculateUpVector(this.getViewXRot(pPartialTicks), this.getViewYRot(pPartialTicks));
   }

   protected final Vector3d calculateUpVector(float pXRot, float pYRot) {
      return this.calculateViewVector(pXRot - 90.0F, pYRot);
   }

   public final Vector3d getEyePosition(float pPartialTicks) {
      if (pPartialTicks == 1.0F) {
         return new Vector3d(this.getX(), this.getEyeY(), this.getZ());
      } else {
         double d0 = MathHelper.lerp((double)pPartialTicks, this.xo, this.getX());
         double d1 = MathHelper.lerp((double)pPartialTicks, this.yo, this.getY()) + (double)this.getEyeHeight();
         double d2 = MathHelper.lerp((double)pPartialTicks, this.zo, this.getZ());
         return new Vector3d(d0, d1, d2);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public Vector3d getLightProbePosition(float pPartialTicks) {
      return this.getEyePosition(pPartialTicks);
   }

   @OnlyIn(Dist.CLIENT)
   public final Vector3d getPosition(float pPartialTicks) {
      double d0 = MathHelper.lerp((double)pPartialTicks, this.xo, this.getX());
      double d1 = MathHelper.lerp((double)pPartialTicks, this.yo, this.getY());
      double d2 = MathHelper.lerp((double)pPartialTicks, this.zo, this.getZ());
      return new Vector3d(d0, d1, d2);
   }

   public RayTraceResult pick(double pHitDistance, float pPartialTicks, boolean pHitFluids) {
      Vector3d vector3d = this.getEyePosition(pPartialTicks);
      Vector3d vector3d1 = this.getViewVector(pPartialTicks);
      Vector3d vector3d2 = vector3d.add(vector3d1.x * pHitDistance, vector3d1.y * pHitDistance, vector3d1.z * pHitDistance);
      return this.level.clip(new RayTraceContext(vector3d, vector3d2, RayTraceContext.BlockMode.OUTLINE, pHitFluids ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE, this));
   }

   /**
    * Returns true if other Entities should be prevented from moving through this Entity.
    */
   public boolean isPickable() {
      return false;
   }

   /**
    * Returns true if this entity should push and be pushed by other entities when colliding.
    */
   public boolean isPushable() {
      return false;
   }

   public void awardKillScore(Entity pKilled, int pScoreValue, DamageSource pDamageSource) {
      if (pKilled instanceof ServerPlayerEntity) {
         CriteriaTriggers.ENTITY_KILLED_PLAYER.trigger((ServerPlayerEntity)pKilled, this, pDamageSource);
      }

   }

   @OnlyIn(Dist.CLIENT)
   public boolean shouldRender(double pX, double pY, double pZ) {
      double d0 = this.getX() - pX;
      double d1 = this.getY() - pY;
      double d2 = this.getZ() - pZ;
      double d3 = d0 * d0 + d1 * d1 + d2 * d2;
      return this.shouldRenderAtSqrDistance(d3);
   }

   /**
    * Checks if the entity is in range to render.
    */
   @OnlyIn(Dist.CLIENT)
   public boolean shouldRenderAtSqrDistance(double pDistance) {
      double d0 = this.getBoundingBox().getSize();
      if (Double.isNaN(d0)) {
         d0 = 1.0D;
      }

      d0 = d0 * 64.0D * viewScale;
      return pDistance < d0 * d0;
   }

   /**
    * Writes this entity to NBT, unless it has been removed. Also writes this entity's passengers, and the entity type
    * ID (so the produced NBT is sufficient to recreate the entity).
    * 
    * Generally, {@link #writeUnlessPassenger} or {@link #writeWithoutTypeId} should be used instead of this method.
    * 
    * @return True if the entity was written (and the passed compound should be saved)" false if the entity was not
    * written.
    */
   public boolean saveAsPassenger(CompoundNBT pCompound) {
      String s = this.getEncodeId();
      if (!this.removed && s != null) {
         pCompound.putString("id", s);
         this.saveWithoutId(pCompound);
         return true;
      } else {
         return false;
      }
   }

   /**
    * Writes this entity to NBT, unless it has been removed or it is a passenger. Also writes this entity's passengers,
    * and the entity type ID (so the produced NBT is sufficient to recreate the entity).
    * To always write the entity, use {@link #writeWithoutTypeId}.
    * 
    * @return True if the entity was written (and the passed compound should be saved)" false if the entity was not
    * written.
    */
   public boolean save(CompoundNBT pCompound) {
      return this.isPassenger() ? false : this.saveAsPassenger(pCompound);
   }

   /**
    * Writes this entity, including passengers, to NBT, regardless as to whether or not it is removed or a passenger.
    * Does <b>not</b> include the entity's type ID, so the NBT is insufficient to recreate the entity using {@link
    * AnvilChunkLoader#readWorldEntity}. Use {@link #writeUnlessPassenger} for that purpose.
    */
   public CompoundNBT saveWithoutId(CompoundNBT pCompound) {
      try {
         if (this.vehicle != null) {
            pCompound.put("Pos", this.newDoubleList(this.vehicle.getX(), this.getY(), this.vehicle.getZ()));
         } else {
            pCompound.put("Pos", this.newDoubleList(this.getX(), this.getY(), this.getZ()));
         }

         Vector3d vector3d = this.getDeltaMovement();
         pCompound.put("Motion", this.newDoubleList(vector3d.x, vector3d.y, vector3d.z));
         pCompound.put("Rotation", this.newFloatList(this.yRot, this.xRot));
         pCompound.putFloat("FallDistance", this.fallDistance);
         pCompound.putShort("Fire", (short)this.remainingFireTicks);
         pCompound.putShort("Air", (short)this.getAirSupply());
         pCompound.putBoolean("OnGround", this.onGround);
         pCompound.putBoolean("Invulnerable", this.invulnerable);
         pCompound.putInt("PortalCooldown", this.portalCooldown);
         pCompound.putUUID("UUID", this.getUUID());
         ITextComponent itextcomponent = this.getCustomName();
         if (itextcomponent != null) {
            pCompound.putString("CustomName", ITextComponent.Serializer.toJson(itextcomponent));
         }

         if (this.isCustomNameVisible()) {
            pCompound.putBoolean("CustomNameVisible", this.isCustomNameVisible());
         }

         if (this.isSilent()) {
            pCompound.putBoolean("Silent", this.isSilent());
         }

         if (this.isNoGravity()) {
            pCompound.putBoolean("NoGravity", this.isNoGravity());
         }

         if (this.glowing) {
            pCompound.putBoolean("Glowing", this.glowing);
         }
         pCompound.putBoolean("CanUpdate", canUpdate);

         if (!this.tags.isEmpty()) {
            ListNBT listnbt = new ListNBT();

            for(String s : this.tags) {
               listnbt.add(StringNBT.valueOf(s));
            }

            pCompound.put("Tags", listnbt);
         }

         CompoundNBT caps = serializeCaps();
         if (caps != null) pCompound.put("ForgeCaps", caps);
         if (persistentData != null) pCompound.put("ForgeData", persistentData);

         this.addAdditionalSaveData(pCompound);
         if (this.isVehicle()) {
            ListNBT listnbt1 = new ListNBT();

            for(Entity entity : this.getPassengers()) {
               CompoundNBT compoundnbt = new CompoundNBT();
               if (entity.saveAsPassenger(compoundnbt)) {
                  listnbt1.add(compoundnbt);
               }
            }

            if (!listnbt1.isEmpty()) {
               pCompound.put("Passengers", listnbt1);
            }
         }

         return pCompound;
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Saving entity NBT");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being saved");
         this.fillCrashReportCategory(crashreportcategory);
         throw new ReportedException(crashreport);
      }
   }

   /**
    * Reads the entity from NBT (calls an abstract helper method to read specialized data)
    */
   public void load(CompoundNBT pCompound) {
      try {
         ListNBT listnbt = pCompound.getList("Pos", 6);
         ListNBT listnbt1 = pCompound.getList("Motion", 6);
         ListNBT listnbt2 = pCompound.getList("Rotation", 5);
         double d0 = listnbt1.getDouble(0);
         double d1 = listnbt1.getDouble(1);
         double d2 = listnbt1.getDouble(2);
         this.setDeltaMovement(Math.abs(d0) > 10.0D ? 0.0D : d0, Math.abs(d1) > 10.0D ? 0.0D : d1, Math.abs(d2) > 10.0D ? 0.0D : d2);
         this.setPosAndOldPos(listnbt.getDouble(0), listnbt.getDouble(1), listnbt.getDouble(2));
         this.yRot = listnbt2.getFloat(0);
         this.xRot = listnbt2.getFloat(1);
         this.yRotO = this.yRot;
         this.xRotO = this.xRot;
         this.setYHeadRot(this.yRot);
         this.setYBodyRot(this.yRot);
         this.fallDistance = pCompound.getFloat("FallDistance");
         this.remainingFireTicks = pCompound.getShort("Fire");
         this.setAirSupply(pCompound.getShort("Air"));
         this.onGround = pCompound.getBoolean("OnGround");
         this.invulnerable = pCompound.getBoolean("Invulnerable");
         this.portalCooldown = pCompound.getInt("PortalCooldown");
         if (pCompound.hasUUID("UUID")) {
            this.uuid = pCompound.getUUID("UUID");
            this.stringUUID = this.uuid.toString();
         }

         if (Double.isFinite(this.getX()) && Double.isFinite(this.getY()) && Double.isFinite(this.getZ())) {
            if (Double.isFinite((double)this.yRot) && Double.isFinite((double)this.xRot)) {
               this.reapplyPosition();
               this.setRot(this.yRot, this.xRot);
               if (pCompound.contains("CustomName", 8)) {
                  String s = pCompound.getString("CustomName");

                  try {
                     this.setCustomName(ITextComponent.Serializer.fromJson(s));
                  } catch (Exception exception) {
                     LOGGER.warn("Failed to parse entity custom name {}", s, exception);
                  }
               }

               this.setCustomNameVisible(pCompound.getBoolean("CustomNameVisible"));
               this.setSilent(pCompound.getBoolean("Silent"));
               this.setNoGravity(pCompound.getBoolean("NoGravity"));
               this.setGlowing(pCompound.getBoolean("Glowing"));
               if (pCompound.contains("ForgeData", 10)) persistentData = pCompound.getCompound("ForgeData");
               if (pCompound.contains("CanUpdate", 99)) this.canUpdate(pCompound.getBoolean("CanUpdate"));
               if (pCompound.contains("ForgeCaps", 10)) deserializeCaps(pCompound.getCompound("ForgeCaps"));
               if (pCompound.contains("Tags", 9)) {
                  this.tags.clear();
                  ListNBT listnbt3 = pCompound.getList("Tags", 8);
                  int i = Math.min(listnbt3.size(), 1024);

                  for(int j = 0; j < i; ++j) {
                     this.tags.add(listnbt3.getString(j));
                  }
               }

               this.readAdditionalSaveData(pCompound);
               if (this.repositionEntityAfterLoad()) {
                  this.reapplyPosition();
               }

            } else {
               throw new IllegalStateException("Entity has invalid rotation");
            }
         } else {
            throw new IllegalStateException("Entity has invalid position");
         }
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Loading entity NBT");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being loaded");
         this.fillCrashReportCategory(crashreportcategory);
         throw new ReportedException(crashreport);
      }
   }

   protected boolean repositionEntityAfterLoad() {
      return true;
   }

   /**
    * Returns the string that identifies this Entity's class
    */
   @Nullable
   public final String getEncodeId() {
      EntityType<?> entitytype = this.getType();
      ResourceLocation resourcelocation = EntityType.getKey(entitytype);
      return entitytype.canSerialize() && resourcelocation != null ? resourcelocation.toString() : null;
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   protected abstract void readAdditionalSaveData(CompoundNBT pCompound);

   protected abstract void addAdditionalSaveData(CompoundNBT pCompound);

   /**
    * creates a NBT list from the array of doubles passed to this function
    */
   protected ListNBT newDoubleList(double... pNumbers) {
      ListNBT listnbt = new ListNBT();

      for(double d0 : pNumbers) {
         listnbt.add(DoubleNBT.valueOf(d0));
      }

      return listnbt;
   }

   /**
    * Returns a new NBTTagList filled with the specified floats
    */
   protected ListNBT newFloatList(float... pNumbers) {
      ListNBT listnbt = new ListNBT();

      for(float f : pNumbers) {
         listnbt.add(FloatNBT.valueOf(f));
      }

      return listnbt;
   }

   @Nullable
   public ItemEntity spawnAtLocation(IItemProvider pItem) {
      return this.spawnAtLocation(pItem, 0);
   }

   @Nullable
   public ItemEntity spawnAtLocation(IItemProvider pItem, int pOffsetY) {
      return this.spawnAtLocation(new ItemStack(pItem), (float)pOffsetY);
   }

   @Nullable
   public ItemEntity spawnAtLocation(ItemStack pStack) {
      return this.spawnAtLocation(pStack, 0.0F);
   }

   /**
    * Drops an item at the position of the entity.
    */
   @Nullable
   public ItemEntity spawnAtLocation(ItemStack pStack, float pOffsetY) {
      if (pStack.isEmpty()) {
         return null;
      } else if (this.level.isClientSide) {
         return null;
      } else {
         ItemEntity itementity = new ItemEntity(this.level, this.getX(), this.getY() + (double)pOffsetY, this.getZ(), pStack);
         itementity.setDefaultPickUpDelay();
         if (captureDrops() != null) captureDrops().add(itementity);
         else
         this.level.addFreshEntity(itementity);
         return itementity;
      }
   }

   /**
    * Returns true if the entity has not been {@link #removed}.
    */
   public boolean isAlive() {
      return !this.removed;
   }

   /**
    * Checks if this entity is inside of an opaque block
    */
   public boolean isInWall() {
      if (this.noPhysics) {
         return false;
      } else {
         float f = 0.1F;
         float f1 = this.dimensions.width * 0.8F;
         AxisAlignedBB axisalignedbb = AxisAlignedBB.ofSize((double)f1, (double)0.1F, (double)f1).move(this.getX(), this.getEyeY(), this.getZ());
         return this.level.getBlockCollisions(this, axisalignedbb, (p_241338_1_, p_241338_2_) -> {
            return p_241338_1_.isSuffocating(this.level, p_241338_2_);
         }).findAny().isPresent();
      }
   }

   public ActionResultType interact(PlayerEntity pPlayer, Hand pHand) {
      return ActionResultType.PASS;
   }

   public boolean canCollideWith(Entity pEntity) {
      return pEntity.canBeCollidedWith() && !this.isPassengerOfSameVehicle(pEntity);
   }

   public boolean canBeCollidedWith() {
      return false;
   }

   /**
    * Handles updating while riding another entity
    */
   public void rideTick() {
      this.setDeltaMovement(Vector3d.ZERO);
      if (canUpdate())
      this.tick();
      if (this.isPassenger()) {
         this.getVehicle().positionRider(this);
      }
   }

   public void positionRider(Entity pPassenger) {
      this.positionRider(pPassenger, Entity::setPos);
   }

   private void positionRider(Entity pPassenger, Entity.IMoveCallback pCallback) {
      if (this.hasPassenger(pPassenger)) {
         double d0 = this.getY() + this.getPassengersRidingOffset() + pPassenger.getMyRidingOffset();
         pCallback.accept(pPassenger, this.getX(), d0, this.getZ());
      }
   }

   /**
    * Applies this entity's orientation to another entity. Used to update passenger orientation.
    */
   @OnlyIn(Dist.CLIENT)
   public void onPassengerTurned(Entity pEntityToUpdate) {
   }

   /**
    * Returns the Y Offset of this entity.
    */
   public double getMyRidingOffset() {
      return 0.0D;
   }

   /**
    * Returns the Y offset from the entity's position for any entity riding this one.
    */
   public double getPassengersRidingOffset() {
      return (double)this.dimensions.height * 0.75D;
   }

   public boolean startRiding(Entity pVehicle) {
      return this.startRiding(pVehicle, false);
   }

   @OnlyIn(Dist.CLIENT)
   public boolean showVehicleHealth() {
      return this instanceof LivingEntity;
   }

   public boolean startRiding(Entity pEntity, boolean pForce) {
      for(Entity entity = pEntity; entity.vehicle != null; entity = entity.vehicle) {
         if (entity.vehicle == this) {
            return false;
         }
      }

      if (!net.minecraftforge.event.ForgeEventFactory.canMountEntity(this, pEntity, true)) return false;
      if (pForce || this.canRide(pEntity) && pEntity.canAddPassenger(this)) {
         if (this.isPassenger()) {
            this.stopRiding();
         }

         this.setPose(Pose.STANDING);
         this.vehicle = pEntity;
         this.vehicle.addPassenger(this);
         return true;
      } else {
         return false;
      }
   }

   protected boolean canRide(Entity pEntity) {
      return !this.isShiftKeyDown() && this.boardingCooldown <= 0;
   }

   protected boolean canEnterPose(Pose pPose) {
      return this.level.noCollision(this, this.getBoundingBoxForPose(pPose).deflate(1.0E-7D));
   }

   /**
    * Dismounts all entities riding this entity from this entity.
    */
   public void ejectPassengers() {
      for(int i = this.passengers.size() - 1; i >= 0; --i) {
         this.passengers.get(i).stopRiding();
      }

   }

   public void removeVehicle() {
      if (this.vehicle != null) {
         Entity entity = this.vehicle;
         if (!net.minecraftforge.event.ForgeEventFactory.canMountEntity(this, entity, false)) return;
         this.vehicle = null;
         entity.removePassenger(this);
      }

   }

   /**
    * Dismounts this entity from the entity it is riding.
    */
   public void stopRiding() {
      this.removeVehicle();
   }

   protected void addPassenger(Entity pPassenger) {
      if (pPassenger.getVehicle() != this) {
         throw new IllegalStateException("Use x.startRiding(y), not y.addPassenger(x)");
      } else {
         if (!this.level.isClientSide && pPassenger instanceof PlayerEntity && !(this.getControllingPassenger() instanceof PlayerEntity)) {
            this.passengers.add(0, pPassenger);
         } else {
            this.passengers.add(pPassenger);
         }

      }
   }

   protected void removePassenger(Entity pPassenger) {
      if (pPassenger.getVehicle() == this) {
         throw new IllegalStateException("Use x.stopRiding(y), not y.removePassenger(x)");
      } else {
         this.passengers.remove(pPassenger);
         pPassenger.boardingCooldown = 60;
      }
   }

   protected boolean canAddPassenger(Entity pPassenger) {
      return this.getPassengers().size() < 1;
   }

   /**
    * Sets a target for the client to interpolate towards over the next few ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void lerpTo(double pX, double pY, double pZ, float pYRot, float pXRot, int pLerpSteps, boolean pTeleport) {
      this.setPos(pX, pY, pZ);
      this.setRot(pYRot, pXRot);
   }

   @OnlyIn(Dist.CLIENT)
   public void lerpHeadTo(float pYaw, int pPitch) {
      this.setYHeadRot(pYaw);
   }

   public float getPickRadius() {
      return 0.0F;
   }

   /**
    * returns a (normalized) vector of where this entity is looking
    */
   public Vector3d getLookAngle() {
      return this.calculateViewVector(this.xRot, this.yRot);
   }

   /**
    * returns the Entity's pitch and yaw as a Vec2f
    */
   public Vector2f getRotationVector() {
      return new Vector2f(this.xRot, this.yRot);
   }

   @OnlyIn(Dist.CLIENT)
   public Vector3d getForward() {
      return Vector3d.directionFromRotation(this.getRotationVector());
   }

   /**
    * Marks the entity as being inside a portal, activating teleportation logic in onEntityUpdate() in the following
    * tick(s).
    */
   public void handleInsidePortal(BlockPos pPos) {
      if (this.isOnPortalCooldown()) {
         this.setPortalCooldown();
      } else {
         if (!this.level.isClientSide && !pPos.equals(this.portalEntrancePos)) {
            this.portalEntrancePos = pPos.immutable();
         }

         this.isInsidePortal = true;
      }
   }

   protected void handleNetherPortal() {
      if (this.level instanceof ServerWorld) {
         int i = this.getPortalWaitTime();
         ServerWorld serverworld = (ServerWorld)this.level;
         if (this.isInsidePortal) {
            MinecraftServer minecraftserver = serverworld.getServer();
            RegistryKey<World> registrykey = this.level.dimension() == World.NETHER ? World.OVERWORLD : World.NETHER;
            ServerWorld serverworld1 = minecraftserver.getLevel(registrykey);
            if (serverworld1 != null && minecraftserver.isNetherEnabled() && !this.isPassenger() && this.portalTime++ >= i) {
               this.level.getProfiler().push("portal");
               this.portalTime = i;
               this.setPortalCooldown();
               this.changeDimension(serverworld1);
               this.level.getProfiler().pop();
            }

            this.isInsidePortal = false;
         } else {
            if (this.portalTime > 0) {
               this.portalTime -= 4;
            }

            if (this.portalTime < 0) {
               this.portalTime = 0;
            }
         }

         this.processPortalCooldown();
      }
   }

   /**
    * Return the amount of cooldown before this entity can use a portal again.
    */
   public int getDimensionChangingDelay() {
      return 300;
   }

   /**
    * Updates the entity motion clientside, called by packets from the server
    */
   @OnlyIn(Dist.CLIENT)
   public void lerpMotion(double pX, double pY, double pZ) {
      this.setDeltaMovement(pX, pY, pZ);
   }

   /**
    * Handles an entity event fired from {@link net.minecraft.world.level.Level#broadcastEntityEvent}.
    */
   @OnlyIn(Dist.CLIENT)
   public void handleEntityEvent(byte pId) {
      switch(pId) {
      case 53:
         HoneyBlock.showSlideParticles(this);
      default:
      }
   }

   /**
    * Setups the entity to do the hurt animation. Only used by packets in multiplayer.
    */
   @OnlyIn(Dist.CLIENT)
   public void animateHurt() {
   }

   public Iterable<ItemStack> getHandSlots() {
      return EMPTY_LIST;
   }

   public Iterable<ItemStack> getArmorSlots() {
      return EMPTY_LIST;
   }

   public Iterable<ItemStack> getAllSlots() {
      return Iterables.concat(this.getHandSlots(), this.getArmorSlots());
   }

   public void setItemSlot(EquipmentSlotType pSlot, ItemStack pStack) {
   }

   /**
    * Returns true if the entity is on fire. Used by render to add the fire effect on rendering.
    */
   public boolean isOnFire() {
      boolean flag = this.level != null && this.level.isClientSide;
      return !this.fireImmune() && (this.remainingFireTicks > 0 || flag && this.getSharedFlag(0));
   }

   public boolean isPassenger() {
      return this.getVehicle() != null;
   }

   /**
    * If at least 1 entity is riding this one
    */
   public boolean isVehicle() {
      return !this.getPassengers().isEmpty();
   }

   @Deprecated //Forge: Use rider sensitive version
   public boolean rideableUnderWater() {
      return true;
   }

   public void setShiftKeyDown(boolean pKeyDown) {
      this.setSharedFlag(1, pKeyDown);
   }

   public boolean isShiftKeyDown() {
      return this.getSharedFlag(1);
   }

   public boolean isSteppingCarefully() {
      return this.isShiftKeyDown();
   }

   public boolean isSuppressingBounce() {
      return this.isShiftKeyDown();
   }

   public boolean isDiscrete() {
      return this.isShiftKeyDown();
   }

   public boolean isDescending() {
      return this.isShiftKeyDown();
   }

   public boolean isCrouching() {
      return this.getPose() == Pose.CROUCHING;
   }

   /**
    * Get if the Entity is sprinting.
    */
   public boolean isSprinting() {
      return this.getSharedFlag(3);
   }

   /**
    * Set sprinting switch for Entity.
    */
   public void setSprinting(boolean pSprinting) {
      this.setSharedFlag(3, pSprinting);
   }

   public boolean isSwimming() {
      return this.getSharedFlag(4);
   }

   public boolean isVisuallySwimming() {
      return this.getPose() == Pose.SWIMMING;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isVisuallyCrawling() {
      return this.isVisuallySwimming() && !this.isInWater();
   }

   public void setSwimming(boolean pSwimming) {
      this.setSharedFlag(4, pSwimming);
   }

   public boolean isGlowing() {
      return this.glowing || this.level.isClientSide && this.getSharedFlag(6);
   }

   public void setGlowing(boolean pGlowing) {
      this.glowing = pGlowing;
      if (!this.level.isClientSide) {
         this.setSharedFlag(6, this.glowing);
      }

   }

   public boolean isInvisible() {
      return this.getSharedFlag(5);
   }

   /**
    * Only used by renderer in EntityLivingBase subclasses.
    * Determines if an entity is visible or not to a specific player, if the entity is normally invisible.
    * For EntityLivingBase subclasses, returning false when invisible will render the entity semi-transparent.
    */
   @OnlyIn(Dist.CLIENT)
   public boolean isInvisibleTo(PlayerEntity pPlayer) {
      if (pPlayer.isSpectator()) {
         return false;
      } else {
         Team team = this.getTeam();
         return team != null && pPlayer != null && pPlayer.getTeam() == team && team.canSeeFriendlyInvisibles() ? false : this.isInvisible();
      }
   }

   @Nullable
   public Team getTeam() {
      return this.level.getScoreboard().getPlayersTeam(this.getScoreboardName());
   }

   /**
    * Returns whether this Entity is on the same team as the given Entity.
    */
   public boolean isAlliedTo(Entity pEntity) {
      return this.isAlliedTo(pEntity.getTeam());
   }

   /**
    * Returns whether this Entity is on the given scoreboard team.
    */
   public boolean isAlliedTo(Team pTeam) {
      return this.getTeam() != null ? this.getTeam().isAlliedTo(pTeam) : false;
   }

   public void setInvisible(boolean pInvisible) {
      this.setSharedFlag(5, pInvisible);
   }

   /**
    * Returns true if the flag is active for the entity. Known flags: 0: burning 1: sneaking 2: unused 3: sprinting 4:
    * swimming 5: invisible 6: glowing 7: elytra flying
    */
   protected boolean getSharedFlag(int pFlag) {
      return (this.entityData.get(DATA_SHARED_FLAGS_ID) & 1 << pFlag) != 0;
   }

   /**
    * Enable or disable a entity flag, see getEntityFlag to read the know flags.
    */
   protected void setSharedFlag(int pFlag, boolean pSet) {
      byte b0 = this.entityData.get(DATA_SHARED_FLAGS_ID);
      if (pSet) {
         this.entityData.set(DATA_SHARED_FLAGS_ID, (byte)(b0 | 1 << pFlag));
      } else {
         this.entityData.set(DATA_SHARED_FLAGS_ID, (byte)(b0 & ~(1 << pFlag)));
      }

   }

   public int getMaxAirSupply() {
      return 300;
   }

   public int getAirSupply() {
      return this.entityData.get(DATA_AIR_SUPPLY_ID);
   }

   public void setAirSupply(int pAir) {
      this.entityData.set(DATA_AIR_SUPPLY_ID, pAir);
   }

   public void thunderHit(ServerWorld pLevel, LightningBoltEntity pLightning) {
      this.setRemainingFireTicks(this.remainingFireTicks + 1);
      if (this.remainingFireTicks == 0) {
         this.setSecondsOnFire(8);
      }

      this.hurt(DamageSource.LIGHTNING_BOLT, pLightning.getDamage());
   }

   public void onAboveBubbleCol(boolean pDownwards) {
      Vector3d vector3d = this.getDeltaMovement();
      double d0;
      if (pDownwards) {
         d0 = Math.max(-0.9D, vector3d.y - 0.03D);
      } else {
         d0 = Math.min(1.8D, vector3d.y + 0.1D);
      }

      this.setDeltaMovement(vector3d.x, d0, vector3d.z);
   }

   public void onInsideBubbleColumn(boolean pDownwards) {
      Vector3d vector3d = this.getDeltaMovement();
      double d0;
      if (pDownwards) {
         d0 = Math.max(-0.3D, vector3d.y - 0.03D);
      } else {
         d0 = Math.min(0.7D, vector3d.y + 0.06D);
      }

      this.setDeltaMovement(vector3d.x, d0, vector3d.z);
      this.fallDistance = 0.0F;
   }

   public void killed(ServerWorld pLevel, LivingEntity pKilledEntity) {
   }

   protected void moveTowardsClosestSpace(double pX, double pY, double pZ) {
      BlockPos blockpos = new BlockPos(pX, pY, pZ);
      Vector3d vector3d = new Vector3d(pX - (double)blockpos.getX(), pY - (double)blockpos.getY(), pZ - (double)blockpos.getZ());
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
      Direction direction = Direction.UP;
      double d0 = Double.MAX_VALUE;

      for(Direction direction1 : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP}) {
         blockpos$mutable.setWithOffset(blockpos, direction1);
         if (!this.level.getBlockState(blockpos$mutable).isCollisionShapeFullBlock(this.level, blockpos$mutable)) {
            double d1 = vector3d.get(direction1.getAxis());
            double d2 = direction1.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0D - d1 : d1;
            if (d2 < d0) {
               d0 = d2;
               direction = direction1;
            }
         }
      }

      float f = this.random.nextFloat() * 0.2F + 0.1F;
      float f1 = (float)direction.getAxisDirection().getStep();
      Vector3d vector3d1 = this.getDeltaMovement().scale(0.75D);
      if (direction.getAxis() == Direction.Axis.X) {
         this.setDeltaMovement((double)(f1 * f), vector3d1.y, vector3d1.z);
      } else if (direction.getAxis() == Direction.Axis.Y) {
         this.setDeltaMovement(vector3d1.x, (double)(f1 * f), vector3d1.z);
      } else if (direction.getAxis() == Direction.Axis.Z) {
         this.setDeltaMovement(vector3d1.x, vector3d1.y, (double)(f1 * f));
      }

   }

   public void makeStuckInBlock(BlockState pState, Vector3d pMotionMultiplier) {
      this.fallDistance = 0.0F;
      this.stuckSpeedMultiplier = pMotionMultiplier;
   }

   private static ITextComponent removeAction(ITextComponent pName) {
      IFormattableTextComponent iformattabletextcomponent = pName.plainCopy().setStyle(pName.getStyle().withClickEvent((ClickEvent)null));

      for(ITextComponent itextcomponent : pName.getSiblings()) {
         iformattabletextcomponent.append(removeAction(itextcomponent));
      }

      return iformattabletextcomponent;
   }

   public ITextComponent getName() {
      ITextComponent itextcomponent = this.getCustomName();
      return itextcomponent != null ? removeAction(itextcomponent) : this.getTypeName();
   }

   protected ITextComponent getTypeName() {
      return this.getType().getDescription(); // Forge: Use getter to allow overriding by mods
   }

   /**
    * Returns true if Entity argument is equal to this Entity
    */
   public boolean is(Entity pEntity) {
      return this == pEntity;
   }

   public float getYHeadRot() {
      return 0.0F;
   }

   /**
    * Sets the head's Y rotation of the entity.
    */
   public void setYHeadRot(float pYHeadRot) {
   }

   /**
    * Set the body Y rotation of the entity.
    */
   public void setYBodyRot(float pYBodyRot) {
   }

   /**
    * Returns true if it's possible to attack this entity with an item.
    */
   public boolean isAttackable() {
      return true;
   }

   /**
    * Called when a player attacks an entity. If this returns true the attack will not happen.
    */
   public boolean skipAttackInteraction(Entity pEntity) {
      return false;
   }

   public String toString() {
      return String.format(Locale.ROOT, "%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f]", this.getClass().getSimpleName(), this.getName().getString(), this.id, this.level == null ? "~NULL~" : this.level.toString(), this.getX(), this.getY(), this.getZ());
   }

   /**
    * Returns whether this Entity is invulnerable to the given DamageSource.
    */
   public boolean isInvulnerableTo(DamageSource pDamageSource) {
      return this.invulnerable && pDamageSource != DamageSource.OUT_OF_WORLD && !pDamageSource.isCreativePlayer();
   }

   public boolean isInvulnerable() {
      return this.invulnerable;
   }

   /**
    * Sets whether this Entity is invulnerable.
    */
   public void setInvulnerable(boolean pIsInvulnerable) {
      this.invulnerable = pIsInvulnerable;
   }

   /**
    * Sets this entity's location and angles to the location and angles of the passed in entity.
    */
   public void copyPosition(Entity pEntity) {
      this.moveTo(pEntity.getX(), pEntity.getY(), pEntity.getZ(), pEntity.yRot, pEntity.xRot);
   }

   /**
    * Prepares this entity in new dimension by copying NBT data from entity in old dimension
    */
   public void restoreFrom(Entity pEntity) {
      CompoundNBT compoundnbt = pEntity.saveWithoutId(new CompoundNBT());
      compoundnbt.remove("Dimension");
      this.load(compoundnbt);
      this.portalCooldown = pEntity.portalCooldown;
      this.portalEntrancePos = pEntity.portalEntrancePos;
   }

   @Nullable
   public Entity changeDimension(ServerWorld pServer) {
      return this.changeDimension(pServer, pServer.getPortalForcer());
   }
   @Nullable
   public Entity changeDimension(ServerWorld pServer, net.minecraftforge.common.util.ITeleporter teleporter) {
      if (this.level instanceof ServerWorld && !this.removed) {
         this.level.getProfiler().push("changeDimension");
         this.unRide();
         this.level.getProfiler().push("reposition");
         PortalInfo portalinfo = teleporter.getPortalInfo(this, pServer, this::findDimensionEntryPoint);
         if (portalinfo == null) {
            return null;
         } else {
            Entity transportedEntity = teleporter.placeEntity(this, (ServerWorld) this.level, pServer, this.yRot, spawnPortal -> { //Forge: Start vanilla logic
            this.level.getProfiler().popPush("reloading");
            Entity entity = this.getType().create(pServer);
            if (entity != null) {
               entity.restoreFrom(this);
               entity.moveTo(portalinfo.pos.x, portalinfo.pos.y, portalinfo.pos.z, portalinfo.yRot, entity.xRot);
               entity.setDeltaMovement(portalinfo.speed);
               pServer.addFromAnotherDimension(entity);
               if (spawnPortal && pServer.dimension() == World.END) {
                  ServerWorld.makeObsidianPlatform(pServer);
               }
            }
            return entity;
            }); //Forge: End vanilla logic

            this.removeAfterChangingDimensions();
            this.level.getProfiler().pop();
            ((ServerWorld)this.level).resetEmptyTime();
            pServer.resetEmptyTime();
            this.level.getProfiler().pop();
            return transportedEntity;
         }
      } else {
         return null;
      }
   }

   protected void removeAfterChangingDimensions() {
      this.removed = true;
   }

   @Nullable
   protected PortalInfo findDimensionEntryPoint(ServerWorld pDestination) {
      boolean flag = this.level.dimension() == World.END && pDestination.dimension() == World.OVERWORLD;
      boolean flag1 = pDestination.dimension() == World.END;
      if (!flag && !flag1) {
         boolean flag2 = pDestination.dimension() == World.NETHER;
         if (this.level.dimension() != World.NETHER && !flag2) {
            return null;
         } else {
            WorldBorder worldborder = pDestination.getWorldBorder();
            double d0 = Math.max(-2.9999872E7D, worldborder.getMinX() + 16.0D);
            double d1 = Math.max(-2.9999872E7D, worldborder.getMinZ() + 16.0D);
            double d2 = Math.min(2.9999872E7D, worldborder.getMaxX() - 16.0D);
            double d3 = Math.min(2.9999872E7D, worldborder.getMaxZ() - 16.0D);
            double d4 = DimensionType.getTeleportationScale(this.level.dimensionType(), pDestination.dimensionType());
            BlockPos blockpos1 = new BlockPos(MathHelper.clamp(this.getX() * d4, d0, d2), this.getY(), MathHelper.clamp(this.getZ() * d4, d1, d3));
            return this.getExitPortal(pDestination, blockpos1, flag2).map((p_242275_2_) -> {
               BlockState blockstate = this.level.getBlockState(this.portalEntrancePos);
               Direction.Axis direction$axis;
               Vector3d vector3d;
               if (blockstate.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                  direction$axis = blockstate.getValue(BlockStateProperties.HORIZONTAL_AXIS);
                  TeleportationRepositioner.Result teleportationrepositioner$result = TeleportationRepositioner.getLargestRectangleAround(this.portalEntrancePos, direction$axis, 21, Direction.Axis.Y, 21, (p_242276_2_) -> {
                     return this.level.getBlockState(p_242276_2_) == blockstate;
                  });
                  vector3d = this.getRelativePortalPosition(direction$axis, teleportationrepositioner$result);
               } else {
                  direction$axis = Direction.Axis.X;
                  vector3d = new Vector3d(0.5D, 0.0D, 0.0D);
               }

               return PortalSize.createPortalInfo(pDestination, p_242275_2_, direction$axis, vector3d, this.getDimensionsForge(this.getPose()), this.getDeltaMovement(), this.yRot, this.xRot);
            }).orElse((PortalInfo)null);
         }
      } else {
         BlockPos blockpos;
         if (flag1) {
            blockpos = ServerWorld.END_SPAWN_POINT;
         } else {
            blockpos = pDestination.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pDestination.getSharedSpawnPos());
         }

         return new PortalInfo(new Vector3d((double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D), this.getDeltaMovement(), this.yRot, this.xRot);
      }
   }

   protected Vector3d getRelativePortalPosition(Direction.Axis pAxis, TeleportationRepositioner.Result pPortal) {
      return PortalSize.getRelativePosition(pPortal, pAxis, this.position(), this.getDimensionsForge(this.getPose()));
   }

   /**
    * 
    * @param pFindFrom Position where searching starts from
    */
   protected Optional<TeleportationRepositioner.Result> getExitPortal(ServerWorld pDestination, BlockPos pFindFrom, boolean pIsToNether) {
      return pDestination.getPortalForcer().findPortalAround(pFindFrom, pIsToNether);
   }

   /**
    * Returns false if this Entity can't move between dimensions. True if it can.
    */
   public boolean canChangeDimensions() {
      return true;
   }

   /**
    * Explosion resistance of a block relative to this entity
    */
   public float getBlockExplosionResistance(Explosion pExplosion, IBlockReader pLevel, BlockPos pPos, BlockState pBlockState, FluidState pFluidState, float pExplosionPower) {
      return pExplosionPower;
   }

   public boolean shouldBlockExplode(Explosion pExplosion, IBlockReader pLevel, BlockPos pPos, BlockState pBlockState, float pExplosionPower) {
      return true;
   }

   /**
    * The maximum height from where the entity is alowed to jump (used in pathfinder)
    */
   public int getMaxFallDistance() {
      return 3;
   }

   /**
    * Return whether this entity should NOT trigger a pressure plate or a tripwire.
    */
   public boolean isIgnoringBlockTriggers() {
      return false;
   }

   public void fillCrashReportCategory(CrashReportCategory pCategory) {
      pCategory.setDetail("Entity Type", () -> {
         return EntityType.getKey(this.getType()) + " (" + this.getClass().getCanonicalName() + ")";
      });
      pCategory.setDetail("Entity ID", this.id);
      pCategory.setDetail("Entity Name", () -> {
         return this.getName().getString();
      });
      pCategory.setDetail("Entity's Exact location", String.format(Locale.ROOT, "%.2f, %.2f, %.2f", this.getX(), this.getY(), this.getZ()));
      pCategory.setDetail("Entity's Block location", CrashReportCategory.formatLocation(MathHelper.floor(this.getX()), MathHelper.floor(this.getY()), MathHelper.floor(this.getZ())));
      Vector3d vector3d = this.getDeltaMovement();
      pCategory.setDetail("Entity's Momentum", String.format(Locale.ROOT, "%.2f, %.2f, %.2f", vector3d.x, vector3d.y, vector3d.z));
      pCategory.setDetail("Entity's Passengers", () -> {
         return this.getPassengers().toString();
      });
      pCategory.setDetail("Entity's Vehicle", () -> {
         return this.getVehicle().toString();
      });
   }

   /**
    * Return whether this entity should be rendered as on fire.
    */
   @OnlyIn(Dist.CLIENT)
   public boolean displayFireAnimation() {
      return this.isOnFire() && !this.isSpectator();
   }

   public void setUUID(UUID pUniqueId) {
      this.uuid = pUniqueId;
      this.stringUUID = this.uuid.toString();
   }

   public UUID getUUID() {
      return this.uuid;
   }

   public String getStringUUID() {
      return this.stringUUID;
   }

   /**
    * Returns a String to use as this entity's name in the scoreboard/entity selector systems
    */
   public String getScoreboardName() {
      return this.stringUUID;
   }

   public boolean isPushedByFluid() {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   public static double getViewScale() {
      return viewScale;
   }

   @OnlyIn(Dist.CLIENT)
   public static void setViewScale(double pRenderDistWeight) {
      viewScale = pRenderDistWeight;
   }

   public ITextComponent getDisplayName() {
      return ScorePlayerTeam.formatNameForTeam(this.getTeam(), this.getName()).withStyle((p_211516_1_) -> {
         return p_211516_1_.withHoverEvent(this.createHoverEvent()).withInsertion(this.getStringUUID());
      });
   }

   public void setCustomName(@Nullable ITextComponent pName) {
      this.entityData.set(DATA_CUSTOM_NAME, Optional.ofNullable(pName));
   }

   @Nullable
   public ITextComponent getCustomName() {
      return this.entityData.get(DATA_CUSTOM_NAME).orElse((ITextComponent)null);
   }

   public boolean hasCustomName() {
      return this.entityData.get(DATA_CUSTOM_NAME).isPresent();
   }

   public void setCustomNameVisible(boolean pAlwaysRenderNameTag) {
      this.entityData.set(DATA_CUSTOM_NAME_VISIBLE, pAlwaysRenderNameTag);
   }

   public boolean isCustomNameVisible() {
      return this.entityData.get(DATA_CUSTOM_NAME_VISIBLE);
   }

   /**
    * Teleports the entity, forcing the destination to stay loaded for a short time
    */
   public final void teleportToWithTicket(double pX, double pY, double pZ) {
      if (this.level instanceof ServerWorld) {
         ChunkPos chunkpos = new ChunkPos(new BlockPos(pX, pY, pZ));
         ((ServerWorld)this.level).getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkpos, 0, this.getId());
         this.level.getChunk(chunkpos.x, chunkpos.z);
         this.teleportTo(pX, pY, pZ);
      }
   }

   /**
    * Sets the position of the entity and updates the 'last' variables
    */
   public void teleportTo(double pX, double pY, double pZ) {
      if (this.level instanceof ServerWorld) {
         ServerWorld serverworld = (ServerWorld)this.level;
         this.moveTo(pX, pY, pZ, this.yRot, this.xRot);
         this.getSelfAndPassengers().forEach((p_233565_1_) -> {
            serverworld.updateChunkPos(p_233565_1_);
            p_233565_1_.forceChunkAddition = true;

            for(Entity entity : p_233565_1_.passengers) {
               p_233565_1_.positionRider(entity, Entity::moveTo);
            }

         });
      }
   }

   @OnlyIn(Dist.CLIENT)
   public boolean shouldShowName() {
      return this.isCustomNameVisible();
   }

   public void onSyncedDataUpdated(DataParameter<?> pKey) {
      if (DATA_POSE.equals(pKey)) {
         this.refreshDimensions();
      }

   }

   public void refreshDimensions() {
      EntitySize entitysize = this.dimensions;
      Pose pose = this.getPose();
      EntitySize entitysize1 = this.getDimensions(pose);
      net.minecraftforge.event.entity.EntityEvent.Size sizeEvent = net.minecraftforge.event.ForgeEventFactory.getEntitySizeForge(this, pose, entitysize, entitysize1, this.getEyeHeight(pose, entitysize1));
      entitysize1 = sizeEvent.getNewSize();
      this.dimensions = entitysize1;
      this.eyeHeight = sizeEvent.getNewEyeHeight();
      if (entitysize1.width < entitysize.width) {
         double d0 = (double)entitysize1.width / 2.0D;
         this.setBoundingBox(new AxisAlignedBB(this.getX() - d0, this.getY(), this.getZ() - d0, this.getX() + d0, this.getY() + (double)entitysize1.height, this.getZ() + d0));
      } else {
         AxisAlignedBB axisalignedbb = this.getBoundingBox();
         this.setBoundingBox(new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.minX + (double)entitysize1.width, axisalignedbb.minY + (double)entitysize1.height, axisalignedbb.minZ + (double)entitysize1.width));
         if (entitysize1.width > entitysize.width && !this.firstTick && !this.level.isClientSide) {
            float f = entitysize.width - entitysize1.width;
            this.move(MoverType.SELF, new Vector3d((double)f, 0.0D, (double)f));
         }

      }
   }

   /**
    * Gets the horizontal facing direction of this Entity.
    */
   public Direction getDirection() {
      return Direction.fromYRot((double)this.yRot);
   }

   /**
    * Gets the horizontal facing direction of this Entity, adjusted to take specially-treated entity types into account.
    */
   public Direction getMotionDirection() {
      return this.getDirection();
   }

   protected HoverEvent createHoverEvent() {
      return new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new HoverEvent.EntityHover(this.getType(), this.getUUID(), this.getName()));
   }

   public boolean broadcastToPlayer(ServerPlayerEntity pPlayer) {
      return true;
   }

   public AxisAlignedBB getBoundingBox() {
      return this.bb;
   }

   /**
    * Gets the bounding box of this Entity, adjusted to take auxiliary entities into account (e.g. the tile contained by
    * a minecart, such as a command block).
    */
   @OnlyIn(Dist.CLIENT)
   public AxisAlignedBB getBoundingBoxForCulling() {
      return this.getBoundingBox();
   }

   protected AxisAlignedBB getBoundingBoxForPose(Pose pPose) {
      EntitySize entitysize = this.getDimensionsForge(pPose);
      float f = entitysize.width / 2.0F;
      Vector3d vector3d = new Vector3d(this.getX() - (double)f, this.getY(), this.getZ() - (double)f);
      Vector3d vector3d1 = new Vector3d(this.getX() + (double)f, this.getY() + (double)entitysize.height, this.getZ() + (double)f);
      return new AxisAlignedBB(vector3d, vector3d1);
   }

   public void setBoundingBox(AxisAlignedBB pBb) {
      this.bb = pBb;
   }

   /**
    * @deprecated Can be overridden but call {@link #getEyeHeightForge(Pose, EntitySize)} instead.
    */
   @Deprecated
   protected float getEyeHeight(Pose pPose, EntitySize pSize) {
      return pSize.height * 0.85F;
   }

   @OnlyIn(Dist.CLIENT)
   public float getEyeHeight(Pose pPose) {
      return this.getEyeHeightForge(pPose, this.getDimensionsForge(pPose));
   }

   public final float getEyeHeight() {
      return this.eyeHeight;
   }

   @OnlyIn(Dist.CLIENT)
   public Vector3d getLeashOffset() {
      return new Vector3d(0.0D, (double)this.getEyeHeight(), (double)(this.getBbWidth() * 0.4F));
   }

   public boolean setSlot(int pSlotIndex, ItemStack pStack) {
      return false;
   }

   /**
    * Send a chat message to the CommandSender
    */
   public void sendMessage(ITextComponent pComponent, UUID pSenderUUID) {
   }

   /**
    * Get the world, if available. <b>{@code null} is not allowed!</b> If you are not an entity in the world, return the
    * overworld
    */
   public World getCommandSenderWorld() {
      return this.level;
   }

   /**
    * Get the Minecraft server instance
    */
   @Nullable
   public MinecraftServer getServer() {
      return this.level.getServer();
   }

   /**
    * Applies the given player interaction to this Entity.
    */
   public ActionResultType interactAt(PlayerEntity pPlayer, Vector3d pVec, Hand pHand) {
      return ActionResultType.PASS;
   }

   public boolean ignoreExplosion() {
      return false;
   }

   public void doEnchantDamageEffects(LivingEntity pLivingEntity, Entity pEntity) {
      if (pEntity instanceof LivingEntity) {
         EnchantmentHelper.doPostHurtEffects((LivingEntity)pEntity, pLivingEntity);
      }

      EnchantmentHelper.doPostDamageEffects(pLivingEntity, pEntity);
   }

   /**
    * Add the given player to the list of players tracking this entity. For instance, a player may track a boss in order
    * to view its associated boss bar.
    */
   public void startSeenByPlayer(ServerPlayerEntity pServerPlayer) {
   }

   /**
    * Removes the given player from the list of players tracking this entity. See {@link Entity#addTrackingPlayer} for
    * more information on tracking.
    */
   public void stopSeenByPlayer(ServerPlayerEntity pServerPlayer) {
   }

   /**
    * Transforms the entity's current yaw with the given Rotation and returns it. This does not have a side-effect.
    */
   public float rotate(Rotation pTransformRotation) {
      float f = MathHelper.wrapDegrees(this.yRot);
      switch(pTransformRotation) {
      case CLOCKWISE_180:
         return f + 180.0F;
      case COUNTERCLOCKWISE_90:
         return f + 270.0F;
      case CLOCKWISE_90:
         return f + 90.0F;
      default:
         return f;
      }
   }

   /**
    * Transforms the entity's current yaw with the given Mirror and returns it. This does not have a side-effect.
    */
   public float mirror(Mirror pTransformMirror) {
      float f = MathHelper.wrapDegrees(this.yRot);
      switch(pTransformMirror) {
      case LEFT_RIGHT:
         return -f;
      case FRONT_BACK:
         return 180.0F - f;
      default:
         return f;
      }
   }

   /**
    * Checks if players can use this entity to access operator (permission level 2) commands either directly or
    * indirectly, such as give or setblock. A similar method exists for entities at {@link
    * net.minecraft.tileentity.TileEntity#onlyOpsCanSetNbt()}.<p>For example, {@link
    * net.minecraft.entity.item.EntityMinecartCommandBlock#ignoreItemEntityData() command block minecarts} and {@link
    * net.minecraft.entity.item.EntityMinecartMobSpawner#ignoreItemEntityData() mob spawner minecarts} (spawning command
    * block minecarts or drops) are considered accessible.</p>@return true if this entity offers ways for unauthorized
    * players to use restricted commands
    */
   public boolean onlyOpCanSetNbt() {
      return false;
   }

   public boolean checkAndResetForcedChunkAdditionFlag() {
      boolean flag = this.forceChunkAddition;
      this.forceChunkAddition = false;
      return flag;
   }

   public boolean checkAndResetUpdateChunkPos() {
      boolean flag = this.movedSinceLastChunkCheck;
      this.movedSinceLastChunkCheck = false;
      return flag;
   }

   /**
    * For vehicles, the first passenger is generally considered the controller and "drives" the vehicle. For example,
    * Pigs, Horses, and Boats are generally "steered" by the controlling passenger.
    */
   @Nullable
   public Entity getControllingPassenger() {
      return null;
   }

   public List<Entity> getPassengers() {
      return (List<Entity>)(this.passengers.isEmpty() ? Collections.emptyList() : Lists.newArrayList(this.passengers));
   }

   public boolean hasPassenger(Entity pEntity) {
      for(Entity entity : this.getPassengers()) {
         if (entity.equals(pEntity)) {
            return true;
         }
      }

      return false;
   }

   public boolean hasPassenger(Class<? extends Entity> pPassengerClass) {
      for(Entity entity : this.getPassengers()) {
         if (pPassengerClass.isAssignableFrom(entity.getClass())) {
            return true;
         }
      }

      return false;
   }

   public Collection<Entity> getIndirectPassengers() {
      Set<Entity> set = Sets.newHashSet();

      for(Entity entity : this.getPassengers()) {
         set.add(entity);
         entity.fillIndirectPassengers(false, set);
      }

      return set;
   }

   public Stream<Entity> getSelfAndPassengers() {
      return Stream.concat(Stream.of(this), this.passengers.stream().flatMap(Entity::getSelfAndPassengers));
   }

   public boolean hasOnePlayerPassenger() {
      Set<Entity> set = Sets.newHashSet();
      this.fillIndirectPassengers(true, set);
      return set.size() == 1;
   }

   private void fillIndirectPassengers(boolean pOnlyPlayers, Set<Entity> pFillTo) {
      for(Entity entity : this.getPassengers()) {
         if (!pOnlyPlayers || ServerPlayerEntity.class.isAssignableFrom(entity.getClass())) {
            pFillTo.add(entity);
         }

         entity.fillIndirectPassengers(pOnlyPlayers, pFillTo);
      }

   }

   public Entity getRootVehicle() {
      Entity entity;
      for(entity = this; entity.isPassenger(); entity = entity.getVehicle()) {
      }

      return entity;
   }

   public boolean isPassengerOfSameVehicle(Entity pEntity) {
      return this.getRootVehicle() == pEntity.getRootVehicle();
   }

   @OnlyIn(Dist.CLIENT)
   public boolean hasIndirectPassenger(Entity pEntity) {
      for(Entity entity : this.getPassengers()) {
         if (entity.equals(pEntity)) {
            return true;
         }

         if (entity.hasIndirectPassenger(pEntity)) {
            return true;
         }
      }

      return false;
   }

   public boolean isControlledByLocalInstance() {
      Entity entity = this.getControllingPassenger();
      if (entity instanceof PlayerEntity) {
         return ((PlayerEntity)entity).isLocalPlayer();
      } else {
         return !this.level.isClientSide;
      }
   }

   protected static Vector3d getCollisionHorizontalEscapeVector(double pVehicleWidth, double pPassengerWidth, float pYRot) {
      double d0 = (pVehicleWidth + pPassengerWidth + (double)1.0E-5F) / 2.0D;
      float f = -MathHelper.sin(pYRot * ((float)Math.PI / 180F));
      float f1 = MathHelper.cos(pYRot * ((float)Math.PI / 180F));
      float f2 = Math.max(Math.abs(f), Math.abs(f1));
      return new Vector3d((double)f * d0 / (double)f2, 0.0D, (double)f1 * d0 / (double)f2);
   }

   public Vector3d getDismountLocationForPassenger(LivingEntity pLivingEntity) {
      return new Vector3d(this.getX(), this.getBoundingBox().maxY, this.getZ());
   }

   /**
    * Get entity this is riding
    */
   @Nullable
   public Entity getVehicle() {
      return this.vehicle;
   }

   public PushReaction getPistonPushReaction() {
      return PushReaction.NORMAL;
   }

   public SoundCategory getSoundSource() {
      return SoundCategory.NEUTRAL;
   }

   protected int getFireImmuneTicks() {
      return 1;
   }

   public CommandSource createCommandSourceStack() {
      return new CommandSource(this, this.position(), this.getRotationVector(), this.level instanceof ServerWorld ? (ServerWorld)this.level : null, this.getPermissionLevel(), this.getName().getString(), this.getDisplayName(), this.level.getServer(), this);
   }

   protected int getPermissionLevel() {
      return 0;
   }

   public boolean hasPermissions(int pLevel) {
      return this.getPermissionLevel() >= pLevel;
   }

   public boolean acceptsSuccess() {
      return this.level.getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK);
   }

   public boolean acceptsFailure() {
      return true;
   }

   public boolean shouldInformAdmins() {
      return true;
   }

   public void lookAt(EntityAnchorArgument.Type pAnchor, Vector3d pTarget) {
      Vector3d vector3d = pAnchor.apply(this);
      double d0 = pTarget.x - vector3d.x;
      double d1 = pTarget.y - vector3d.y;
      double d2 = pTarget.z - vector3d.z;
      double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
      this.xRot = MathHelper.wrapDegrees((float)(-(MathHelper.atan2(d1, d3) * (double)(180F / (float)Math.PI))));
      this.yRot = MathHelper.wrapDegrees((float)(MathHelper.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F);
      this.setYHeadRot(this.yRot);
      this.xRotO = this.xRot;
      this.yRotO = this.yRot;
   }

   public boolean updateFluidHeightAndDoFluidPushing(ITag<Fluid> pFluidTag, double pMotionScale) {
      AxisAlignedBB axisalignedbb = this.getBoundingBox().deflate(0.001D);
      int i = MathHelper.floor(axisalignedbb.minX);
      int j = MathHelper.ceil(axisalignedbb.maxX);
      int k = MathHelper.floor(axisalignedbb.minY);
      int l = MathHelper.ceil(axisalignedbb.maxY);
      int i1 = MathHelper.floor(axisalignedbb.minZ);
      int j1 = MathHelper.ceil(axisalignedbb.maxZ);
      if (!this.level.hasChunksAt(i, k, i1, j, l, j1)) {
         return false;
      } else {
         double d0 = 0.0D;
         boolean flag = this.isPushedByFluid();
         boolean flag1 = false;
         Vector3d vector3d = Vector3d.ZERO;
         int k1 = 0;
         BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

         for(int l1 = i; l1 < j; ++l1) {
            for(int i2 = k; i2 < l; ++i2) {
               for(int j2 = i1; j2 < j1; ++j2) {
                  blockpos$mutable.set(l1, i2, j2);
                  FluidState fluidstate = this.level.getFluidState(blockpos$mutable);
                  if (fluidstate.is(pFluidTag)) {
                     double d1 = (double)((float)i2 + fluidstate.getHeight(this.level, blockpos$mutable));
                     if (d1 >= axisalignedbb.minY) {
                        flag1 = true;
                        d0 = Math.max(d1 - axisalignedbb.minY, d0);
                        if (flag) {
                           Vector3d vector3d1 = fluidstate.getFlow(this.level, blockpos$mutable);
                           if (d0 < 0.4D) {
                              vector3d1 = vector3d1.scale(d0);
                           }

                           vector3d = vector3d.add(vector3d1);
                           ++k1;
                        }
                     }
                  }
               }
            }
         }

         if (vector3d.length() > 0.0D) {
            if (k1 > 0) {
               vector3d = vector3d.scale(1.0D / (double)k1);
            }

            if (!(this instanceof PlayerEntity)) {
               vector3d = vector3d.normalize();
            }

            Vector3d vector3d2 = this.getDeltaMovement();
            vector3d = vector3d.scale(pMotionScale * 1.0D);
            double d2 = 0.003D;
            if (Math.abs(vector3d2.x) < 0.003D && Math.abs(vector3d2.z) < 0.003D && vector3d.length() < 0.0045000000000000005D) {
               vector3d = vector3d.normalize().scale(0.0045000000000000005D);
            }

            this.setDeltaMovement(this.getDeltaMovement().add(vector3d));
         }

         this.fluidHeight.put(pFluidTag, d0);
         return flag1;
      }
   }

   public double getFluidHeight(ITag<Fluid> pFluidTag) {
      return this.fluidHeight.getDouble(pFluidTag);
   }

   public double getFluidJumpThreshold() {
      return (double)this.getEyeHeight() < 0.4D ? 0.0D : 0.4D;
   }

   public final float getBbWidth() {
      return this.dimensions.width;
   }

   public final float getBbHeight() {
      return this.dimensions.height;
   }

   public abstract IPacket<?> getAddEntityPacket();

   /**
    * @deprecated Can be overridden but call {@link #getDimensionsForge(Pose)} instead.
    */
   @Deprecated
   public EntitySize getDimensions(Pose pPose) {
      return this.type.getDimensions();
   }

   public Vector3d position() {
      return this.position;
   }

   public BlockPos blockPosition() {
      return this.blockPosition;
   }

   public Vector3d getDeltaMovement() {
      return this.deltaMovement;
   }

   public void setDeltaMovement(Vector3d pMotion) {
      this.deltaMovement = pMotion;
   }

   public void setDeltaMovement(double pX, double pY, double pZ) {
      this.setDeltaMovement(new Vector3d(pX, pY, pZ));
   }

   public final double getX() {
      return this.position.x;
   }

   public double getX(double pScale) {
      return this.position.x + (double)this.getBbWidth() * pScale;
   }

   public double getRandomX(double pScale) {
      return this.getX((2.0D * this.random.nextDouble() - 1.0D) * pScale);
   }

   public final double getY() {
      return this.position.y;
   }

   public double getY(double pScale) {
      return this.position.y + (double)this.getBbHeight() * pScale;
   }

   public double getRandomY() {
      return this.getY(this.random.nextDouble());
   }

   public double getEyeY() {
      return this.position.y + (double)this.eyeHeight;
   }

   public final double getZ() {
      return this.position.z;
   }

   public double getZ(double pScale) {
      return this.position.z + (double)this.getBbWidth() * pScale;
   }

   public double getRandomZ(double pScale) {
      return this.getZ((2.0D * this.random.nextDouble() - 1.0D) * pScale);
   }

   /**
    * Directly updates the {@link #posX}, {@link posY}, and {@link posZ} fields, without performing any collision
    * checks, updating the bounding box position, or sending any packets. In general, this is not what you want and
    * {@link #setPosition} is better, as that handles the bounding box.
    */
   public void setPosRaw(double pX, double pY, double pZ) {
      if (this.position.x != pX || this.position.y != pY || this.position.z != pZ) {
         this.position = new Vector3d(pX, pY, pZ);
         int i = MathHelper.floor(pX);
         int j = MathHelper.floor(pY);
         int k = MathHelper.floor(pZ);
         if (i != this.blockPosition.getX() || j != this.blockPosition.getY() || k != this.blockPosition.getZ()) {
            this.blockPosition = new BlockPos(i, j, k);
         }

         this.movedSinceLastChunkCheck = true;
      }
      if (this.isAddedToWorld() && !this.level.isClientSide && !this.removed) this.level.getChunk((int) Math.floor(pX) >> 4, (int) Math.floor(pZ) >> 4); // Forge - ensure target chunk is loaded.

   }

   /**
    * Makes the entity despawn if requirements are reached
    */
   public void checkDespawn() {
   }

   @OnlyIn(Dist.CLIENT)
   public Vector3d getRopeHoldPosition(float pPartialTicks) {
      return this.getPosition(pPartialTicks).add(0.0D, (double)this.eyeHeight * 0.7D, 0.0D);
   }

   @FunctionalInterface
   public interface IMoveCallback {
      void accept(Entity p_accept_1_, double p_accept_2_, double p_accept_4_, double p_accept_6_);
   }

   /* ================================== Forge Start =====================================*/

   private boolean canUpdate = true;
   @Override
   public void canUpdate(boolean value) {
      this.canUpdate = value;
   }
   @Override
   public boolean canUpdate() {
      return this.canUpdate;
   }
   private Collection<ItemEntity> captureDrops = null;
   @Override
   public Collection<ItemEntity> captureDrops() {
      return captureDrops;
   }
   @Override
   public Collection<ItemEntity> captureDrops(Collection<ItemEntity> value) {
      Collection<ItemEntity> ret = captureDrops;
      this.captureDrops = value;
      return ret;
   }
   private CompoundNBT persistentData;
   @Override
   public CompoundNBT getPersistentData() {
      if (persistentData == null)
         persistentData = new CompoundNBT();
      return persistentData;
   }
   @Override
   public boolean canTrample(BlockState state, BlockPos pos, float fallDistance) {
      return level.random.nextFloat() < fallDistance - 0.5F
              && this instanceof LivingEntity
              && (this instanceof PlayerEntity || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(level, this))
              && this.getBbWidth() * this.getBbWidth() * this.getBbHeight() > 0.512F;
   }

   /**
    * Internal use for keeping track of entities that are tracked by a world, to
    * allow guarantees that entity position changes will force a chunk load, avoiding
    * potential issues with entity desyncing and bad chunk data.
    */
   private boolean isAddedToWorld;

   @Override
   public final boolean isAddedToWorld() { return this.isAddedToWorld; }

   @Override
   public void onAddedToWorld() { this.isAddedToWorld = true; }

   @Override
   public void onRemovedFromWorld() { this.isAddedToWorld = false; }

   @Override
   public void revive() {
      this.removed = false;
      this.reviveCaps();
   }

   // no AT because of overrides
   /**
    * Accessor method for {@link #getEyeHeight(Pose, EntitySize)}
    */
   public float getEyeHeightAccess(Pose pose, EntitySize size) {
      return this.getEyeHeight(pose, size);
   }
}
