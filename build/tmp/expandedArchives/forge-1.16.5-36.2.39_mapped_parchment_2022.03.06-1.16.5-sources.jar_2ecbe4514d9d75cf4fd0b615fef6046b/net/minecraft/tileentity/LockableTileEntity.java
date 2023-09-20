package net.minecraft.tileentity;

import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.INameable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.LockCode;

public abstract class LockableTileEntity extends TileEntity implements IInventory, INamedContainerProvider, INameable {
   private LockCode lockKey = LockCode.NO_LOCK;
   private ITextComponent name;

   protected LockableTileEntity(TileEntityType<?> p_i48285_1_) {
      super(p_i48285_1_);
   }

   public void load(BlockState p_230337_1_, CompoundNBT p_230337_2_) {
      super.load(p_230337_1_, p_230337_2_);
      this.lockKey = LockCode.fromTag(p_230337_2_);
      if (p_230337_2_.contains("CustomName", 8)) {
         this.name = ITextComponent.Serializer.fromJson(p_230337_2_.getString("CustomName"));
      }

   }

   public CompoundNBT save(CompoundNBT pCompound) {
      super.save(pCompound);
      this.lockKey.addToTag(pCompound);
      if (this.name != null) {
         pCompound.putString("CustomName", ITextComponent.Serializer.toJson(this.name));
      }

      return pCompound;
   }

   public void setCustomName(ITextComponent pName) {
      this.name = pName;
   }

   public ITextComponent getName() {
      return this.name != null ? this.name : this.getDefaultName();
   }

   public ITextComponent getDisplayName() {
      return this.getName();
   }

   @Nullable
   public ITextComponent getCustomName() {
      return this.name;
   }

   protected abstract ITextComponent getDefaultName();

   public boolean canOpen(PlayerEntity pPlayer) {
      return canUnlock(pPlayer, this.lockKey, this.getDisplayName());
   }

   public static boolean canUnlock(PlayerEntity pPlayer, LockCode pCode, ITextComponent p_213905_2_) {
      if (!pPlayer.isSpectator() && !pCode.unlocksWith(pPlayer.getMainHandItem())) {
         pPlayer.displayClientMessage(new TranslationTextComponent("container.isLocked", p_213905_2_), true);
         pPlayer.playNotifySound(SoundEvents.CHEST_LOCKED, SoundCategory.BLOCKS, 1.0F, 1.0F);
         return false;
      } else {
         return true;
      }
   }

   @Nullable
   public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {
      return this.canOpen(p_createMenu_3_) ? this.createMenu(p_createMenu_1_, p_createMenu_2_) : null;
   }

   protected abstract Container createMenu(int pId, PlayerInventory pPlayer);

   private net.minecraftforge.common.util.LazyOptional<?> itemHandler = net.minecraftforge.common.util.LazyOptional.of(() -> createUnSidedHandler());
   protected net.minecraftforge.items.IItemHandler createUnSidedHandler() {
      return new net.minecraftforge.items.wrapper.InvWrapper(this);
   }

   public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap, @javax.annotation.Nullable net.minecraft.util.Direction side) {
      if (!this.remove && cap == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
         return itemHandler.cast();
      return super.getCapability(cap, side);
   }

   @Override
   protected void invalidateCaps() {
      super.invalidateCaps();
      itemHandler.invalidate();
   }
}
