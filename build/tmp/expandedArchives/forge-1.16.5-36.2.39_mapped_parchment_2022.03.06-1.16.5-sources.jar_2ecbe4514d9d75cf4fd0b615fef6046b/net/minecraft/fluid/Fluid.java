package net.minecraft.fluid;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.particles.IParticleData;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.ITag;
import net.minecraft.util.Direction;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class Fluid extends net.minecraftforge.registries.ForgeRegistryEntry<Fluid> implements net.minecraftforge.common.extensions.IForgeFluid {
   public static final ObjectIntIdentityMap<FluidState> FLUID_STATE_REGISTRY = new ObjectIntIdentityMap<>();
   protected final StateContainer<Fluid, FluidState> stateDefinition;
   private FluidState defaultFluidState;

   protected Fluid() {
      StateContainer.Builder<Fluid, FluidState> builder = new StateContainer.Builder<>(this);
      this.createFluidStateDefinition(builder);
      this.stateDefinition = builder.create(Fluid::defaultFluidState, FluidState::new);
      this.registerDefaultState(this.stateDefinition.any());
   }

   protected void createFluidStateDefinition(StateContainer.Builder<Fluid, FluidState> pBuilder) {
   }

   public StateContainer<Fluid, FluidState> getStateDefinition() {
      return this.stateDefinition;
   }

   protected final void registerDefaultState(FluidState pState) {
      this.defaultFluidState = pState;
   }

   public final FluidState defaultFluidState() {
      return this.defaultFluidState;
   }

   public abstract Item getBucket();

   @OnlyIn(Dist.CLIENT)
   protected void animateTick(World pLevel, BlockPos pPos, FluidState pState, Random pRandom) {
   }

   protected void tick(World pLevel, BlockPos pPos, FluidState pState) {
   }

   protected void randomTick(World pLevel, BlockPos pPos, FluidState pState, Random pRandom) {
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   protected IParticleData getDripParticle() {
      return null;
   }

   protected abstract boolean canBeReplacedWith(FluidState pFluidState, IBlockReader pBlockReader, BlockPos pPos, Fluid pFluid, Direction pDirection);

   protected abstract Vector3d getFlow(IBlockReader pBlockReader, BlockPos pPos, FluidState pFluidState);

   public abstract int getTickDelay(IWorldReader p_205569_1_);

   protected boolean isRandomlyTicking() {
      return false;
   }

   protected boolean isEmpty() {
      return false;
   }

   protected abstract float getExplosionResistance();

   public abstract float getHeight(FluidState p_215662_1_, IBlockReader p_215662_2_, BlockPos p_215662_3_);

   public abstract float getOwnHeight(FluidState p_223407_1_);

   protected abstract BlockState createLegacyBlock(FluidState pState);

   public abstract boolean isSource(FluidState pState);

   public abstract int getAmount(FluidState pState);

   public boolean isSame(Fluid pFluid) {
      return pFluid == this;
   }

   public boolean is(ITag<Fluid> pTag) {
      return pTag.contains(this);
   }

   public abstract VoxelShape getShape(FluidState p_215664_1_, IBlockReader p_215664_2_, BlockPos p_215664_3_);

   private final net.minecraftforge.common.util.ReverseTagWrapper<Fluid> reverseTags = new net.minecraftforge.common.util.ReverseTagWrapper<>(this, net.minecraft.tags.FluidTags::getAllTags);
   @Override
   public java.util.Set<net.minecraft.util.ResourceLocation> getTags() {
      return reverseTags.getTagNames();
   }

   /**
    * Creates the fluid attributes object, which will contain all the extended values for the fluid that aren't part of the vanilla system.
    * Do not call this from outside. To retrieve the values use {@link Fluid#getAttributes()}
    */
   protected net.minecraftforge.fluids.FluidAttributes createAttributes()
   {
      return net.minecraftforge.common.ForgeHooks.createVanillaFluidAttributes(this);
   }

   private net.minecraftforge.fluids.FluidAttributes forgeFluidAttributes;
   public final net.minecraftforge.fluids.FluidAttributes getAttributes() {
      if (forgeFluidAttributes == null)
         forgeFluidAttributes = createAttributes();
      return forgeFluidAttributes;
   }
}
