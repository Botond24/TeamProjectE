package net.minecraft.client.renderer;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.state.Property;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockModelShapes {
   private final Map<BlockState, IBakedModel> modelByStateCache = Maps.newIdentityHashMap();
   private final ModelManager modelManager;

   public BlockModelShapes(ModelManager p_i46245_1_) {
      this.modelManager = p_i46245_1_;
   }

   @Deprecated
   public TextureAtlasSprite getParticleIcon(BlockState pState) {
      return this.getBlockModel(pState).getParticleTexture(net.minecraftforge.client.model.data.EmptyModelData.INSTANCE);
   }

   public TextureAtlasSprite getTexture(BlockState state, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos) {
      net.minecraftforge.client.model.data.IModelData data = net.minecraftforge.client.model.ModelDataManager.getModelData(world, pos);
      IBakedModel model = this.getBlockModel(state);
      return model.getParticleTexture(model.getModelData(world, pos, state, data == null ? net.minecraftforge.client.model.data.EmptyModelData.INSTANCE : data));
   }

   public IBakedModel getBlockModel(BlockState pState) {
      IBakedModel ibakedmodel = this.modelByStateCache.get(pState);
      if (ibakedmodel == null) {
         ibakedmodel = this.modelManager.getMissingModel();
      }

      return ibakedmodel;
   }

   public ModelManager getModelManager() {
      return this.modelManager;
   }

   public void rebuildCache() {
      this.modelByStateCache.clear();

      for(Block block : Registry.BLOCK) {
         block.getStateDefinition().getPossibleStates().forEach((p_209551_1_) -> {
            IBakedModel ibakedmodel = this.modelByStateCache.put(p_209551_1_, this.modelManager.getModel(stateToModelLocation(p_209551_1_)));
         });
      }

   }

   public static ModelResourceLocation stateToModelLocation(BlockState pState) {
      return stateToModelLocation(Registry.BLOCK.getKey(pState.getBlock()), pState);
   }

   public static ModelResourceLocation stateToModelLocation(ResourceLocation pLocation, BlockState pState) {
      return new ModelResourceLocation(pLocation, statePropertiesToString(pState.getValues()));
   }

   public static String statePropertiesToString(Map<Property<?>, Comparable<?>> pPropertyValues) {
      StringBuilder stringbuilder = new StringBuilder();

      for(Entry<Property<?>, Comparable<?>> entry : pPropertyValues.entrySet()) {
         if (stringbuilder.length() != 0) {
            stringbuilder.append(',');
         }

         Property<?> property = entry.getKey();
         stringbuilder.append(property.getName());
         stringbuilder.append('=');
         stringbuilder.append(getValue(property, entry.getValue()));
      }

      return stringbuilder.toString();
   }

   private static <T extends Comparable<T>> String getValue(Property<T> pProperty, Comparable<?> pValue) {
      return pProperty.getName((T)pValue);
   }
}
