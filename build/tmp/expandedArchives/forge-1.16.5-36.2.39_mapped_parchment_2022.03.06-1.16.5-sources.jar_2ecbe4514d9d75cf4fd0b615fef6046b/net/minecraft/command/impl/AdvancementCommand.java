package net.minecraft.command.impl;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

public class AdvancementCommand {
   private static final SuggestionProvider<CommandSource> SUGGEST_ADVANCEMENTS = (p_198206_0_, p_198206_1_) -> {
      Collection<Advancement> collection = p_198206_0_.getSource().getServer().getAdvancements().getAllAdvancements();
      return ISuggestionProvider.suggestResource(collection.stream().map(Advancement::getId), p_198206_1_);
   };

   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      pDispatcher.register(Commands.literal("advancement").requires((p_198205_0_) -> {
         return p_198205_0_.hasPermission(2);
      }).then(Commands.literal("grant").then(Commands.argument("targets", EntityArgument.players()).then(Commands.literal("only").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes((p_198202_0_) -> {
         return perform(p_198202_0_.getSource(), EntityArgument.getPlayers(p_198202_0_, "targets"), AdvancementCommand.Action.GRANT, getAdvancements(ResourceLocationArgument.getAdvancement(p_198202_0_, "advancement"), AdvancementCommand.Mode.ONLY));
      }).then(Commands.argument("criterion", StringArgumentType.greedyString()).suggests((p_198209_0_, p_198209_1_) -> {
         return ISuggestionProvider.suggest(ResourceLocationArgument.getAdvancement(p_198209_0_, "advancement").getCriteria().keySet(), p_198209_1_);
      }).executes((p_198212_0_) -> {
         return performCriterion(p_198212_0_.getSource(), EntityArgument.getPlayers(p_198212_0_, "targets"), AdvancementCommand.Action.GRANT, ResourceLocationArgument.getAdvancement(p_198212_0_, "advancement"), StringArgumentType.getString(p_198212_0_, "criterion"));
      })))).then(Commands.literal("from").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes((p_198215_0_) -> {
         return perform(p_198215_0_.getSource(), EntityArgument.getPlayers(p_198215_0_, "targets"), AdvancementCommand.Action.GRANT, getAdvancements(ResourceLocationArgument.getAdvancement(p_198215_0_, "advancement"), AdvancementCommand.Mode.FROM));
      }))).then(Commands.literal("until").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes((p_198204_0_) -> {
         return perform(p_198204_0_.getSource(), EntityArgument.getPlayers(p_198204_0_, "targets"), AdvancementCommand.Action.GRANT, getAdvancements(ResourceLocationArgument.getAdvancement(p_198204_0_, "advancement"), AdvancementCommand.Mode.UNTIL));
      }))).then(Commands.literal("through").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes((p_198211_0_) -> {
         return perform(p_198211_0_.getSource(), EntityArgument.getPlayers(p_198211_0_, "targets"), AdvancementCommand.Action.GRANT, getAdvancements(ResourceLocationArgument.getAdvancement(p_198211_0_, "advancement"), AdvancementCommand.Mode.THROUGH));
      }))).then(Commands.literal("everything").executes((p_198217_0_) -> {
         return perform(p_198217_0_.getSource(), EntityArgument.getPlayers(p_198217_0_, "targets"), AdvancementCommand.Action.GRANT, p_198217_0_.getSource().getServer().getAdvancements().getAllAdvancements());
      })))).then(Commands.literal("revoke").then(Commands.argument("targets", EntityArgument.players()).then(Commands.literal("only").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes((p_198198_0_) -> {
         return perform(p_198198_0_.getSource(), EntityArgument.getPlayers(p_198198_0_, "targets"), AdvancementCommand.Action.REVOKE, getAdvancements(ResourceLocationArgument.getAdvancement(p_198198_0_, "advancement"), AdvancementCommand.Mode.ONLY));
      }).then(Commands.argument("criterion", StringArgumentType.greedyString()).suggests((p_198210_0_, p_198210_1_) -> {
         return ISuggestionProvider.suggest(ResourceLocationArgument.getAdvancement(p_198210_0_, "advancement").getCriteria().keySet(), p_198210_1_);
      }).executes((p_198200_0_) -> {
         return performCriterion(p_198200_0_.getSource(), EntityArgument.getPlayers(p_198200_0_, "targets"), AdvancementCommand.Action.REVOKE, ResourceLocationArgument.getAdvancement(p_198200_0_, "advancement"), StringArgumentType.getString(p_198200_0_, "criterion"));
      })))).then(Commands.literal("from").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes((p_198208_0_) -> {
         return perform(p_198208_0_.getSource(), EntityArgument.getPlayers(p_198208_0_, "targets"), AdvancementCommand.Action.REVOKE, getAdvancements(ResourceLocationArgument.getAdvancement(p_198208_0_, "advancement"), AdvancementCommand.Mode.FROM));
      }))).then(Commands.literal("until").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes((p_198201_0_) -> {
         return perform(p_198201_0_.getSource(), EntityArgument.getPlayers(p_198201_0_, "targets"), AdvancementCommand.Action.REVOKE, getAdvancements(ResourceLocationArgument.getAdvancement(p_198201_0_, "advancement"), AdvancementCommand.Mode.UNTIL));
      }))).then(Commands.literal("through").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes((p_198197_0_) -> {
         return perform(p_198197_0_.getSource(), EntityArgument.getPlayers(p_198197_0_, "targets"), AdvancementCommand.Action.REVOKE, getAdvancements(ResourceLocationArgument.getAdvancement(p_198197_0_, "advancement"), AdvancementCommand.Mode.THROUGH));
      }))).then(Commands.literal("everything").executes((p_198213_0_) -> {
         return perform(p_198213_0_.getSource(), EntityArgument.getPlayers(p_198213_0_, "targets"), AdvancementCommand.Action.REVOKE, p_198213_0_.getSource().getServer().getAdvancements().getAllAdvancements());
      })))));
   }

   /**
    * Performs the given action on each advancement in the list, for each player.
    * 
    * @return The number of affected advancements across all players.
    */
   private static int perform(CommandSource pSource, Collection<ServerPlayerEntity> pTargets, AdvancementCommand.Action pAction, Collection<Advancement> pAdvancements) {
      int i = 0;

      for(ServerPlayerEntity serverplayerentity : pTargets) {
         i += pAction.perform(serverplayerentity, pAdvancements);
      }

      if (i == 0) {
         if (pAdvancements.size() == 1) {
            if (pTargets.size() == 1) {
               throw new CommandException(new TranslationTextComponent(pAction.getKey() + ".one.to.one.failure", pAdvancements.iterator().next().getChatComponent(), pTargets.iterator().next().getDisplayName()));
            } else {
               throw new CommandException(new TranslationTextComponent(pAction.getKey() + ".one.to.many.failure", pAdvancements.iterator().next().getChatComponent(), pTargets.size()));
            }
         } else if (pTargets.size() == 1) {
            throw new CommandException(new TranslationTextComponent(pAction.getKey() + ".many.to.one.failure", pAdvancements.size(), pTargets.iterator().next().getDisplayName()));
         } else {
            throw new CommandException(new TranslationTextComponent(pAction.getKey() + ".many.to.many.failure", pAdvancements.size(), pTargets.size()));
         }
      } else {
         if (pAdvancements.size() == 1) {
            if (pTargets.size() == 1) {
               pSource.sendSuccess(new TranslationTextComponent(pAction.getKey() + ".one.to.one.success", pAdvancements.iterator().next().getChatComponent(), pTargets.iterator().next().getDisplayName()), true);
            } else {
               pSource.sendSuccess(new TranslationTextComponent(pAction.getKey() + ".one.to.many.success", pAdvancements.iterator().next().getChatComponent(), pTargets.size()), true);
            }
         } else if (pTargets.size() == 1) {
            pSource.sendSuccess(new TranslationTextComponent(pAction.getKey() + ".many.to.one.success", pAdvancements.size(), pTargets.iterator().next().getDisplayName()), true);
         } else {
            pSource.sendSuccess(new TranslationTextComponent(pAction.getKey() + ".many.to.many.success", pAdvancements.size(), pTargets.size()), true);
         }

         return i;
      }
   }

   /**
    * Updates a single criterion based on the given action.
    * 
    * @return The number of affected criteria across all players.
    */
   private static int performCriterion(CommandSource pSource, Collection<ServerPlayerEntity> pTargets, AdvancementCommand.Action pAction, Advancement pAdvancement, String pCriterionName) {
      int i = 0;
      if (!pAdvancement.getCriteria().containsKey(pCriterionName)) {
         throw new CommandException(new TranslationTextComponent("commands.advancement.criterionNotFound", pAdvancement.getChatComponent(), pCriterionName));
      } else {
         for(ServerPlayerEntity serverplayerentity : pTargets) {
            if (pAction.performCriterion(serverplayerentity, pAdvancement, pCriterionName)) {
               ++i;
            }
         }

         if (i == 0) {
            if (pTargets.size() == 1) {
               throw new CommandException(new TranslationTextComponent(pAction.getKey() + ".criterion.to.one.failure", pCriterionName, pAdvancement.getChatComponent(), pTargets.iterator().next().getDisplayName()));
            } else {
               throw new CommandException(new TranslationTextComponent(pAction.getKey() + ".criterion.to.many.failure", pCriterionName, pAdvancement.getChatComponent(), pTargets.size()));
            }
         } else {
            if (pTargets.size() == 1) {
               pSource.sendSuccess(new TranslationTextComponent(pAction.getKey() + ".criterion.to.one.success", pCriterionName, pAdvancement.getChatComponent(), pTargets.iterator().next().getDisplayName()), true);
            } else {
               pSource.sendSuccess(new TranslationTextComponent(pAction.getKey() + ".criterion.to.many.success", pCriterionName, pAdvancement.getChatComponent(), pTargets.size()), true);
            }

            return i;
         }
      }
   }

   /**
    * Gets all advancements that match the given mode.
    */
   private static List<Advancement> getAdvancements(Advancement pAdvancement, AdvancementCommand.Mode pMode) {
      List<Advancement> list = Lists.newArrayList();
      if (pMode.parents) {
         for(Advancement advancement = pAdvancement.getParent(); advancement != null; advancement = advancement.getParent()) {
            list.add(advancement);
         }
      }

      list.add(pAdvancement);
      if (pMode.children) {
         addChildren(pAdvancement, list);
      }

      return list;
   }

   /**
    * Recursively adds all children of the given advancement to the given list. Does not add the advancement itself to
    * the list.
    */
   private static void addChildren(Advancement pAdvancement, List<Advancement> pList) {
      for(Advancement advancement : pAdvancement.getChildren()) {
         pList.add(advancement);
         addChildren(advancement, pList);
      }

   }

   static enum Action {
      GRANT("grant") {
         /**
          * Applies this action to the given advancement.
          * 
          * @return True if the player was affected.
          */
         protected boolean perform(ServerPlayerEntity pPlayer, Advancement pAdvancement) {
            AdvancementProgress advancementprogress = pPlayer.getAdvancements().getOrStartProgress(pAdvancement);
            if (advancementprogress.isDone()) {
               return false;
            } else {
               for(String s : advancementprogress.getRemainingCriteria()) {
                  pPlayer.getAdvancements().award(pAdvancement, s);
               }

               return true;
            }
         }

         /**
          * Applies this action to the given criterion.
          * 
          * @return True if the player was affected.
          */
         protected boolean performCriterion(ServerPlayerEntity pPlayer, Advancement pAdvancement, String pCriterionName) {
            return pPlayer.getAdvancements().award(pAdvancement, pCriterionName);
         }
      },
      REVOKE("revoke") {
         /**
          * Applies this action to the given advancement.
          * 
          * @return True if the player was affected.
          */
         protected boolean perform(ServerPlayerEntity pPlayer, Advancement pAdvancement) {
            AdvancementProgress advancementprogress = pPlayer.getAdvancements().getOrStartProgress(pAdvancement);
            if (!advancementprogress.hasProgress()) {
               return false;
            } else {
               for(String s : advancementprogress.getCompletedCriteria()) {
                  pPlayer.getAdvancements().revoke(pAdvancement, s);
               }

               return true;
            }
         }

         /**
          * Applies this action to the given criterion.
          * 
          * @return True if the player was affected.
          */
         protected boolean performCriterion(ServerPlayerEntity pPlayer, Advancement pAdvancement, String pCriterionName) {
            return pPlayer.getAdvancements().revoke(pAdvancement, pCriterionName);
         }
      };

      private final String key;

      private Action(String p_i48092_3_) {
         this.key = "commands.advancement." + p_i48092_3_;
      }

      /**
       * Applies this action to all of the given advancements.
       * 
       * @return The number of players affected.
       */
      public int perform(ServerPlayerEntity pPlayer, Iterable<Advancement> pAdvancements) {
         int i = 0;

         for(Advancement advancement : pAdvancements) {
            if (this.perform(pPlayer, advancement)) {
               ++i;
            }
         }

         return i;
      }

      /**
       * Applies this action to the given advancement.
       * 
       * @return True if the player was affected.
       */
      protected abstract boolean perform(ServerPlayerEntity pPlayer, Advancement pAdvancement);

      /**
       * Applies this action to the given criterion.
       * 
       * @return True if the player was affected.
       */
      protected abstract boolean performCriterion(ServerPlayerEntity pPlayer, Advancement pAdvancement, String pCriterionName);

      protected String getKey() {
         return this.key;
      }
   }

   static enum Mode {
      ONLY(false, false),
      THROUGH(true, true),
      FROM(false, true),
      UNTIL(true, false),
      EVERYTHING(true, true);

      private final boolean parents;
      private final boolean children;

      private Mode(boolean p_i48091_3_, boolean p_i48091_4_) {
         this.parents = p_i48091_3_;
         this.children = p_i48091_4_;
      }
   }
}