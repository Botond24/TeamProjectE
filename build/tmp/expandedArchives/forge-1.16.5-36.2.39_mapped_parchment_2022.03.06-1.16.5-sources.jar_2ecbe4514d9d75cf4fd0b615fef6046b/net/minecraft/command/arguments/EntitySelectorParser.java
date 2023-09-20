package net.minecraft.command.arguments;

import com.google.common.primitives.Doubles;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.MinMaxBoundsWrapped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;

public class EntitySelectorParser {
   public static final SimpleCommandExceptionType ERROR_INVALID_NAME_OR_UUID = new SimpleCommandExceptionType(new TranslationTextComponent("argument.entity.invalid"));
   public static final DynamicCommandExceptionType ERROR_UNKNOWN_SELECTOR_TYPE = new DynamicCommandExceptionType((p_208703_0_) -> {
      return new TranslationTextComponent("argument.entity.selector.unknown", p_208703_0_);
   });
   public static final SimpleCommandExceptionType ERROR_SELECTORS_NOT_ALLOWED = new SimpleCommandExceptionType(new TranslationTextComponent("argument.entity.selector.not_allowed"));
   public static final SimpleCommandExceptionType ERROR_MISSING_SELECTOR_TYPE = new SimpleCommandExceptionType(new TranslationTextComponent("argument.entity.selector.missing"));
   public static final SimpleCommandExceptionType ERROR_EXPECTED_END_OF_OPTIONS = new SimpleCommandExceptionType(new TranslationTextComponent("argument.entity.options.unterminated"));
   public static final DynamicCommandExceptionType ERROR_EXPECTED_OPTION_VALUE = new DynamicCommandExceptionType((p_208711_0_) -> {
      return new TranslationTextComponent("argument.entity.options.valueless", p_208711_0_);
   });
   public static final BiConsumer<Vector3d, List<? extends Entity>> ORDER_ARBITRARY = (p_197402_0_, p_197402_1_) -> {
   };
   public static final BiConsumer<Vector3d, List<? extends Entity>> ORDER_NEAREST = (p_197392_0_, p_197392_1_) -> {
      p_197392_1_.sort((p_197393_1_, p_197393_2_) -> {
         return Doubles.compare(p_197393_1_.distanceToSqr(p_197392_0_), p_197393_2_.distanceToSqr(p_197392_0_));
      });
   };
   public static final BiConsumer<Vector3d, List<? extends Entity>> ORDER_FURTHEST = (p_197383_0_, p_197383_1_) -> {
      p_197383_1_.sort((p_197369_1_, p_197369_2_) -> {
         return Doubles.compare(p_197369_2_.distanceToSqr(p_197383_0_), p_197369_1_.distanceToSqr(p_197383_0_));
      });
   };
   public static final BiConsumer<Vector3d, List<? extends Entity>> ORDER_RANDOM = (p_197368_0_, p_197368_1_) -> {
      Collections.shuffle(p_197368_1_);
   };
   public static final BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> SUGGEST_NOTHING = (p_201342_0_, p_201342_1_) -> {
      return p_201342_0_.buildFuture();
   };
   private final StringReader reader;
   private final boolean allowSelectors;
   private int maxResults;
   private boolean includesEntities;
   private boolean worldLimited;
   private MinMaxBounds.FloatBound distance = MinMaxBounds.FloatBound.ANY;
   private MinMaxBounds.IntBound level = MinMaxBounds.IntBound.ANY;
   @Nullable
   private Double x;
   @Nullable
   private Double y;
   @Nullable
   private Double z;
   @Nullable
   private Double deltaX;
   @Nullable
   private Double deltaY;
   @Nullable
   private Double deltaZ;
   private MinMaxBoundsWrapped rotX = MinMaxBoundsWrapped.ANY;
   private MinMaxBoundsWrapped rotY = MinMaxBoundsWrapped.ANY;
   private Predicate<Entity> predicate = (p_197375_0_) -> {
      return true;
   };
   private BiConsumer<Vector3d, List<? extends Entity>> order = ORDER_ARBITRARY;
   private boolean currentEntity;
   @Nullable
   private String playerName;
   private int startPosition;
   @Nullable
   private UUID entityUUID;
   private BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> suggestions = SUGGEST_NOTHING;
   private boolean hasNameEquals;
   private boolean hasNameNotEquals;
   private boolean isLimited;
   private boolean isSorted;
   private boolean hasGamemodeEquals;
   private boolean hasGamemodeNotEquals;
   private boolean hasTeamEquals;
   private boolean hasTeamNotEquals;
   @Nullable
   private EntityType<?> type;
   private boolean typeInverse;
   private boolean hasScores;
   private boolean hasAdvancements;
   private boolean usesSelectors;

   public EntitySelectorParser(StringReader pReader) {
      this(pReader, true);
   }

   public EntitySelectorParser(StringReader pReader, boolean pAllowSelectors) {
      this.reader = pReader;
      this.allowSelectors = pAllowSelectors;
   }

   public EntitySelector getSelector() {
      AxisAlignedBB axisalignedbb;
      if (this.deltaX == null && this.deltaY == null && this.deltaZ == null) {
         if (this.distance.getMax() != null) {
            float f = this.distance.getMax();
            axisalignedbb = new AxisAlignedBB((double)(-f), (double)(-f), (double)(-f), (double)(f + 1.0F), (double)(f + 1.0F), (double)(f + 1.0F));
         } else {
            axisalignedbb = null;
         }
      } else {
         axisalignedbb = this.createAabb(this.deltaX == null ? 0.0D : this.deltaX, this.deltaY == null ? 0.0D : this.deltaY, this.deltaZ == null ? 0.0D : this.deltaZ);
      }

      Function<Vector3d, Vector3d> function;
      if (this.x == null && this.y == null && this.z == null) {
         function = (p_197379_0_) -> {
            return p_197379_0_;
         };
      } else {
         function = (p_197367_1_) -> {
            return new Vector3d(this.x == null ? p_197367_1_.x : this.x, this.y == null ? p_197367_1_.y : this.y, this.z == null ? p_197367_1_.z : this.z);
         };
      }

      return new EntitySelector(this.maxResults, this.includesEntities, this.worldLimited, this.predicate, this.distance, function, axisalignedbb, this.order, this.currentEntity, this.playerName, this.entityUUID, this.type, this.usesSelectors);
   }

   private AxisAlignedBB createAabb(double pSizeX, double pSizeY, double pSizeZ) {
      boolean flag = pSizeX < 0.0D;
      boolean flag1 = pSizeY < 0.0D;
      boolean flag2 = pSizeZ < 0.0D;
      double d0 = flag ? pSizeX : 0.0D;
      double d1 = flag1 ? pSizeY : 0.0D;
      double d2 = flag2 ? pSizeZ : 0.0D;
      double d3 = (flag ? 0.0D : pSizeX) + 1.0D;
      double d4 = (flag1 ? 0.0D : pSizeY) + 1.0D;
      double d5 = (flag2 ? 0.0D : pSizeZ) + 1.0D;
      return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
   }

   public void finalizePredicates() {
      if (this.rotX != MinMaxBoundsWrapped.ANY) {
         this.predicate = this.predicate.and(this.createRotationPredicate(this.rotX, (p_197386_0_) -> {
            return (double)p_197386_0_.xRot;
         }));
      }

      if (this.rotY != MinMaxBoundsWrapped.ANY) {
         this.predicate = this.predicate.and(this.createRotationPredicate(this.rotY, (p_197385_0_) -> {
            return (double)p_197385_0_.yRot;
         }));
      }

      if (!this.level.isAny()) {
         this.predicate = this.predicate.and((p_197371_1_) -> {
            return !(p_197371_1_ instanceof ServerPlayerEntity) ? false : this.level.matches(((ServerPlayerEntity)p_197371_1_).experienceLevel);
         });
      }

   }

   private Predicate<Entity> createRotationPredicate(MinMaxBoundsWrapped pAngleBounds, ToDoubleFunction<Entity> pAngleFunction) {
      double d0 = (double)MathHelper.wrapDegrees(pAngleBounds.getMin() == null ? 0.0F : pAngleBounds.getMin());
      double d1 = (double)MathHelper.wrapDegrees(pAngleBounds.getMax() == null ? 359.0F : pAngleBounds.getMax());
      return (p_197374_5_) -> {
         double d2 = MathHelper.wrapDegrees(pAngleFunction.applyAsDouble(p_197374_5_));
         if (d0 > d1) {
            return d2 >= d0 || d2 <= d1;
         } else {
            return d2 >= d0 && d2 <= d1;
         }
      };
   }

   protected void parseSelector() throws CommandSyntaxException {
      this.usesSelectors = true;
      this.suggestions = this::suggestSelector;
      if (!this.reader.canRead()) {
         throw ERROR_MISSING_SELECTOR_TYPE.createWithContext(this.reader);
      } else {
         int i = this.reader.getCursor();
         char c0 = this.reader.read();
         if (c0 == 'p') {
            this.maxResults = 1;
            this.includesEntities = false;
            this.order = ORDER_NEAREST;
            this.limitToType(EntityType.PLAYER);
         } else if (c0 == 'a') {
            this.maxResults = Integer.MAX_VALUE;
            this.includesEntities = false;
            this.order = ORDER_ARBITRARY;
            this.limitToType(EntityType.PLAYER);
         } else if (c0 == 'r') {
            this.maxResults = 1;
            this.includesEntities = false;
            this.order = ORDER_RANDOM;
            this.limitToType(EntityType.PLAYER);
         } else if (c0 == 's') {
            this.maxResults = 1;
            this.includesEntities = true;
            this.currentEntity = true;
         } else {
            if (c0 != 'e') {
               this.reader.setCursor(i);
               throw ERROR_UNKNOWN_SELECTOR_TYPE.createWithContext(this.reader, '@' + String.valueOf(c0));
            }

            this.maxResults = Integer.MAX_VALUE;
            this.includesEntities = true;
            this.order = ORDER_ARBITRARY;
            this.predicate = Entity::isAlive;
         }

         this.suggestions = this::suggestOpenOptions;
         if (this.reader.canRead() && this.reader.peek() == '[') {
            this.reader.skip();
            this.suggestions = this::suggestOptionsKeyOrClose;
            this.parseOptions();
         }

      }
   }

   protected void parseNameOrUUID() throws CommandSyntaxException {
      if (this.reader.canRead()) {
         this.suggestions = this::suggestName;
      }

      int i = this.reader.getCursor();
      String s = this.reader.readString();

      try {
         this.entityUUID = UUID.fromString(s);
         this.includesEntities = true;
      } catch (IllegalArgumentException illegalargumentexception) {
         if (s.isEmpty() || s.length() > 16) {
            this.reader.setCursor(i);
            throw ERROR_INVALID_NAME_OR_UUID.createWithContext(this.reader);
         }

         this.includesEntities = false;
         this.playerName = s;
      }

      this.maxResults = 1;
   }

   public void parseOptions() throws CommandSyntaxException {
      this.suggestions = this::suggestOptionsKey;
      this.reader.skipWhitespace();

      while(true) {
         if (this.reader.canRead() && this.reader.peek() != ']') {
            this.reader.skipWhitespace();
            int i = this.reader.getCursor();
            String s = this.reader.readString();
            EntityOptions.IFilter entityoptions$ifilter = EntityOptions.get(this, s, i);
            this.reader.skipWhitespace();
            if (!this.reader.canRead() || this.reader.peek() != '=') {
               this.reader.setCursor(i);
               throw ERROR_EXPECTED_OPTION_VALUE.createWithContext(this.reader, s);
            }

            this.reader.skip();
            this.reader.skipWhitespace();
            this.suggestions = SUGGEST_NOTHING;
            entityoptions$ifilter.handle(this);
            this.reader.skipWhitespace();
            this.suggestions = this::suggestOptionsNextOrClose;
            if (!this.reader.canRead()) {
               continue;
            }

            if (this.reader.peek() == ',') {
               this.reader.skip();
               this.suggestions = this::suggestOptionsKey;
               continue;
            }

            if (this.reader.peek() != ']') {
               throw ERROR_EXPECTED_END_OF_OPTIONS.createWithContext(this.reader);
            }
         }

         if (this.reader.canRead()) {
            this.reader.skip();
            this.suggestions = SUGGEST_NOTHING;
            return;
         }

         throw ERROR_EXPECTED_END_OF_OPTIONS.createWithContext(this.reader);
      }
   }

   public boolean shouldInvertValue() {
      this.reader.skipWhitespace();
      if (this.reader.canRead() && this.reader.peek() == '!') {
         this.reader.skip();
         this.reader.skipWhitespace();
         return true;
      } else {
         return false;
      }
   }

   public boolean isTag() {
      this.reader.skipWhitespace();
      if (this.reader.canRead() && this.reader.peek() == '#') {
         this.reader.skip();
         this.reader.skipWhitespace();
         return true;
      } else {
         return false;
      }
   }

   public StringReader getReader() {
      return this.reader;
   }

   public void addPredicate(Predicate<Entity> pPredicate) {
      this.predicate = this.predicate.and(pPredicate);
   }

   public void setWorldLimited() {
      this.worldLimited = true;
   }

   public MinMaxBounds.FloatBound getDistance() {
      return this.distance;
   }

   public void setDistance(MinMaxBounds.FloatBound pDistance) {
      this.distance = pDistance;
   }

   public MinMaxBounds.IntBound getLevel() {
      return this.level;
   }

   public void setLevel(MinMaxBounds.IntBound pLevel) {
      this.level = pLevel;
   }

   public MinMaxBoundsWrapped getRotX() {
      return this.rotX;
   }

   public void setRotX(MinMaxBoundsWrapped pRotX) {
      this.rotX = pRotX;
   }

   public MinMaxBoundsWrapped getRotY() {
      return this.rotY;
   }

   public void setRotY(MinMaxBoundsWrapped pRotY) {
      this.rotY = pRotY;
   }

   @Nullable
   public Double getX() {
      return this.x;
   }

   @Nullable
   public Double getY() {
      return this.y;
   }

   @Nullable
   public Double getZ() {
      return this.z;
   }

   public void setX(double pX) {
      this.x = pX;
   }

   public void setY(double pY) {
      this.y = pY;
   }

   public void setZ(double pZ) {
      this.z = pZ;
   }

   public void setDeltaX(double pDeltaX) {
      this.deltaX = pDeltaX;
   }

   public void setDeltaY(double pDeltaY) {
      this.deltaY = pDeltaY;
   }

   public void setDeltaZ(double pDeltaZ) {
      this.deltaZ = pDeltaZ;
   }

   @Nullable
   public Double getDeltaX() {
      return this.deltaX;
   }

   @Nullable
   public Double getDeltaY() {
      return this.deltaY;
   }

   @Nullable
   public Double getDeltaZ() {
      return this.deltaZ;
   }

   public void setMaxResults(int pMaxResults) {
      this.maxResults = pMaxResults;
   }

   public void setIncludesEntities(boolean pIncludesEntities) {
      this.includesEntities = pIncludesEntities;
   }

   public void setOrder(BiConsumer<Vector3d, List<? extends Entity>> pOrder) {
      this.order = pOrder;
   }

   public EntitySelector parse() throws CommandSyntaxException {
      this.startPosition = this.reader.getCursor();
      this.suggestions = this::suggestNameOrSelector;
      if (this.reader.canRead() && this.reader.peek() == '@') {
         if (!this.allowSelectors) {
            throw ERROR_SELECTORS_NOT_ALLOWED.createWithContext(this.reader);
         }

         this.reader.skip();
         EntitySelector forgeSelector = net.minecraftforge.common.command.EntitySelectorManager.parseSelector(this);
         if (forgeSelector != null)
            return forgeSelector;
         this.parseSelector();
      } else {
         this.parseNameOrUUID();
      }

      this.finalizePredicates();
      return this.getSelector();
   }

   private static void fillSelectorSuggestions(SuggestionsBuilder pBuilder) {
      pBuilder.suggest("@p", new TranslationTextComponent("argument.entity.selector.nearestPlayer"));
      pBuilder.suggest("@a", new TranslationTextComponent("argument.entity.selector.allPlayers"));
      pBuilder.suggest("@r", new TranslationTextComponent("argument.entity.selector.randomPlayer"));
      pBuilder.suggest("@s", new TranslationTextComponent("argument.entity.selector.self"));
      pBuilder.suggest("@e", new TranslationTextComponent("argument.entity.selector.allEntities"));
      net.minecraftforge.common.command.EntitySelectorManager.fillSelectorSuggestions(pBuilder);
   }

   private CompletableFuture<Suggestions> suggestNameOrSelector(SuggestionsBuilder p_201981_1_, Consumer<SuggestionsBuilder> p_201981_2_) {
      p_201981_2_.accept(p_201981_1_);
      if (this.allowSelectors) {
         fillSelectorSuggestions(p_201981_1_);
      }

      return p_201981_1_.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestName(SuggestionsBuilder p_201974_1_, Consumer<SuggestionsBuilder> p_201974_2_) {
      SuggestionsBuilder suggestionsbuilder = p_201974_1_.createOffset(this.startPosition);
      p_201974_2_.accept(suggestionsbuilder);
      return p_201974_1_.add(suggestionsbuilder).buildFuture();
   }

   private CompletableFuture<Suggestions> suggestSelector(SuggestionsBuilder p_201959_1_, Consumer<SuggestionsBuilder> p_201959_2_) {
      SuggestionsBuilder suggestionsbuilder = p_201959_1_.createOffset(p_201959_1_.getStart() - 1);
      fillSelectorSuggestions(suggestionsbuilder);
      p_201959_1_.add(suggestionsbuilder);
      return p_201959_1_.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestOpenOptions(SuggestionsBuilder p_201989_1_, Consumer<SuggestionsBuilder> p_201989_2_) {
      p_201989_1_.suggest(String.valueOf('['));
      return p_201989_1_.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestOptionsKeyOrClose(SuggestionsBuilder p_201996_1_, Consumer<SuggestionsBuilder> p_201996_2_) {
      p_201996_1_.suggest(String.valueOf(']'));
      EntityOptions.suggestNames(this, p_201996_1_);
      return p_201996_1_.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestOptionsKey(SuggestionsBuilder p_201994_1_, Consumer<SuggestionsBuilder> p_201994_2_) {
      EntityOptions.suggestNames(this, p_201994_1_);
      return p_201994_1_.buildFuture();
   }

   private CompletableFuture<Suggestions> suggestOptionsNextOrClose(SuggestionsBuilder p_201969_1_, Consumer<SuggestionsBuilder> p_201969_2_) {
      p_201969_1_.suggest(String.valueOf(','));
      p_201969_1_.suggest(String.valueOf(']'));
      return p_201969_1_.buildFuture();
   }

   public boolean isCurrentEntity() {
      return this.currentEntity;
   }

   public void setSuggestions(BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> pSuggestionHandler) {
      this.suggestions = pSuggestionHandler;
   }

   public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder pBuilder, Consumer<SuggestionsBuilder> pConsumer) {
      return this.suggestions.apply(pBuilder.createOffset(this.reader.getCursor()), pConsumer);
   }

   public boolean hasNameEquals() {
      return this.hasNameEquals;
   }

   public void setHasNameEquals(boolean pHasNameEquals) {
      this.hasNameEquals = pHasNameEquals;
   }

   public boolean hasNameNotEquals() {
      return this.hasNameNotEquals;
   }

   public void setHasNameNotEquals(boolean pHasNameNotEquals) {
      this.hasNameNotEquals = pHasNameNotEquals;
   }

   public boolean isLimited() {
      return this.isLimited;
   }

   public void setLimited(boolean pIsLimited) {
      this.isLimited = pIsLimited;
   }

   public boolean isSorted() {
      return this.isSorted;
   }

   public void setSorted(boolean pIsSorted) {
      this.isSorted = pIsSorted;
   }

   public boolean hasGamemodeEquals() {
      return this.hasGamemodeEquals;
   }

   public void setHasGamemodeEquals(boolean pHasGamemodeEquals) {
      this.hasGamemodeEquals = pHasGamemodeEquals;
   }

   public boolean hasGamemodeNotEquals() {
      return this.hasGamemodeNotEquals;
   }

   public void setHasGamemodeNotEquals(boolean pHasGamemodeNotEquals) {
      this.hasGamemodeNotEquals = pHasGamemodeNotEquals;
   }

   public boolean hasTeamEquals() {
      return this.hasTeamEquals;
   }

   public void setHasTeamEquals(boolean pHasTeamEquals) {
      this.hasTeamEquals = pHasTeamEquals;
   }

   public void setHasTeamNotEquals(boolean pHasTeamNotEquals) {
      this.hasTeamNotEquals = pHasTeamNotEquals;
   }

   public void limitToType(EntityType<?> pType) {
      this.type = pType;
   }

   public void setTypeLimitedInversely() {
      this.typeInverse = true;
   }

   public boolean isTypeLimited() {
      return this.type != null;
   }

   public boolean isTypeLimitedInversely() {
      return this.typeInverse;
   }

   public boolean hasScores() {
      return this.hasScores;
   }

   public void setHasScores(boolean pHasScores) {
      this.hasScores = pHasScores;
   }

   public boolean hasAdvancements() {
      return this.hasAdvancements;
   }

   public void setHasAdvancements(boolean pHasAdvancements) {
      this.hasAdvancements = pHasAdvancements;
   }
}
