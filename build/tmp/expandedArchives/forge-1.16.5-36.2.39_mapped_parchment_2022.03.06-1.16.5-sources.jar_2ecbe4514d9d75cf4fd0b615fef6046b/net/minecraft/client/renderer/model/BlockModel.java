package net.minecraft.client.renderer.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class BlockModel implements IUnbakedModel {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final FaceBakery FACE_BAKERY = new FaceBakery();
   @VisibleForTesting
   static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(BlockModel.class, new BlockModel.Deserializer()).registerTypeAdapter(BlockPart.class, new BlockPart.Deserializer()).registerTypeAdapter(BlockPartFace.class, new BlockPartFace.Deserializer()).registerTypeAdapter(BlockFaceUV.class, new BlockFaceUV.Deserializer()).registerTypeAdapter(ItemTransformVec3f.class, new ItemTransformVec3f.Deserializer()).registerTypeAdapter(ItemCameraTransforms.class, new ItemCameraTransforms.Deserializer()).registerTypeAdapter(ItemOverride.class, new ItemOverride.Deserializer()).create();
   private final List<BlockPart> elements;
   @Nullable
   private final BlockModel.GuiLight guiLight;
   public final boolean hasAmbientOcclusion;
   private final ItemCameraTransforms transforms;
   private final List<ItemOverride> overrides;
   public String name = "";
   @VisibleForTesting
   public final Map<String, Either<RenderMaterial, String>> textureMap;
   @Nullable
   public BlockModel parent;
   @Nullable
   protected ResourceLocation parentLocation;
   public final net.minecraftforge.client.model.BlockModelConfiguration customData = new net.minecraftforge.client.model.BlockModelConfiguration(this);

   public static BlockModel fromStream(Reader pReader) {
      return JSONUtils.fromJson(net.minecraftforge.client.model.ModelLoaderRegistry.ExpandedBlockModelDeserializer.INSTANCE, pReader, BlockModel.class);
   }

   public static BlockModel fromString(String pJsonString) {
      return fromStream(new StringReader(pJsonString));
   }

   public BlockModel(@Nullable ResourceLocation p_i230056_1_, List<BlockPart> p_i230056_2_, Map<String, Either<RenderMaterial, String>> p_i230056_3_, boolean p_i230056_4_, @Nullable BlockModel.GuiLight p_i230056_5_, ItemCameraTransforms p_i230056_6_, List<ItemOverride> p_i230056_7_) {
      this.elements = p_i230056_2_;
      this.hasAmbientOcclusion = p_i230056_4_;
      this.guiLight = p_i230056_5_;
      this.textureMap = p_i230056_3_;
      this.parentLocation = p_i230056_1_;
      this.transforms = p_i230056_6_;
      this.overrides = p_i230056_7_;
   }

   @Deprecated
   public List<BlockPart> getElements() {
      if (customData.hasCustomGeometry()) return java.util.Collections.emptyList();
      return this.elements.isEmpty() && this.parent != null ? this.parent.getElements() : this.elements;
   }

   @Nullable
   public ResourceLocation getParentLocation() { return parentLocation; }

   public boolean hasAmbientOcclusion() {
      return this.parent != null ? this.parent.hasAmbientOcclusion() : this.hasAmbientOcclusion;
   }

   public BlockModel.GuiLight getGuiLight() {
      if (this.guiLight != null) {
         return this.guiLight;
      } else {
         return this.parent != null ? this.parent.getGuiLight() : BlockModel.GuiLight.SIDE;
      }
   }

   public List<ItemOverride> getOverrides() {
      return this.overrides;
   }

   private ItemOverrideList getItemOverrides(ModelBakery pModelBakery, BlockModel pModel) {
      return this.overrides.isEmpty() ? ItemOverrideList.EMPTY : new ItemOverrideList(pModelBakery, pModel, pModelBakery::getModel, this.overrides);
   }

   public ItemOverrideList getOverrides(ModelBakery pModelBakery, BlockModel pModel, Function<RenderMaterial, TextureAtlasSprite> textureGetter) {
      return this.overrides.isEmpty() ? ItemOverrideList.EMPTY : new ItemOverrideList(pModelBakery, pModel, pModelBakery::getModel, textureGetter, this.overrides);
   }

   public Collection<ResourceLocation> getDependencies() {
      Set<ResourceLocation> set = Sets.newHashSet();

      for(ItemOverride itemoverride : this.overrides) {
         set.add(itemoverride.getModel());
      }

      if (this.parentLocation != null) {
         set.add(this.parentLocation);
      }

      return set;
   }

   public Collection<RenderMaterial> getMaterials(Function<ResourceLocation, IUnbakedModel> pModelGetter, Set<Pair<String, String>> pMissingTextureErrors) {
      Set<IUnbakedModel> set = Sets.newLinkedHashSet();

      for(BlockModel blockmodel = this; blockmodel.parentLocation != null && blockmodel.parent == null; blockmodel = blockmodel.parent) {
         set.add(blockmodel);
         IUnbakedModel iunbakedmodel = pModelGetter.apply(blockmodel.parentLocation);
         if (iunbakedmodel == null) {
            LOGGER.warn("No parent '{}' while loading model '{}'", this.parentLocation, blockmodel);
         }

         if (set.contains(iunbakedmodel)) {
            LOGGER.warn("Found 'parent' loop while loading model '{}' in chain: {} -> {}", blockmodel, set.stream().map(Object::toString).collect(Collectors.joining(" -> ")), this.parentLocation);
            iunbakedmodel = null;
         }

         if (iunbakedmodel == null) {
            blockmodel.parentLocation = ModelBakery.MISSING_MODEL_LOCATION;
            iunbakedmodel = pModelGetter.apply(blockmodel.parentLocation);
         }

         if (!(iunbakedmodel instanceof BlockModel)) {
            throw new IllegalStateException("BlockModel parent has to be a block model.");
         }

         blockmodel.parent = (BlockModel)iunbakedmodel;
      }

      Set<RenderMaterial> set1 = Sets.newHashSet(this.getMaterial("particle"));

      if(customData.hasCustomGeometry())
         set1.addAll(customData.getTextureDependencies(pModelGetter, pMissingTextureErrors));
      else
      for(BlockPart blockpart : this.getElements()) {
         for(BlockPartFace blockpartface : blockpart.faces.values()) {
            RenderMaterial rendermaterial = this.getMaterial(blockpartface.texture);
            if (Objects.equals(rendermaterial.texture(), MissingTextureSprite.getLocation())) {
               pMissingTextureErrors.add(Pair.of(blockpartface.texture, this.name));
            }

            set1.add(rendermaterial);
         }
      }

      this.overrides.forEach((p_228815_4_) -> {
         IUnbakedModel iunbakedmodel1 = pModelGetter.apply(p_228815_4_.getModel());
         if (!Objects.equals(iunbakedmodel1, this)) {
            set1.addAll(iunbakedmodel1.getMaterials(pModelGetter, pMissingTextureErrors));
         }
      });
      if (this.getRootModel() == ModelBakery.GENERATION_MARKER) {
         ItemModelGenerator.LAYERS.forEach((p_228814_2_) -> {
            set1.add(this.getMaterial(p_228814_2_));
         });
      }

      return set1;
   }

   @Deprecated //Forge: Use Boolean variant
   public IBakedModel bake(ModelBakery pModelBakery, Function<RenderMaterial, TextureAtlasSprite> pSpriteGetter, IModelTransform pTransform, ResourceLocation pLocation) {
      return this.bake(pModelBakery, this, pSpriteGetter, pTransform, pLocation, true);
   }

   public IBakedModel bake(ModelBakery pModelBakery, BlockModel pModel, Function<RenderMaterial, TextureAtlasSprite> pSpriteGetter, IModelTransform pTransform, ResourceLocation pLocation, boolean pGuiLight3d) {
      return net.minecraftforge.client.model.ModelLoaderRegistry.bakeHelper(this, pModelBakery, pModel, pSpriteGetter, pTransform, pLocation, pGuiLight3d);
   }

   @Deprecated //Forge: exposed for our callbacks only. Use the above function.
   public IBakedModel bakeVanilla(ModelBakery pModelBakery, BlockModel pModel, Function<RenderMaterial, TextureAtlasSprite> pSpriteGetter, IModelTransform pTransform, ResourceLocation pLocation, boolean pGuiLight3d) {
      TextureAtlasSprite textureatlassprite = pSpriteGetter.apply(this.getMaterial("particle"));
      if (this.getRootModel() == ModelBakery.BLOCK_ENTITY_MARKER) {
         return new BuiltInModel(this.getTransforms(), this.getItemOverrides(pModelBakery, pModel), textureatlassprite, this.getGuiLight().lightLikeBlock());
      } else {
         SimpleBakedModel.Builder simplebakedmodel$builder = (new SimpleBakedModel.Builder(this, this.getItemOverrides(pModelBakery, pModel), pGuiLight3d)).particle(textureatlassprite);

         for(BlockPart blockpart : this.getElements()) {
            for(Direction direction : blockpart.faces.keySet()) {
               BlockPartFace blockpartface = blockpart.faces.get(direction);
               TextureAtlasSprite textureatlassprite1 = pSpriteGetter.apply(this.getMaterial(blockpartface.texture));
               if (blockpartface.cullForDirection == null) {
                  simplebakedmodel$builder.addUnculledFace(bakeFace(blockpart, blockpartface, textureatlassprite1, direction, pTransform, pLocation));
               } else {
                  simplebakedmodel$builder.addCulledFace(Direction.rotate(pTransform.getRotation().getMatrix(), blockpartface.cullForDirection), bakeFace(blockpart, blockpartface, textureatlassprite1, direction, pTransform, pLocation));
               }
            }
         }

         return simplebakedmodel$builder.build();
      }
   }

   private static BakedQuad bakeFace(BlockPart pPart, BlockPartFace pPartFace, TextureAtlasSprite pSprite, Direction pDirection, IModelTransform pTransform, ResourceLocation pLocation) {
      return FACE_BAKERY.bakeQuad(pPart.from, pPart.to, pPartFace, pSprite, pDirection, pTransform, pPart.rotation, pPart.shade, pLocation);
   }

   public static BakedQuad makeBakedQuad(BlockPart pPart, BlockPartFace pPartFace, TextureAtlasSprite pSprite, Direction pDirection, IModelTransform pTransform, ResourceLocation pLocation) {
      return bakeFace(pPart, pPartFace, pSprite, pDirection, pTransform, pLocation);
   }

   public boolean hasTexture(String pTextureName) {
      return !MissingTextureSprite.getLocation().equals(this.getMaterial(pTextureName).texture());
   }

   public RenderMaterial getMaterial(String pName) {
      if (isTextureReference(pName)) {
         pName = pName.substring(1);
      }

      List<String> list = Lists.newArrayList();

      while(true) {
         Either<RenderMaterial, String> either = this.findTextureEntry(pName);
         Optional<RenderMaterial> optional = either.left();
         if (optional.isPresent()) {
            return optional.get();
         }

         pName = either.right().get();
         if (list.contains(pName)) {
            LOGGER.warn("Unable to resolve texture due to reference chain {}->{} in {}", Joiner.on("->").join(list), pName, this.name);
            return new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, MissingTextureSprite.getLocation());
         }

         list.add(pName);
      }
   }

   private Either<RenderMaterial, String> findTextureEntry(String pName) {
      for(BlockModel blockmodel = this; blockmodel != null; blockmodel = blockmodel.parent) {
         Either<RenderMaterial, String> either = blockmodel.textureMap.get(pName);
         if (either != null) {
            return either;
         }
      }

      return Either.left(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, MissingTextureSprite.getLocation()));
   }

   private static boolean isTextureReference(String pStr) {
      return pStr.charAt(0) == '#';
   }

   public BlockModel getRootModel() {
      return this.parent == null ? this : this.parent.getRootModel();
   }

   public ItemCameraTransforms getTransforms() {
      ItemTransformVec3f itemtransformvec3f = this.getTransform(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND);
      ItemTransformVec3f itemtransformvec3f1 = this.getTransform(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND);
      ItemTransformVec3f itemtransformvec3f2 = this.getTransform(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND);
      ItemTransformVec3f itemtransformvec3f3 = this.getTransform(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND);
      ItemTransformVec3f itemtransformvec3f4 = this.getTransform(ItemCameraTransforms.TransformType.HEAD);
      ItemTransformVec3f itemtransformvec3f5 = this.getTransform(ItemCameraTransforms.TransformType.GUI);
      ItemTransformVec3f itemtransformvec3f6 = this.getTransform(ItemCameraTransforms.TransformType.GROUND);
      ItemTransformVec3f itemtransformvec3f7 = this.getTransform(ItemCameraTransforms.TransformType.FIXED);
      return new ItemCameraTransforms(itemtransformvec3f, itemtransformvec3f1, itemtransformvec3f2, itemtransformvec3f3, itemtransformvec3f4, itemtransformvec3f5, itemtransformvec3f6, itemtransformvec3f7);
   }

   private ItemTransformVec3f getTransform(ItemCameraTransforms.TransformType pType) {
      return this.parent != null && !this.transforms.hasTransform(pType) ? this.parent.getTransform(pType) : this.transforms.getTransform(pType);
   }

   public String toString() {
      return this.name;
   }

   @OnlyIn(Dist.CLIENT)
   public static class Deserializer implements JsonDeserializer<BlockModel> {
      public BlockModel deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {
         JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
         List<BlockPart> list = this.getElements(p_deserialize_3_, jsonobject);
         String s = this.getParentName(jsonobject);
         Map<String, Either<RenderMaterial, String>> map = this.getTextureMap(jsonobject);
         boolean flag = this.getAmbientOcclusion(jsonobject);
         ItemCameraTransforms itemcameratransforms = ItemCameraTransforms.NO_TRANSFORMS;
         if (jsonobject.has("display")) {
            JsonObject jsonobject1 = JSONUtils.getAsJsonObject(jsonobject, "display");
            itemcameratransforms = p_deserialize_3_.deserialize(jsonobject1, ItemCameraTransforms.class);
         }

         List<ItemOverride> list1 = this.getOverrides(p_deserialize_3_, jsonobject);
         BlockModel.GuiLight blockmodel$guilight = null;
         if (jsonobject.has("gui_light")) {
            blockmodel$guilight = BlockModel.GuiLight.getByName(JSONUtils.getAsString(jsonobject, "gui_light"));
         }

         ResourceLocation resourcelocation = s.isEmpty() ? null : new ResourceLocation(s);
         return new BlockModel(resourcelocation, list, map, flag, blockmodel$guilight, itemcameratransforms, list1);
      }

      protected List<ItemOverride> getOverrides(JsonDeserializationContext pDeserializationContext, JsonObject pObject) {
         List<ItemOverride> list = Lists.newArrayList();
         if (pObject.has("overrides")) {
            for(JsonElement jsonelement : JSONUtils.getAsJsonArray(pObject, "overrides")) {
               list.add(pDeserializationContext.deserialize(jsonelement, ItemOverride.class));
            }
         }

         return list;
      }

      private Map<String, Either<RenderMaterial, String>> getTextureMap(JsonObject pObject) {
         ResourceLocation resourcelocation = AtlasTexture.LOCATION_BLOCKS;
         Map<String, Either<RenderMaterial, String>> map = Maps.newHashMap();
         if (pObject.has("textures")) {
            JsonObject jsonobject = JSONUtils.getAsJsonObject(pObject, "textures");

            for(Entry<String, JsonElement> entry : jsonobject.entrySet()) {
               map.put(entry.getKey(), parseTextureLocationOrReference(resourcelocation, entry.getValue().getAsString()));
            }
         }

         return map;
      }

      private static Either<RenderMaterial, String> parseTextureLocationOrReference(ResourceLocation pLocation, String pName) {
         if (BlockModel.isTextureReference(pName)) {
            return Either.right(pName.substring(1));
         } else {
            ResourceLocation resourcelocation = ResourceLocation.tryParse(pName);
            if (resourcelocation == null) {
               throw new JsonParseException(pName + " is not valid resource location");
            } else {
               return Either.left(new RenderMaterial(pLocation, resourcelocation));
            }
         }
      }

      private String getParentName(JsonObject pObject) {
         return JSONUtils.getAsString(pObject, "parent", "");
      }

      protected boolean getAmbientOcclusion(JsonObject pObject) {
         return JSONUtils.getAsBoolean(pObject, "ambientocclusion", true);
      }

      protected List<BlockPart> getElements(JsonDeserializationContext pDeserializationContext, JsonObject pObject) {
         List<BlockPart> list = Lists.newArrayList();
         if (pObject.has("elements")) {
            for(JsonElement jsonelement : JSONUtils.getAsJsonArray(pObject, "elements")) {
               list.add(pDeserializationContext.deserialize(jsonelement, BlockPart.class));
            }
         }

         return list;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static enum GuiLight {
      FRONT("front"),
      SIDE("side");

      private final String name;

      private GuiLight(String p_i230057_3_) {
         this.name = p_i230057_3_;
      }

      public static BlockModel.GuiLight getByName(String pName) {
         for(BlockModel.GuiLight blockmodel$guilight : values()) {
            if (blockmodel$guilight.name.equals(pName)) {
               return blockmodel$guilight;
            }
         }

         throw new IllegalArgumentException("Invalid gui light: " + pName);
      }

      public boolean lightLikeBlock() {
         return this == SIDE;
      }
      
      public String getSerializedName() { return name; }
   }
}
