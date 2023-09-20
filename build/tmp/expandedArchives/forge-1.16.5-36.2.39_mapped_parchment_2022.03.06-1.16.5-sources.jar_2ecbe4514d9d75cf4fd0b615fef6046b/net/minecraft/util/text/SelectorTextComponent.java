package net.minecraft.util.text;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.command.arguments.EntitySelectorParser;
import net.minecraft.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A component which shows the display names of entities selected by an {@link EntitySelector}.
 */
public class SelectorTextComponent extends TextComponent implements ITargetedTextComponent {
   private static final Logger LOGGER = LogManager.getLogger();
   /** The selector used to find the matching entities of this text component */
   private final String pattern;
   @Nullable
   private final EntitySelector selector;

   public SelectorTextComponent(String p_i45996_1_) {
      this.pattern = p_i45996_1_;
      EntitySelector entityselector = null;

      try {
         EntitySelectorParser entityselectorparser = new EntitySelectorParser(new StringReader(p_i45996_1_));
         entityselector = entityselectorparser.parse();
      } catch (CommandSyntaxException commandsyntaxexception) {
         LOGGER.warn("Invalid selector component: {}", p_i45996_1_, commandsyntaxexception.getMessage());
      }

      this.selector = entityselector;
   }

   /**
    * Gets the selector of this component, in plain text.
    */
   public String getPattern() {
      return this.pattern;
   }

   public IFormattableTextComponent resolve(@Nullable CommandSource pCommandSourceStack, @Nullable Entity pEntity, int pRecursionDepth) throws CommandSyntaxException {
      return (IFormattableTextComponent)(pCommandSourceStack != null && this.selector != null ? EntitySelector.joinNames(this.selector.findEntities(pCommandSourceStack)) : new StringTextComponent(""));
   }

   /**
    * Gets the raw content of this component if possible. For special components (like {@link TranslatableComponent}
    * this usually returns the empty string.
    */
   public String getContents() {
      return this.pattern;
   }

   /**
    * Creates a copy of this component, losing any style or siblings.
    */
   public SelectorTextComponent plainCopy() {
      return new SelectorTextComponent(this.pattern);
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (!(p_equals_1_ instanceof SelectorTextComponent)) {
         return false;
      } else {
         SelectorTextComponent selectortextcomponent = (SelectorTextComponent)p_equals_1_;
         return this.pattern.equals(selectortextcomponent.pattern) && super.equals(p_equals_1_);
      }
   }

   public String toString() {
      return "SelectorComponent{pattern='" + this.pattern + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
   }
}