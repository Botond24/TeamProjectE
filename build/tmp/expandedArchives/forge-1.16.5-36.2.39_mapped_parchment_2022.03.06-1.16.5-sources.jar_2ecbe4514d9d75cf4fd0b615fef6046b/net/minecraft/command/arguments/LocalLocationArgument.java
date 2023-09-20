package net.minecraft.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

public class LocalLocationArgument implements ILocationArgument {
   private final double left;
   private final double up;
   private final double forwards;

   public LocalLocationArgument(double pLeft, double pUp, double pForwards) {
      this.left = pLeft;
      this.up = pUp;
      this.forwards = pForwards;
   }

   public Vector3d getPosition(CommandSource pSource) {
      Vector2f vector2f = pSource.getRotation();
      Vector3d vector3d = pSource.getAnchor().apply(pSource);
      float f = MathHelper.cos((vector2f.y + 90.0F) * ((float)Math.PI / 180F));
      float f1 = MathHelper.sin((vector2f.y + 90.0F) * ((float)Math.PI / 180F));
      float f2 = MathHelper.cos(-vector2f.x * ((float)Math.PI / 180F));
      float f3 = MathHelper.sin(-vector2f.x * ((float)Math.PI / 180F));
      float f4 = MathHelper.cos((-vector2f.x + 90.0F) * ((float)Math.PI / 180F));
      float f5 = MathHelper.sin((-vector2f.x + 90.0F) * ((float)Math.PI / 180F));
      Vector3d vector3d1 = new Vector3d((double)(f * f2), (double)f3, (double)(f1 * f2));
      Vector3d vector3d2 = new Vector3d((double)(f * f4), (double)f5, (double)(f1 * f4));
      Vector3d vector3d3 = vector3d1.cross(vector3d2).scale(-1.0D);
      double d0 = vector3d1.x * this.forwards + vector3d2.x * this.up + vector3d3.x * this.left;
      double d1 = vector3d1.y * this.forwards + vector3d2.y * this.up + vector3d3.y * this.left;
      double d2 = vector3d1.z * this.forwards + vector3d2.z * this.up + vector3d3.z * this.left;
      return new Vector3d(vector3d.x + d0, vector3d.y + d1, vector3d.z + d2);
   }

   public Vector2f getRotation(CommandSource pSource) {
      return Vector2f.ZERO;
   }

   public boolean isXRelative() {
      return true;
   }

   public boolean isYRelative() {
      return true;
   }

   public boolean isZRelative() {
      return true;
   }

   public static LocalLocationArgument parse(StringReader pReader) throws CommandSyntaxException {
      int i = pReader.getCursor();
      double d0 = readDouble(pReader, i);
      if (pReader.canRead() && pReader.peek() == ' ') {
         pReader.skip();
         double d1 = readDouble(pReader, i);
         if (pReader.canRead() && pReader.peek() == ' ') {
            pReader.skip();
            double d2 = readDouble(pReader, i);
            return new LocalLocationArgument(d0, d1, d2);
         } else {
            pReader.setCursor(i);
            throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(pReader);
         }
      } else {
         pReader.setCursor(i);
         throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(pReader);
      }
   }

   private static double readDouble(StringReader pReader, int pStart) throws CommandSyntaxException {
      if (!pReader.canRead()) {
         throw LocationPart.ERROR_EXPECTED_DOUBLE.createWithContext(pReader);
      } else if (pReader.peek() != '^') {
         pReader.setCursor(pStart);
         throw Vec3Argument.ERROR_MIXED_TYPE.createWithContext(pReader);
      } else {
         pReader.skip();
         return pReader.canRead() && pReader.peek() != ' ' ? pReader.readDouble() : 0.0D;
      }
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (!(p_equals_1_ instanceof LocalLocationArgument)) {
         return false;
      } else {
         LocalLocationArgument locallocationargument = (LocalLocationArgument)p_equals_1_;
         return this.left == locallocationargument.left && this.up == locallocationargument.up && this.forwards == locallocationargument.forwards;
      }
   }

   public int hashCode() {
      return Objects.hash(this.left, this.up, this.forwards);
   }
}