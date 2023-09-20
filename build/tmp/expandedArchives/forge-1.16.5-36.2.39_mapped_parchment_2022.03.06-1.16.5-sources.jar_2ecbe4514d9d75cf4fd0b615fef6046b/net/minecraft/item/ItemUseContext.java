package net.minecraft.item;

import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class ItemUseContext {
   @Nullable
   private final PlayerEntity player;
   private final Hand hand;
   private final BlockRayTraceResult hitResult;
   private final World level;
   private final ItemStack itemStack;

   public ItemUseContext(PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHitResult) {
      this(pPlayer.level, pPlayer, pHand, pPlayer.getItemInHand(pHand), pHitResult);
   }

   public ItemUseContext(World pLevel, @Nullable PlayerEntity pPlayer, Hand pHand, ItemStack pItemStack, BlockRayTraceResult pHitResult) {
      this.player = pPlayer;
      this.hand = pHand;
      this.hitResult = pHitResult;
      this.itemStack = pItemStack;
      this.level = pLevel;
   }

   protected final BlockRayTraceResult getHitResult() {
      return this.hitResult;
   }

   public BlockPos getClickedPos() {
      return this.hitResult.getBlockPos();
   }

   public Direction getClickedFace() {
      return this.hitResult.getDirection();
   }

   public Vector3d getClickLocation() {
      return this.hitResult.getLocation();
   }

   public boolean isInside() {
      return this.hitResult.isInside();
   }

   public ItemStack getItemInHand() {
      return this.itemStack;
   }

   @Nullable
   public PlayerEntity getPlayer() {
      return this.player;
   }

   public Hand getHand() {
      return this.hand;
   }

   public World getLevel() {
      return this.level;
   }

   public Direction getHorizontalDirection() {
      return this.player == null ? Direction.NORTH : this.player.getDirection();
   }

   public boolean isSecondaryUseActive() {
      return this.player != null && this.player.isSecondaryUseActive();
   }

   public float getRotation() {
      return this.player == null ? 0.0F : this.player.yRot;
   }
}