package net.minecraft.advancements;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.command.CommandSource;
import net.minecraft.command.FunctionObject;
import net.minecraft.resources.FunctionReloader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.GameRules;

public class FunctionManager {
   private static final ResourceLocation TICK_FUNCTION_TAG = new ResourceLocation("tick");
   private static final ResourceLocation LOAD_FUNCTION_TAG = new ResourceLocation("load");
   private final MinecraftServer server;
   private boolean isInFunction;
   private final ArrayDeque<FunctionManager.QueuedCommand> commandQueue = new ArrayDeque<>();
   private final List<FunctionManager.QueuedCommand> nestedCalls = Lists.newArrayList();
   private final List<FunctionObject> ticking = Lists.newArrayList();
   private boolean postReload;
   private FunctionReloader library;

   public FunctionManager(MinecraftServer p_i232597_1_, FunctionReloader p_i232597_2_) {
      this.server = p_i232597_1_;
      this.library = p_i232597_2_;
      this.postReload(p_i232597_2_);
   }

   public int getCommandLimit() {
      return this.server.getGameRules().getInt(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH);
   }

   public CommandDispatcher<CommandSource> getDispatcher() {
      return this.server.getCommands().getDispatcher();
   }

   public void tick() {
      this.executeTagFunctions(this.ticking, TICK_FUNCTION_TAG);
      if (this.postReload) {
         this.postReload = false;
         Collection<FunctionObject> collection = this.library.getTags().getTagOrEmpty(LOAD_FUNCTION_TAG).getValues();
         this.executeTagFunctions(collection, LOAD_FUNCTION_TAG);
      }

   }

   private void executeTagFunctions(Collection<FunctionObject> pFunctionObjects, ResourceLocation pIdentifier) {
      this.server.getProfiler().push(pIdentifier::toString);

      for(FunctionObject functionobject : pFunctionObjects) {
         this.execute(functionobject, this.getGameLoopSender());
      }

      this.server.getProfiler().pop();
   }

   public int execute(FunctionObject pFunctionObject, CommandSource pSource) {
      int i = this.getCommandLimit();
      if (this.isInFunction) {
         if (this.commandQueue.size() + this.nestedCalls.size() < i) {
            this.nestedCalls.add(new FunctionManager.QueuedCommand(this, pSource, new FunctionObject.FunctionEntry(pFunctionObject)));
         }

         return 0;
      } else {
         try {
            this.isInFunction = true;
            int j = 0;
            FunctionObject.IEntry[] afunctionobject$ientry = pFunctionObject.getEntries();

            for(int k = afunctionobject$ientry.length - 1; k >= 0; --k) {
               this.commandQueue.push(new FunctionManager.QueuedCommand(this, pSource, afunctionobject$ientry[k]));
            }

            while(!this.commandQueue.isEmpty()) {
               try {
                  FunctionManager.QueuedCommand functionmanager$queuedcommand = this.commandQueue.removeFirst();
                  this.server.getProfiler().push(functionmanager$queuedcommand::toString);
                  functionmanager$queuedcommand.execute(this.commandQueue, i);
                  if (!this.nestedCalls.isEmpty()) {
                     Lists.reverse(this.nestedCalls).forEach(this.commandQueue::addFirst);
                     this.nestedCalls.clear();
                  }
               } finally {
                  this.server.getProfiler().pop();
               }

               ++j;
               if (j >= i) {
                  return j;
               }
            }

            return j;
         } finally {
            this.commandQueue.clear();
            this.nestedCalls.clear();
            this.isInFunction = false;
         }
      }
   }

   public void replaceLibrary(FunctionReloader pReloader) {
      this.library = pReloader;
      this.postReload(pReloader);
   }

   private void postReload(FunctionReloader pReloader) {
      this.ticking.clear();
      this.ticking.addAll(pReloader.getTags().getTagOrEmpty(TICK_FUNCTION_TAG).getValues());
      this.postReload = true;
   }

   public CommandSource getGameLoopSender() {
      return this.server.createCommandSourceStack().withPermission(2).withSuppressedOutput();
   }

   public Optional<FunctionObject> get(ResourceLocation pFunctionIdentifier) {
      return this.library.getFunction(pFunctionIdentifier);
   }

   public ITag<FunctionObject> getTag(ResourceLocation pFunctionTagIdentifier) {
      return this.library.getTag(pFunctionTagIdentifier);
   }

   public Iterable<ResourceLocation> getFunctionNames() {
      return this.library.getFunctions().keySet();
   }

   public Iterable<ResourceLocation> getTagNames() {
      return this.library.getTags().getAvailableTags();
   }

   public static class QueuedCommand {
      private final FunctionManager manager;
      private final CommandSource sender;
      private final FunctionObject.IEntry entry;

      public QueuedCommand(FunctionManager p_i48018_1_, CommandSource p_i48018_2_, FunctionObject.IEntry p_i48018_3_) {
         this.manager = p_i48018_1_;
         this.sender = p_i48018_2_;
         this.entry = p_i48018_3_;
      }

      public void execute(ArrayDeque<FunctionManager.QueuedCommand> p_194222_1_, int p_194222_2_) {
         try {
            this.entry.execute(this.manager, this.sender, p_194222_1_, p_194222_2_);
         } catch (Throwable throwable) {
         }

      }

      public String toString() {
         return this.entry.toString();
      }
   }
}