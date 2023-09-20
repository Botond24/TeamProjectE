package net.minecraft.advancements;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DisplayInfo {
   private final ITextComponent title;
   private final ITextComponent description;
   private final ItemStack icon;
   private final ResourceLocation background;
   private final FrameType frame;
   private final boolean showToast;
   private final boolean announceChat;
   private final boolean hidden;
   private float x;
   private float y;

   public DisplayInfo(ItemStack pIcon, ITextComponent pTitle, ITextComponent pDescription, @Nullable ResourceLocation pBackground, FrameType pFrame, boolean pShowToast, boolean pAnnounceChat, boolean pHidden) {
      this.title = pTitle;
      this.description = pDescription;
      this.icon = pIcon;
      this.background = pBackground;
      this.frame = pFrame;
      this.showToast = pShowToast;
      this.announceChat = pAnnounceChat;
      this.hidden = pHidden;
   }

   public void setLocation(float pX, float pY) {
      this.x = pX;
      this.y = pY;
   }

   public ITextComponent getTitle() {
      return this.title;
   }

   public ITextComponent getDescription() {
      return this.description;
   }

   @OnlyIn(Dist.CLIENT)
   public ItemStack getIcon() {
      return this.icon;
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public ResourceLocation getBackground() {
      return this.background;
   }

   public FrameType getFrame() {
      return this.frame;
   }

   @OnlyIn(Dist.CLIENT)
   public float getX() {
      return this.x;
   }

   @OnlyIn(Dist.CLIENT)
   public float getY() {
      return this.y;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean shouldShowToast() {
      return this.showToast;
   }

   public boolean shouldAnnounceChat() {
      return this.announceChat;
   }

   public boolean isHidden() {
      return this.hidden;
   }

   public static DisplayInfo fromJson(JsonObject pObject) {
      ITextComponent itextcomponent = ITextComponent.Serializer.fromJson(pObject.get("title"));
      ITextComponent itextcomponent1 = ITextComponent.Serializer.fromJson(pObject.get("description"));
      if (itextcomponent != null && itextcomponent1 != null) {
         ItemStack itemstack = getIcon(JSONUtils.getAsJsonObject(pObject, "icon"));
         ResourceLocation resourcelocation = pObject.has("background") ? new ResourceLocation(JSONUtils.getAsString(pObject, "background")) : null;
         FrameType frametype = pObject.has("frame") ? FrameType.byName(JSONUtils.getAsString(pObject, "frame")) : FrameType.TASK;
         boolean flag = JSONUtils.getAsBoolean(pObject, "show_toast", true);
         boolean flag1 = JSONUtils.getAsBoolean(pObject, "announce_to_chat", true);
         boolean flag2 = JSONUtils.getAsBoolean(pObject, "hidden", false);
         return new DisplayInfo(itemstack, itextcomponent, itextcomponent1, resourcelocation, frametype, flag, flag1, flag2);
      } else {
         throw new JsonSyntaxException("Both title and description must be set");
      }
   }

   private static ItemStack getIcon(JsonObject pObject) {
      if (!pObject.has("item")) {
         throw new JsonSyntaxException("Unsupported icon type, currently only items are supported (add 'item' key)");
      } else {
         Item item = JSONUtils.getAsItem(pObject, "item");
         if (pObject.has("data")) {
            throw new JsonParseException("Disallowed data tag found");
         } else {
            ItemStack itemstack = new ItemStack(item);
            if (pObject.has("nbt")) {
               try {
                  CompoundNBT compoundnbt = JsonToNBT.parseTag(JSONUtils.convertToString(pObject.get("nbt"), "nbt"));
                  itemstack.setTag(compoundnbt);
               } catch (CommandSyntaxException commandsyntaxexception) {
                  throw new JsonSyntaxException("Invalid nbt tag: " + commandsyntaxexception.getMessage());
               }
            }

            return itemstack;
         }
      }
   }

   public void serializeToNetwork(PacketBuffer pBuf) {
      pBuf.writeComponent(this.title);
      pBuf.writeComponent(this.description);
      pBuf.writeItem(this.icon);
      pBuf.writeEnum(this.frame);
      int i = 0;
      if (this.background != null) {
         i |= 1;
      }

      if (this.showToast) {
         i |= 2;
      }

      if (this.hidden) {
         i |= 4;
      }

      pBuf.writeInt(i);
      if (this.background != null) {
         pBuf.writeResourceLocation(this.background);
      }

      pBuf.writeFloat(this.x);
      pBuf.writeFloat(this.y);
   }

   public static DisplayInfo fromNetwork(PacketBuffer pBuf) {
      ITextComponent itextcomponent = pBuf.readComponent();
      ITextComponent itextcomponent1 = pBuf.readComponent();
      ItemStack itemstack = pBuf.readItem();
      FrameType frametype = pBuf.readEnum(FrameType.class);
      int i = pBuf.readInt();
      ResourceLocation resourcelocation = (i & 1) != 0 ? pBuf.readResourceLocation() : null;
      boolean flag = (i & 2) != 0;
      boolean flag1 = (i & 4) != 0;
      DisplayInfo displayinfo = new DisplayInfo(itemstack, itextcomponent, itextcomponent1, resourcelocation, frametype, flag, false, flag1);
      displayinfo.setLocation(pBuf.readFloat(), pBuf.readFloat());
      return displayinfo;
   }

   public JsonElement serializeToJson() {
      JsonObject jsonobject = new JsonObject();
      jsonobject.add("icon", this.serializeIcon());
      jsonobject.add("title", ITextComponent.Serializer.toJsonTree(this.title));
      jsonobject.add("description", ITextComponent.Serializer.toJsonTree(this.description));
      jsonobject.addProperty("frame", this.frame.getName());
      jsonobject.addProperty("show_toast", this.showToast);
      jsonobject.addProperty("announce_to_chat", this.announceChat);
      jsonobject.addProperty("hidden", this.hidden);
      if (this.background != null) {
         jsonobject.addProperty("background", this.background.toString());
      }

      return jsonobject;
   }

   private JsonObject serializeIcon() {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("item", Registry.ITEM.getKey(this.icon.getItem()).toString());
      if (this.icon.hasTag()) {
         jsonobject.addProperty("nbt", this.icon.getTag().toString());
      }

      return jsonobject;
   }
}