package net.minecraft.client.multiplayer;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.AdvancementToast;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.play.client.CSeenAdvancementsPacket;
import net.minecraft.network.play.server.SAdvancementInfoPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientAdvancementManager {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Minecraft minecraft;
   private final AdvancementList advancements = new AdvancementList();
   private final Map<Advancement, AdvancementProgress> progress = Maps.newHashMap();
   @Nullable
   private ClientAdvancementManager.IListener listener;
   @Nullable
   private Advancement selectedTab;

   public ClientAdvancementManager(Minecraft p_i47380_1_) {
      this.minecraft = p_i47380_1_;
   }

   public void update(SAdvancementInfoPacket pPacket) {
      if (pPacket.shouldReset()) {
         this.advancements.clear();
         this.progress.clear();
      }

      this.advancements.remove(pPacket.getRemoved());
      this.advancements.add(pPacket.getAdded());

      for(Entry<ResourceLocation, AdvancementProgress> entry : pPacket.getProgress().entrySet()) {
         Advancement advancement = this.advancements.get(entry.getKey());
         if (advancement != null) {
            AdvancementProgress advancementprogress = entry.getValue();
            advancementprogress.update(advancement.getCriteria(), advancement.getRequirements());
            this.progress.put(advancement, advancementprogress);
            if (this.listener != null) {
               this.listener.onUpdateAdvancementProgress(advancement, advancementprogress);
            }

            if (!pPacket.shouldReset() && advancementprogress.isDone() && advancement.getDisplay() != null && advancement.getDisplay().shouldShowToast()) {
               this.minecraft.getToasts().addToast(new AdvancementToast(advancement));
            }
         } else {
            LOGGER.warn("Server informed client about progress for unknown advancement {}", entry.getKey());
         }
      }

   }

   public AdvancementList getAdvancements() {
      return this.advancements;
   }

   public void setSelectedTab(@Nullable Advancement pAdvancement, boolean pTellServer) {
      ClientPlayNetHandler clientplaynethandler = this.minecraft.getConnection();
      if (clientplaynethandler != null && pAdvancement != null && pTellServer) {
         clientplaynethandler.send(CSeenAdvancementsPacket.openedTab(pAdvancement));
      }

      if (this.selectedTab != pAdvancement) {
         this.selectedTab = pAdvancement;
         if (this.listener != null) {
            this.listener.onSelectedTabChanged(pAdvancement);
         }
      }

   }

   public void setListener(@Nullable ClientAdvancementManager.IListener pListener) {
      this.listener = pListener;
      this.advancements.setListener(pListener);
      if (pListener != null) {
         for(Entry<Advancement, AdvancementProgress> entry : this.progress.entrySet()) {
            pListener.onUpdateAdvancementProgress(entry.getKey(), entry.getValue());
         }

         pListener.onSelectedTabChanged(this.selectedTab);
      }

   }

   @OnlyIn(Dist.CLIENT)
   public interface IListener extends AdvancementList.IListener {
      void onUpdateAdvancementProgress(Advancement pAdvancement, AdvancementProgress pProgress);

      void onSelectedTabChanged(@Nullable Advancement pAdvancement);
   }
}