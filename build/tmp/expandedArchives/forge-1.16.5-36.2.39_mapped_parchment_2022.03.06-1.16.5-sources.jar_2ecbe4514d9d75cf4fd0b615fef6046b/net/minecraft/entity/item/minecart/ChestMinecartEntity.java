package net.minecraft.entity.item.minecart;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class ChestMinecartEntity extends ContainerMinecartEntity {
   public ChestMinecartEntity(EntityType<? extends ChestMinecartEntity> p_i50124_1_, World p_i50124_2_) {
      super(p_i50124_1_, p_i50124_2_);
   }

   public ChestMinecartEntity(World pLevel, double pX, double pY, double pZ) {
      super(EntityType.CHEST_MINECART, pX, pY, pZ, pLevel);
   }

   public void destroy(DamageSource pSource) {
      super.destroy(pSource);
      if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
         this.spawnAtLocation(Blocks.CHEST);
      }

   }

   /**
    * Returns the number of slots in the inventory.
    */
   public int getContainerSize() {
      return 27;
   }

   public AbstractMinecartEntity.Type getMinecartType() {
      return AbstractMinecartEntity.Type.CHEST;
   }

   public BlockState getDefaultDisplayBlockState() {
      return Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH);
   }

   public int getDefaultDisplayOffset() {
      return 8;
   }

   public Container createMenu(int pContainerId, PlayerInventory pPlayerInventory) {
      return ChestContainer.threeRows(pContainerId, pPlayerInventory, this);
   }
}