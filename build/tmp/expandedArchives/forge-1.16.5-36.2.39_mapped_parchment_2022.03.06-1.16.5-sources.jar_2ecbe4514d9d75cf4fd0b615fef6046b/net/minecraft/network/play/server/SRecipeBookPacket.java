package net.minecraft.network.play.server;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.item.crafting.RecipeBookStatus;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SRecipeBookPacket implements IPacket<IClientPlayNetHandler> {
   private SRecipeBookPacket.State state;
   private List<ResourceLocation> recipes;
   private List<ResourceLocation> toHighlight;
   private RecipeBookStatus bookSettings;

   public SRecipeBookPacket() {
   }

   public SRecipeBookPacket(SRecipeBookPacket.State pState, Collection<ResourceLocation> pRecipes, Collection<ResourceLocation> pToHighlight, RecipeBookStatus pBookSettings) {
      this.state = pState;
      this.recipes = ImmutableList.copyOf(pRecipes);
      this.toHighlight = ImmutableList.copyOf(pToHighlight);
      this.bookSettings = pBookSettings;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleAddOrRemoveRecipes(this);
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.state = p_148837_1_.readEnum(SRecipeBookPacket.State.class);
      this.bookSettings = RecipeBookStatus.read(p_148837_1_);
      int i = p_148837_1_.readVarInt();
      this.recipes = Lists.newArrayList();

      for(int j = 0; j < i; ++j) {
         this.recipes.add(p_148837_1_.readResourceLocation());
      }

      if (this.state == SRecipeBookPacket.State.INIT) {
         i = p_148837_1_.readVarInt();
         this.toHighlight = Lists.newArrayList();

         for(int k = 0; k < i; ++k) {
            this.toHighlight.add(p_148837_1_.readResourceLocation());
         }
      }

   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeEnum(this.state);
      this.bookSettings.write(pBuffer);
      pBuffer.writeVarInt(this.recipes.size());

      for(ResourceLocation resourcelocation : this.recipes) {
         pBuffer.writeResourceLocation(resourcelocation);
      }

      if (this.state == SRecipeBookPacket.State.INIT) {
         pBuffer.writeVarInt(this.toHighlight.size());

         for(ResourceLocation resourcelocation1 : this.toHighlight) {
            pBuffer.writeResourceLocation(resourcelocation1);
         }
      }

   }

   @OnlyIn(Dist.CLIENT)
   public List<ResourceLocation> getRecipes() {
      return this.recipes;
   }

   @OnlyIn(Dist.CLIENT)
   public List<ResourceLocation> getHighlights() {
      return this.toHighlight;
   }

   @OnlyIn(Dist.CLIENT)
   public RecipeBookStatus getBookSettings() {
      return this.bookSettings;
   }

   @OnlyIn(Dist.CLIENT)
   public SRecipeBookPacket.State getState() {
      return this.state;
   }

   public static enum State {
      INIT,
      ADD,
      REMOVE;
   }
}