package net.minecraft.command.impl;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.IntFunction;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.BlockPredicateArgument;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.IRangeArgument;
import net.minecraft.command.arguments.NBTPathArgument;
import net.minecraft.command.arguments.ObjectiveArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.RotationArgument;
import net.minecraft.command.arguments.ScoreHolderArgument;
import net.minecraft.command.arguments.SwizzleArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.command.impl.data.DataCommand;
import net.minecraft.command.impl.data.IDataAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootPredicateManager;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.nbt.ShortNBT;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.CustomServerBossInfo;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public class ExecuteCommand {
   private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType((p_208885_0_, p_208885_1_) -> {
      return new TranslationTextComponent("commands.execute.blocks.toobig", p_208885_0_, p_208885_1_);
   });
   private static final SimpleCommandExceptionType ERROR_CONDITIONAL_FAILED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.execute.conditional.fail"));
   private static final DynamicCommandExceptionType ERROR_CONDITIONAL_FAILED_COUNT = new DynamicCommandExceptionType((p_210446_0_) -> {
      return new TranslationTextComponent("commands.execute.conditional.fail_count", p_210446_0_);
   });
   private static final BinaryOperator<ResultConsumer<CommandSource>> CALLBACK_CHAINER = (p_209937_0_, p_209937_1_) -> {
      return (p_209939_2_, p_209939_3_, p_209939_4_) -> {
         p_209937_0_.onCommandComplete(p_209939_2_, p_209939_3_, p_209939_4_);
         p_209937_1_.onCommandComplete(p_209939_2_, p_209939_3_, p_209939_4_);
      };
   };
   private static final SuggestionProvider<CommandSource> SUGGEST_PREDICATE = (p_229763_0_, p_229763_1_) -> {
      LootPredicateManager lootpredicatemanager = p_229763_0_.getSource().getServer().getPredicateManager();
      return ISuggestionProvider.suggestResource(lootpredicatemanager.getKeys(), p_229763_1_);
   };

   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      LiteralCommandNode<CommandSource> literalcommandnode = pDispatcher.register(Commands.literal("execute").requires((p_198387_0_) -> {
         return p_198387_0_.hasPermission(2);
      }));
      pDispatcher.register(Commands.literal("execute").requires((p_229766_0_) -> {
         return p_229766_0_.hasPermission(2);
      }).then(Commands.literal("run").redirect(pDispatcher.getRoot())).then(addConditionals(literalcommandnode, Commands.literal("if"), true)).then(addConditionals(literalcommandnode, Commands.literal("unless"), false)).then(Commands.literal("as").then(Commands.argument("targets", EntityArgument.entities()).fork(literalcommandnode, (p_198384_0_) -> {
         List<CommandSource> list = Lists.newArrayList();

         for(Entity entity : EntityArgument.getOptionalEntities(p_198384_0_, "targets")) {
            list.add(p_198384_0_.getSource().withEntity(entity));
         }

         return list;
      }))).then(Commands.literal("at").then(Commands.argument("targets", EntityArgument.entities()).fork(literalcommandnode, (p_229809_0_) -> {
         List<CommandSource> list = Lists.newArrayList();

         for(Entity entity : EntityArgument.getOptionalEntities(p_229809_0_, "targets")) {
            list.add(p_229809_0_.getSource().withLevel((ServerWorld)entity.level).withPosition(entity.position()).withRotation(entity.getRotationVector()));
         }

         return list;
      }))).then(Commands.literal("store").then(wrapStores(literalcommandnode, Commands.literal("result"), true)).then(wrapStores(literalcommandnode, Commands.literal("success"), false))).then(Commands.literal("positioned").then(Commands.argument("pos", Vec3Argument.vec3()).redirect(literalcommandnode, (p_229808_0_) -> {
         return p_229808_0_.getSource().withPosition(Vec3Argument.getVec3(p_229808_0_, "pos")).withAnchor(EntityAnchorArgument.Type.FEET);
      })).then(Commands.literal("as").then(Commands.argument("targets", EntityArgument.entities()).fork(literalcommandnode, (p_229807_0_) -> {
         List<CommandSource> list = Lists.newArrayList();

         for(Entity entity : EntityArgument.getOptionalEntities(p_229807_0_, "targets")) {
            list.add(p_229807_0_.getSource().withPosition(entity.position()));
         }

         return list;
      })))).then(Commands.literal("rotated").then(Commands.argument("rot", RotationArgument.rotation()).redirect(literalcommandnode, (p_229806_0_) -> {
         return p_229806_0_.getSource().withRotation(RotationArgument.getRotation(p_229806_0_, "rot").getRotation(p_229806_0_.getSource()));
      })).then(Commands.literal("as").then(Commands.argument("targets", EntityArgument.entities()).fork(literalcommandnode, (p_201083_0_) -> {
         List<CommandSource> list = Lists.newArrayList();

         for(Entity entity : EntityArgument.getOptionalEntities(p_201083_0_, "targets")) {
            list.add(p_201083_0_.getSource().withRotation(entity.getRotationVector()));
         }

         return list;
      })))).then(Commands.literal("facing").then(Commands.literal("entity").then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("anchor", EntityAnchorArgument.anchor()).fork(literalcommandnode, (p_229805_0_) -> {
         List<CommandSource> list = Lists.newArrayList();
         EntityAnchorArgument.Type entityanchorargument$type = EntityAnchorArgument.getAnchor(p_229805_0_, "anchor");

         for(Entity entity : EntityArgument.getOptionalEntities(p_229805_0_, "targets")) {
            list.add(p_229805_0_.getSource().facing(entity, entityanchorargument$type));
         }

         return list;
      })))).then(Commands.argument("pos", Vec3Argument.vec3()).redirect(literalcommandnode, (p_198381_0_) -> {
         return p_198381_0_.getSource().facing(Vec3Argument.getVec3(p_198381_0_, "pos"));
      }))).then(Commands.literal("align").then(Commands.argument("axes", SwizzleArgument.swizzle()).redirect(literalcommandnode, (p_201091_0_) -> {
         return p_201091_0_.getSource().withPosition(p_201091_0_.getSource().getPosition().align(SwizzleArgument.getSwizzle(p_201091_0_, "axes")));
      }))).then(Commands.literal("anchored").then(Commands.argument("anchor", EntityAnchorArgument.anchor()).redirect(literalcommandnode, (p_201089_0_) -> {
         return p_201089_0_.getSource().withAnchor(EntityAnchorArgument.getAnchor(p_201089_0_, "anchor"));
      }))).then(Commands.literal("in").then(Commands.argument("dimension", DimensionArgument.dimension()).redirect(literalcommandnode, (p_229804_0_) -> {
         return p_229804_0_.getSource().withLevel(DimensionArgument.getDimension(p_229804_0_, "dimension"));
      }))));
   }

   private static ArgumentBuilder<CommandSource, ?> wrapStores(LiteralCommandNode<CommandSource> pParent, LiteralArgumentBuilder<CommandSource> pLiteral, boolean pStoringResult) {
      pLiteral.then(Commands.literal("score").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective()).redirect(pParent, (p_201468_1_) -> {
         return storeValue(p_201468_1_.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(p_201468_1_, "targets"), ObjectiveArgument.getObjective(p_201468_1_, "objective"), pStoringResult);
      }))));
      pLiteral.then(Commands.literal("bossbar").then(Commands.argument("id", ResourceLocationArgument.id()).suggests(BossBarCommand.SUGGEST_BOSS_BAR).then(Commands.literal("value").redirect(pParent, (p_201457_1_) -> {
         return storeValue(p_201457_1_.getSource(), BossBarCommand.getBossBar(p_201457_1_), true, pStoringResult);
      })).then(Commands.literal("max").redirect(pParent, (p_229795_1_) -> {
         return storeValue(p_229795_1_.getSource(), BossBarCommand.getBossBar(p_229795_1_), false, pStoringResult);
      }))));

      for(DataCommand.IDataProvider datacommand$idataprovider : DataCommand.TARGET_PROVIDERS) {
         datacommand$idataprovider.wrap(pLiteral, (p_229765_3_) -> {
            return p_229765_3_.then(Commands.argument("path", NBTPathArgument.nbtPath()).then(Commands.literal("int").then(Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect(pParent, (p_229801_2_) -> {
               return storeData(p_229801_2_.getSource(), datacommand$idataprovider.access(p_229801_2_), NBTPathArgument.getPath(p_229801_2_, "path"), (p_229800_1_) -> {
                  return IntNBT.valueOf((int)((double)p_229800_1_ * DoubleArgumentType.getDouble(p_229801_2_, "scale")));
               }, pStoringResult);
            }))).then(Commands.literal("float").then(Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect(pParent, (p_229798_2_) -> {
               return storeData(p_229798_2_.getSource(), datacommand$idataprovider.access(p_229798_2_), NBTPathArgument.getPath(p_229798_2_, "path"), (p_229797_1_) -> {
                  return FloatNBT.valueOf((float)((double)p_229797_1_ * DoubleArgumentType.getDouble(p_229798_2_, "scale")));
               }, pStoringResult);
            }))).then(Commands.literal("short").then(Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect(pParent, (p_229794_2_) -> {
               return storeData(p_229794_2_.getSource(), datacommand$idataprovider.access(p_229794_2_), NBTPathArgument.getPath(p_229794_2_, "path"), (p_229792_1_) -> {
                  return ShortNBT.valueOf((short)((int)((double)p_229792_1_ * DoubleArgumentType.getDouble(p_229794_2_, "scale"))));
               }, pStoringResult);
            }))).then(Commands.literal("long").then(Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect(pParent, (p_229790_2_) -> {
               return storeData(p_229790_2_.getSource(), datacommand$idataprovider.access(p_229790_2_), NBTPathArgument.getPath(p_229790_2_, "path"), (p_229788_1_) -> {
                  return LongNBT.valueOf((long)((double)p_229788_1_ * DoubleArgumentType.getDouble(p_229790_2_, "scale")));
               }, pStoringResult);
            }))).then(Commands.literal("double").then(Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect(pParent, (p_229784_2_) -> {
               return storeData(p_229784_2_.getSource(), datacommand$idataprovider.access(p_229784_2_), NBTPathArgument.getPath(p_229784_2_, "path"), (p_229781_1_) -> {
                  return DoubleNBT.valueOf((double)p_229781_1_ * DoubleArgumentType.getDouble(p_229784_2_, "scale"));
               }, pStoringResult);
            }))).then(Commands.literal("byte").then(Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect(pParent, (p_229774_2_) -> {
               return storeData(p_229774_2_.getSource(), datacommand$idataprovider.access(p_229774_2_), NBTPathArgument.getPath(p_229774_2_, "path"), (p_229762_1_) -> {
                  return ByteNBT.valueOf((byte)((int)((double)p_229762_1_ * DoubleArgumentType.getDouble(p_229774_2_, "scale"))));
               }, pStoringResult);
            }))));
         });
      }

      return pLiteral;
   }

   private static CommandSource storeValue(CommandSource pSource, Collection<String> pTargets, ScoreObjective pObjective, boolean pStoringResult) {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();
      return pSource.withCallback((p_229769_4_, p_229769_5_, p_229769_6_) -> {
         for(String s : pTargets) {
            Score score = scoreboard.getOrCreatePlayerScore(s, pObjective);
            int i = pStoringResult ? p_229769_6_ : (p_229769_5_ ? 1 : 0);
            score.setScore(i);
         }

      }, CALLBACK_CHAINER);
   }

   private static CommandSource storeValue(CommandSource pSource, CustomServerBossInfo pBar, boolean pStoringValue, boolean pStoringResult) {
      return pSource.withCallback((p_229779_3_, p_229779_4_, p_229779_5_) -> {
         int i = pStoringResult ? p_229779_5_ : (p_229779_4_ ? 1 : 0);
         if (pStoringValue) {
            pBar.setValue(i);
         } else {
            pBar.setMax(i);
         }

      }, CALLBACK_CHAINER);
   }

   private static CommandSource storeData(CommandSource pSource, IDataAccessor pAccessor, NBTPathArgument.NBTPath pPath, IntFunction<INBT> pTagConverter, boolean pStoringResult) {
      return pSource.withCallback((p_229772_4_, p_229772_5_, p_229772_6_) -> {
         try {
            CompoundNBT compoundnbt = pAccessor.getData();
            int i = pStoringResult ? p_229772_6_ : (p_229772_5_ ? 1 : 0);
            pPath.set(compoundnbt, () -> {
               return pTagConverter.apply(i);
            });
            pAccessor.setData(compoundnbt);
         } catch (CommandSyntaxException commandsyntaxexception) {
         }

      }, CALLBACK_CHAINER);
   }

   private static ArgumentBuilder<CommandSource, ?> addConditionals(CommandNode<CommandSource> pParent, LiteralArgumentBuilder<CommandSource> pLiteral, boolean pIsIf) {
      pLiteral.then(Commands.literal("block").then(Commands.argument("pos", BlockPosArgument.blockPos()).then(addConditional(pParent, Commands.argument("block", BlockPredicateArgument.blockPredicate()), pIsIf, (p_210438_0_) -> {
         return BlockPredicateArgument.getBlockPredicate(p_210438_0_, "block").test(new CachedBlockInfo(p_210438_0_.getSource().getLevel(), BlockPosArgument.getLoadedBlockPos(p_210438_0_, "pos"), true));
      })))).then(Commands.literal("score").then(Commands.argument("target", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("targetObjective", ObjectiveArgument.objective()).then(Commands.literal("=").then(Commands.argument("source", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(addConditional(pParent, Commands.argument("sourceObjective", ObjectiveArgument.objective()), pIsIf, (p_229803_0_) -> {
         return checkScore(p_229803_0_, Integer::equals);
      })))).then(Commands.literal("<").then(Commands.argument("source", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(addConditional(pParent, Commands.argument("sourceObjective", ObjectiveArgument.objective()), pIsIf, (p_229802_0_) -> {
         return checkScore(p_229802_0_, (p_229793_0_, p_229793_1_) -> {
            return p_229793_0_ < p_229793_1_;
         });
      })))).then(Commands.literal("<=").then(Commands.argument("source", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(addConditional(pParent, Commands.argument("sourceObjective", ObjectiveArgument.objective()), pIsIf, (p_229799_0_) -> {
         return checkScore(p_229799_0_, (p_229789_0_, p_229789_1_) -> {
            return p_229789_0_ <= p_229789_1_;
         });
      })))).then(Commands.literal(">").then(Commands.argument("source", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(addConditional(pParent, Commands.argument("sourceObjective", ObjectiveArgument.objective()), pIsIf, (p_229796_0_) -> {
         return checkScore(p_229796_0_, (p_229782_0_, p_229782_1_) -> {
            return p_229782_0_ > p_229782_1_;
         });
      })))).then(Commands.literal(">=").then(Commands.argument("source", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(addConditional(pParent, Commands.argument("sourceObjective", ObjectiveArgument.objective()), pIsIf, (p_201088_0_) -> {
         return checkScore(p_201088_0_, (p_229768_0_, p_229768_1_) -> {
            return p_229768_0_ >= p_229768_1_;
         });
      })))).then(Commands.literal("matches").then(addConditional(pParent, Commands.argument("range", IRangeArgument.intRange()), pIsIf, (p_229787_0_) -> {
         return checkScore(p_229787_0_, IRangeArgument.IntRange.getRange(p_229787_0_, "range"));
      })))))).then(Commands.literal("blocks").then(Commands.argument("start", BlockPosArgument.blockPos()).then(Commands.argument("end", BlockPosArgument.blockPos()).then(Commands.argument("destination", BlockPosArgument.blockPos()).then(addIfBlocksConditional(pParent, Commands.literal("all"), pIsIf, false)).then(addIfBlocksConditional(pParent, Commands.literal("masked"), pIsIf, true)))))).then(Commands.literal("entity").then(Commands.argument("entities", EntityArgument.entities()).fork(pParent, (p_229791_1_) -> {
         return expect(p_229791_1_, pIsIf, !EntityArgument.getOptionalEntities(p_229791_1_, "entities").isEmpty());
      }).executes(createNumericConditionalHandler(pIsIf, (p_229780_0_) -> {
         return EntityArgument.getOptionalEntities(p_229780_0_, "entities").size();
      })))).then(Commands.literal("predicate").then(addConditional(pParent, Commands.argument("predicate", ResourceLocationArgument.id()).suggests(SUGGEST_PREDICATE), pIsIf, (p_229761_0_) -> {
         return checkCustomPredicate(p_229761_0_.getSource(), ResourceLocationArgument.getPredicate(p_229761_0_, "predicate"));
      })));

      for(DataCommand.IDataProvider datacommand$idataprovider : DataCommand.SOURCE_PROVIDERS) {
         pLiteral.then(datacommand$idataprovider.wrap(Commands.literal("data"), (p_229764_3_) -> {
            return p_229764_3_.then(Commands.argument("path", NBTPathArgument.nbtPath()).fork(pParent, (p_229777_2_) -> {
               return expect(p_229777_2_, pIsIf, checkMatchingData(datacommand$idataprovider.access(p_229777_2_), NBTPathArgument.getPath(p_229777_2_, "path")) > 0);
            }).executes(createNumericConditionalHandler(pIsIf, (p_229773_1_) -> {
               return checkMatchingData(datacommand$idataprovider.access(p_229773_1_), NBTPathArgument.getPath(p_229773_1_, "path"));
            })));
         }));
      }

      return pLiteral;
   }

   private static Command<CommandSource> createNumericConditionalHandler(boolean p_218834_0_, ExecuteCommand.INumericTest p_218834_1_) {
      return p_218834_0_ ? (p_229783_1_) -> {
         int i = p_218834_1_.test(p_229783_1_);
         if (i > 0) {
            p_229783_1_.getSource().sendSuccess(new TranslationTextComponent("commands.execute.conditional.pass_count", i), false);
            return i;
         } else {
            throw ERROR_CONDITIONAL_FAILED.create();
         }
      } : (p_229771_1_) -> {
         int i = p_218834_1_.test(p_229771_1_);
         if (i == 0) {
            p_229771_1_.getSource().sendSuccess(new TranslationTextComponent("commands.execute.conditional.pass"), false);
            return 1;
         } else {
            throw ERROR_CONDITIONAL_FAILED_COUNT.create(i);
         }
      };
   }

   private static int checkMatchingData(IDataAccessor p_218831_0_, NBTPathArgument.NBTPath p_218831_1_) throws CommandSyntaxException {
      return p_218831_1_.countMatching(p_218831_0_.getData());
   }

   private static boolean checkScore(CommandContext<CommandSource> pContext, BiPredicate<Integer, Integer> pComparison) throws CommandSyntaxException {
      String s = ScoreHolderArgument.getName(pContext, "target");
      ScoreObjective scoreobjective = ObjectiveArgument.getObjective(pContext, "targetObjective");
      String s1 = ScoreHolderArgument.getName(pContext, "source");
      ScoreObjective scoreobjective1 = ObjectiveArgument.getObjective(pContext, "sourceObjective");
      Scoreboard scoreboard = pContext.getSource().getServer().getScoreboard();
      if (scoreboard.hasPlayerScore(s, scoreobjective) && scoreboard.hasPlayerScore(s1, scoreobjective1)) {
         Score score = scoreboard.getOrCreatePlayerScore(s, scoreobjective);
         Score score1 = scoreboard.getOrCreatePlayerScore(s1, scoreobjective1);
         return pComparison.test(score.getScore(), score1.getScore());
      } else {
         return false;
      }
   }

   private static boolean checkScore(CommandContext<CommandSource> pContext, MinMaxBounds.IntBound pBounds) throws CommandSyntaxException {
      String s = ScoreHolderArgument.getName(pContext, "target");
      ScoreObjective scoreobjective = ObjectiveArgument.getObjective(pContext, "targetObjective");
      Scoreboard scoreboard = pContext.getSource().getServer().getScoreboard();
      return !scoreboard.hasPlayerScore(s, scoreobjective) ? false : pBounds.matches(scoreboard.getOrCreatePlayerScore(s, scoreobjective).getScore());
   }

   private static boolean checkCustomPredicate(CommandSource p_229767_0_, ILootCondition p_229767_1_) {
      ServerWorld serverworld = p_229767_0_.getLevel();
      LootContext.Builder lootcontext$builder = (new LootContext.Builder(serverworld)).withParameter(LootParameters.ORIGIN, p_229767_0_.getPosition()).withOptionalParameter(LootParameters.THIS_ENTITY, p_229767_0_.getEntity());
      return p_229767_1_.test(lootcontext$builder.create(LootParameterSets.COMMAND));
   }

   /**
    * If actual and expected match, returns a collection containing only the source player.
    */
   private static Collection<CommandSource> expect(CommandContext<CommandSource> pContext, boolean pActual, boolean pExpected) {
      return (Collection<CommandSource>)(pExpected == pActual ? Collections.singleton(pContext.getSource()) : Collections.emptyList());
   }

   private static ArgumentBuilder<CommandSource, ?> addConditional(CommandNode<CommandSource> pContext, ArgumentBuilder<CommandSource, ?> pBuilder, boolean pValue, ExecuteCommand.IBooleanTest pTest) {
      return pBuilder.fork(pContext, (p_229786_2_) -> {
         return expect(p_229786_2_, pValue, pTest.test(p_229786_2_));
      }).executes((p_229776_2_) -> {
         if (pValue == pTest.test(p_229776_2_)) {
            p_229776_2_.getSource().sendSuccess(new TranslationTextComponent("commands.execute.conditional.pass"), false);
            return 1;
         } else {
            throw ERROR_CONDITIONAL_FAILED.create();
         }
      });
   }

   private static ArgumentBuilder<CommandSource, ?> addIfBlocksConditional(CommandNode<CommandSource> pParent, ArgumentBuilder<CommandSource, ?> pLiteral, boolean pIsIf, boolean pIsMasked) {
      return pLiteral.fork(pParent, (p_229778_2_) -> {
         return expect(p_229778_2_, pIsIf, checkRegions(p_229778_2_, pIsMasked).isPresent());
      }).executes(pIsIf ? (p_229785_1_) -> {
         return checkIfRegions(p_229785_1_, pIsMasked);
      } : (p_229775_1_) -> {
         return checkUnlessRegions(p_229775_1_, pIsMasked);
      });
   }

   private static int checkIfRegions(CommandContext<CommandSource> pContext, boolean pIsMasked) throws CommandSyntaxException {
      OptionalInt optionalint = checkRegions(pContext, pIsMasked);
      if (optionalint.isPresent()) {
         pContext.getSource().sendSuccess(new TranslationTextComponent("commands.execute.conditional.pass_count", optionalint.getAsInt()), false);
         return optionalint.getAsInt();
      } else {
         throw ERROR_CONDITIONAL_FAILED.create();
      }
   }

   private static int checkUnlessRegions(CommandContext<CommandSource> pContext, boolean pIsMasked) throws CommandSyntaxException {
      OptionalInt optionalint = checkRegions(pContext, pIsMasked);
      if (optionalint.isPresent()) {
         throw ERROR_CONDITIONAL_FAILED_COUNT.create(optionalint.getAsInt());
      } else {
         pContext.getSource().sendSuccess(new TranslationTextComponent("commands.execute.conditional.pass"), false);
         return 1;
      }
   }

   private static OptionalInt checkRegions(CommandContext<CommandSource> pContext, boolean pIsMasked) throws CommandSyntaxException {
      return checkRegions(pContext.getSource().getLevel(), BlockPosArgument.getLoadedBlockPos(pContext, "start"), BlockPosArgument.getLoadedBlockPos(pContext, "end"), BlockPosArgument.getLoadedBlockPos(pContext, "destination"), pIsMasked);
   }

   private static OptionalInt checkRegions(ServerWorld pLevel, BlockPos pBegin, BlockPos pEnd, BlockPos pDestination, boolean pIsMasked) throws CommandSyntaxException {
      MutableBoundingBox mutableboundingbox = new MutableBoundingBox(pBegin, pEnd);
      MutableBoundingBox mutableboundingbox1 = new MutableBoundingBox(pDestination, pDestination.offset(mutableboundingbox.getLength()));
      BlockPos blockpos = new BlockPos(mutableboundingbox1.x0 - mutableboundingbox.x0, mutableboundingbox1.y0 - mutableboundingbox.y0, mutableboundingbox1.z0 - mutableboundingbox.z0);
      int i = mutableboundingbox.getXSpan() * mutableboundingbox.getYSpan() * mutableboundingbox.getZSpan();
      if (i > 32768) {
         throw ERROR_AREA_TOO_LARGE.create(32768, i);
      } else {
         int j = 0;

         for(int k = mutableboundingbox.z0; k <= mutableboundingbox.z1; ++k) {
            for(int l = mutableboundingbox.y0; l <= mutableboundingbox.y1; ++l) {
               for(int i1 = mutableboundingbox.x0; i1 <= mutableboundingbox.x1; ++i1) {
                  BlockPos blockpos1 = new BlockPos(i1, l, k);
                  BlockPos blockpos2 = blockpos1.offset(blockpos);
                  BlockState blockstate = pLevel.getBlockState(blockpos1);
                  if (!pIsMasked || !blockstate.is(Blocks.AIR)) {
                     if (blockstate != pLevel.getBlockState(blockpos2)) {
                        return OptionalInt.empty();
                     }

                     TileEntity tileentity = pLevel.getBlockEntity(blockpos1);
                     TileEntity tileentity1 = pLevel.getBlockEntity(blockpos2);
                     if (tileentity != null) {
                        if (tileentity1 == null) {
                           return OptionalInt.empty();
                        }

                        CompoundNBT compoundnbt = tileentity.save(new CompoundNBT());
                        compoundnbt.remove("x");
                        compoundnbt.remove("y");
                        compoundnbt.remove("z");
                        CompoundNBT compoundnbt1 = tileentity1.save(new CompoundNBT());
                        compoundnbt1.remove("x");
                        compoundnbt1.remove("y");
                        compoundnbt1.remove("z");
                        if (!compoundnbt.equals(compoundnbt1)) {
                           return OptionalInt.empty();
                        }
                     }

                     ++j;
                  }
               }
            }
         }

         return OptionalInt.of(j);
      }
   }

   @FunctionalInterface
   interface IBooleanTest {
      boolean test(CommandContext<CommandSource> p_test_1_) throws CommandSyntaxException;
   }

   @FunctionalInterface
   interface INumericTest {
      int test(CommandContext<CommandSource> p_test_1_) throws CommandSyntaxException;
   }
}