package net.minecraft.command.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.world.server.ServerWorld;

public class EntitySelector {
   private final int maxResults;
   private final boolean includesEntities;
   private final boolean worldLimited;
   private final Predicate<Entity> predicate;
   private final MinMaxBounds.FloatBound range;
   private final Function<Vector3d, Vector3d> position;
   @Nullable
   private final AxisAlignedBB aabb;
   private final BiConsumer<Vector3d, List<? extends Entity>> order;
   private final boolean currentEntity;
   @Nullable
   private final String playerName;
   @Nullable
   private final UUID entityUUID;
   @Nullable
   private final EntityType<?> type;
   private final boolean usesSelector;

   public EntitySelector(int pMaxResults, boolean pIncludesEntities, boolean pWorldLimited, Predicate<Entity> pPredicate, MinMaxBounds.FloatBound pRange, Function<Vector3d, Vector3d> pPosition, @Nullable AxisAlignedBB pAabb, BiConsumer<Vector3d, List<? extends Entity>> pOrder, boolean pCurrentEntity, @Nullable String pPlayerName, @Nullable UUID pEntityUUID, @Nullable EntityType<?> pType, boolean pUsesSelector) {
      this.maxResults = pMaxResults;
      this.includesEntities = pIncludesEntities;
      this.worldLimited = pWorldLimited;
      this.predicate = pPredicate;
      this.range = pRange;
      this.position = pPosition;
      this.aabb = pAabb;
      this.order = pOrder;
      this.currentEntity = pCurrentEntity;
      this.playerName = pPlayerName;
      this.entityUUID = pEntityUUID;
      this.type = pType;
      this.usesSelector = pUsesSelector;
   }

   public int getMaxResults() {
      return this.maxResults;
   }

   public boolean includesEntities() {
      return this.includesEntities;
   }

   public boolean isSelfSelector() {
      return this.currentEntity;
   }

   public boolean isWorldLimited() {
      return this.worldLimited;
   }

   private void checkPermissions(CommandSource pSource) throws CommandSyntaxException {
      if (this.usesSelector && !pSource.hasPermission(2)) {
         throw EntityArgument.ERROR_SELECTORS_NOT_ALLOWED.create();
      }
   }

   public Entity findSingleEntity(CommandSource pSource) throws CommandSyntaxException {
      this.checkPermissions(pSource);
      List<? extends Entity> list = this.findEntities(pSource);
      if (list.isEmpty()) {
         throw EntityArgument.NO_ENTITIES_FOUND.create();
      } else if (list.size() > 1) {
         throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
      } else {
         return list.get(0);
      }
   }

   public List<? extends Entity> findEntities(CommandSource pSource) throws CommandSyntaxException {
      this.checkPermissions(pSource);
      if (!this.includesEntities) {
         return this.findPlayers(pSource);
      } else if (this.playerName != null) {
         ServerPlayerEntity serverplayerentity = pSource.getServer().getPlayerList().getPlayerByName(this.playerName);
         return (List<? extends Entity>)(serverplayerentity == null ? Collections.emptyList() : Lists.newArrayList(serverplayerentity));
      } else if (this.entityUUID != null) {
         for(ServerWorld serverworld1 : pSource.getServer().getAllLevels()) {
            Entity entity = serverworld1.getEntity(this.entityUUID);
            if (entity != null) {
               return Lists.newArrayList(entity);
            }
         }

         return Collections.emptyList();
      } else {
         Vector3d vector3d = this.position.apply(pSource.getPosition());
         Predicate<Entity> predicate = this.getPredicate(vector3d);
         if (this.currentEntity) {
            return (List<? extends Entity>)(pSource.getEntity() != null && predicate.test(pSource.getEntity()) ? Lists.newArrayList(pSource.getEntity()) : Collections.emptyList());
         } else {
            List<Entity> list = Lists.newArrayList();
            if (this.isWorldLimited()) {
               this.addEntities(list, pSource.getLevel(), vector3d, predicate);
            } else {
               for(ServerWorld serverworld : pSource.getServer().getAllLevels()) {
                  this.addEntities(list, serverworld, vector3d, predicate);
               }
            }

            return this.sortAndLimit(vector3d, list);
         }
      }
   }

   /**
    * Gets all entities matching this selector, and adds them to the passed list.
    */
   private void addEntities(List<Entity> pResult, ServerWorld pLevel, Vector3d pPos, Predicate<Entity> pPredicate) {
      if (this.aabb != null) {
         pResult.addAll(pLevel.getEntities(this.type, this.aabb.move(pPos), pPredicate));
      } else {
         pResult.addAll(pLevel.getEntities(this.type, pPredicate));
      }

   }

   public ServerPlayerEntity findSinglePlayer(CommandSource pSource) throws CommandSyntaxException {
      this.checkPermissions(pSource);
      List<ServerPlayerEntity> list = this.findPlayers(pSource);
      if (list.size() != 1) {
         throw EntityArgument.NO_PLAYERS_FOUND.create();
      } else {
         return list.get(0);
      }
   }

   public List<ServerPlayerEntity> findPlayers(CommandSource pSource) throws CommandSyntaxException {
      this.checkPermissions(pSource);
      if (this.playerName != null) {
         ServerPlayerEntity serverplayerentity2 = pSource.getServer().getPlayerList().getPlayerByName(this.playerName);
         return (List<ServerPlayerEntity>)(serverplayerentity2 == null ? Collections.emptyList() : Lists.newArrayList(serverplayerentity2));
      } else if (this.entityUUID != null) {
         ServerPlayerEntity serverplayerentity1 = pSource.getServer().getPlayerList().getPlayer(this.entityUUID);
         return (List<ServerPlayerEntity>)(serverplayerentity1 == null ? Collections.emptyList() : Lists.newArrayList(serverplayerentity1));
      } else {
         Vector3d vector3d = this.position.apply(pSource.getPosition());
         Predicate<Entity> predicate = this.getPredicate(vector3d);
         if (this.currentEntity) {
            if (pSource.getEntity() instanceof ServerPlayerEntity) {
               ServerPlayerEntity serverplayerentity3 = (ServerPlayerEntity)pSource.getEntity();
               if (predicate.test(serverplayerentity3)) {
                  return Lists.newArrayList(serverplayerentity3);
               }
            }

            return Collections.emptyList();
         } else {
            List<ServerPlayerEntity> list;
            if (this.isWorldLimited()) {
               list = pSource.getLevel().getPlayers(predicate::test);
            } else {
               list = Lists.newArrayList();

               for(ServerPlayerEntity serverplayerentity : pSource.getServer().getPlayerList().getPlayers()) {
                  if (predicate.test(serverplayerentity)) {
                     list.add(serverplayerentity);
                  }
               }
            }

            return this.sortAndLimit(vector3d, list);
         }
      }
   }

   /**
    * Returns a modified version of the predicate on this selector that also checks the AABB and distance.
    */
   private Predicate<Entity> getPredicate(Vector3d pPos) {
      Predicate<Entity> predicate = this.predicate;
      if (this.aabb != null) {
         AxisAlignedBB axisalignedbb = this.aabb.move(pPos);
         predicate = predicate.and((p_197344_1_) -> {
            return axisalignedbb.intersects(p_197344_1_.getBoundingBox());
         });
      }

      if (!this.range.isAny()) {
         predicate = predicate.and((p_211376_2_) -> {
            return this.range.matchesSqr(p_211376_2_.distanceToSqr(pPos));
         });
      }

      return predicate;
   }

   private <T extends Entity> List<T> sortAndLimit(Vector3d pPos, List<T> pEntities) {
      if (pEntities.size() > 1) {
         this.order.accept(pPos, pEntities);
      }

      return pEntities.subList(0, Math.min(this.maxResults, pEntities.size()));
   }

   public static IFormattableTextComponent joinNames(List<? extends Entity> pEntities) {
      return TextComponentUtils.formatList(pEntities, Entity::getDisplayName);
   }
}