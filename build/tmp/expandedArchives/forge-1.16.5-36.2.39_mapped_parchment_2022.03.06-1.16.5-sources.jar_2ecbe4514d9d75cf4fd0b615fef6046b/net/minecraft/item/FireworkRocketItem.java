package net.minecraft.item;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FireworkRocketItem extends Item {
   public FireworkRocketItem(Item.Properties p_i48498_1_) {
      super(p_i48498_1_);
   }

   /**
    * Called when this item is used when targetting a Block
    */
   public ActionResultType useOn(ItemUseContext pContext) {
      World world = pContext.getLevel();
      if (!world.isClientSide) {
         ItemStack itemstack = pContext.getItemInHand();
         Vector3d vector3d = pContext.getClickLocation();
         Direction direction = pContext.getClickedFace();
         FireworkRocketEntity fireworkrocketentity = new FireworkRocketEntity(world, pContext.getPlayer(), vector3d.x + (double)direction.getStepX() * 0.15D, vector3d.y + (double)direction.getStepY() * 0.15D, vector3d.z + (double)direction.getStepZ() * 0.15D, itemstack);
         world.addFreshEntity(fireworkrocketentity);
         itemstack.shrink(1);
      }

      return ActionResultType.sidedSuccess(world.isClientSide);
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pHand) {
      if (pPlayer.isFallFlying()) {
         ItemStack itemstack = pPlayer.getItemInHand(pHand);
         if (!pLevel.isClientSide) {
            pLevel.addFreshEntity(new FireworkRocketEntity(pLevel, itemstack, pPlayer));
            if (!pPlayer.abilities.instabuild) {
               itemstack.shrink(1);
            }
         }

         return ActionResult.sidedSuccess(pPlayer.getItemInHand(pHand), pLevel.isClientSide());
      } else {
         return ActionResult.pass(pPlayer.getItemInHand(pHand));
      }
   }

   /**
    * allows items to add custom lines of information to the mouseover description
    */
   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(ItemStack pStack, @Nullable World pLevel, List<ITextComponent> pTooltip, ITooltipFlag pFlag) {
      CompoundNBT compoundnbt = pStack.getTagElement("Fireworks");
      if (compoundnbt != null) {
         if (compoundnbt.contains("Flight", 99)) {
            pTooltip.add((new TranslationTextComponent("item.minecraft.firework_rocket.flight")).append(" ").append(String.valueOf((int)compoundnbt.getByte("Flight"))).withStyle(TextFormatting.GRAY));
         }

         ListNBT listnbt = compoundnbt.getList("Explosions", 10);
         if (!listnbt.isEmpty()) {
            for(int i = 0; i < listnbt.size(); ++i) {
               CompoundNBT compoundnbt1 = listnbt.getCompound(i);
               List<ITextComponent> list = Lists.newArrayList();
               FireworkStarItem.appendHoverText(compoundnbt1, list);
               if (!list.isEmpty()) {
                  for(int j = 1; j < list.size(); ++j) {
                     list.set(j, (new StringTextComponent("  ")).append(list.get(j)).withStyle(TextFormatting.GRAY));
                  }

                  pTooltip.addAll(list);
               }
            }
         }

      }
   }

   public static enum Shape {
      SMALL_BALL(0, "small_ball"),
      LARGE_BALL(1, "large_ball"),
      STAR(2, "star"),
      CREEPER(3, "creeper"),
      BURST(4, "burst");

      private static final FireworkRocketItem.Shape[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt((p_199796_0_) -> {
         return p_199796_0_.id;
      })).toArray((p_199797_0_) -> {
         return new FireworkRocketItem.Shape[p_199797_0_];
      });
      private final int id;
      private final String name;

      private Shape(int pId, String pName) {
         this.id = pId;
         this.name = pName;
      }

      public int getId() {
         return this.id;
      }

      @OnlyIn(Dist.CLIENT)
      public String getName() {
         return this.name;
      }

      @OnlyIn(Dist.CLIENT)
      public static FireworkRocketItem.Shape byId(int pIndex) {
         return pIndex >= 0 && pIndex < BY_ID.length ? BY_ID[pIndex] : SMALL_BALL;
      }
   }
}