package net.minecraft.client.util;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SearchTreeManager implements IResourceManagerReloadListener {
   public static final SearchTreeManager.Key<ItemStack> CREATIVE_NAMES = new SearchTreeManager.Key<>();
   public static final SearchTreeManager.Key<ItemStack> CREATIVE_TAGS = new SearchTreeManager.Key<>();
   public static final SearchTreeManager.Key<RecipeList> RECIPE_COLLECTIONS = new SearchTreeManager.Key<>();
   private final Map<SearchTreeManager.Key<?>, IMutableSearchTree<?>> searchTrees = Maps.newHashMap();

   public void onResourceManagerReload(IResourceManager pResourceManager) {
      for(IMutableSearchTree<?> imutablesearchtree : this.searchTrees.values()) {
         imutablesearchtree.refresh();
      }

   }

   public <T> void register(SearchTreeManager.Key<T> pKey, IMutableSearchTree<T> pValue) {
      this.searchTrees.put(pKey, pValue);
   }

   public <T> IMutableSearchTree<T> getTree(SearchTreeManager.Key<T> pKey) {
      return (IMutableSearchTree<T>)this.searchTrees.get(pKey);
   }

   @Override
   public net.minecraftforge.resource.IResourceType getResourceType() {
      return net.minecraftforge.resource.VanillaResourceType.LANGUAGES;
   }

   @OnlyIn(Dist.CLIENT)
   public static class Key<T> {
   }
}
