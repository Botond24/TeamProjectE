package net.minecraft.data;

import com.google.gson.JsonElement;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

public class TexturedModel {
   public static final TexturedModel.ISupplier CUBE = createDefault(ModelTextures::cube, StockModelShapes.CUBE_ALL);
   public static final TexturedModel.ISupplier CUBE_MIRRORED = createDefault(ModelTextures::cube, StockModelShapes.CUBE_MIRRORED_ALL);
   public static final TexturedModel.ISupplier COLUMN = createDefault(ModelTextures::column, StockModelShapes.CUBE_COLUMN);
   public static final TexturedModel.ISupplier COLUMN_HORIZONTAL = createDefault(ModelTextures::column, StockModelShapes.CUBE_COLUMN_HORIZONTAL);
   public static final TexturedModel.ISupplier CUBE_TOP_BOTTOM = createDefault(ModelTextures::cubeBottomTop, StockModelShapes.CUBE_BOTTOM_TOP);
   public static final TexturedModel.ISupplier CUBE_TOP = createDefault(ModelTextures::cubeTop, StockModelShapes.CUBE_TOP);
   public static final TexturedModel.ISupplier ORIENTABLE_ONLY_TOP = createDefault(ModelTextures::orientableCubeOnlyTop, StockModelShapes.CUBE_ORIENTABLE);
   public static final TexturedModel.ISupplier ORIENTABLE = createDefault(ModelTextures::orientableCube, StockModelShapes.CUBE_ORIENTABLE_TOP_BOTTOM);
   public static final TexturedModel.ISupplier CARPET = createDefault(ModelTextures::wool, StockModelShapes.CARPET);
   public static final TexturedModel.ISupplier GLAZED_TERRACOTTA = createDefault(ModelTextures::pattern, StockModelShapes.GLAZED_TERRACOTTA);
   public static final TexturedModel.ISupplier CORAL_FAN = createDefault(ModelTextures::fan, StockModelShapes.CORAL_FAN);
   public static final TexturedModel.ISupplier PARTICLE_ONLY = createDefault(ModelTextures::particle, StockModelShapes.PARTICLE_ONLY);
   public static final TexturedModel.ISupplier ANVIL = createDefault(ModelTextures::top, StockModelShapes.ANVIL);
   public static final TexturedModel.ISupplier LEAVES = createDefault(ModelTextures::cube, StockModelShapes.LEAVES);
   public static final TexturedModel.ISupplier LANTERN = createDefault(ModelTextures::lantern, StockModelShapes.LANTERN);
   public static final TexturedModel.ISupplier HANGING_LANTERN = createDefault(ModelTextures::lantern, StockModelShapes.HANGING_LANTERN);
   public static final TexturedModel.ISupplier SEAGRASS = createDefault(ModelTextures::defaultTexture, StockModelShapes.SEAGRASS);
   public static final TexturedModel.ISupplier COLUMN_ALT = createDefault(ModelTextures::logColumn, StockModelShapes.CUBE_COLUMN);
   public static final TexturedModel.ISupplier COLUMN_HORIZONTAL_ALT = createDefault(ModelTextures::logColumn, StockModelShapes.CUBE_COLUMN_HORIZONTAL);
   public static final TexturedModel.ISupplier TOP_BOTTOM_WITH_WALL = createDefault(ModelTextures::cubeBottomTopWithWall, StockModelShapes.CUBE_BOTTOM_TOP);
   public static final TexturedModel.ISupplier COLUMN_WITH_WALL = createDefault(ModelTextures::columnWithWall, StockModelShapes.CUBE_COLUMN);
   private final ModelTextures mapping;
   private final ModelsUtil template;

   private TexturedModel(ModelTextures pMapping, ModelsUtil pTemplate) {
      this.mapping = pMapping;
      this.template = pTemplate;
   }

   public ModelsUtil getTemplate() {
      return this.template;
   }

   public ModelTextures getMapping() {
      return this.mapping;
   }

   public TexturedModel updateTextures(Consumer<ModelTextures> pTextureMappingConsumer) {
      pTextureMappingConsumer.accept(this.mapping);
      return this;
   }

   public ResourceLocation create(Block pModelBlock, BiConsumer<ResourceLocation, Supplier<JsonElement>> pModelOutput) {
      return this.template.create(pModelBlock, this.mapping, pModelOutput);
   }

   public ResourceLocation createWithSuffix(Block pModelBlock, String pModelLocationSuffix, BiConsumer<ResourceLocation, Supplier<JsonElement>> pModelOutput) {
      return this.template.createWithSuffix(pModelBlock, pModelLocationSuffix, this.mapping, pModelOutput);
   }

   private static TexturedModel.ISupplier createDefault(Function<Block, ModelTextures> pBlockToTextureMapping, ModelsUtil pModelTemplate) {
      return (p_240462_2_) -> {
         return new TexturedModel(pBlockToTextureMapping.apply(p_240462_2_), pModelTemplate);
      };
   }

   public static TexturedModel createAllSame(ResourceLocation pAllTextureLocation) {
      return new TexturedModel(ModelTextures.cube(pAllTextureLocation), StockModelShapes.CUBE_ALL);
   }

   @FunctionalInterface
   public interface ISupplier {
      TexturedModel get(Block p_get_1_);

      default ResourceLocation create(Block pModelBlock, BiConsumer<ResourceLocation, Supplier<JsonElement>> pModelOutput) {
         return this.get(pModelBlock).create(pModelBlock, pModelOutput);
      }

      default ResourceLocation createWithSuffix(Block pModelBlock, String pModelLocationSuffix, BiConsumer<ResourceLocation, Supplier<JsonElement>> pModelOutput) {
         return this.get(pModelBlock).createWithSuffix(pModelBlock, pModelLocationSuffix, pModelOutput);
      }

      default TexturedModel.ISupplier updateTexture(Consumer<ModelTextures> pTextureMappingConsumer) {
         return (p_240468_2_) -> {
            return this.get(p_240468_2_).updateTextures(pTextureMappingConsumer);
         };
      }
   }
}