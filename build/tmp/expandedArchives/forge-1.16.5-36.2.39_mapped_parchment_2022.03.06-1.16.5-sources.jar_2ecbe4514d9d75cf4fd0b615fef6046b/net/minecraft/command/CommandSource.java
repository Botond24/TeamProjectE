package net.minecraft.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class CommandSource implements ISuggestionProvider {
   public static final SimpleCommandExceptionType ERROR_NOT_PLAYER = new SimpleCommandExceptionType(new TranslationTextComponent("permissions.requires.player"));
   public static final SimpleCommandExceptionType ERROR_NOT_ENTITY = new SimpleCommandExceptionType(new TranslationTextComponent("permissions.requires.entity"));
   private final ICommandSource source;
   private final Vector3d worldPosition;
   private final ServerWorld level;
   private final int permissionLevel;
   private final String textName;
   private final ITextComponent displayName;
   private final MinecraftServer server;
   private final boolean silent;
   @Nullable
   private final Entity entity;
   private final ResultConsumer<CommandSource> consumer;
   private final EntityAnchorArgument.Type anchor;
   private final Vector2f rotation;

   public CommandSource(ICommandSource pSource, Vector3d pWorldPosition, Vector2f pRotation, ServerWorld pLevel, int pPermissionLevel, String pTextName, ITextComponent pDisplayName, MinecraftServer pServer, @Nullable Entity pEntity) {
      this(pSource, pWorldPosition, pRotation, pLevel, pPermissionLevel, pTextName, pDisplayName, pServer, pEntity, false, (p_197032_0_, p_197032_1_, p_197032_2_) -> {
      }, EntityAnchorArgument.Type.FEET);
   }

   protected CommandSource(ICommandSource pSource, Vector3d pWorldPosition, Vector2f pRotation, ServerWorld pLevel, int pPermissionLevel, String pTextName, ITextComponent pDisplayName, MinecraftServer pServer, @Nullable Entity pEntity, boolean pSilent, ResultConsumer<CommandSource> pConsumer, EntityAnchorArgument.Type pAnchor) {
      this.source = pSource;
      this.worldPosition = pWorldPosition;
      this.level = pLevel;
      this.silent = pSilent;
      this.entity = pEntity;
      this.permissionLevel = pPermissionLevel;
      this.textName = pTextName;
      this.displayName = pDisplayName;
      this.server = pServer;
      this.consumer = pConsumer;
      this.anchor = pAnchor;
      this.rotation = pRotation;
   }

   public CommandSource withEntity(Entity pEntity) {
      return this.entity == pEntity ? this : new CommandSource(this.source, this.worldPosition, this.rotation, this.level, this.permissionLevel, pEntity.getName().getString(), pEntity.getDisplayName(), this.server, pEntity, this.silent, this.consumer, this.anchor);
   }

   public CommandSource withPosition(Vector3d pPos) {
      return this.worldPosition.equals(pPos) ? this : new CommandSource(this.source, pPos, this.rotation, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.consumer, this.anchor);
   }

   public CommandSource withRotation(Vector2f pRotation) {
      return this.rotation.equals(pRotation) ? this : new CommandSource(this.source, this.worldPosition, pRotation, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.consumer, this.anchor);
   }

   public CommandSource withCallback(ResultConsumer<CommandSource> pConsumer) {
      return this.consumer.equals(pConsumer) ? this : new CommandSource(this.source, this.worldPosition, this.rotation, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, pConsumer, this.anchor);
   }

   public CommandSource withCallback(ResultConsumer<CommandSource> pResultConsumer, BinaryOperator<ResultConsumer<CommandSource>> pResultConsumerSelector) {
      ResultConsumer<CommandSource> resultconsumer = pResultConsumerSelector.apply(this.consumer, pResultConsumer);
      return this.withCallback(resultconsumer);
   }

   public CommandSource withSuppressedOutput() {
      return this.silent ? this : new CommandSource(this.source, this.worldPosition, this.rotation, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, true, this.consumer, this.anchor);
   }

   public CommandSource withPermission(int pPermissionLevel) {
      return pPermissionLevel == this.permissionLevel ? this : new CommandSource(this.source, this.worldPosition, this.rotation, this.level, pPermissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.consumer, this.anchor);
   }

   public CommandSource withMaximumPermission(int pPermissionLevel) {
      return pPermissionLevel <= this.permissionLevel ? this : new CommandSource(this.source, this.worldPosition, this.rotation, this.level, pPermissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.consumer, this.anchor);
   }

   public CommandSource withAnchor(EntityAnchorArgument.Type pAnchor) {
      return pAnchor == this.anchor ? this : new CommandSource(this.source, this.worldPosition, this.rotation, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.consumer, pAnchor);
   }

   public CommandSource withLevel(ServerWorld pLevel) {
      if (pLevel == this.level) {
         return this;
      } else {
         double d0 = DimensionType.getTeleportationScale(this.level.dimensionType(), pLevel.dimensionType());
         Vector3d vector3d = new Vector3d(this.worldPosition.x * d0, this.worldPosition.y, this.worldPosition.z * d0);
         return new CommandSource(this.source, vector3d, this.rotation, pLevel, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.consumer, this.anchor);
      }
   }

   public CommandSource facing(Entity pEntity, EntityAnchorArgument.Type pAnchor) throws CommandSyntaxException {
      return this.facing(pAnchor.apply(pEntity));
   }

   public CommandSource facing(Vector3d pLookPos) throws CommandSyntaxException {
      Vector3d vector3d = this.anchor.apply(this);
      double d0 = pLookPos.x - vector3d.x;
      double d1 = pLookPos.y - vector3d.y;
      double d2 = pLookPos.z - vector3d.z;
      double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
      float f = MathHelper.wrapDegrees((float)(-(MathHelper.atan2(d1, d3) * (double)(180F / (float)Math.PI))));
      float f1 = MathHelper.wrapDegrees((float)(MathHelper.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F);
      return this.withRotation(new Vector2f(f, f1));
   }

   public ITextComponent getDisplayName() {
      return this.displayName;
   }

   public String getTextName() {
      return this.textName;
   }

   public boolean hasPermission(int pLevel) {
      return this.permissionLevel >= pLevel;
   }

   public Vector3d getPosition() {
      return this.worldPosition;
   }

   public ServerWorld getLevel() {
      return this.level;
   }

   @Nullable
   public Entity getEntity() {
      return this.entity;
   }

   public Entity getEntityOrException() throws CommandSyntaxException {
      if (this.entity == null) {
         throw ERROR_NOT_ENTITY.create();
      } else {
         return this.entity;
      }
   }

   public ServerPlayerEntity getPlayerOrException() throws CommandSyntaxException {
      if (!(this.entity instanceof ServerPlayerEntity)) {
         throw ERROR_NOT_PLAYER.create();
      } else {
         return (ServerPlayerEntity)this.entity;
      }
   }

   public Vector2f getRotation() {
      return this.rotation;
   }

   public MinecraftServer getServer() {
      return this.server;
   }

   public EntityAnchorArgument.Type getAnchor() {
      return this.anchor;
   }

   public void sendSuccess(ITextComponent pMessage, boolean pAllowLogging) {
      if (this.source.acceptsSuccess() && !this.silent) {
         this.source.sendMessage(pMessage, Util.NIL_UUID);
      }

      if (pAllowLogging && this.source.shouldInformAdmins() && !this.silent) {
         this.broadcastToAdmins(pMessage);
      }

   }

   private void broadcastToAdmins(ITextComponent pMessage) {
      ITextComponent itextcomponent = (new TranslationTextComponent("chat.type.admin", this.getDisplayName(), pMessage)).withStyle(new TextFormatting[]{TextFormatting.GRAY, TextFormatting.ITALIC});
      if (this.server.getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
         for(ServerPlayerEntity serverplayerentity : this.server.getPlayerList().getPlayers()) {
            if (serverplayerentity != this.source && this.server.getPlayerList().isOp(serverplayerentity.getGameProfile())) {
               serverplayerentity.sendMessage(itextcomponent, Util.NIL_UUID);
            }
         }
      }

      if (this.source != this.server && this.server.getGameRules().getBoolean(GameRules.RULE_LOGADMINCOMMANDS)) {
         this.server.sendMessage(itextcomponent, Util.NIL_UUID);
      }

   }

   public void sendFailure(ITextComponent pMessage) {
      if (this.source.acceptsFailure() && !this.silent) {
         this.source.sendMessage((new StringTextComponent("")).append(pMessage).withStyle(TextFormatting.RED), Util.NIL_UUID);
      }

   }

   public void onCommandComplete(CommandContext<CommandSource> pContext, boolean pSuccess, int pResult) {
      if (this.consumer != null) {
         this.consumer.onCommandComplete(pContext, pSuccess, pResult);
      }

   }

   public Collection<String> getOnlinePlayerNames() {
      return Lists.newArrayList(this.server.getPlayerNames());
   }

   public Collection<String> getAllTeams() {
      return this.server.getScoreboard().getTeamNames();
   }

   public Collection<ResourceLocation> getAvailableSoundEvents() {
      return Registry.SOUND_EVENT.keySet();
   }

   public Stream<ResourceLocation> getRecipeNames() {
      return this.server.getRecipeManager().getRecipeIds();
   }

   public CompletableFuture<Suggestions> customSuggestion(CommandContext<ISuggestionProvider> pContext, SuggestionsBuilder pSuggestionsBuilder) {
      return null;
   }

   public Set<RegistryKey<World>> levels() {
      return this.server.levelKeys();
   }

   public DynamicRegistries registryAccess() {
      return this.server.registryAccess();
   }
}