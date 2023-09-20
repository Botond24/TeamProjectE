package net.minecraft.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ArgumentTypes;

public class CommandsReport implements IDataProvider {
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
   private final DataGenerator generator;

   public CommandsReport(DataGenerator pGenerator) {
      this.generator = pGenerator;
   }

   /**
    * Performs this provider's action.
    */
   public void run(DirectoryCache pCache) throws IOException {
      Path path = this.generator.getOutputFolder().resolve("reports/commands.json");
      CommandDispatcher<CommandSource> commanddispatcher = (new Commands(Commands.EnvironmentType.ALL)).getDispatcher();
      IDataProvider.save(GSON, pCache, ArgumentTypes.serializeNodeToJson(commanddispatcher, commanddispatcher.getRoot()), path);
   }

   /**
    * Gets a name for this provider, to use in logging.
    */
   public String getName() {
      return "Command Syntax";
   }
}