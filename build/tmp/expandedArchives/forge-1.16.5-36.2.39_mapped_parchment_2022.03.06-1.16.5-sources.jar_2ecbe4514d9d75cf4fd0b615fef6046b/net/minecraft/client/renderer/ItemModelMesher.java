package net.minecraft.client.renderer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemModelMesher {
   public final Int2ObjectMap<ModelResourceLocation> shapes = new Int2ObjectOpenHashMap<>(256);
   private final Int2ObjectMap<IBakedModel> shapesCache = new Int2ObjectOpenHashMap<>(256);
   private final ModelManager modelManager;

   public ItemModelMesher(ModelManager p_i46250_1_) {
      this.modelManager = p_i46250_1_;
   }

   public TextureAtlasSprite getParticleIcon(IItemProvider pItemProvider) {
      return this.getParticleIcon(new ItemStack(pItemProvider));
   }

   public TextureAtlasSprite getParticleIcon(ItemStack pStack) {
      IBakedModel ibakedmodel = this.getItemModel(pStack);
      // FORGE: Make sure to call the item overrides
      return ibakedmodel == this.modelManager.getMissingModel() && pStack.getItem() instanceof BlockItem ? this.modelManager.getBlockModelShaper().getParticleIcon(((BlockItem)pStack.getItem()).getBlock().defaultBlockState()) : ibakedmodel.getOverrides().resolve(ibakedmodel, pStack, null, null).getParticleTexture(net.minecraftforge.client.model.data.EmptyModelData.INSTANCE);
   }

   public IBakedModel getItemModel(ItemStack pStack) {
      IBakedModel ibakedmodel = this.getItemModel(pStack.getItem());
      return ibakedmodel == null ? this.modelManager.getMissingModel() : ibakedmodel;
   }

   @Nullable
   public IBakedModel getItemModel(Item pItem) {
      return this.shapesCache.get(getIndex(pItem));
   }

   private static int getIndex(Item pItem) {
      return Item.getId(pItem);
   }

   public void register(Item pItem, ModelResourceLocation pModelLocation) {
      this.shapes.put(getIndex(pItem), pModelLocation);
   }

   public ModelManager getModelManager() {
      return this.modelManager;
   }

   public void rebuildCache() {
      this.shapesCache.clear();

      for(Entry<Integer, ModelResourceLocation> entry : this.shapes.entrySet()) {
         this.shapesCache.put(entry.getKey(), this.modelManager.getModel(entry.getValue()));
      }

   }
}
