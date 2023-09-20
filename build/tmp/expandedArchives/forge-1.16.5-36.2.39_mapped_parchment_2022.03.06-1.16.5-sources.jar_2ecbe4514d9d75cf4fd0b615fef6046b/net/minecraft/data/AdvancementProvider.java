package net.minecraft.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.advancements.AdventureAdvancements;
import net.minecraft.data.advancements.EndAdvancements;
import net.minecraft.data.advancements.HusbandryAdvancements;
import net.minecraft.data.advancements.NetherAdvancements;
import net.minecraft.data.advancements.StoryAdvancements;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AdvancementProvider implements IDataProvider {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private final DataGenerator generator;
   private final List<Consumer<Consumer<Advancement>>> tabs = ImmutableList.of(new EndAdvancements(), new HusbandryAdvancements(), new AdventureAdvancements(), new NetherAdvancements(), new StoryAdvancements());
   protected net.minecraftforge.common.data.ExistingFileHelper fileHelper;

   @Deprecated
   public AdvancementProvider(DataGenerator pGenerator) {
      this.generator = pGenerator;
   }

   public AdvancementProvider(DataGenerator generatorIn, net.minecraftforge.common.data.ExistingFileHelper fileHelperIn) {
      this.generator = generatorIn;
      this.fileHelper = fileHelperIn;
   }

   /**
    * Performs this provider's action.
    */
   public void run(DirectoryCache pCache) throws IOException {
      Path path = this.generator.getOutputFolder();
      Set<ResourceLocation> set = Sets.newHashSet();
      Consumer<Advancement> consumer = (p_204017_3_) -> {
         if (!set.add(p_204017_3_.getId())) {
            throw new IllegalStateException("Duplicate advancement " + p_204017_3_.getId());
         } else {
            Path path1 = createPath(path, p_204017_3_);

            try {
               IDataProvider.save(GSON, pCache, p_204017_3_.deconstruct().serializeToJson(), path1);
            } catch (IOException ioexception) {
               LOGGER.error("Couldn't save advancement {}", path1, ioexception);
            }

         }
      };

      registerAdvancements(consumer, fileHelper);
   }

   /**
    * Override this method for registering and generating custom {@link Advancement}s. <p>
    * Just use {@link Advancement.Builder} to build your Advancements, you don't need an extra consumer like the vanilla classes.
    * @param consumer used for the register function from {@link Advancement.Builder}
    * @param fileHelper used for the register function from {@link Advancement.Builder}
    */
   protected void registerAdvancements(Consumer<Advancement> consumer, net.minecraftforge.common.data.ExistingFileHelper fileHelper) {
      for(Consumer<Consumer<Advancement>> consumer1 : this.tabs) {
         consumer1.accept(consumer);
      }

   }

   private static Path createPath(Path pPath, Advancement pAdvancement) {
      return pPath.resolve("data/" + pAdvancement.getId().getNamespace() + "/advancements/" + pAdvancement.getId().getPath() + ".json");
   }

   /**
    * Gets a name for this provider, to use in logging.
    */
   public String getName() {
      return "Advancements";
   }
}
