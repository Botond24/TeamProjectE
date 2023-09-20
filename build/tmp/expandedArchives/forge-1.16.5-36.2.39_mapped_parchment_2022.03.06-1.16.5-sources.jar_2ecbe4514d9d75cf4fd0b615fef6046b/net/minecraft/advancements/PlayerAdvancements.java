package net.minecraft.advancements;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SAdvancementInfoPacket;
import net.minecraft.network.play.server.SSelectAdvancementsTabPacket;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DefaultTypeReferences;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlayerAdvancements {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(AdvancementProgress.class, new AdvancementProgress.Serializer()).registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).setPrettyPrinting().create();
   private static final TypeToken<Map<ResourceLocation, AdvancementProgress>> TYPE_TOKEN = new TypeToken<Map<ResourceLocation, AdvancementProgress>>() {
   };
   private final DataFixer dataFixer;
   private final PlayerList playerList;
   private final File file;
   private final Map<Advancement, AdvancementProgress> advancements = Maps.newLinkedHashMap();
   private final Set<Advancement> visible = Sets.newLinkedHashSet();
   private final Set<Advancement> visibilityChanged = Sets.newLinkedHashSet();
   private final Set<Advancement> progressChanged = Sets.newLinkedHashSet();
   private ServerPlayerEntity player;
   @Nullable
   private Advancement lastSelectedTab;
   private boolean isFirstPacket = true;

   public PlayerAdvancements(DataFixer p_i232594_1_, PlayerList p_i232594_2_, AdvancementManager p_i232594_3_, File p_i232594_4_, ServerPlayerEntity p_i232594_5_) {
      this.dataFixer = p_i232594_1_;
      this.playerList = p_i232594_2_;
      this.file = p_i232594_4_;
      this.player = p_i232594_5_;
      this.load(p_i232594_3_);
   }

   public void setPlayer(ServerPlayerEntity pPlayer) {
      this.player = pPlayer;
   }

   public void stopListening() {
      for(ICriterionTrigger<?> icriteriontrigger : CriteriaTriggers.all()) {
         icriteriontrigger.removePlayerListeners(this);
      }

   }

   public void reload(AdvancementManager pManager) {
      this.stopListening();
      this.advancements.clear();
      this.visible.clear();
      this.visibilityChanged.clear();
      this.progressChanged.clear();
      this.isFirstPacket = true;
      this.lastSelectedTab = null;
      this.load(pManager);
   }

   private void registerListeners(AdvancementManager pManager) {
      for(Advancement advancement : pManager.getAllAdvancements()) {
         this.registerListeners(advancement);
      }

   }

   private void ensureAllVisible() {
      List<Advancement> list = Lists.newArrayList();

      for(Entry<Advancement, AdvancementProgress> entry : this.advancements.entrySet()) {
         if (entry.getValue().isDone()) {
            list.add(entry.getKey());
            this.progressChanged.add(entry.getKey());
         }
      }

      for(Advancement advancement : list) {
         this.ensureVisibility(advancement);
      }

   }

   private void checkForAutomaticTriggers(AdvancementManager pManager) {
      for(Advancement advancement : pManager.getAllAdvancements()) {
         if (advancement.getCriteria().isEmpty()) {
            this.award(advancement, "");
            advancement.getRewards().grant(this.player);
         }
      }

   }

   private void load(AdvancementManager pManager) {
      if (this.file.isFile()) {
         try (JsonReader jsonreader = new JsonReader(new StringReader(Files.toString(this.file, StandardCharsets.UTF_8)))) {
            jsonreader.setLenient(false);
            Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, Streams.parse(jsonreader));
            if (!dynamic.get("DataVersion").asNumber().result().isPresent()) {
               dynamic = dynamic.set("DataVersion", dynamic.createInt(1343));
            }

            dynamic = this.dataFixer.update(DefaultTypeReferences.ADVANCEMENTS.getType(), dynamic, dynamic.get("DataVersion").asInt(0), SharedConstants.getCurrentVersion().getWorldVersion());
            dynamic = dynamic.remove("DataVersion");
            Map<ResourceLocation, AdvancementProgress> map = GSON.getAdapter(TYPE_TOKEN).fromJsonTree(dynamic.getValue());
            if (map == null) {
               throw new JsonParseException("Found null for advancements");
            }

            Stream<Entry<ResourceLocation, AdvancementProgress>> stream = map.entrySet().stream().sorted(Comparator.comparing(Entry::getValue));

            for(Entry<ResourceLocation, AdvancementProgress> entry : stream.collect(Collectors.toList())) {
               Advancement advancement = pManager.getAdvancement(entry.getKey());
               if (advancement == null) {
                  LOGGER.warn("Ignored advancement '{}' in progress file {} - it doesn't exist anymore?", entry.getKey(), this.file);
               } else {
                  this.startProgress(advancement, entry.getValue());
               }
            }
         } catch (JsonParseException jsonparseexception) {
            LOGGER.error("Couldn't parse player advancements in {}", this.file, jsonparseexception);
         } catch (IOException ioexception) {
            LOGGER.error("Couldn't access player advancements in {}", this.file, ioexception);
         }
      }

      this.checkForAutomaticTriggers(pManager);

      if (net.minecraftforge.common.ForgeConfig.SERVER.fixAdvancementLoading.get())
         net.minecraftforge.common.AdvancementLoadFix.loadVisibility(this, this.visible, this.visibilityChanged, this.advancements, this.progressChanged, this::shouldBeVisible);
      else
      this.ensureAllVisible();
      this.registerListeners(pManager);
   }

   public void save() {
      Map<ResourceLocation, AdvancementProgress> map = Maps.newHashMap();

      for(Entry<Advancement, AdvancementProgress> entry : this.advancements.entrySet()) {
         AdvancementProgress advancementprogress = entry.getValue();
         if (advancementprogress.hasProgress()) {
            map.put(entry.getKey().getId(), advancementprogress);
         }
      }

      if (this.file.getParentFile() != null) {
         this.file.getParentFile().mkdirs();
      }

      JsonElement jsonelement = GSON.toJsonTree(map);
      jsonelement.getAsJsonObject().addProperty("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());

      try (
         OutputStream outputstream = new FileOutputStream(this.file);
         Writer writer = new OutputStreamWriter(outputstream, Charsets.UTF_8.newEncoder());
      ) {
         GSON.toJson(jsonelement, writer);
      } catch (IOException ioexception) {
         LOGGER.error("Couldn't save player advancements to {}", this.file, ioexception);
      }

   }

   public boolean award(Advancement pAdvancement, String pCriterionKey) {
      // Forge: don't grant advancements for fake players
      if (this.player instanceof net.minecraftforge.common.util.FakePlayer) return false;
      boolean flag = false;
      AdvancementProgress advancementprogress = this.getOrStartProgress(pAdvancement);
      boolean flag1 = advancementprogress.isDone();
      if (advancementprogress.grantProgress(pCriterionKey)) {
         this.unregisterListeners(pAdvancement);
         this.progressChanged.add(pAdvancement);
         flag = true;
         if (!flag1 && advancementprogress.isDone()) {
            pAdvancement.getRewards().grant(this.player);
            if (pAdvancement.getDisplay() != null && pAdvancement.getDisplay().shouldAnnounceChat() && this.player.level.getGameRules().getBoolean(GameRules.RULE_ANNOUNCE_ADVANCEMENTS)) {
               this.playerList.broadcastMessage(new TranslationTextComponent("chat.type.advancement." + pAdvancement.getDisplay().getFrame().getName(), this.player.getDisplayName(), pAdvancement.getChatComponent()), ChatType.SYSTEM, Util.NIL_UUID);
            }
            net.minecraftforge.common.ForgeHooks.onAdvancement(this.player, pAdvancement);
         }
      }

      if (advancementprogress.isDone()) {
         this.ensureVisibility(pAdvancement);
      }

      return flag;
   }

   public boolean revoke(Advancement pAdvancement, String pCriterionKey) {
      boolean flag = false;
      AdvancementProgress advancementprogress = this.getOrStartProgress(pAdvancement);
      if (advancementprogress.revokeProgress(pCriterionKey)) {
         this.registerListeners(pAdvancement);
         this.progressChanged.add(pAdvancement);
         flag = true;
      }

      if (!advancementprogress.hasProgress()) {
         this.ensureVisibility(pAdvancement);
      }

      return flag;
   }

   private void registerListeners(Advancement pAdvancement) {
      AdvancementProgress advancementprogress = this.getOrStartProgress(pAdvancement);
      if (!advancementprogress.isDone()) {
         for(Entry<String, Criterion> entry : pAdvancement.getCriteria().entrySet()) {
            CriterionProgress criterionprogress = advancementprogress.getCriterion(entry.getKey());
            if (criterionprogress != null && !criterionprogress.isDone()) {
               ICriterionInstance icriterioninstance = entry.getValue().getTrigger();
               if (icriterioninstance != null) {
                  ICriterionTrigger<ICriterionInstance> icriteriontrigger = CriteriaTriggers.getCriterion(icriterioninstance.getCriterion());
                  if (icriteriontrigger != null) {
                     icriteriontrigger.addPlayerListener(this, new ICriterionTrigger.Listener<>(icriterioninstance, pAdvancement, entry.getKey()));
                  }
               }
            }
         }

      }
   }

   private void unregisterListeners(Advancement pAdvancement) {
      AdvancementProgress advancementprogress = this.getOrStartProgress(pAdvancement);

      for(Entry<String, Criterion> entry : pAdvancement.getCriteria().entrySet()) {
         CriterionProgress criterionprogress = advancementprogress.getCriterion(entry.getKey());
         if (criterionprogress != null && (criterionprogress.isDone() || advancementprogress.isDone())) {
            ICriterionInstance icriterioninstance = entry.getValue().getTrigger();
            if (icriterioninstance != null) {
               ICriterionTrigger<ICriterionInstance> icriteriontrigger = CriteriaTriggers.getCriterion(icriterioninstance.getCriterion());
               if (icriteriontrigger != null) {
                  icriteriontrigger.removePlayerListener(this, new ICriterionTrigger.Listener<>(icriterioninstance, pAdvancement, entry.getKey()));
               }
            }
         }
      }

   }

   public void flushDirty(ServerPlayerEntity pServerPlayer) {
      if (this.isFirstPacket || !this.visibilityChanged.isEmpty() || !this.progressChanged.isEmpty()) {
         Map<ResourceLocation, AdvancementProgress> map = Maps.newHashMap();
         Set<Advancement> set = Sets.newLinkedHashSet();
         Set<ResourceLocation> set1 = Sets.newLinkedHashSet();

         for(Advancement advancement : this.progressChanged) {
            if (this.visible.contains(advancement)) {
               map.put(advancement.getId(), this.advancements.get(advancement));
            }
         }

         for(Advancement advancement1 : this.visibilityChanged) {
            if (this.visible.contains(advancement1)) {
               set.add(advancement1);
            } else {
               set1.add(advancement1.getId());
            }
         }

         if (this.isFirstPacket || !map.isEmpty() || !set.isEmpty() || !set1.isEmpty()) {
            pServerPlayer.connection.send(new SAdvancementInfoPacket(this.isFirstPacket, set, set1, map));
            this.visibilityChanged.clear();
            this.progressChanged.clear();
         }
      }

      this.isFirstPacket = false;
   }

   public void setSelectedTab(@Nullable Advancement pAdvancement) {
      Advancement advancement = this.lastSelectedTab;
      if (pAdvancement != null && pAdvancement.getParent() == null && pAdvancement.getDisplay() != null) {
         this.lastSelectedTab = pAdvancement;
      } else {
         this.lastSelectedTab = null;
      }

      if (advancement != this.lastSelectedTab) {
         this.player.connection.send(new SSelectAdvancementsTabPacket(this.lastSelectedTab == null ? null : this.lastSelectedTab.getId()));
      }

   }

   public AdvancementProgress getOrStartProgress(Advancement pAdvancement) {
      AdvancementProgress advancementprogress = this.advancements.get(pAdvancement);
      if (advancementprogress == null) {
         advancementprogress = new AdvancementProgress();
         this.startProgress(pAdvancement, advancementprogress);
      }

      return advancementprogress;
   }

   private void startProgress(Advancement pAdvancement, AdvancementProgress pProgress) {
      pProgress.update(pAdvancement.getCriteria(), pAdvancement.getRequirements());
      this.advancements.put(pAdvancement, pProgress);
   }

   private void ensureVisibility(Advancement pAdvancement) {
      boolean flag = this.shouldBeVisible(pAdvancement);
      boolean flag1 = this.visible.contains(pAdvancement);
      if (flag && !flag1) {
         this.visible.add(pAdvancement);
         this.visibilityChanged.add(pAdvancement);
         if (this.advancements.containsKey(pAdvancement)) {
            this.progressChanged.add(pAdvancement);
         }
      } else if (!flag && flag1) {
         this.visible.remove(pAdvancement);
         this.visibilityChanged.add(pAdvancement);
      }

      if (flag != flag1 && pAdvancement.getParent() != null) {
         this.ensureVisibility(pAdvancement.getParent());
      }

      for(Advancement advancement : pAdvancement.getChildren()) {
         this.ensureVisibility(advancement);
      }

   }

   private boolean shouldBeVisible(Advancement pAdvancement) {
      for(int i = 0; pAdvancement != null && i <= 2; ++i) {
         if (i == 0 && this.hasCompletedChildrenOrSelf(pAdvancement)) {
            return true;
         }

         if (pAdvancement.getDisplay() == null) {
            return false;
         }

         AdvancementProgress advancementprogress = this.getOrStartProgress(pAdvancement);
         if (advancementprogress.isDone()) {
            return true;
         }

         if (pAdvancement.getDisplay().isHidden()) {
            return false;
         }

         pAdvancement = pAdvancement.getParent();
      }

      return false;
   }

   private boolean hasCompletedChildrenOrSelf(Advancement pAdvancement) {
      AdvancementProgress advancementprogress = this.getOrStartProgress(pAdvancement);
      if (advancementprogress.isDone()) {
         return true;
      } else {
         for(Advancement advancement : pAdvancement.getChildren()) {
            if (this.hasCompletedChildrenOrSelf(advancement)) {
               return true;
            }
         }

         return false;
      }
   }
}
