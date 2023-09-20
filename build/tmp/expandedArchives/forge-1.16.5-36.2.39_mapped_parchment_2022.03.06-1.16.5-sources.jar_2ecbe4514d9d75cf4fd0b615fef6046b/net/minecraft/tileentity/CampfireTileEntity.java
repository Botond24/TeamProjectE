package net.minecraft.tileentity;

import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.inventory.IClearable;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CampfireCookingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class CampfireTileEntity extends TileEntity implements IClearable, ITickableTileEntity {
   private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
   private final int[] cookingProgress = new int[4];
   private final int[] cookingTime = new int[4];

   public CampfireTileEntity() {
      super(TileEntityType.CAMPFIRE);
   }

   public void tick() {
      boolean flag = this.getBlockState().getValue(CampfireBlock.LIT);
      boolean flag1 = this.level.isClientSide;
      if (flag1) {
         if (flag) {
            this.makeParticles();
         }

      } else {
         if (flag) {
            this.cook();
         } else {
            for(int i = 0; i < this.items.size(); ++i) {
               if (this.cookingProgress[i] > 0) {
                  this.cookingProgress[i] = MathHelper.clamp(this.cookingProgress[i] - 2, 0, this.cookingTime[i]);
               }
            }
         }

      }
   }

   private void cook() {
      for(int i = 0; i < this.items.size(); ++i) {
         ItemStack itemstack = this.items.get(i);
         if (!itemstack.isEmpty()) {
            int j = this.cookingProgress[i]++;
            if (this.cookingProgress[i] >= this.cookingTime[i]) {
               IInventory iinventory = new Inventory(itemstack);
               ItemStack itemstack1 = this.level.getRecipeManager().getRecipeFor(IRecipeType.CAMPFIRE_COOKING, iinventory, this.level).map((p_213979_1_) -> {
                  return p_213979_1_.assemble(iinventory);
               }).orElse(itemstack);
               BlockPos blockpos = this.getBlockPos();
               InventoryHelper.dropItemStack(this.level, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), itemstack1);
               this.items.set(i, ItemStack.EMPTY);
               this.markUpdated();
            }
         }
      }

   }

   private void makeParticles() {
      World world = this.getLevel();
      if (world != null) {
         BlockPos blockpos = this.getBlockPos();
         Random random = world.random;
         if (random.nextFloat() < 0.11F) {
            for(int i = 0; i < random.nextInt(2) + 2; ++i) {
               CampfireBlock.makeParticles(world, blockpos, this.getBlockState().getValue(CampfireBlock.SIGNAL_FIRE), false);
            }
         }

         int l = this.getBlockState().getValue(CampfireBlock.FACING).get2DDataValue();

         for(int j = 0; j < this.items.size(); ++j) {
            if (!this.items.get(j).isEmpty() && random.nextFloat() < 0.2F) {
               Direction direction = Direction.from2DDataValue(Math.floorMod(j + l, 4));
               float f = 0.3125F;
               double d0 = (double)blockpos.getX() + 0.5D - (double)((float)direction.getStepX() * 0.3125F) + (double)((float)direction.getClockWise().getStepX() * 0.3125F);
               double d1 = (double)blockpos.getY() + 0.5D;
               double d2 = (double)blockpos.getZ() + 0.5D - (double)((float)direction.getStepZ() * 0.3125F) + (double)((float)direction.getClockWise().getStepZ() * 0.3125F);

               for(int k = 0; k < 4; ++k) {
                  world.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 5.0E-4D, 0.0D);
               }
            }
         }

      }
   }

   /**
    * @return the items currently held in this campfire
    */
   public NonNullList<ItemStack> getItems() {
      return this.items;
   }

   public void load(BlockState p_230337_1_, CompoundNBT p_230337_2_) {
      super.load(p_230337_1_, p_230337_2_);
      this.items.clear();
      ItemStackHelper.loadAllItems(p_230337_2_, this.items);
      if (p_230337_2_.contains("CookingTimes", 11)) {
         int[] aint = p_230337_2_.getIntArray("CookingTimes");
         System.arraycopy(aint, 0, this.cookingProgress, 0, Math.min(this.cookingTime.length, aint.length));
      }

      if (p_230337_2_.contains("CookingTotalTimes", 11)) {
         int[] aint1 = p_230337_2_.getIntArray("CookingTotalTimes");
         System.arraycopy(aint1, 0, this.cookingTime, 0, Math.min(this.cookingTime.length, aint1.length));
      }

   }

   public CompoundNBT save(CompoundNBT pCompound) {
      this.saveMetadataAndItems(pCompound);
      pCompound.putIntArray("CookingTimes", this.cookingProgress);
      pCompound.putIntArray("CookingTotalTimes", this.cookingTime);
      return pCompound;
   }

   private CompoundNBT saveMetadataAndItems(CompoundNBT pCompound) {
      super.save(pCompound);
      ItemStackHelper.saveAllItems(pCompound, this.items, true);
      return pCompound;
   }

   /**
    * Retrieves packet to send to the client whenever this Tile Entity is resynced via World.notifyBlockUpdate. For
    * modded TE's, this packet comes back to you clientside in {@link #onDataPacket}
    */
   @Nullable
   public SUpdateTileEntityPacket getUpdatePacket() {
      return new SUpdateTileEntityPacket(this.worldPosition, 13, this.getUpdateTag());
   }

   /**
    * Get an NBT compound to sync to the client with SPacketChunkData, used for initial loading of the chunk or when
    * many blocks change at once. This compound comes back to you clientside in {@link handleUpdateTag}
    */
   public CompoundNBT getUpdateTag() {
      return this.saveMetadataAndItems(new CompoundNBT());
   }

   public Optional<CampfireCookingRecipe> getCookableRecipe(ItemStack pStack) {
      return this.items.stream().noneMatch(ItemStack::isEmpty) ? Optional.empty() : this.level.getRecipeManager().getRecipeFor(IRecipeType.CAMPFIRE_COOKING, new Inventory(pStack), this.level);
   }

   public boolean placeFood(ItemStack pStack, int pCookTime) {
      for(int i = 0; i < this.items.size(); ++i) {
         ItemStack itemstack = this.items.get(i);
         if (itemstack.isEmpty()) {
            this.cookingTime[i] = pCookTime;
            this.cookingProgress[i] = 0;
            this.items.set(i, pStack.split(1));
            this.markUpdated();
            return true;
         }
      }

      return false;
   }

   private void markUpdated() {
      this.setChanged();
      this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
   }

   public void clearContent() {
      this.items.clear();
   }

   public void dowse() {
      if (this.level != null) {
         if (!this.level.isClientSide) {
            InventoryHelper.dropContents(this.level, this.getBlockPos(), this.getItems());
         }

         this.markUpdated();
      }

   }
}