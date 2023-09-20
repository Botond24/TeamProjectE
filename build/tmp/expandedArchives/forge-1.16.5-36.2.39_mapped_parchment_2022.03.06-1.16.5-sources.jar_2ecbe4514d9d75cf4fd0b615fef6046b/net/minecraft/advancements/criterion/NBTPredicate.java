package net.minecraft.advancements.criterion;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.JSONUtils;

public class NBTPredicate {
   public static final NBTPredicate ANY = new NBTPredicate((CompoundNBT)null);
   @Nullable
   private final CompoundNBT tag;

   public NBTPredicate(@Nullable CompoundNBT p_i47536_1_) {
      this.tag = p_i47536_1_;
   }

   public boolean matches(ItemStack pItem) {
      return this == ANY ? true : this.matches(pItem.getTag());
   }

   public boolean matches(Entity pEntity) {
      return this == ANY ? true : this.matches(getEntityTagToCompare(pEntity));
   }

   public boolean matches(@Nullable INBT pNbt) {
      if (pNbt == null) {
         return this == ANY;
      } else {
         return this.tag == null || NBTUtil.compareNbt(this.tag, pNbt, true);
      }
   }

   public JsonElement serializeToJson() {
      return (JsonElement)(this != ANY && this.tag != null ? new JsonPrimitive(this.tag.toString()) : JsonNull.INSTANCE);
   }

   public static NBTPredicate fromJson(@Nullable JsonElement pJson) {
      if (pJson != null && !pJson.isJsonNull()) {
         CompoundNBT compoundnbt;
         try {
            compoundnbt = JsonToNBT.parseTag(JSONUtils.convertToString(pJson, "nbt"));
         } catch (CommandSyntaxException commandsyntaxexception) {
            throw new JsonSyntaxException("Invalid nbt tag: " + commandsyntaxexception.getMessage());
         }

         return new NBTPredicate(compoundnbt);
      } else {
         return ANY;
      }
   }

   public static CompoundNBT getEntityTagToCompare(Entity pEntity) {
      CompoundNBT compoundnbt = pEntity.saveWithoutId(new CompoundNBT());
      if (pEntity instanceof PlayerEntity) {
         ItemStack itemstack = ((PlayerEntity)pEntity).inventory.getSelected();
         if (!itemstack.isEmpty()) {
            compoundnbt.put("SelectedItem", itemstack.save(new CompoundNBT()));
         }
      }

      return compoundnbt;
   }
}