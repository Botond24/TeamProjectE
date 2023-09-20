package net.minecraft.tileentity;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ISidedInventoryProvider;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.HopperContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class HopperTileEntity extends LockableLootTileEntity implements IHopper, ITickableTileEntity {
   private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
   private int cooldownTime = -1;
   private long tickedGameTime;

   public HopperTileEntity() {
      super(TileEntityType.HOPPER);
   }

   public void load(BlockState p_230337_1_, CompoundNBT p_230337_2_) {
      super.load(p_230337_1_, p_230337_2_);
      this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      if (!this.tryLoadLootTable(p_230337_2_)) {
         ItemStackHelper.loadAllItems(p_230337_2_, this.items);
      }

      this.cooldownTime = p_230337_2_.getInt("TransferCooldown");
   }

   public CompoundNBT save(CompoundNBT pCompound) {
      super.save(pCompound);
      if (!this.trySaveLootTable(pCompound)) {
         ItemStackHelper.saveAllItems(pCompound, this.items);
      }

      pCompound.putInt("TransferCooldown", this.cooldownTime);
      return pCompound;
   }

   /**
    * Returns the number of slots in the inventory.
    */
   public int getContainerSize() {
      return this.items.size();
   }

   /**
    * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
    */
   public ItemStack removeItem(int pIndex, int pCount) {
      this.unpackLootTable((PlayerEntity)null);
      return ItemStackHelper.removeItem(this.getItems(), pIndex, pCount);
   }

   /**
    * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
    */
   public void setItem(int pIndex, ItemStack pStack) {
      this.unpackLootTable((PlayerEntity)null);
      this.getItems().set(pIndex, pStack);
      if (pStack.getCount() > this.getMaxStackSize()) {
         pStack.setCount(this.getMaxStackSize());
      }

   }

   protected ITextComponent getDefaultName() {
      return new TranslationTextComponent("container.hopper");
   }

   public void tick() {
      if (this.level != null && !this.level.isClientSide) {
         --this.cooldownTime;
         this.tickedGameTime = this.level.getGameTime();
         if (!this.isOnCooldown()) {
            this.setCooldown(0);
            this.tryMoveItems(() -> {
               return suckInItems(this);
            });
         }

      }
   }

   private boolean tryMoveItems(Supplier<Boolean> p_200109_1_) {
      if (this.level != null && !this.level.isClientSide) {
         if (!this.isOnCooldown() && this.getBlockState().getValue(HopperBlock.ENABLED)) {
            boolean flag = false;
            if (!this.isEmpty()) {
               flag = this.ejectItems();
            }

            if (!this.inventoryFull()) {
               flag |= p_200109_1_.get();
            }

            if (flag) {
               this.setCooldown(8);
               this.setChanged();
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean inventoryFull() {
      for(ItemStack itemstack : this.items) {
         if (itemstack.isEmpty() || itemstack.getCount() != itemstack.getMaxStackSize()) {
            return false;
         }
      }

      return true;
   }

   private boolean ejectItems() {
      if (net.minecraftforge.items.VanillaInventoryCodeHooks.insertHook(this)) return true;
      IInventory iinventory = this.getAttachedContainer();
      if (iinventory == null) {
         return false;
      } else {
         Direction direction = this.getBlockState().getValue(HopperBlock.FACING).getOpposite();
         if (this.isFullContainer(iinventory, direction)) {
            return false;
         } else {
            for(int i = 0; i < this.getContainerSize(); ++i) {
               if (!this.getItem(i).isEmpty()) {
                  ItemStack itemstack = this.getItem(i).copy();
                  ItemStack itemstack1 = addItem(this, iinventory, this.removeItem(i, 1), direction);
                  if (itemstack1.isEmpty()) {
                     iinventory.setChanged();
                     return true;
                  }

                  this.setItem(i, itemstack);
               }
            }

            return false;
         }
      }
   }

   private static IntStream getSlots(IInventory p_213972_0_, Direction p_213972_1_) {
      return p_213972_0_ instanceof ISidedInventory ? IntStream.of(((ISidedInventory)p_213972_0_).getSlotsForFace(p_213972_1_)) : IntStream.range(0, p_213972_0_.getContainerSize());
   }

   /**
    * @return false if the container has any room to place items in
    */
   private boolean isFullContainer(IInventory pSide, Direction p_174919_2_) {
      return getSlots(pSide, p_174919_2_).allMatch((p_213970_1_) -> {
         ItemStack itemstack = pSide.getItem(p_213970_1_);
         return itemstack.getCount() >= itemstack.getMaxStackSize();
      });
   }

   /**
    * @return whether the given Container is empty from the given face
    */
   private static boolean isEmptyContainer(IInventory pContainer, Direction pDirection) {
      return getSlots(pContainer, pDirection).allMatch((p_213973_1_) -> {
         return pContainer.getItem(p_213973_1_).isEmpty();
      });
   }

   public static boolean suckInItems(IHopper p_145891_0_) {
      Boolean ret = net.minecraftforge.items.VanillaInventoryCodeHooks.extractHook(p_145891_0_);
      if (ret != null) return ret;
      IInventory iinventory = getSourceContainer(p_145891_0_);
      if (iinventory != null) {
         Direction direction = Direction.DOWN;
         return isEmptyContainer(iinventory, direction) ? false : getSlots(iinventory, direction).anyMatch((p_213971_3_) -> {
            return tryTakeInItemFromSlot(p_145891_0_, iinventory, p_213971_3_, direction);
         });
      } else {
         for(ItemEntity itementity : getItemsAtAndAbove(p_145891_0_)) {
            if (addItem(p_145891_0_, itementity)) {
               return true;
            }
         }

         return false;
      }
   }

   /**
    * Pulls from the specified slot in the container and places in any available slot in the hopper.
    * @return true if the entire stack was moved
    */
   private static boolean tryTakeInItemFromSlot(IHopper pHopper, IInventory pContainer, int pSlot, Direction pDirection) {
      ItemStack itemstack = pContainer.getItem(pSlot);
      if (!itemstack.isEmpty() && canTakeItemFromContainer(pContainer, itemstack, pSlot, pDirection)) {
         ItemStack itemstack1 = itemstack.copy();
         ItemStack itemstack2 = addItem(pContainer, pHopper, pContainer.removeItem(pSlot, 1), (Direction)null);
         if (itemstack2.isEmpty()) {
            pContainer.setChanged();
            return true;
         }

         pContainer.setItem(pSlot, itemstack1);
      }

      return false;
   }

   public static boolean addItem(IInventory p_200114_0_, ItemEntity p_200114_1_) {
      boolean flag = false;
      ItemStack itemstack = p_200114_1_.getItem().copy();
      ItemStack itemstack1 = addItem((IInventory)null, p_200114_0_, itemstack, (Direction)null);
      if (itemstack1.isEmpty()) {
         flag = true;
         p_200114_1_.remove();
      } else {
         p_200114_1_.setItem(itemstack1);
      }

      return flag;
   }

   /**
    * Attempts to place the passed stack in the container, using as many slots as required.
    * @return any leftover stack
    */
   public static ItemStack addItem(@Nullable IInventory pSource, IInventory pDestination, ItemStack pStack, @Nullable Direction pDirection) {
      if (pDestination instanceof ISidedInventory && pDirection != null) {
         ISidedInventory isidedinventory = (ISidedInventory)pDestination;
         int[] aint = isidedinventory.getSlotsForFace(pDirection);

         for(int k = 0; k < aint.length && !pStack.isEmpty(); ++k) {
            pStack = tryMoveInItem(pSource, pDestination, pStack, aint[k], pDirection);
         }
      } else {
         int i = pDestination.getContainerSize();

         for(int j = 0; j < i && !pStack.isEmpty(); ++j) {
            pStack = tryMoveInItem(pSource, pDestination, pStack, j, pDirection);
         }
      }

      return pStack;
   }

   /**
    * Can this hopper insert the specified item from the specified slot on the specified side?
    */
   private static boolean canPlaceItemInContainer(IInventory pContainer, ItemStack pStack, int pSlot, @Nullable Direction pDirection) {
      if (!pContainer.canPlaceItem(pSlot, pStack)) {
         return false;
      } else {
         return !(pContainer instanceof ISidedInventory) || ((ISidedInventory)pContainer).canPlaceItemThroughFace(pSlot, pStack, pDirection);
      }
   }

   /**
    * Can this hopper extract the specified item from the specified slot on the specified side?
    */
   private static boolean canTakeItemFromContainer(IInventory pContainer, ItemStack pStack, int pSlot, Direction pDirection) {
      return !(pContainer instanceof ISidedInventory) || ((ISidedInventory)pContainer).canTakeItemThroughFace(pSlot, pStack, pDirection);
   }

   /**
    * Insert the specified stack to the specified inventory and return any leftover items
    */
   private static ItemStack tryMoveInItem(@Nullable IInventory pSource, IInventory pDestination, ItemStack pStack, int pSlot, @Nullable Direction pDirection) {
      ItemStack itemstack = pDestination.getItem(pSlot);
      if (canPlaceItemInContainer(pDestination, pStack, pSlot, pDirection)) {
         boolean flag = false;
         boolean flag1 = pDestination.isEmpty();
         if (itemstack.isEmpty()) {
            pDestination.setItem(pSlot, pStack);
            pStack = ItemStack.EMPTY;
            flag = true;
         } else if (canMergeItems(itemstack, pStack)) {
            int i = pStack.getMaxStackSize() - itemstack.getCount();
            int j = Math.min(pStack.getCount(), i);
            pStack.shrink(j);
            itemstack.grow(j);
            flag = j > 0;
         }

         if (flag) {
            if (flag1 && pDestination instanceof HopperTileEntity) {
               HopperTileEntity hoppertileentity1 = (HopperTileEntity)pDestination;
               if (!hoppertileentity1.isOnCustomCooldown()) {
                  int k = 0;
                  if (pSource instanceof HopperTileEntity) {
                     HopperTileEntity hoppertileentity = (HopperTileEntity)pSource;
                     if (hoppertileentity1.tickedGameTime >= hoppertileentity.tickedGameTime) {
                        k = 1;
                     }
                  }

                  hoppertileentity1.setCooldown(8 - k);
               }
            }

            pDestination.setChanged();
         }
      }

      return pStack;
   }

   @Nullable
   private IInventory getAttachedContainer() {
      Direction direction = this.getBlockState().getValue(HopperBlock.FACING);
      return getContainerAt(this.getLevel(), this.worldPosition.relative(direction));
   }

   @Nullable
   public static IInventory getSourceContainer(IHopper p_145884_0_) {
      return getContainerAt(p_145884_0_.getLevel(), p_145884_0_.getLevelX(), p_145884_0_.getLevelY() + 1.0D, p_145884_0_.getLevelZ());
   }

   public static List<ItemEntity> getItemsAtAndAbove(IHopper p_200115_0_) {
      return p_200115_0_.getSuckShape().toAabbs().stream().flatMap((p_200110_1_) -> {
         return p_200115_0_.getLevel().getEntitiesOfClass(ItemEntity.class, p_200110_1_.move(p_200115_0_.getLevelX() - 0.5D, p_200115_0_.getLevelY() - 0.5D, p_200115_0_.getLevelZ() - 0.5D), EntityPredicates.ENTITY_STILL_ALIVE).stream();
      }).collect(Collectors.toList());
   }

   @Nullable
   public static IInventory getContainerAt(World p_195484_0_, BlockPos p_195484_1_) {
      return getContainerAt(p_195484_0_, (double)p_195484_1_.getX() + 0.5D, (double)p_195484_1_.getY() + 0.5D, (double)p_195484_1_.getZ() + 0.5D);
   }

   /**
    * @return the container for the given position or null if none was found
    */
   @Nullable
   public static IInventory getContainerAt(World pLevel, double pX, double pY, double pZ) {
      IInventory iinventory = null;
      BlockPos blockpos = new BlockPos(pX, pY, pZ);
      BlockState blockstate = pLevel.getBlockState(blockpos);
      Block block = blockstate.getBlock();
      if (block instanceof ISidedInventoryProvider) {
         iinventory = ((ISidedInventoryProvider)block).getContainer(blockstate, pLevel, blockpos);
      } else if (blockstate.hasTileEntity()) {
         TileEntity tileentity = pLevel.getBlockEntity(blockpos);
         if (tileentity instanceof IInventory) {
            iinventory = (IInventory)tileentity;
            if (iinventory instanceof ChestTileEntity && block instanceof ChestBlock) {
               iinventory = ChestBlock.getContainer((ChestBlock)block, blockstate, pLevel, blockpos, true);
            }
         }
      }

      if (iinventory == null) {
         List<Entity> list = pLevel.getEntities((Entity)null, new AxisAlignedBB(pX - 0.5D, pY - 0.5D, pZ - 0.5D, pX + 0.5D, pY + 0.5D, pZ + 0.5D), EntityPredicates.CONTAINER_ENTITY_SELECTOR);
         if (!list.isEmpty()) {
            iinventory = (IInventory)list.get(pLevel.random.nextInt(list.size()));
         }
      }

      return iinventory;
   }

   private static boolean canMergeItems(ItemStack pStack1, ItemStack pStack2) {
      if (pStack1.getItem() != pStack2.getItem()) {
         return false;
      } else if (pStack1.getDamageValue() != pStack2.getDamageValue()) {
         return false;
      } else if (pStack1.getCount() > pStack1.getMaxStackSize()) {
         return false;
      } else {
         return ItemStack.tagMatches(pStack1, pStack2);
      }
   }

   /**
    * Gets the world X position for this hopper entity.
    */
   public double getLevelX() {
      return (double)this.worldPosition.getX() + 0.5D;
   }

   /**
    * Gets the world Y position for this hopper entity.
    */
   public double getLevelY() {
      return (double)this.worldPosition.getY() + 0.5D;
   }

   /**
    * Gets the world Z position for this hopper entity.
    */
   public double getLevelZ() {
      return (double)this.worldPosition.getZ() + 0.5D;
   }

   public void setCooldown(int pCooldownTime) {
      this.cooldownTime = pCooldownTime;
   }

   private boolean isOnCooldown() {
      return this.cooldownTime > 0;
   }

   public boolean isOnCustomCooldown() {
      return this.cooldownTime > 8;
   }

   protected NonNullList<ItemStack> getItems() {
      return this.items;
   }

   protected void setItems(NonNullList<ItemStack> pItems) {
      this.items = pItems;
   }

   public void entityInside(Entity p_200113_1_) {
      if (p_200113_1_ instanceof ItemEntity) {
         BlockPos blockpos = this.getBlockPos();
         if (VoxelShapes.joinIsNotEmpty(VoxelShapes.create(p_200113_1_.getBoundingBox().move((double)(-blockpos.getX()), (double)(-blockpos.getY()), (double)(-blockpos.getZ()))), this.getSuckShape(), IBooleanFunction.AND)) {
            this.tryMoveItems(() -> {
               return addItem(this, (ItemEntity)p_200113_1_);
            });
         }
      }

   }

   protected Container createMenu(int pId, PlayerInventory pPlayer) {
      return new HopperContainer(pId, pPlayer, this);
   }

   @Override
   protected net.minecraftforge.items.IItemHandler createUnSidedHandler() {
      return new net.minecraftforge.items.VanillaHopperItemHandler(this);
   }

   public long getLastUpdateTime() {
      return this.tickedGameTime;
   }
}
