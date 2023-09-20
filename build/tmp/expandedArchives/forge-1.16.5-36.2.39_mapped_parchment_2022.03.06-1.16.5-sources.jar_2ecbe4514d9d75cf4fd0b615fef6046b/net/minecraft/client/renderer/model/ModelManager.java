package net.minecraft.client.renderer.model;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.SpriteMap;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.fluid.FluidState;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelManager extends ReloadListener<ModelBakery> implements AutoCloseable {
   private Map<ResourceLocation, IBakedModel> bakedRegistry = new java.util.HashMap<>();
   @Nullable
   private SpriteMap atlases;
   private final BlockModelShapes blockModelShaper;
   private final TextureManager textureManager;
   private final BlockColors blockColors;
   private int maxMipmapLevels;
   private IBakedModel missingModel;
   private Object2IntMap<BlockState> modelGroups;

   public ModelManager(TextureManager p_i226057_1_, BlockColors p_i226057_2_, int p_i226057_3_) {
      this.textureManager = p_i226057_1_;
      this.blockColors = p_i226057_2_;
      this.maxMipmapLevels = p_i226057_3_;
      this.blockModelShaper = new BlockModelShapes(this);
   }

   public IBakedModel getModel(ResourceLocation modelLocation) {
      return this.bakedRegistry.getOrDefault(modelLocation, this.missingModel);
   }

   public IBakedModel getModel(ModelResourceLocation pModelLocation) {
      return this.bakedRegistry.getOrDefault(pModelLocation, this.missingModel);
   }

   public IBakedModel getMissingModel() {
      return this.missingModel;
   }

   public BlockModelShapes getBlockModelShaper() {
      return this.blockModelShaper;
   }

   /**
    * Performs any reloading that can be done off-thread, such as file IO
    */
   protected ModelBakery prepare(IResourceManager pResourceManager, IProfiler pProfiler) {
      pProfiler.startTick();
      net.minecraftforge.client.model.ModelLoader modelbakery = new net.minecraftforge.client.model.ModelLoader(pResourceManager, this.blockColors, pProfiler, this.maxMipmapLevels);
      pProfiler.endTick();
      return modelbakery;
   }

   protected void apply(ModelBakery pObject, IResourceManager pResourceManager, IProfiler pProfiler) {
      pProfiler.startTick();
      pProfiler.push("upload");
      if (this.atlases != null) {
         this.atlases.close();
      }

      this.atlases = pObject.uploadTextures(this.textureManager, pProfiler);
      this.bakedRegistry = pObject.getBakedTopLevelModels();
      this.modelGroups = pObject.getModelGroups();
      this.missingModel = this.bakedRegistry.get(ModelBakery.MISSING_MODEL_LOCATION);
      net.minecraftforge.client.ForgeHooksClient.onModelBake(this, this.bakedRegistry, (net.minecraftforge.client.model.ModelLoader) pObject);
      pProfiler.popPush("cache");
      this.blockModelShaper.rebuildCache();
      pProfiler.pop();
      pProfiler.endTick();
   }

   public boolean requiresRender(BlockState pOldState, BlockState pNewState) {
      if (pOldState == pNewState) {
         return false;
      } else {
         int i = this.modelGroups.getInt(pOldState);
         if (i != -1) {
            int j = this.modelGroups.getInt(pNewState);
            if (i == j) {
               FluidState fluidstate = pOldState.getFluidState();
               FluidState fluidstate1 = pNewState.getFluidState();
               return fluidstate != fluidstate1;
            }
         }

         return true;
      }
   }

   public AtlasTexture getAtlas(ResourceLocation pLocation) {
      if (this.atlases == null) throw new RuntimeException("getAtlasTexture called too early!");
      return this.atlases.getAtlas(pLocation);
   }

   public void close() {
      if (this.atlases != null) {
         this.atlases.close();
      }

   }

   public void updateMaxMipLevel(int pLevel) {
      this.maxMipmapLevels = pLevel;
   }

   // TODO
   //@Override
   public net.minecraftforge.resource.IResourceType getResourceType() {
      return net.minecraftforge.resource.VanillaResourceType.MODELS;
   }
}
