package net.minecraft.advancements;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AdvancementProgress implements Comparable<AdvancementProgress> {
   private final Map<String, CriterionProgress> criteria = Maps.newHashMap();
   private String[][] requirements = new String[0][];

   /**
    * Update this AdvancementProgress' criteria and requirements
    */
   public void update(Map<String, Criterion> pCriteria, String[][] pRequirements) {
      Set<String> set = pCriteria.keySet();
      this.criteria.entrySet().removeIf((p_209539_1_) -> {
         return !set.contains(p_209539_1_.getKey());
      });

      for(String s : set) {
         if (!this.criteria.containsKey(s)) {
            this.criteria.put(s, new CriterionProgress());
         }
      }

      this.requirements = pRequirements;
   }

   public boolean isDone() {
      if (this.requirements.length == 0) {
         return false;
      } else {
         for(String[] astring : this.requirements) {
            boolean flag = false;

            for(String s : astring) {
               CriterionProgress criterionprogress = this.getCriterion(s);
               if (criterionprogress != null && criterionprogress.isDone()) {
                  flag = true;
                  break;
               }
            }

            if (!flag) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean hasProgress() {
      for(CriterionProgress criterionprogress : this.criteria.values()) {
         if (criterionprogress.isDone()) {
            return true;
         }
      }

      return false;
   }

   public boolean grantProgress(String pCriterion) {
      CriterionProgress criterionprogress = this.criteria.get(pCriterion);
      if (criterionprogress != null && !criterionprogress.isDone()) {
         criterionprogress.grant();
         return true;
      } else {
         return false;
      }
   }

   public boolean revokeProgress(String pCriterion) {
      CriterionProgress criterionprogress = this.criteria.get(pCriterion);
      if (criterionprogress != null && criterionprogress.isDone()) {
         criterionprogress.revoke();
         return true;
      } else {
         return false;
      }
   }

   public String toString() {
      return "AdvancementProgress{criteria=" + this.criteria + ", requirements=" + Arrays.deepToString(this.requirements) + '}';
   }

   public void serializeToNetwork(PacketBuffer pBuffer) {
      pBuffer.writeVarInt(this.criteria.size());

      for(Entry<String, CriterionProgress> entry : this.criteria.entrySet()) {
         pBuffer.writeUtf(entry.getKey());
         entry.getValue().serializeToNetwork(pBuffer);
      }

   }

   public static AdvancementProgress fromNetwork(PacketBuffer pBuffer) {
      AdvancementProgress advancementprogress = new AdvancementProgress();
      int i = pBuffer.readVarInt();

      for(int j = 0; j < i; ++j) {
         advancementprogress.criteria.put(pBuffer.readUtf(32767), CriterionProgress.fromNetwork(pBuffer));
      }

      return advancementprogress;
   }

   @Nullable
   public CriterionProgress getCriterion(String pCriterion) {
      return this.criteria.get(pCriterion);
   }

   @OnlyIn(Dist.CLIENT)
   public float getPercent() {
      if (this.criteria.isEmpty()) {
         return 0.0F;
      } else {
         float f = (float)this.requirements.length;
         float f1 = (float)this.countCompletedRequirements();
         return f1 / f;
      }
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public String getProgressText() {
      if (this.criteria.isEmpty()) {
         return null;
      } else {
         int i = this.requirements.length;
         if (i <= 1) {
            return null;
         } else {
            int j = this.countCompletedRequirements();
            return j + "/" + i;
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   private int countCompletedRequirements() {
      int i = 0;

      for(String[] astring : this.requirements) {
         boolean flag = false;

         for(String s : astring) {
            CriterionProgress criterionprogress = this.getCriterion(s);
            if (criterionprogress != null && criterionprogress.isDone()) {
               flag = true;
               break;
            }
         }

         if (flag) {
            ++i;
         }
      }

      return i;
   }

   public Iterable<String> getRemainingCriteria() {
      List<String> list = Lists.newArrayList();

      for(Entry<String, CriterionProgress> entry : this.criteria.entrySet()) {
         if (!entry.getValue().isDone()) {
            list.add(entry.getKey());
         }
      }

      return list;
   }

   public Iterable<String> getCompletedCriteria() {
      List<String> list = Lists.newArrayList();

      for(Entry<String, CriterionProgress> entry : this.criteria.entrySet()) {
         if (entry.getValue().isDone()) {
            list.add(entry.getKey());
         }
      }

      return list;
   }

   @Nullable
   public Date getFirstProgressDate() {
      Date date = null;

      for(CriterionProgress criterionprogress : this.criteria.values()) {
         if (criterionprogress.isDone() && (date == null || criterionprogress.getObtained().before(date))) {
            date = criterionprogress.getObtained();
         }
      }

      return date;
   }

   public int compareTo(AdvancementProgress p_compareTo_1_) {
      Date date = this.getFirstProgressDate();
      Date date1 = p_compareTo_1_.getFirstProgressDate();
      if (date == null && date1 != null) {
         return 1;
      } else if (date != null && date1 == null) {
         return -1;
      } else {
         return date == null && date1 == null ? 0 : date.compareTo(date1);
      }
   }

   public static class Serializer implements JsonDeserializer<AdvancementProgress>, JsonSerializer<AdvancementProgress> {
      public JsonElement serialize(AdvancementProgress p_serialize_1_, Type p_serialize_2_, JsonSerializationContext p_serialize_3_) {
         JsonObject jsonobject = new JsonObject();
         JsonObject jsonobject1 = new JsonObject();

         for(Entry<String, CriterionProgress> entry : p_serialize_1_.criteria.entrySet()) {
            CriterionProgress criterionprogress = entry.getValue();
            if (criterionprogress.isDone()) {
               jsonobject1.add(entry.getKey(), criterionprogress.serializeToJson());
            }
         }

         if (!jsonobject1.entrySet().isEmpty()) {
            jsonobject.add("criteria", jsonobject1);
         }

         jsonobject.addProperty("done", p_serialize_1_.isDone());
         return jsonobject;
      }

      public AdvancementProgress deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {
         JsonObject jsonobject = JSONUtils.convertToJsonObject(p_deserialize_1_, "advancement");
         JsonObject jsonobject1 = JSONUtils.getAsJsonObject(jsonobject, "criteria", new JsonObject());
         AdvancementProgress advancementprogress = new AdvancementProgress();

         for(Entry<String, JsonElement> entry : jsonobject1.entrySet()) {
            String s = entry.getKey();
            advancementprogress.criteria.put(s, CriterionProgress.fromJson(JSONUtils.convertToString(entry.getValue(), s)));
         }

         return advancementprogress;
      }
   }
}