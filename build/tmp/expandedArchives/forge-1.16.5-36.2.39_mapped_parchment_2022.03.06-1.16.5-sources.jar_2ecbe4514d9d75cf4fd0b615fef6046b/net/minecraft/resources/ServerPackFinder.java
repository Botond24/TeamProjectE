package net.minecraft.resources;

import java.util.function.Consumer;

public class ServerPackFinder implements IPackFinder {
   private final VanillaPack vanillaPack = new VanillaPack("minecraft");

   public void loadPacks(Consumer<ResourcePackInfo> pInfoConsumer, ResourcePackInfo.IFactory pInfoFactory) {
      ResourcePackInfo resourcepackinfo = ResourcePackInfo.create("vanilla", false, () -> {
         return this.vanillaPack;
      }, pInfoFactory, ResourcePackInfo.Priority.BOTTOM, IPackNameDecorator.BUILT_IN);
      if (resourcepackinfo != null) {
         pInfoConsumer.accept(resourcepackinfo);
      }

   }
}