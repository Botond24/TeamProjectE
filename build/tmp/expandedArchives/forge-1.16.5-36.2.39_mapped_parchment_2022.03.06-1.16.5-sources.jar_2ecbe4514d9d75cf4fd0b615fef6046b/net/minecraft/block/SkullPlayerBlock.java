package net.minecraft.block;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;

public class SkullPlayerBlock extends SkullBlock {
   public SkullPlayerBlock(AbstractBlock.Properties p_i48354_1_) {
      super(SkullBlock.Types.PLAYER, p_i48354_1_);
   }

   /**
    * Called by ItemBlocks after a block is set in the world, to allow post-place logic
    */
   public void setPlacedBy(World pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
      super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      if (tileentity instanceof SkullTileEntity) {
         SkullTileEntity skulltileentity = (SkullTileEntity)tileentity;
         GameProfile gameprofile = null;
         if (pStack.hasTag()) {
            CompoundNBT compoundnbt = pStack.getTag();
            if (compoundnbt.contains("SkullOwner", 10)) {
               gameprofile = NBTUtil.readGameProfile(compoundnbt.getCompound("SkullOwner"));
            } else if (compoundnbt.contains("SkullOwner", 8) && !StringUtils.isBlank(compoundnbt.getString("SkullOwner"))) {
               gameprofile = new GameProfile((UUID)null, compoundnbt.getString("SkullOwner"));
            }
         }

         skulltileentity.setOwner(gameprofile);
      }

   }
}