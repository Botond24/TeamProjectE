package net.minecraft.world;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.DynamicLike;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.network.play.server.SEntityStatusPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameRules {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Map<GameRules.RuleKey<?>, GameRules.RuleType<?>> GAME_RULE_TYPES = Maps.newTreeMap(Comparator.comparing((p_223597_0_) -> {
      return p_223597_0_.id;
   }));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_DOFIRETICK = register("doFireTick", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_MOBGRIEFING = register("mobGriefing", GameRules.Category.MOBS, GameRules.BooleanValue.create(true));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_KEEPINVENTORY = register("keepInventory", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_DOMOBSPAWNING = register("doMobSpawning", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_DOMOBLOOT = register("doMobLoot", GameRules.Category.DROPS, GameRules.BooleanValue.create(true));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_DOBLOCKDROPS = register("doTileDrops", GameRules.Category.DROPS, GameRules.BooleanValue.create(true));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_DOENTITYDROPS = register("doEntityDrops", GameRules.Category.DROPS, GameRules.BooleanValue.create(true));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_COMMANDBLOCKOUTPUT = register("commandBlockOutput", GameRules.Category.CHAT, GameRules.BooleanValue.create(true));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_NATURAL_REGENERATION = register("naturalRegeneration", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_DAYLIGHT = register("doDaylightCycle", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_LOGADMINCOMMANDS = register("logAdminCommands", GameRules.Category.CHAT, GameRules.BooleanValue.create(true));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_SHOWDEATHMESSAGES = register("showDeathMessages", GameRules.Category.CHAT, GameRules.BooleanValue.create(true));
   public static final GameRules.RuleKey<GameRules.IntegerValue> RULE_RANDOMTICKING = register("randomTickSpeed", GameRules.Category.UPDATES, GameRules.IntegerValue.create(3));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_SENDCOMMANDFEEDBACK = register("sendCommandFeedback", GameRules.Category.CHAT, GameRules.BooleanValue.create(true));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_REDUCEDDEBUGINFO = register("reducedDebugInfo", GameRules.Category.MISC, GameRules.BooleanValue.create(false, (p_223589_0_, p_223589_1_) -> {
      byte b0 = (byte)(p_223589_1_.get() ? 22 : 23);

      for(ServerPlayerEntity serverplayerentity : p_223589_0_.getPlayerList().getPlayers()) {
         serverplayerentity.connection.send(new SEntityStatusPacket(serverplayerentity, b0));
      }

   }));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_SPECTATORSGENERATECHUNKS = register("spectatorsGenerateChunks", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
   public static final GameRules.RuleKey<GameRules.IntegerValue> RULE_SPAWN_RADIUS = register("spawnRadius", GameRules.Category.PLAYER, GameRules.IntegerValue.create(10));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_DISABLE_ELYTRA_MOVEMENT_CHECK = register("disableElytraMovementCheck", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
   public static final GameRules.RuleKey<GameRules.IntegerValue> RULE_MAX_ENTITY_CRAMMING = register("maxEntityCramming", GameRules.Category.MOBS, GameRules.IntegerValue.create(24));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_WEATHER_CYCLE = register("doWeatherCycle", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_LIMITED_CRAFTING = register("doLimitedCrafting", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
   public static final GameRules.RuleKey<GameRules.IntegerValue> RULE_MAX_COMMAND_CHAIN_LENGTH = register("maxCommandChainLength", GameRules.Category.MISC, GameRules.IntegerValue.create(65536));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_ANNOUNCE_ADVANCEMENTS = register("announceAdvancements", GameRules.Category.CHAT, GameRules.BooleanValue.create(true));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_DISABLE_RAIDS = register("disableRaids", GameRules.Category.MOBS, GameRules.BooleanValue.create(false));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_DOINSOMNIA = register("doInsomnia", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_DO_IMMEDIATE_RESPAWN = register("doImmediateRespawn", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false, (p_226686_0_, p_226686_1_) -> {
      for(ServerPlayerEntity serverplayerentity : p_226686_0_.getPlayerList().getPlayers()) {
         serverplayerentity.connection.send(new SChangeGameStatePacket(SChangeGameStatePacket.IMMEDIATE_RESPAWN, p_226686_1_.get() ? 1.0F : 0.0F));
      }

   }));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_DROWNING_DAMAGE = register("drowningDamage", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_FALL_DAMAGE = register("fallDamage", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_FIRE_DAMAGE = register("fireDamage", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_DO_PATROL_SPAWNING = register("doPatrolSpawning", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_DO_TRADER_SPAWNING = register("doTraderSpawning", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_FORGIVE_DEAD_PLAYERS = register("forgiveDeadPlayers", GameRules.Category.MOBS, GameRules.BooleanValue.create(true));
   public static final GameRules.RuleKey<GameRules.BooleanValue> RULE_UNIVERSAL_ANGER = register("universalAnger", GameRules.Category.MOBS, GameRules.BooleanValue.create(false));
   private final Map<GameRules.RuleKey<?>, GameRules.RuleValue<?>> rules;

   public static <T extends GameRules.RuleValue<T>> GameRules.RuleKey<T> register(String pName, GameRules.Category pCategory, GameRules.RuleType<T> pType) {
      GameRules.RuleKey<T> rulekey = new GameRules.RuleKey<>(pName, pCategory);
      GameRules.RuleType<?> ruletype = GAME_RULE_TYPES.put(rulekey, pType);
      if (ruletype != null) {
         throw new IllegalStateException("Duplicate game rule registration for " + pName);
      } else {
         return rulekey;
      }
   }

   public GameRules(DynamicLike<?> p_i231611_1_) {
      this();
      this.loadFromTag(p_i231611_1_);
   }

   public GameRules() {
      this.rules = GAME_RULE_TYPES.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, (p_226684_0_) -> {
         return p_226684_0_.getValue().createRule();
      }));
   }

   private GameRules(Map<GameRules.RuleKey<?>, GameRules.RuleValue<?>> pRules) {
      this.rules = pRules;
   }

   public <T extends GameRules.RuleValue<T>> T getRule(GameRules.RuleKey<T> pKey) {
      return (T)(this.rules.get(pKey));
   }

   /**
    * Return the defined game rules as NBT.
    */
   public CompoundNBT createTag() {
      CompoundNBT compoundnbt = new CompoundNBT();
      this.rules.forEach((p_226688_1_, p_226688_2_) -> {
         compoundnbt.putString(p_226688_1_.id, p_226688_2_.serialize());
      });
      return compoundnbt;
   }

   private void loadFromTag(DynamicLike<?> pDynamic) {
      this.rules.forEach((p_234902_1_, p_234902_2_) -> {
         pDynamic.get(p_234902_1_.id).asString().result().ifPresent(p_234902_2_::deserialize);
      });
   }

   public GameRules copy() {
      return new GameRules(this.rules.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, (p_234904_0_) -> {
         return p_234904_0_.getValue().copy();
      })));
   }

   public static void visitGameRuleTypes(GameRules.IRuleEntryVisitor pVisitor) {
      GAME_RULE_TYPES.forEach((p_234906_1_, p_234906_2_) -> {
         callVisitorCap(pVisitor, p_234906_1_, p_234906_2_);
      });
   }

   private static <T extends GameRules.RuleValue<T>> void callVisitorCap(GameRules.IRuleEntryVisitor pVisitor, GameRules.RuleKey<?> pKey, GameRules.RuleType<?> pType) {
      pVisitor.visit((GameRules.RuleKey)pKey, pType);
      pType.callVisitor(pVisitor,(GameRules.RuleKey) pKey);
   }

   @OnlyIn(Dist.CLIENT)
   public void assignFrom(GameRules pRules, @Nullable MinecraftServer pServer) {
      pRules.rules.keySet().forEach((p_234900_3_) -> {
         this.assignCap(p_234900_3_, pRules, pServer);
      });
   }

   @OnlyIn(Dist.CLIENT)
   private <T extends GameRules.RuleValue<T>> void assignCap(GameRules.RuleKey<T> pKey, GameRules pRules, @Nullable MinecraftServer pServer) {
      T t = pRules.getRule(pKey);
      this.<T>getRule(pKey).setFrom(t, pServer);
   }

   public boolean getBoolean(GameRules.RuleKey<GameRules.BooleanValue> pKey) {
      return this.getRule(pKey).get();
   }

   public int getInt(GameRules.RuleKey<GameRules.IntegerValue> pKey) {
      return this.getRule(pKey).get();
   }

   public static class BooleanValue extends GameRules.RuleValue<GameRules.BooleanValue> {
      private boolean value;

      private static GameRules.RuleType<GameRules.BooleanValue> create(boolean pDefaultValue, BiConsumer<MinecraftServer, GameRules.BooleanValue> pChangeListener) {
         return new GameRules.RuleType<>(BoolArgumentType::bool, (p_223574_1_) -> {
            return new GameRules.BooleanValue(p_223574_1_, pDefaultValue);
         }, pChangeListener, GameRules.IRuleEntryVisitor::visitBoolean);
      }

      private static GameRules.RuleType<GameRules.BooleanValue> create(boolean pDefaultValue) {
         return create(pDefaultValue, (p_223569_0_, p_223569_1_) -> {
         });
      }

      public BooleanValue(GameRules.RuleType<GameRules.BooleanValue> pType, boolean pValue) {
         super(pType);
         this.value = pValue;
      }

      protected void updateFromArgument(CommandContext<CommandSource> pContext, String pParamName) {
         this.value = BoolArgumentType.getBool(pContext, pParamName);
      }

      public boolean get() {
         return this.value;
      }

      public void set(boolean pValue, @Nullable MinecraftServer pServer) {
         this.value = pValue;
         this.onChanged(pServer);
      }

      public String serialize() {
         return Boolean.toString(this.value);
      }

      protected void deserialize(String pValue) {
         this.value = Boolean.parseBoolean(pValue);
      }

      public int getCommandResult() {
         return this.value ? 1 : 0;
      }

      protected GameRules.BooleanValue getSelf() {
         return this;
      }

      protected GameRules.BooleanValue copy() {
         return new GameRules.BooleanValue(this.type, this.value);
      }

      @OnlyIn(Dist.CLIENT)
      public void setFrom(GameRules.BooleanValue pValue, @Nullable MinecraftServer pServer) {
         this.value = pValue.value;
         this.onChanged(pServer);
      }
   }

   public static enum Category {
      PLAYER("gamerule.category.player"),
      MOBS("gamerule.category.mobs"),
      SPAWNING("gamerule.category.spawning"),
      DROPS("gamerule.category.drops"),
      UPDATES("gamerule.category.updates"),
      CHAT("gamerule.category.chat"),
      MISC("gamerule.category.misc");

      private final String descriptionId;

      private Category(String pDescriptionId) {
         this.descriptionId = pDescriptionId;
      }

      @OnlyIn(Dist.CLIENT)
      public String getDescriptionId() {
         return this.descriptionId;
      }
   }

   interface IRule<T extends GameRules.RuleValue<T>> {
      void call(GameRules.IRuleEntryVisitor p_call_1_, GameRules.RuleKey<T> p_call_2_, GameRules.RuleType<T> p_call_3_);
   }

   public interface IRuleEntryVisitor {
      default <T extends GameRules.RuleValue<T>> void visit(GameRules.RuleKey<T> pKey, GameRules.RuleType<T> pType) {
      }

      default void visitBoolean(GameRules.RuleKey<GameRules.BooleanValue> pKey, GameRules.RuleType<GameRules.BooleanValue> pType) {
      }

      default void visitInteger(GameRules.RuleKey<GameRules.IntegerValue> pKey, GameRules.RuleType<GameRules.IntegerValue> pType) {
      }
   }

   public static class IntegerValue extends GameRules.RuleValue<GameRules.IntegerValue> {
      private int value;

      private static GameRules.RuleType<GameRules.IntegerValue> create(int pDefaultValue, BiConsumer<MinecraftServer, GameRules.IntegerValue> pChangeListener) {
         return new GameRules.RuleType<>(IntegerArgumentType::integer, (p_223565_1_) -> {
            return new GameRules.IntegerValue(p_223565_1_, pDefaultValue);
         }, pChangeListener, GameRules.IRuleEntryVisitor::visitInteger);
      }

      private static GameRules.RuleType<GameRules.IntegerValue> create(int pDefaultValue) {
         return create(pDefaultValue, (p_223561_0_, p_223561_1_) -> {
         });
      }

      public IntegerValue(GameRules.RuleType<GameRules.IntegerValue> pType, int pValue) {
         super(pType);
         this.value = pValue;
      }

      protected void updateFromArgument(CommandContext<CommandSource> pContext, String pParamName) {
         this.value = IntegerArgumentType.getInteger(pContext, pParamName);
      }

      public int get() {
         return this.value;
      }

      public String serialize() {
         return Integer.toString(this.value);
      }

      protected void deserialize(String pValue) {
         this.value = safeParse(pValue);
      }

      @OnlyIn(Dist.CLIENT)
      public boolean tryDeserialize(String pName) {
         try {
            this.value = Integer.parseInt(pName);
            return true;
         } catch (NumberFormatException numberformatexception) {
            return false;
         }
      }

      private static int safeParse(String pStrValue) {
         if (!pStrValue.isEmpty()) {
            try {
               return Integer.parseInt(pStrValue);
            } catch (NumberFormatException numberformatexception) {
               GameRules.LOGGER.warn("Failed to parse integer {}", (Object)pStrValue);
            }
         }

         return 0;
      }

      public int getCommandResult() {
         return this.value;
      }

      protected GameRules.IntegerValue getSelf() {
         return this;
      }

      protected GameRules.IntegerValue copy() {
         return new GameRules.IntegerValue(this.type, this.value);
      }

      @OnlyIn(Dist.CLIENT)
      public void setFrom(GameRules.IntegerValue pValue, @Nullable MinecraftServer pServer) {
         this.value = pValue.value;
         this.onChanged(pServer);
      }
   }

   public static final class RuleKey<T extends GameRules.RuleValue<T>> {
      private final String id;
      private final GameRules.Category category;

      public RuleKey(String pId, GameRules.Category pCategory) {
         this.id = pId;
         this.category = pCategory;
      }

      public String toString() {
         return this.id;
      }

      public boolean equals(Object p_equals_1_) {
         if (this == p_equals_1_) {
            return true;
         } else {
            return p_equals_1_ instanceof GameRules.RuleKey && ((GameRules.RuleKey)p_equals_1_).id.equals(this.id);
         }
      }

      public int hashCode() {
         return this.id.hashCode();
      }

      public String getId() {
         return this.id;
      }

      public String getDescriptionId() {
         return "gamerule." + this.id;
      }

      @OnlyIn(Dist.CLIENT)
      public GameRules.Category getCategory() {
         return this.category;
      }
   }

   public static class RuleType<T extends GameRules.RuleValue<T>> {
      private final Supplier<ArgumentType<?>> argument;
      private final Function<GameRules.RuleType<T>, T> constructor;
      private final BiConsumer<MinecraftServer, T> callback;
      private final GameRules.IRule<T> visitorCaller;

      private RuleType(Supplier<ArgumentType<?>> pArgument, Function<GameRules.RuleType<T>, T> pConstructor, BiConsumer<MinecraftServer, T> pCallback, GameRules.IRule<T> pVisitorCaller) {
         this.argument = pArgument;
         this.constructor = pConstructor;
         this.callback = pCallback;
         this.visitorCaller = pVisitorCaller;
      }

      public RequiredArgumentBuilder<CommandSource, ?> createArgument(String pName) {
         return Commands.argument(pName, this.argument.get());
      }

      public T createRule() {
         return this.constructor.apply(this);
      }

      public void callVisitor(GameRules.IRuleEntryVisitor pVisitor, GameRules.RuleKey<T> pKey) {
         this.visitorCaller.call(pVisitor, pKey, this);
      }
   }

   public abstract static class RuleValue<T extends GameRules.RuleValue<T>> {
      protected final GameRules.RuleType<T> type;

      public RuleValue(GameRules.RuleType<T> pType) {
         this.type = pType;
      }

      protected abstract void updateFromArgument(CommandContext<CommandSource> pContext, String pParamName);

      public void setFromArgument(CommandContext<CommandSource> pContext, String pParamName) {
         this.updateFromArgument(pContext, pParamName);
         this.onChanged(pContext.getSource().getServer());
      }

      protected void onChanged(@Nullable MinecraftServer pServer) {
         if (pServer != null) {
            this.type.callback.accept(pServer, this.getSelf());
         }

      }

      protected abstract void deserialize(String pValue);

      public abstract String serialize();

      public String toString() {
         return this.serialize();
      }

      public abstract int getCommandResult();

      protected abstract T getSelf();

      protected abstract T copy();

      @OnlyIn(Dist.CLIENT)
      public abstract void setFrom(T pValue, @Nullable MinecraftServer pServer);
   }
}