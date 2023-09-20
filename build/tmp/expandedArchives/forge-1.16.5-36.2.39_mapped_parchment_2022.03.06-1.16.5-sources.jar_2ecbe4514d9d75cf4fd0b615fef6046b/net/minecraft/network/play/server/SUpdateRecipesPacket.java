package net.minecraft.network.play.server;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SUpdateRecipesPacket implements IPacket<IClientPlayNetHandler> {
   private List<IRecipe<?>> recipes;

   public SUpdateRecipesPacket() {
   }

   public SUpdateRecipesPacket(Collection<IRecipe<?>> pRecipes) {
      this.recipes = Lists.newArrayList(pRecipes);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleUpdateRecipes(this);
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.recipes = Lists.newArrayList();
      int i = p_148837_1_.readVarInt();

      for(int j = 0; j < i; ++j) {
         this.recipes.add(fromNetwork(p_148837_1_));
      }

   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.recipes.size());

      for(IRecipe<?> irecipe : this.recipes) {
         toNetwork(irecipe, pBuffer);
      }

   }

   @OnlyIn(Dist.CLIENT)
   public List<IRecipe<?>> getRecipes() {
      return this.recipes;
   }

   public static IRecipe<?> fromNetwork(PacketBuffer pBuffer) {
      ResourceLocation resourcelocation = pBuffer.readResourceLocation();
      ResourceLocation resourcelocation1 = pBuffer.readResourceLocation();
      return Registry.RECIPE_SERIALIZER.getOptional(resourcelocation).orElseThrow(() -> {
         return new IllegalArgumentException("Unknown recipe serializer " + resourcelocation);
      }).fromNetwork(resourcelocation1, pBuffer);
   }

   public static <T extends IRecipe<?>> void toNetwork(T p_218771_0_, PacketBuffer p_218771_1_) {
      p_218771_1_.writeResourceLocation(Registry.RECIPE_SERIALIZER.getKey(p_218771_0_.getSerializer()));
      p_218771_1_.writeResourceLocation(p_218771_0_.getId());
      ((net.minecraft.item.crafting.IRecipeSerializer<T>)p_218771_0_.getSerializer()).toNetwork(p_218771_1_, p_218771_0_);
   }
}