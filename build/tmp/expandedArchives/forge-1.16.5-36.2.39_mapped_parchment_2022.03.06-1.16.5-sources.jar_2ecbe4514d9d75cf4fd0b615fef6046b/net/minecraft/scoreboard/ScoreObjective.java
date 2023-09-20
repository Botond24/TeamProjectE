package net.minecraft.scoreboard;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ScoreObjective {
   private final Scoreboard scoreboard;
   private final String name;
   private final ScoreCriteria criteria;
   private ITextComponent displayName;
   private ITextComponent formattedDisplayName;
   private ScoreCriteria.RenderType renderType;

   public ScoreObjective(Scoreboard pScoreboard, String pName, ScoreCriteria pCriteria, ITextComponent pDisplayName, ScoreCriteria.RenderType pRenderType) {
      this.scoreboard = pScoreboard;
      this.name = pName;
      this.criteria = pCriteria;
      this.displayName = pDisplayName;
      this.formattedDisplayName = this.createFormattedDisplayName();
      this.renderType = pRenderType;
   }

   @OnlyIn(Dist.CLIENT)
   public Scoreboard getScoreboard() {
      return this.scoreboard;
   }

   public String getName() {
      return this.name;
   }

   public ScoreCriteria getCriteria() {
      return this.criteria;
   }

   public ITextComponent getDisplayName() {
      return this.displayName;
   }

   private ITextComponent createFormattedDisplayName() {
      return TextComponentUtils.wrapInSquareBrackets(this.displayName.copy().withStyle((p_237497_1_) -> {
         return p_237497_1_.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(this.name)));
      }));
   }

   public ITextComponent getFormattedDisplayName() {
      return this.formattedDisplayName;
   }

   public void setDisplayName(ITextComponent pDisplayName) {
      this.displayName = pDisplayName;
      this.formattedDisplayName = this.createFormattedDisplayName();
      this.scoreboard.onObjectiveChanged(this);
   }

   public ScoreCriteria.RenderType getRenderType() {
      return this.renderType;
   }

   public void setRenderType(ScoreCriteria.RenderType pRenderType) {
      this.renderType = pRenderType;
      this.scoreboard.onObjectiveChanged(this);
   }
}