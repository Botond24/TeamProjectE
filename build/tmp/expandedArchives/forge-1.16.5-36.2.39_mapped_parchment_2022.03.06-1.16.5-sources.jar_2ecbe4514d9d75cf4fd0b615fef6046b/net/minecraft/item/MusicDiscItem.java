package net.minecraft.item;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MusicDiscItem extends Item {
   @Deprecated // Forge: refer to WorldRender#playRecord. Modders: there's no need to reflectively modify this map!
   private static final Map<SoundEvent, MusicDiscItem> BY_NAME = Maps.newHashMap();
   private final int analogOutput;
   @Deprecated // Forge: refer to soundSupplier
   private final SoundEvent sound;
   private final java.util.function.Supplier<SoundEvent> soundSupplier;

   @Deprecated // Forge: Use the constructor that takes a supplier instead
   public MusicDiscItem(int pAnalogOutput, SoundEvent pSound, Item.Properties pProperties) {
      super(pProperties);
      this.analogOutput = pAnalogOutput;
      this.sound = pSound;
      BY_NAME.put(this.sound, this);
      this.soundSupplier = this.sound.delegate;
   }

   /**
    * For mod use, allows to create a music disc without having to create a new
    * SoundEvent before their registry event is fired.
    *
    * @param comparatorValue The value this music disc should output on the comparator. Must be between 0 and 15.
    * @param soundSupplier A supplier that provides the sound that should be played. Use a
    *                      {@link net.minecraftforge.fml.RegistryObject}{@code <SoundEvent>} or a
    *                      {@link net.minecraftforge.registries.IRegistryDelegate} for this parameter.
    * @param builder A set of {@link Item.Properties} that describe this item.
    */
   public MusicDiscItem(int comparatorValue, java.util.function.Supplier<SoundEvent> soundSupplier, Item.Properties builder)
   {
      super(builder);
      this.analogOutput = comparatorValue;
      this.sound = null;
      this.soundSupplier = soundSupplier;
   }

   /**
    * Called when this item is used when targetting a Block
    */
   public ActionResultType useOn(ItemUseContext pContext) {
      World world = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      BlockState blockstate = world.getBlockState(blockpos);
      if (blockstate.is(Blocks.JUKEBOX) && !blockstate.getValue(JukeboxBlock.HAS_RECORD)) {
         ItemStack itemstack = pContext.getItemInHand();
         if (!world.isClientSide) {
            ((JukeboxBlock)Blocks.JUKEBOX).setRecord(world, blockpos, blockstate, itemstack);
            world.levelEvent((PlayerEntity)null, 1010, blockpos, Item.getId(this));
            itemstack.shrink(1);
            PlayerEntity playerentity = pContext.getPlayer();
            if (playerentity != null) {
               playerentity.awardStat(Stats.PLAY_RECORD);
            }
         }

         return ActionResultType.sidedSuccess(world.isClientSide);
      } else {
         return ActionResultType.PASS;
      }
   }

   public int getAnalogOutput() {
      return this.analogOutput;
   }

   /**
    * allows items to add custom lines of information to the mouseover description
    */
   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(ItemStack pStack, @Nullable World pLevel, List<ITextComponent> pTooltip, ITooltipFlag pFlag) {
      pTooltip.add(this.getDisplayName().withStyle(TextFormatting.GRAY));
   }

   @OnlyIn(Dist.CLIENT)
   public IFormattableTextComponent getDisplayName() {
      return new TranslationTextComponent(this.getDescriptionId() + ".desc");
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public static MusicDiscItem getBySound(SoundEvent pSound) {
      return BY_NAME.get(pSound);
   }

   @OnlyIn(Dist.CLIENT)
   public SoundEvent getSound() {
      return this.soundSupplier.get();
   }
}
