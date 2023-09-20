package net.minecraft.tileentity;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(
   value = Dist.CLIENT,
   _interface = IChestLid.class
)
public class EnderChestTileEntity extends TileEntity implements IChestLid, ITickableTileEntity {
   public float openness;
   public float oOpenness;
   public int openCount;
   private int tickInterval;

   public EnderChestTileEntity() {
      super(TileEntityType.ENDER_CHEST);
   }

   public void tick() {
      if (++this.tickInterval % 20 * 4 == 0) {
         this.level.blockEvent(this.worldPosition, Blocks.ENDER_CHEST, 1, this.openCount);
      }

      this.oOpenness = this.openness;
      int i = this.worldPosition.getX();
      int j = this.worldPosition.getY();
      int k = this.worldPosition.getZ();
      float f = 0.1F;
      if (this.openCount > 0 && this.openness == 0.0F) {
         double d0 = (double)i + 0.5D;
         double d1 = (double)k + 0.5D;
         this.level.playSound((PlayerEntity)null, d0, (double)j + 0.5D, d1, SoundEvents.ENDER_CHEST_OPEN, SoundCategory.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
      }

      if (this.openCount == 0 && this.openness > 0.0F || this.openCount > 0 && this.openness < 1.0F) {
         float f2 = this.openness;
         if (this.openCount > 0) {
            this.openness += 0.1F;
         } else {
            this.openness -= 0.1F;
         }

         if (this.openness > 1.0F) {
            this.openness = 1.0F;
         }

         float f1 = 0.5F;
         if (this.openness < 0.5F && f2 >= 0.5F) {
            double d3 = (double)i + 0.5D;
            double d2 = (double)k + 0.5D;
            this.level.playSound((PlayerEntity)null, d3, (double)j + 0.5D, d2, SoundEvents.ENDER_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
         }

         if (this.openness < 0.0F) {
            this.openness = 0.0F;
         }
      }

   }

   /**
    * See {@link Block#eventReceived} for more information. This must return true serverside before it is called
    * clientside.
    */
   public boolean triggerEvent(int pId, int pType) {
      if (pId == 1) {
         this.openCount = pType;
         return true;
      } else {
         return super.triggerEvent(pId, pType);
      }
   }

   /**
    * invalidates a tile entity
    */
   public void setRemoved() {
      this.clearCache();
      super.setRemoved();
   }

   public void startOpen() {
      ++this.openCount;
      this.level.blockEvent(this.worldPosition, Blocks.ENDER_CHEST, 1, this.openCount);
   }

   public void stopOpen() {
      --this.openCount;
      this.level.blockEvent(this.worldPosition, Blocks.ENDER_CHEST, 1, this.openCount);
   }

   public boolean stillValid(PlayerEntity pPlayer) {
      if (this.level.getBlockEntity(this.worldPosition) != this) {
         return false;
      } else {
         return !(pPlayer.distanceToSqr((double)this.worldPosition.getX() + 0.5D, (double)this.worldPosition.getY() + 0.5D, (double)this.worldPosition.getZ() + 0.5D) > 64.0D);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public float getOpenNess(float pPartialTicks) {
      return MathHelper.lerp(pPartialTicks, this.oOpenness, this.openness);
   }
}