package net.minecraft.stats;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.play.server.SStatisticsPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DefaultTypeReferences;
import net.minecraft.util.registry.Registry;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerStatisticsManager extends StatisticsManager {
   private static final Logger LOGGER = LogManager.getLogger();
   private final MinecraftServer server;
   private final File file;
   private final Set<Stat<?>> dirty = Sets.newHashSet();
   private int lastStatRequest = -300;

   public ServerStatisticsManager(MinecraftServer pServer, File pFile) {
      this.server = pServer;
      this.file = pFile;
      if (pFile.isFile()) {
         try {
            this.parseLocal(pServer.getFixerUpper(), FileUtils.readFileToString(pFile));
         } catch (IOException ioexception) {
            LOGGER.error("Couldn't read statistics file {}", pFile, ioexception);
         } catch (JsonParseException jsonparseexception) {
            LOGGER.error("Couldn't parse statistics file {}", pFile, jsonparseexception);
         }
      }

   }

   public void save() {
      try {
         FileUtils.writeStringToFile(this.file, this.toJson());
      } catch (IOException ioexception) {
         LOGGER.error("Couldn't save stats", (Throwable)ioexception);
      }

   }

   /**
    * Triggers the logging of an achievement and attempts to announce to server
    */
   public void setValue(PlayerEntity pPlayer, Stat<?> pStat, int pValue) {
      super.setValue(pPlayer, pStat, pValue);
      this.dirty.add(pStat);
   }

   private Set<Stat<?>> getDirty() {
      Set<Stat<?>> set = Sets.newHashSet(this.dirty);
      this.dirty.clear();
      return set;
   }

   public void parseLocal(DataFixer pDataFixer, String pFileData) {
      try (JsonReader jsonreader = new JsonReader(new StringReader(pFileData))) {
         jsonreader.setLenient(false);
         JsonElement jsonelement = Streams.parse(jsonreader);
         if (jsonelement.isJsonNull()) {
            LOGGER.error("Unable to parse Stat data from {}", (Object)this.file);
            return;
         }

         CompoundNBT compoundnbt = fromJson(jsonelement.getAsJsonObject());
         if (!compoundnbt.contains("DataVersion", 99)) {
            compoundnbt.putInt("DataVersion", 1343);
         }

         compoundnbt = NBTUtil.update(pDataFixer, DefaultTypeReferences.STATS, compoundnbt, compoundnbt.getInt("DataVersion"));
         if (compoundnbt.contains("stats", 10)) {
            CompoundNBT compoundnbt1 = compoundnbt.getCompound("stats");

            for(String s : compoundnbt1.getAllKeys()) {
               if (compoundnbt1.contains(s, 10)) {
                  Util.ifElse(Registry.STAT_TYPE.getOptional(new ResourceLocation(s)), (p_219731_3_) -> {
                     CompoundNBT compoundnbt2 = compoundnbt1.getCompound(s);

                     for(String s1 : compoundnbt2.getAllKeys()) {
                        if (compoundnbt2.contains(s1, 99)) {
                           Util.ifElse(this.getStat(p_219731_3_, s1), (p_219730_3_) -> {
                              this.stats.put(p_219730_3_, compoundnbt2.getInt(s1));
                           }, () -> {
                              LOGGER.warn("Invalid statistic in {}: Don't know what {} is", this.file, s1);
                           });
                        } else {
                           LOGGER.warn("Invalid statistic value in {}: Don't know what {} is for key {}", this.file, compoundnbt2.get(s1), s1);
                        }
                     }

                  }, () -> {
                     LOGGER.warn("Invalid statistic type in {}: Don't know what {} is", this.file, s);
                  });
               }
            }
         }
      } catch (IOException | JsonParseException jsonparseexception) {
         LOGGER.error("Unable to parse Stat data from {}", this.file, jsonparseexception);
      }

   }

   private <T> Optional<Stat<T>> getStat(StatType<T> pType, String pId) {
      return Optional.ofNullable(ResourceLocation.tryParse(pId)).flatMap(pType.getRegistry()::getOptional).map(pType::get);
   }

   private static CompoundNBT fromJson(JsonObject pTagObject) {
      CompoundNBT compoundnbt = new CompoundNBT();

      for(Entry<String, JsonElement> entry : pTagObject.entrySet()) {
         JsonElement jsonelement = entry.getValue();
         if (jsonelement.isJsonObject()) {
            compoundnbt.put(entry.getKey(), fromJson(jsonelement.getAsJsonObject()));
         } else if (jsonelement.isJsonPrimitive()) {
            JsonPrimitive jsonprimitive = jsonelement.getAsJsonPrimitive();
            if (jsonprimitive.isNumber()) {
               compoundnbt.putInt(entry.getKey(), jsonprimitive.getAsInt());
            }
         }
      }

      return compoundnbt;
   }

   protected String toJson() {
      Map<StatType<?>, JsonObject> map = Maps.newHashMap();

      for(it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Stat<?>> entry : this.stats.object2IntEntrySet()) {
         Stat<?> stat = entry.getKey();
         map.computeIfAbsent(stat.getType(), (p_199064_0_) -> {
            return new JsonObject();
         }).addProperty(getKey(stat).toString(), entry.getIntValue());
      }

      JsonObject jsonobject = new JsonObject();

      for(Entry<StatType<?>, JsonObject> entry1 : map.entrySet()) {
         jsonobject.add(Registry.STAT_TYPE.getKey(entry1.getKey()).toString(), entry1.getValue());
      }

      JsonObject jsonobject1 = new JsonObject();
      jsonobject1.add("stats", jsonobject);
      jsonobject1.addProperty("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
      return jsonobject1.toString();
   }

   private static <T> ResourceLocation getKey(Stat<T> pStat) {
      return pStat.getType().getRegistry().getKey(pStat.getValue());
   }

   public void markAllDirty() {
      this.dirty.addAll(this.stats.keySet());
   }

   public void sendStats(ServerPlayerEntity pPlayer) {
      int i = this.server.getTickCount();
      Object2IntMap<Stat<?>> object2intmap = new Object2IntOpenHashMap<>();
      if (i - this.lastStatRequest > 300) {
         this.lastStatRequest = i;

         for(Stat<?> stat : this.getDirty()) {
            object2intmap.put(stat, this.getValue(stat));
         }
      }

      pPlayer.connection.send(new SStatisticsPacket(object2intmap));
   }
}