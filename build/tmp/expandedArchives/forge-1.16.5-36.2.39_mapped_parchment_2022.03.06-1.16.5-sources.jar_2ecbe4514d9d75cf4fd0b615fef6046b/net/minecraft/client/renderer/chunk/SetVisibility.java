package net.minecraft.client.renderer.chunk;

import java.util.BitSet;
import java.util.Set;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SetVisibility {
   private static final int FACINGS = Direction.values().length;
   private final BitSet data = new BitSet(FACINGS * FACINGS);

   public void add(Set<Direction> pFacing) {
      for(Direction direction : pFacing) {
         for(Direction direction1 : pFacing) {
            this.set(direction, direction1, true);
         }
      }

   }

   public void set(Direction pFacing, Direction pFacing2, boolean pValue) {
      this.data.set(pFacing.ordinal() + pFacing2.ordinal() * FACINGS, pValue);
      this.data.set(pFacing2.ordinal() + pFacing.ordinal() * FACINGS, pValue);
   }

   public void setAll(boolean pVisible) {
      this.data.set(0, this.data.size(), pVisible);
   }

   public boolean visibilityBetween(Direction pFacing, Direction pFacing2) {
      return this.data.get(pFacing.ordinal() + pFacing2.ordinal() * FACINGS);
   }

   public String toString() {
      StringBuilder stringbuilder = new StringBuilder();
      stringbuilder.append(' ');

      for(Direction direction : Direction.values()) {
         stringbuilder.append(' ').append(direction.toString().toUpperCase().charAt(0));
      }

      stringbuilder.append('\n');

      for(Direction direction2 : Direction.values()) {
         stringbuilder.append(direction2.toString().toUpperCase().charAt(0));

         for(Direction direction1 : Direction.values()) {
            if (direction2 == direction1) {
               stringbuilder.append("  ");
            } else {
               boolean flag = this.visibilityBetween(direction2, direction1);
               stringbuilder.append(' ').append((char)(flag ? 'Y' : 'n'));
            }
         }

         stringbuilder.append('\n');
      }

      return stringbuilder.toString();
   }
}