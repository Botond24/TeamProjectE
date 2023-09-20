package net.minecraft.tileentity;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.util.INameable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BannerTileEntity extends TileEntity implements INameable {
   @Nullable
   private ITextComponent name;
   @Nullable
   private DyeColor baseColor = DyeColor.WHITE;
   /** A list of all the banner patterns. */
   @Nullable
   private ListNBT itemPatterns;
   private boolean receivedData;
   /** A list of all patterns stored on this banner. */
   @Nullable
   private List<Pair<BannerPattern, DyeColor>> patterns;

   public BannerTileEntity() {
      super(TileEntityType.BANNER);
   }

   public BannerTileEntity(DyeColor p_i47731_1_) {
      this();
      this.baseColor = p_i47731_1_;
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public static ListNBT getItemPatterns(ItemStack pStack) {
      ListNBT listnbt = null;
      CompoundNBT compoundnbt = pStack.getTagElement("BlockEntityTag");
      if (compoundnbt != null && compoundnbt.contains("Patterns", 9)) {
         listnbt = compoundnbt.getList("Patterns", 10).copy();
      }

      return listnbt;
   }

   @OnlyIn(Dist.CLIENT)
   public void fromItem(ItemStack pStack, DyeColor pColor) {
      this.itemPatterns = getItemPatterns(pStack);
      this.baseColor = pColor;
      this.patterns = null;
      this.receivedData = true;
      this.name = pStack.hasCustomHoverName() ? pStack.getHoverName() : null;
   }

   public ITextComponent getName() {
      return (ITextComponent)(this.name != null ? this.name : new TranslationTextComponent("block.minecraft.banner"));
   }

   @Nullable
   public ITextComponent getCustomName() {
      return this.name;
   }

   public void setCustomName(ITextComponent pName) {
      this.name = pName;
   }

   public CompoundNBT save(CompoundNBT pCompound) {
      super.save(pCompound);
      if (this.itemPatterns != null) {
         pCompound.put("Patterns", this.itemPatterns);
      }

      if (this.name != null) {
         pCompound.putString("CustomName", ITextComponent.Serializer.toJson(this.name));
      }

      return pCompound;
   }

   public void load(BlockState p_230337_1_, CompoundNBT p_230337_2_) {
      super.load(p_230337_1_, p_230337_2_);
      if (p_230337_2_.contains("CustomName", 8)) {
         this.name = ITextComponent.Serializer.fromJson(p_230337_2_.getString("CustomName"));
      }

      if (this.hasLevel()) {
         this.baseColor = ((AbstractBannerBlock)this.getBlockState().getBlock()).getColor();
      } else {
         this.baseColor = null;
      }

      this.itemPatterns = p_230337_2_.getList("Patterns", 10);
      this.patterns = null;
      this.receivedData = true;
   }

   /**
    * Retrieves packet to send to the client whenever this Tile Entity is resynced via World.notifyBlockUpdate. For
    * modded TE's, this packet comes back to you clientside in {@link #onDataPacket}
    */
   @Nullable
   public SUpdateTileEntityPacket getUpdatePacket() {
      return new SUpdateTileEntityPacket(this.worldPosition, 6, this.getUpdateTag());
   }

   /**
    * Get an NBT compound to sync to the client with SPacketChunkData, used for initial loading of the chunk or when
    * many blocks change at once. This compound comes back to you clientside in {@link handleUpdateTag}
    */
   public CompoundNBT getUpdateTag() {
      return this.save(new CompoundNBT());
   }

   /**
    * @return the amount of patterns stored in the given ItemStack. Defaults to zero if none are stored.
    */
   public static int getPatternCount(ItemStack pStack) {
      CompoundNBT compoundnbt = pStack.getTagElement("BlockEntityTag");
      return compoundnbt != null && compoundnbt.contains("Patterns") ? compoundnbt.getList("Patterns", 10).size() : 0;
   }

   /**
    * @return the patterns for this banner.
    */
   @OnlyIn(Dist.CLIENT)
   public List<Pair<BannerPattern, DyeColor>> getPatterns() {
      if (this.patterns == null && this.receivedData) {
         this.patterns = createPatterns(this.getBaseColor(this::getBlockState), this.itemPatterns);
      }

      return this.patterns;
   }

   @OnlyIn(Dist.CLIENT)
   public static List<Pair<BannerPattern, DyeColor>> createPatterns(DyeColor pColor, @Nullable ListNBT pListTag) {
      List<Pair<BannerPattern, DyeColor>> list = Lists.newArrayList();
      list.add(Pair.of(BannerPattern.BASE, pColor));
      if (pListTag != null) {
         for(int i = 0; i < pListTag.size(); ++i) {
            CompoundNBT compoundnbt = pListTag.getCompound(i);
            BannerPattern bannerpattern = BannerPattern.byHash(compoundnbt.getString("Pattern"));
            if (bannerpattern != null) {
               int j = compoundnbt.getInt("Color");
               list.add(Pair.of(bannerpattern, DyeColor.byId(j)));
            }
         }
      }

      return list;
   }

   /**
    * Removes all banner data from the given ItemStack.
    */
   public static void removeLastPattern(ItemStack pStack) {
      CompoundNBT compoundnbt = pStack.getTagElement("BlockEntityTag");
      if (compoundnbt != null && compoundnbt.contains("Patterns", 9)) {
         ListNBT listnbt = compoundnbt.getList("Patterns", 10);
         if (!listnbt.isEmpty()) {
            listnbt.remove(listnbt.size() - 1);
            if (listnbt.isEmpty()) {
               pStack.removeTagKey("BlockEntityTag");
            }

         }
      }
   }

   public ItemStack getItem(BlockState p_190615_1_) {
      ItemStack itemstack = new ItemStack(BannerBlock.byColor(this.getBaseColor(() -> {
         return p_190615_1_;
      })));
      if (this.itemPatterns != null && !this.itemPatterns.isEmpty()) {
         itemstack.getOrCreateTagElement("BlockEntityTag").put("Patterns", this.itemPatterns.copy());
      }

      if (this.name != null) {
         itemstack.setHoverName(this.name);
      }

      return itemstack;
   }

   public DyeColor getBaseColor(Supplier<BlockState> p_195533_1_) {
      if (this.baseColor == null) {
         this.baseColor = ((AbstractBannerBlock)p_195533_1_.get().getBlock()).getColor();
      }

      return this.baseColor;
   }
}