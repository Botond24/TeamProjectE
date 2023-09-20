package net.minecraft.state;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.util.Direction;

public class DirectionProperty extends EnumProperty<Direction> {
   protected DirectionProperty(String pName, Collection<Direction> pValues) {
      super(pName, Direction.class, pValues);
   }

   /**
    * Create a new DirectionProperty with all directions that match the given Predicate
    */
   public static DirectionProperty create(String pName, Predicate<Direction> pFilter) {
      return create(pName, Arrays.stream(Direction.values()).filter(pFilter).collect(Collectors.toList()));
   }

   public static DirectionProperty create(String pName, Direction... pValues) {
      return create(pName, Lists.newArrayList(pValues));
   }

   /**
    * Create a new DirectionProperty for the given direction values
    */
   public static DirectionProperty create(String pName, Collection<Direction> pValues) {
      return new DirectionProperty(pName, pValues);
   }
}