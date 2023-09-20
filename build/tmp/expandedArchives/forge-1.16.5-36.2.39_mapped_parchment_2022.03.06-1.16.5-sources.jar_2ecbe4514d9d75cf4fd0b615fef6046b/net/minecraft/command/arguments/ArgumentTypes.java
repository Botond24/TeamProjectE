package net.minecraft.command.arguments;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.command.arguments.serializers.BrigadierSerializers;
import net.minecraft.network.PacketBuffer;
import net.minecraft.test.TestArgArgument;
import net.minecraft.test.TestTypeArgument;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArgumentTypes {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Map<Class<?>, ArgumentTypes.Entry<?>> BY_CLASS = Maps.newHashMap();
   private static final Map<ResourceLocation, ArgumentTypes.Entry<?>> BY_NAME = Maps.newHashMap();

   public static <T extends ArgumentType<?>> void register(String pName, Class<T> pClazz, IArgumentSerializer<T> pSerializer) {
      ResourceLocation resourcelocation = new ResourceLocation(pName);
      if (BY_CLASS.containsKey(pClazz)) {
         throw new IllegalArgumentException("Class " + pClazz.getName() + " already has a serializer!");
      } else if (BY_NAME.containsKey(resourcelocation)) {
         throw new IllegalArgumentException("'" + resourcelocation + "' is already a registered serializer!");
      } else {
         ArgumentTypes.Entry<T> entry = new ArgumentTypes.Entry<>(pClazz, pSerializer, resourcelocation);
         BY_CLASS.put(pClazz, entry);
         BY_NAME.put(resourcelocation, entry);
      }
   }

   public static void bootStrap() {
      BrigadierSerializers.bootstrap();
      register("entity", EntityArgument.class, new EntityArgument.Serializer());
      register("game_profile", GameProfileArgument.class, new ArgumentSerializer<>(GameProfileArgument::gameProfile));
      register("block_pos", BlockPosArgument.class, new ArgumentSerializer<>(BlockPosArgument::blockPos));
      register("column_pos", ColumnPosArgument.class, new ArgumentSerializer<>(ColumnPosArgument::columnPos));
      register("vec3", Vec3Argument.class, new ArgumentSerializer<>(Vec3Argument::vec3));
      register("vec2", Vec2Argument.class, new ArgumentSerializer<>(Vec2Argument::vec2));
      register("block_state", BlockStateArgument.class, new ArgumentSerializer<>(BlockStateArgument::block));
      register("block_predicate", BlockPredicateArgument.class, new ArgumentSerializer<>(BlockPredicateArgument::blockPredicate));
      register("item_stack", ItemArgument.class, new ArgumentSerializer<>(ItemArgument::item));
      register("item_predicate", ItemPredicateArgument.class, new ArgumentSerializer<>(ItemPredicateArgument::itemPredicate));
      register("color", ColorArgument.class, new ArgumentSerializer<>(ColorArgument::color));
      register("component", ComponentArgument.class, new ArgumentSerializer<>(ComponentArgument::textComponent));
      register("message", MessageArgument.class, new ArgumentSerializer<>(MessageArgument::message));
      register("nbt_compound_tag", NBTCompoundTagArgument.class, new ArgumentSerializer<>(NBTCompoundTagArgument::compoundTag));
      register("nbt_tag", NBTTagArgument.class, new ArgumentSerializer<>(NBTTagArgument::nbtTag));
      register("nbt_path", NBTPathArgument.class, new ArgumentSerializer<>(NBTPathArgument::nbtPath));
      register("objective", ObjectiveArgument.class, new ArgumentSerializer<>(ObjectiveArgument::objective));
      register("objective_criteria", ObjectiveCriteriaArgument.class, new ArgumentSerializer<>(ObjectiveCriteriaArgument::criteria));
      register("operation", OperationArgument.class, new ArgumentSerializer<>(OperationArgument::operation));
      register("particle", ParticleArgument.class, new ArgumentSerializer<>(ParticleArgument::particle));
      register("angle", AngleArgument.class, new ArgumentSerializer<>(AngleArgument::angle));
      register("rotation", RotationArgument.class, new ArgumentSerializer<>(RotationArgument::rotation));
      register("scoreboard_slot", ScoreboardSlotArgument.class, new ArgumentSerializer<>(ScoreboardSlotArgument::displaySlot));
      register("score_holder", ScoreHolderArgument.class, new ScoreHolderArgument.Serializer());
      register("swizzle", SwizzleArgument.class, new ArgumentSerializer<>(SwizzleArgument::swizzle));
      register("team", TeamArgument.class, new ArgumentSerializer<>(TeamArgument::team));
      register("item_slot", SlotArgument.class, new ArgumentSerializer<>(SlotArgument::slot));
      register("resource_location", ResourceLocationArgument.class, new ArgumentSerializer<>(ResourceLocationArgument::id));
      register("mob_effect", PotionArgument.class, new ArgumentSerializer<>(PotionArgument::effect));
      register("function", FunctionArgument.class, new ArgumentSerializer<>(FunctionArgument::functions));
      register("entity_anchor", EntityAnchorArgument.class, new ArgumentSerializer<>(EntityAnchorArgument::anchor));
      register("int_range", IRangeArgument.IntRange.class, new ArgumentSerializer<>(IRangeArgument::intRange));
      register("float_range", IRangeArgument.FloatRange.class, new ArgumentSerializer<>(IRangeArgument::floatRange));
      register("item_enchantment", EnchantmentArgument.class, new ArgumentSerializer<>(EnchantmentArgument::enchantment));
      register("entity_summon", EntitySummonArgument.class, new ArgumentSerializer<>(EntitySummonArgument::id));
      register("dimension", DimensionArgument.class, new ArgumentSerializer<>(DimensionArgument::dimension));
      register("time", TimeArgument.class, new ArgumentSerializer<>(TimeArgument::time));
      register("uuid", UUIDArgument.class, new ArgumentSerializer<>(UUIDArgument::uuid));
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         register("test_argument", TestArgArgument.class, new ArgumentSerializer<>(TestArgArgument::testFunctionArgument));
         register("test_class", TestTypeArgument.class, new ArgumentSerializer<>(TestTypeArgument::testClassName));
      }

   }

   @Nullable
   private static ArgumentTypes.Entry<?> get(ResourceLocation pName) {
      return BY_NAME.get(pName);
   }

   @Nullable
   private static ArgumentTypes.Entry<?> get(ArgumentType<?> pType) {
      return BY_CLASS.get(pType.getClass());
   }

   public static <T extends ArgumentType<?>> void serialize(PacketBuffer pBuffer, T pType) {
      ArgumentTypes.Entry<T> entry = (Entry<T>) get(pType);
      if (entry == null) {
         LOGGER.error("Could not serialize {} ({}) - will not be sent to client!", pType, pType.getClass());
         pBuffer.writeResourceLocation(new ResourceLocation(""));
      } else {
         pBuffer.writeResourceLocation(entry.name);
         entry.serializer.serializeToNetwork(pType, pBuffer);
      }
   }

   @Nullable
   public static ArgumentType<?> deserialize(PacketBuffer pBuffer) {
      ResourceLocation resourcelocation = pBuffer.readResourceLocation();
      ArgumentTypes.Entry<?> entry = get(resourcelocation);
      if (entry == null) {
         LOGGER.error("Could not deserialize {}", (Object)resourcelocation);
         return null;
      } else {
         return entry.serializer.deserializeFromNetwork(pBuffer);
      }
   }

   private static <T extends ArgumentType<?>> void serializeToJson(JsonObject pJson, T pType) {
      ArgumentTypes.Entry<T> entry = (Entry<T>) get(pType);
      if (entry == null) {
         LOGGER.error("Could not serialize argument {} ({})!", pType, pType.getClass());
         pJson.addProperty("type", "unknown");
      } else {
         pJson.addProperty("type", "argument");
         pJson.addProperty("parser", entry.name.toString());
         JsonObject jsonobject = new JsonObject();
         entry.serializer.serializeToJson(pType, jsonobject);
         if (jsonobject.size() > 0) {
            pJson.add("properties", jsonobject);
         }
      }

   }

   public static <S> JsonObject serializeNodeToJson(CommandDispatcher<S> pDispatcher, CommandNode<S> pNode) {
      JsonObject jsonobject = new JsonObject();
      if (pNode instanceof RootCommandNode) {
         jsonobject.addProperty("type", "root");
      } else if (pNode instanceof LiteralCommandNode) {
         jsonobject.addProperty("type", "literal");
      } else if (pNode instanceof ArgumentCommandNode) {
         serializeToJson(jsonobject, ((ArgumentCommandNode)pNode).getType());
      } else {
         LOGGER.error("Could not serialize node {} ({})!", pNode, pNode.getClass());
         jsonobject.addProperty("type", "unknown");
      }

      JsonObject jsonobject1 = new JsonObject();

      for(CommandNode<S> commandnode : pNode.getChildren()) {
         jsonobject1.add(commandnode.getName(), serializeNodeToJson(pDispatcher, commandnode));
      }

      if (jsonobject1.size() > 0) {
         jsonobject.add("children", jsonobject1);
      }

      if (pNode.getCommand() != null) {
         jsonobject.addProperty("executable", true);
      }

      if (pNode.getRedirect() != null) {
         Collection<String> collection = pDispatcher.getPath(pNode.getRedirect());
         if (!collection.isEmpty()) {
            JsonArray jsonarray = new JsonArray();

            for(String s : collection) {
               jsonarray.add(s);
            }

            jsonobject.add("redirect", jsonarray);
         }
      }

      return jsonobject;
   }

   public static boolean isTypeRegistered(ArgumentType<?> pType) {
      return get(pType) != null;
   }

   public static <T> Set<ArgumentType<?>> findUsedArgumentTypes(CommandNode<T> pNode) {
      Set<CommandNode<T>> set = Sets.newIdentityHashSet();
      Set<ArgumentType<?>> set1 = Sets.newHashSet();
      findUsedArgumentTypes(pNode, set1, set);
      return set1;
   }

   private static <T> void findUsedArgumentTypes(CommandNode<T> pNode, Set<ArgumentType<?>> pTypes, Set<CommandNode<T>> pNodes) {
      if (pNodes.add(pNode)) {
         if (pNode instanceof ArgumentCommandNode) {
            pTypes.add(((ArgumentCommandNode)pNode).getType());
         }

         pNode.getChildren().forEach((p_243513_2_) -> {
            findUsedArgumentTypes(p_243513_2_, pTypes, pNodes);
         });
         CommandNode<T> commandnode = pNode.getRedirect();
         if (commandnode != null) {
            findUsedArgumentTypes(commandnode, pTypes, pNodes);
         }

      }
   }

   static class Entry<T extends ArgumentType<?>> {
      public final Class<T> clazz;
      public final IArgumentSerializer<T> serializer;
      public final ResourceLocation name;

      private Entry(Class<T> pClazz, IArgumentSerializer<T> pSerializer, ResourceLocation pName) {
         this.clazz = pClazz;
         this.serializer = pSerializer;
         this.name = pName;
      }
   }
   @javax.annotation.Nullable public static ResourceLocation getId(ArgumentType<?> type) {
      Entry<?> entry = get(type);
      return entry == null ? null : entry.name;
   }
}
