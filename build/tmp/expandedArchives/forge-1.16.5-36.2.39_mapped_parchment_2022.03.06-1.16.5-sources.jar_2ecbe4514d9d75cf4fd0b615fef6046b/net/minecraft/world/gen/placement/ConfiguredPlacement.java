package net.minecraft.world.gen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.IDecoratable;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;

public class ConfiguredPlacement<DC extends IPlacementConfig> implements IDecoratable<ConfiguredPlacement<?>> {
   public static final Codec<ConfiguredPlacement<?>> CODEC = Registry.DECORATOR.dispatch("type", (p_236954_0_) -> {
      return p_236954_0_.decorator;
   }, Placement::configuredCodec);
   private final Placement<DC> decorator;
   private final DC config;

   public ConfiguredPlacement(Placement<DC> pDecorator, DC pConfig) {
      this.decorator = pDecorator;
      this.config = pConfig;
   }

   public Stream<BlockPos> getPositions(WorldDecoratingHelper pContext, Random pRandom, BlockPos pPos) {
      return this.decorator.getPositions(pContext, pRandom, this.config, pPos);
   }

   public String toString() {
      return String.format("[%s %s]", Registry.DECORATOR.getKey(this.decorator), this.config);
   }

   public ConfiguredPlacement<?> decorated(ConfiguredPlacement<?> pDecorator) {
      return new ConfiguredPlacement<>(Placement.DECORATED, new DecoratedPlacementConfig(pDecorator, this));
   }

   public DC config() {
      return this.config;
   }
}