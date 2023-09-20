package net.minecraft.tileentity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IRecipeHelperPopulator;
import net.minecraft.inventory.IRecipeHolder;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public abstract class AbstractFurnaceTileEntity extends LockableTileEntity implements ISidedInventory, IRecipeHolder, IRecipeHelperPopulator, ITickableTileEntity {
   private static final int[] SLOTS_FOR_UP = new int[]{0};
   private static final int[] SLOTS_FOR_DOWN = new int[]{2, 1};
   private static final int[] SLOTS_FOR_SIDES = new int[]{1};
   protected NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
   private int litTime;
   private int litDuration;
   private int cookingProgress;
   private int cookingTotalTime;
   protected final IIntArray dataAccess = new IIntArray() {
      public int get(int pIndex) {
         switch(pIndex) {
         case 0:
            return AbstractFurnaceTileEntity.this.litTime;
         case 1:
            return AbstractFurnaceTileEntity.this.litDuration;
         case 2:
            return AbstractFurnaceTileEntity.this.cookingProgress;
         case 3:
            return AbstractFurnaceTileEntity.this.cookingTotalTime;
         default:
            return 0;
         }
      }

      public void set(int pIndex, int pValue) {
         switch(pIndex) {
         case 0:
            AbstractFurnaceTileEntity.this.litTime = pValue;
            break;
         case 1:
            AbstractFurnaceTileEntity.this.litDuration = pValue;
            break;
         case 2:
            AbstractFurnaceTileEntity.this.cookingProgress = pValue;
            break;
         case 3:
            AbstractFurnaceTileEntity.this.cookingTotalTime = pValue;
         }

      }

      public int getCount() {
         return 4;
      }
   };
   private final Object2IntOpenHashMap<ResourceLocation> recipesUsed = new Object2IntOpenHashMap<>();
   protected final IRecipeType<? extends AbstractCookingRecipe> recipeType;

   protected AbstractFurnaceTileEntity(TileEntityType<?> p_i49964_1_, IRecipeType<? extends AbstractCookingRecipe> p_i49964_2_) {
      super(p_i49964_1_);
      this.recipeType = p_i49964_2_;
   }

   @Deprecated //Forge - get burn times by calling ForgeHooks#getBurnTime(ItemStack)
   public static Map<Item, Integer> getFuel() {
      Map<Item, Integer> map = Maps.newLinkedHashMap();
      add(map, Items.LAVA_BUCKET, 20000);
      add(map, Blocks.COAL_BLOCK, 16000);
      add(map, Items.BLAZE_ROD, 2400);
      add(map, Items.COAL, 1600);
      add(map, Items.CHARCOAL, 1600);
      add(map, ItemTags.LOGS, 300);
      add(map, ItemTags.PLANKS, 300);
      add(map, ItemTags.WOODEN_STAIRS, 300);
      add(map, ItemTags.WOODEN_SLABS, 150);
      add(map, ItemTags.WOODEN_TRAPDOORS, 300);
      add(map, ItemTags.WOODEN_PRESSURE_PLATES, 300);
      add(map, Blocks.OAK_FENCE, 300);
      add(map, Blocks.BIRCH_FENCE, 300);
      add(map, Blocks.SPRUCE_FENCE, 300);
      add(map, Blocks.JUNGLE_FENCE, 300);
      add(map, Blocks.DARK_OAK_FENCE, 300);
      add(map, Blocks.ACACIA_FENCE, 300);
      add(map, Blocks.OAK_FENCE_GATE, 300);
      add(map, Blocks.BIRCH_FENCE_GATE, 300);
      add(map, Blocks.SPRUCE_FENCE_GATE, 300);
      add(map, Blocks.JUNGLE_FENCE_GATE, 300);
      add(map, Blocks.DARK_OAK_FENCE_GATE, 300);
      add(map, Blocks.ACACIA_FENCE_GATE, 300);
      add(map, Blocks.NOTE_BLOCK, 300);
      add(map, Blocks.BOOKSHELF, 300);
      add(map, Blocks.LECTERN, 300);
      add(map, Blocks.JUKEBOX, 300);
      add(map, Blocks.CHEST, 300);
      add(map, Blocks.TRAPPED_CHEST, 300);
      add(map, Blocks.CRAFTING_TABLE, 300);
      add(map, Blocks.DAYLIGHT_DETECTOR, 300);
      add(map, ItemTags.BANNERS, 300);
      add(map, Items.BOW, 300);
      add(map, Items.FISHING_ROD, 300);
      add(map, Blocks.LADDER, 300);
      add(map, ItemTags.SIGNS, 200);
      add(map, Items.WOODEN_SHOVEL, 200);
      add(map, Items.WOODEN_SWORD, 200);
      add(map, Items.WOODEN_HOE, 200);
      add(map, Items.WOODEN_AXE, 200);
      add(map, Items.WOODEN_PICKAXE, 200);
      add(map, ItemTags.WOODEN_DOORS, 200);
      add(map, ItemTags.BOATS, 1200);
      add(map, ItemTags.WOOL, 100);
      add(map, ItemTags.WOODEN_BUTTONS, 100);
      add(map, Items.STICK, 100);
      add(map, ItemTags.SAPLINGS, 100);
      add(map, Items.BOWL, 100);
      add(map, ItemTags.CARPETS, 67);
      add(map, Blocks.DRIED_KELP_BLOCK, 4001);
      add(map, Items.CROSSBOW, 300);
      add(map, Blocks.BAMBOO, 50);
      add(map, Blocks.DEAD_BUSH, 100);
      add(map, Blocks.SCAFFOLDING, 400);
      add(map, Blocks.LOOM, 300);
      add(map, Blocks.BARREL, 300);
      add(map, Blocks.CARTOGRAPHY_TABLE, 300);
      add(map, Blocks.FLETCHING_TABLE, 300);
      add(map, Blocks.SMITHING_TABLE, 300);
      add(map, Blocks.COMPOSTER, 300);
      return map;
   }

   private static boolean isNeverAFurnaceFuel(Item pItem) {
      return ItemTags.NON_FLAMMABLE_WOOD.contains(pItem);
   }

   private static void add(Map<Item, Integer> pMap, ITag<Item> pItemTag, int pBurnTime) {
      for(Item item : pItemTag.getValues()) {
         if (!isNeverAFurnaceFuel(item)) {
            pMap.put(item, pBurnTime);
         }
      }

   }

   private static void add(Map<Item, Integer> pMap, IItemProvider pItem, int pBurnTime) {
      Item item = pItem.asItem();
      if (isNeverAFurnaceFuel(item)) {
         if (SharedConstants.IS_RUNNING_IN_IDE) {
            throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("A developer tried to explicitly make fire resistant item " + item.getName((ItemStack)null).getString() + " a furnace fuel. That will not work!"));
         }
      } else {
         pMap.put(item, pBurnTime);
      }
   }

   private boolean isLit() {
      return this.litTime > 0;
   }

   public void load(BlockState p_230337_1_, CompoundNBT p_230337_2_) { //TODO: MARK
      super.load(p_230337_1_, p_230337_2_);
      this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      ItemStackHelper.loadAllItems(p_230337_2_, this.items);
      this.litTime = p_230337_2_.getInt("BurnTime");
      this.cookingProgress = p_230337_2_.getInt("CookTime");
      this.cookingTotalTime = p_230337_2_.getInt("CookTimeTotal");
      this.litDuration = this.getBurnDuration(this.items.get(1));
      CompoundNBT compoundnbt = p_230337_2_.getCompound("RecipesUsed");

      for(String s : compoundnbt.getAllKeys()) {
         this.recipesUsed.put(new ResourceLocation(s), compoundnbt.getInt(s));
      }

   }

   public CompoundNBT save(CompoundNBT pCompound) {
      super.save(pCompound);
      pCompound.putInt("BurnTime", this.litTime);
      pCompound.putInt("CookTime", this.cookingProgress);
      pCompound.putInt("CookTimeTotal", this.cookingTotalTime);
      ItemStackHelper.saveAllItems(pCompound, this.items);
      CompoundNBT compoundnbt = new CompoundNBT();
      this.recipesUsed.forEach((p_235643_1_, p_235643_2_) -> {
         compoundnbt.putInt(p_235643_1_.toString(), p_235643_2_);
      });
      pCompound.put("RecipesUsed", compoundnbt);
      return pCompound;
   }

   public void tick() {
      boolean flag = this.isLit();
      boolean flag1 = false;
      if (this.isLit()) {
         --this.litTime;
      }

      if (!this.level.isClientSide) {
         ItemStack itemstack = this.items.get(1);
         if (this.isLit() || !itemstack.isEmpty() && !this.items.get(0).isEmpty()) {
            IRecipe<?> irecipe = this.level.getRecipeManager().getRecipeFor((IRecipeType<AbstractCookingRecipe>)this.recipeType, this, this.level).orElse(null);
            if (!this.isLit() && this.canBurn(irecipe)) {
               this.litTime = this.getBurnDuration(itemstack);
               this.litDuration = this.litTime;
               if (this.isLit()) {
                  flag1 = true;
                  if (itemstack.hasContainerItem())
                      this.items.set(1, itemstack.getContainerItem());
                  else
                  if (!itemstack.isEmpty()) {
                     Item item = itemstack.getItem();
                     itemstack.shrink(1);
                     if (itemstack.isEmpty()) {
                        this.items.set(1, itemstack.getContainerItem());
                     }
                  }
               }
            }

            if (this.isLit() && this.canBurn(irecipe)) {
               ++this.cookingProgress;
               if (this.cookingProgress == this.cookingTotalTime) {
                  this.cookingProgress = 0;
                  this.cookingTotalTime = this.getTotalCookTime();
                  this.burn(irecipe);
                  flag1 = true;
               }
            } else {
               this.cookingProgress = 0;
            }
         } else if (!this.isLit() && this.cookingProgress > 0) {
            this.cookingProgress = MathHelper.clamp(this.cookingProgress - 2, 0, this.cookingTotalTime);
         }

         if (flag != this.isLit()) {
            flag1 = true;
            this.level.setBlock(this.worldPosition, this.level.getBlockState(this.worldPosition).setValue(AbstractFurnaceBlock.LIT, Boolean.valueOf(this.isLit())), 3);
         }
      }

      if (flag1) {
         this.setChanged();
      }

   }

   protected boolean canBurn(@Nullable IRecipe<?> p_214008_1_) {
      if (!this.items.get(0).isEmpty() && p_214008_1_ != null) {
         ItemStack itemstack = ((IRecipe<ISidedInventory>) p_214008_1_).assemble(this);
         if (itemstack.isEmpty()) {
            return false;
         } else {
            ItemStack itemstack1 = this.items.get(2);
            if (itemstack1.isEmpty()) {
               return true;
            } else if (!itemstack1.sameItem(itemstack)) {
               return false;
            } else if (itemstack1.getCount() + itemstack.getCount() <= this.getMaxStackSize() && itemstack1.getCount() + itemstack.getCount() <= itemstack1.getMaxStackSize()) { // Forge fix: make furnace respect stack sizes in furnace recipes
               return true;
            } else {
               return itemstack1.getCount() + itemstack.getCount() <= itemstack.getMaxStackSize(); // Forge fix: make furnace respect stack sizes in furnace recipes
            }
         }
      } else {
         return false;
      }
   }

   private void burn(@Nullable IRecipe<?> p_214007_1_) {
      if (p_214007_1_ != null && this.canBurn(p_214007_1_)) {
         ItemStack itemstack = this.items.get(0);
         ItemStack itemstack1 = ((IRecipe<ISidedInventory>) p_214007_1_).assemble(this);
         ItemStack itemstack2 = this.items.get(2);
         if (itemstack2.isEmpty()) {
            this.items.set(2, itemstack1.copy());
         } else if (itemstack2.getItem() == itemstack1.getItem()) {
            itemstack2.grow(itemstack1.getCount());
         }

         if (!this.level.isClientSide) {
            this.setRecipeUsed(p_214007_1_);
         }

         if (itemstack.getItem() == Blocks.WET_SPONGE.asItem() && !this.items.get(1).isEmpty() && this.items.get(1).getItem() == Items.BUCKET) {
            this.items.set(1, new ItemStack(Items.WATER_BUCKET));
         }

         itemstack.shrink(1);
      }
   }

   protected int getBurnDuration(ItemStack pFuel) {
      if (pFuel.isEmpty()) {
         return 0;
      } else {
         Item item = pFuel.getItem();
         return net.minecraftforge.common.ForgeHooks.getBurnTime(pFuel, this.recipeType);
      }
   }

   protected int getTotalCookTime() {
      return this.level.getRecipeManager().getRecipeFor((IRecipeType<AbstractCookingRecipe>)this.recipeType, this, this.level).map(AbstractCookingRecipe::getCookingTime).orElse(200);
   }

   public static boolean isFuel(ItemStack pStack) {
      return net.minecraftforge.common.ForgeHooks.getBurnTime(pStack, null) > 0;
   }

   public int[] getSlotsForFace(Direction pSide) {
      if (pSide == Direction.DOWN) {
         return SLOTS_FOR_DOWN;
      } else {
         return pSide == Direction.UP ? SLOTS_FOR_UP : SLOTS_FOR_SIDES;
      }
   }

   /**
    * Returns true if automation can insert the given item in the given slot from the given side.
    */
   public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
      return this.canPlaceItem(pIndex, pItemStack);
   }

   /**
    * Returns true if automation can extract the given item in the given slot from the given side.
    */
   public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
      if (pDirection == Direction.DOWN && pIndex == 1) {
         Item item = pStack.getItem();
         if (item != Items.WATER_BUCKET && item != Items.BUCKET) {
            return false;
         }
      }

      return true;
   }

   /**
    * Returns the number of slots in the inventory.
    */
   public int getContainerSize() {
      return this.items.size();
   }

   public boolean isEmpty() {
      for(ItemStack itemstack : this.items) {
         if (!itemstack.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   /**
    * Returns the stack in the given slot.
    */
   public ItemStack getItem(int pIndex) {
      return this.items.get(pIndex);
   }

   /**
    * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
    */
   public ItemStack removeItem(int pIndex, int pCount) {
      return ItemStackHelper.removeItem(this.items, pIndex, pCount);
   }

   /**
    * Removes a stack from the given slot and returns it.
    */
   public ItemStack removeItemNoUpdate(int pIndex) {
      return ItemStackHelper.takeItem(this.items, pIndex);
   }

   /**
    * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
    */
   public void setItem(int pIndex, ItemStack pStack) {
      ItemStack itemstack = this.items.get(pIndex);
      boolean flag = !pStack.isEmpty() && pStack.sameItem(itemstack) && ItemStack.tagMatches(pStack, itemstack);
      this.items.set(pIndex, pStack);
      if (pStack.getCount() > this.getMaxStackSize()) {
         pStack.setCount(this.getMaxStackSize());
      }

      if (pIndex == 0 && !flag) {
         this.cookingTotalTime = this.getTotalCookTime();
         this.cookingProgress = 0;
         this.setChanged();
      }

   }

   /**
    * Don't rename this method to canInteractWith due to conflicts with Container
    */
   public boolean stillValid(PlayerEntity pPlayer) {
      if (this.level.getBlockEntity(this.worldPosition) != this) {
         return false;
      } else {
         return pPlayer.distanceToSqr((double)this.worldPosition.getX() + 0.5D, (double)this.worldPosition.getY() + 0.5D, (double)this.worldPosition.getZ() + 0.5D) <= 64.0D;
      }
   }

   /**
    * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot. For
    * guis use Slot.isItemValid
    */
   public boolean canPlaceItem(int pIndex, ItemStack pStack) {
      if (pIndex == 2) {
         return false;
      } else if (pIndex != 1) {
         return true;
      } else {
         ItemStack itemstack = this.items.get(1);
         return net.minecraftforge.common.ForgeHooks.getBurnTime(pStack, this.recipeType) > 0 || pStack.getItem() == Items.BUCKET && itemstack.getItem() != Items.BUCKET;
      }
   }

   public void clearContent() {
      this.items.clear();
   }

   public void setRecipeUsed(@Nullable IRecipe<?> pRecipe) {
      if (pRecipe != null) {
         ResourceLocation resourcelocation = pRecipe.getId();
         this.recipesUsed.addTo(resourcelocation, 1);
      }

   }

   @Nullable
   public IRecipe<?> getRecipeUsed() {
      return null;
   }

   public void awardUsedRecipes(PlayerEntity pPlayer) {
   }

   public void awardUsedRecipesAndPopExperience(PlayerEntity p_235645_1_) {
      List<IRecipe<?>> list = this.getRecipesToAwardAndPopExperience(p_235645_1_.level, p_235645_1_.position());
      p_235645_1_.awardRecipes(list);
      this.recipesUsed.clear();
   }

   public List<IRecipe<?>> getRecipesToAwardAndPopExperience(World p_235640_1_, Vector3d p_235640_2_) {
      List<IRecipe<?>> list = Lists.newArrayList();

      for(Entry<ResourceLocation> entry : this.recipesUsed.object2IntEntrySet()) {
         p_235640_1_.getRecipeManager().byKey(entry.getKey()).ifPresent((p_235642_4_) -> {
            list.add(p_235642_4_);
            createExperience(p_235640_1_, p_235640_2_, entry.getIntValue(), ((AbstractCookingRecipe)p_235642_4_).getExperience());
         });
      }

      return list;
   }

   private static void createExperience(World p_235641_0_, Vector3d p_235641_1_, int p_235641_2_, float p_235641_3_) {
      int i = MathHelper.floor((float)p_235641_2_ * p_235641_3_);
      float f = MathHelper.frac((float)p_235641_2_ * p_235641_3_);
      if (f != 0.0F && Math.random() < (double)f) {
         ++i;
      }

      while(i > 0) {
         int j = ExperienceOrbEntity.getExperienceValue(i);
         i -= j;
         p_235641_0_.addFreshEntity(new ExperienceOrbEntity(p_235641_0_, p_235641_1_.x, p_235641_1_.y, p_235641_1_.z, j));
      }

   }

   public void fillStackedContents(RecipeItemHelper pHelper) {
      for(ItemStack itemstack : this.items) {
         pHelper.accountStack(itemstack);
      }

   }

   net.minecraftforge.common.util.LazyOptional<? extends net.minecraftforge.items.IItemHandler>[] handlers =
           net.minecraftforge.items.wrapper.SidedInvWrapper.create(this, Direction.UP, Direction.DOWN, Direction.NORTH);

   @Override
   public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable Direction facing) {
      if (!this.remove && facing != null && capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
         if (facing == Direction.UP)
            return handlers[0].cast();
         else if (facing == Direction.DOWN)
            return handlers[1].cast();
         else
            return handlers[2].cast();
      }
      return super.getCapability(capability, facing);
   }

   @Override
   protected void invalidateCaps() {
      super.invalidateCaps();
      for (int x = 0; x < handlers.length; x++)
        handlers[x].invalidate();
   }
}
