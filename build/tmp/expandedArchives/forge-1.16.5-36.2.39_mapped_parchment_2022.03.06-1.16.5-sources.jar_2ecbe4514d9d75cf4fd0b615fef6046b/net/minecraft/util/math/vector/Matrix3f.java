package net.minecraft.util.math.vector;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Triple;

public final class Matrix3f {
   private static final float G = 3.0F + 2.0F * (float)Math.sqrt(2.0D);
   private static final float CS = (float)Math.cos((Math.PI / 8D));
   private static final float SS = (float)Math.sin((Math.PI / 8D));
   private static final float SQ2 = 1.0F / (float)Math.sqrt(2.0D);
   protected float m00;
   protected float m01;
   protected float m02;
   protected float m10;
   protected float m11;
   protected float m12;
   protected float m20;
   protected float m21;
   protected float m22;

   /**
    * Copy constructor for this class.
    * @param pOther The instance to copy from
    */
   public Matrix3f() {
   }

   /**
    * Copy constructor for this class.
    * @param pOther The instance to copy from
    */
   public Matrix3f(Quaternion pQuaternion) {
      float f = pQuaternion.i();
      float f1 = pQuaternion.j();
      float f2 = pQuaternion.k();
      float f3 = pQuaternion.r();
      float f4 = 2.0F * f * f;
      float f5 = 2.0F * f1 * f1;
      float f6 = 2.0F * f2 * f2;
      this.m00 = 1.0F - f5 - f6;
      this.m11 = 1.0F - f6 - f4;
      this.m22 = 1.0F - f4 - f5;
      float f7 = f * f1;
      float f8 = f1 * f2;
      float f9 = f2 * f;
      float f10 = f * f3;
      float f11 = f1 * f3;
      float f12 = f2 * f3;
      this.m10 = 2.0F * (f7 + f12);
      this.m01 = 2.0F * (f7 - f12);
      this.m20 = 2.0F * (f9 - f11);
      this.m02 = 2.0F * (f9 + f11);
      this.m21 = 2.0F * (f8 + f10);
      this.m12 = 2.0F * (f8 - f10);
   }

   @OnlyIn(Dist.CLIENT)
   public static Matrix3f createScaleMatrix(float pM00, float pM11, float pM22) {
      Matrix3f matrix3f = new Matrix3f();
      matrix3f.m00 = pM00;
      matrix3f.m11 = pM11;
      matrix3f.m22 = pM22;
      return matrix3f;
   }

   /**
    * Copy constructor for this class.
    * @param pOther The instance to copy from
    */
   public Matrix3f(Matrix4f pMatrix) {
      this.m00 = pMatrix.m00;
      this.m01 = pMatrix.m01;
      this.m02 = pMatrix.m02;
      this.m10 = pMatrix.m10;
      this.m11 = pMatrix.m11;
      this.m12 = pMatrix.m12;
      this.m20 = pMatrix.m20;
      this.m21 = pMatrix.m21;
      this.m22 = pMatrix.m22;
   }

   /**
    * Copy constructor for this class.
    * @param pOther The instance to copy from
    */
   public Matrix3f(Matrix3f pOther) {
      this.m00 = pOther.m00;
      this.m01 = pOther.m01;
      this.m02 = pOther.m02;
      this.m10 = pOther.m10;
      this.m11 = pOther.m11;
      this.m12 = pOther.m12;
      this.m20 = pOther.m20;
      this.m21 = pOther.m21;
      this.m22 = pOther.m22;
   }

   @OnlyIn(Dist.CLIENT)
   private static Pair<Float, Float> approxGivensQuat(float p_226113_0_, float p_226113_1_, float p_226113_2_) {
      float f = 2.0F * (p_226113_0_ - p_226113_2_);
      if (G * p_226113_1_ * p_226113_1_ < f * f) {
         float f1 = MathHelper.fastInvSqrt(p_226113_1_ * p_226113_1_ + f * f);
         return Pair.of(f1 * p_226113_1_, f1 * f);
      } else {
         return Pair.of(SS, CS);
      }
   }

   @OnlyIn(Dist.CLIENT)
   private static Pair<Float, Float> qrGivensQuat(float p_226112_0_, float p_226112_1_) {
      float f = (float)Math.hypot((double)p_226112_0_, (double)p_226112_1_);
      float f1 = f > 1.0E-6F ? p_226112_1_ : 0.0F;
      float f2 = Math.abs(p_226112_0_) + Math.max(f, 1.0E-6F);
      if (p_226112_0_ < 0.0F) {
         float f3 = f1;
         f1 = f2;
         f2 = f3;
      }

      float f4 = MathHelper.fastInvSqrt(f2 * f2 + f1 * f1);
      f2 = f2 * f4;
      f1 = f1 * f4;
      return Pair.of(f1, f2);
   }

   @OnlyIn(Dist.CLIENT)
   private static Quaternion stepJacobi(Matrix3f p_226120_0_) {
      Matrix3f matrix3f = new Matrix3f();
      Quaternion quaternion = Quaternion.ONE.copy();
      if (p_226120_0_.m01 * p_226120_0_.m01 + p_226120_0_.m10 * p_226120_0_.m10 > 1.0E-6F) {
         Pair<Float, Float> pair = approxGivensQuat(p_226120_0_.m00, 0.5F * (p_226120_0_.m01 + p_226120_0_.m10), p_226120_0_.m11);
         Float f = pair.getFirst();
         Float f1 = pair.getSecond();
         Quaternion quaternion1 = new Quaternion(0.0F, 0.0F, f, f1);
         float f2 = f1 * f1 - f * f;
         float f3 = -2.0F * f * f1;
         float f4 = f1 * f1 + f * f;
         quaternion.mul(quaternion1);
         matrix3f.setIdentity();
         matrix3f.m00 = f2;
         matrix3f.m11 = f2;
         matrix3f.m10 = -f3;
         matrix3f.m01 = f3;
         matrix3f.m22 = f4;
         p_226120_0_.mul(matrix3f);
         matrix3f.transpose();
         matrix3f.mul(p_226120_0_);
         p_226120_0_.load(matrix3f);
      }

      if (p_226120_0_.m02 * p_226120_0_.m02 + p_226120_0_.m20 * p_226120_0_.m20 > 1.0E-6F) {
         Pair<Float, Float> pair1 = approxGivensQuat(p_226120_0_.m00, 0.5F * (p_226120_0_.m02 + p_226120_0_.m20), p_226120_0_.m22);
         float f5 = -pair1.getFirst();
         Float f7 = pair1.getSecond();
         Quaternion quaternion2 = new Quaternion(0.0F, f5, 0.0F, f7);
         float f9 = f7 * f7 - f5 * f5;
         float f11 = -2.0F * f5 * f7;
         float f13 = f7 * f7 + f5 * f5;
         quaternion.mul(quaternion2);
         matrix3f.setIdentity();
         matrix3f.m00 = f9;
         matrix3f.m22 = f9;
         matrix3f.m20 = f11;
         matrix3f.m02 = -f11;
         matrix3f.m11 = f13;
         p_226120_0_.mul(matrix3f);
         matrix3f.transpose();
         matrix3f.mul(p_226120_0_);
         p_226120_0_.load(matrix3f);
      }

      if (p_226120_0_.m12 * p_226120_0_.m12 + p_226120_0_.m21 * p_226120_0_.m21 > 1.0E-6F) {
         Pair<Float, Float> pair2 = approxGivensQuat(p_226120_0_.m11, 0.5F * (p_226120_0_.m12 + p_226120_0_.m21), p_226120_0_.m22);
         Float f6 = pair2.getFirst();
         Float f8 = pair2.getSecond();
         Quaternion quaternion3 = new Quaternion(f6, 0.0F, 0.0F, f8);
         float f10 = f8 * f8 - f6 * f6;
         float f12 = -2.0F * f6 * f8;
         float f14 = f8 * f8 + f6 * f6;
         quaternion.mul(quaternion3);
         matrix3f.setIdentity();
         matrix3f.m11 = f10;
         matrix3f.m22 = f10;
         matrix3f.m21 = -f12;
         matrix3f.m12 = f12;
         matrix3f.m00 = f14;
         p_226120_0_.mul(matrix3f);
         matrix3f.transpose();
         matrix3f.mul(p_226120_0_);
         p_226120_0_.load(matrix3f);
      }

      return quaternion;
   }

   @OnlyIn(Dist.CLIENT)
   public void transpose() {
      float f = this.m01;
      this.m01 = this.m10;
      this.m10 = f;
      f = this.m02;
      this.m02 = this.m20;
      this.m20 = f;
      f = this.m12;
      this.m12 = this.m21;
      this.m21 = f;
   }

   @OnlyIn(Dist.CLIENT)
   public Triple<Quaternion, Vector3f, Quaternion> svdDecompose() {
      Quaternion quaternion = Quaternion.ONE.copy();
      Quaternion quaternion1 = Quaternion.ONE.copy();
      Matrix3f matrix3f = this.copy();
      matrix3f.transpose();
      matrix3f.mul(this);

      for(int i = 0; i < 5; ++i) {
         quaternion1.mul(stepJacobi(matrix3f));
      }

      quaternion1.normalize();
      Matrix3f matrix3f4 = new Matrix3f(this);
      matrix3f4.mul(new Matrix3f(quaternion1));
      float f = 1.0F;
      Pair<Float, Float> pair = qrGivensQuat(matrix3f4.m00, matrix3f4.m10);
      Float f1 = pair.getFirst();
      Float f2 = pair.getSecond();
      float f3 = f2 * f2 - f1 * f1;
      float f4 = -2.0F * f1 * f2;
      float f5 = f2 * f2 + f1 * f1;
      Quaternion quaternion2 = new Quaternion(0.0F, 0.0F, f1, f2);
      quaternion.mul(quaternion2);
      Matrix3f matrix3f1 = new Matrix3f();
      matrix3f1.setIdentity();
      matrix3f1.m00 = f3;
      matrix3f1.m11 = f3;
      matrix3f1.m10 = f4;
      matrix3f1.m01 = -f4;
      matrix3f1.m22 = f5;
      f = f * f5;
      matrix3f1.mul(matrix3f4);
      pair = qrGivensQuat(matrix3f1.m00, matrix3f1.m20);
      float f6 = -pair.getFirst();
      Float f7 = pair.getSecond();
      float f8 = f7 * f7 - f6 * f6;
      float f9 = -2.0F * f6 * f7;
      float f10 = f7 * f7 + f6 * f6;
      Quaternion quaternion3 = new Quaternion(0.0F, f6, 0.0F, f7);
      quaternion.mul(quaternion3);
      Matrix3f matrix3f2 = new Matrix3f();
      matrix3f2.setIdentity();
      matrix3f2.m00 = f8;
      matrix3f2.m22 = f8;
      matrix3f2.m20 = -f9;
      matrix3f2.m02 = f9;
      matrix3f2.m11 = f10;
      f = f * f10;
      matrix3f2.mul(matrix3f1);
      pair = qrGivensQuat(matrix3f2.m11, matrix3f2.m21);
      Float f11 = pair.getFirst();
      Float f12 = pair.getSecond();
      float f13 = f12 * f12 - f11 * f11;
      float f14 = -2.0F * f11 * f12;
      float f15 = f12 * f12 + f11 * f11;
      Quaternion quaternion4 = new Quaternion(f11, 0.0F, 0.0F, f12);
      quaternion.mul(quaternion4);
      Matrix3f matrix3f3 = new Matrix3f();
      matrix3f3.setIdentity();
      matrix3f3.m11 = f13;
      matrix3f3.m22 = f13;
      matrix3f3.m21 = f14;
      matrix3f3.m12 = -f14;
      matrix3f3.m00 = f15;
      f = f * f15;
      matrix3f3.mul(matrix3f2);
      f = 1.0F / f;
      quaternion.mul((float)Math.sqrt((double)f));
      Vector3f vector3f = new Vector3f(matrix3f3.m00 * f, matrix3f3.m11 * f, matrix3f3.m22 * f);
      return Triple.of(quaternion, vector3f, quaternion1);
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
         Matrix3f matrix3f = (Matrix3f)p_equals_1_;
         return Float.compare(matrix3f.m00, this.m00) == 0 && Float.compare(matrix3f.m01, this.m01) == 0 && Float.compare(matrix3f.m02, this.m02) == 0 && Float.compare(matrix3f.m10, this.m10) == 0 && Float.compare(matrix3f.m11, this.m11) == 0 && Float.compare(matrix3f.m12, this.m12) == 0 && Float.compare(matrix3f.m20, this.m20) == 0 && Float.compare(matrix3f.m21, this.m21) == 0 && Float.compare(matrix3f.m22, this.m22) == 0;
      } else {
         return false;
      }
   }

   public int hashCode() {
      int i = this.m00 != 0.0F ? Float.floatToIntBits(this.m00) : 0;
      i = 31 * i + (this.m01 != 0.0F ? Float.floatToIntBits(this.m01) : 0);
      i = 31 * i + (this.m02 != 0.0F ? Float.floatToIntBits(this.m02) : 0);
      i = 31 * i + (this.m10 != 0.0F ? Float.floatToIntBits(this.m10) : 0);
      i = 31 * i + (this.m11 != 0.0F ? Float.floatToIntBits(this.m11) : 0);
      i = 31 * i + (this.m12 != 0.0F ? Float.floatToIntBits(this.m12) : 0);
      i = 31 * i + (this.m20 != 0.0F ? Float.floatToIntBits(this.m20) : 0);
      i = 31 * i + (this.m21 != 0.0F ? Float.floatToIntBits(this.m21) : 0);
      return 31 * i + (this.m22 != 0.0F ? Float.floatToIntBits(this.m22) : 0);
   }

   @OnlyIn(Dist.CLIENT)
   public void load(Matrix3f pOther) {
      this.m00 = pOther.m00;
      this.m01 = pOther.m01;
      this.m02 = pOther.m02;
      this.m10 = pOther.m10;
      this.m11 = pOther.m11;
      this.m12 = pOther.m12;
      this.m20 = pOther.m20;
      this.m21 = pOther.m21;
      this.m22 = pOther.m22;
   }

   public String toString() {
      StringBuilder stringbuilder = new StringBuilder();
      stringbuilder.append("Matrix3f:\n");
      stringbuilder.append(this.m00);
      stringbuilder.append(" ");
      stringbuilder.append(this.m01);
      stringbuilder.append(" ");
      stringbuilder.append(this.m02);
      stringbuilder.append("\n");
      stringbuilder.append(this.m10);
      stringbuilder.append(" ");
      stringbuilder.append(this.m11);
      stringbuilder.append(" ");
      stringbuilder.append(this.m12);
      stringbuilder.append("\n");
      stringbuilder.append(this.m20);
      stringbuilder.append(" ");
      stringbuilder.append(this.m21);
      stringbuilder.append(" ");
      stringbuilder.append(this.m22);
      stringbuilder.append("\n");
      return stringbuilder.toString();
   }

   @OnlyIn(Dist.CLIENT)
   public void setIdentity() {
      this.m00 = 1.0F;
      this.m01 = 0.0F;
      this.m02 = 0.0F;
      this.m10 = 0.0F;
      this.m11 = 1.0F;
      this.m12 = 0.0F;
      this.m20 = 0.0F;
      this.m21 = 0.0F;
      this.m22 = 1.0F;
   }

   @OnlyIn(Dist.CLIENT)
   public float adjugateAndDet() {
      float f = this.m11 * this.m22 - this.m12 * this.m21;
      float f1 = -(this.m10 * this.m22 - this.m12 * this.m20);
      float f2 = this.m10 * this.m21 - this.m11 * this.m20;
      float f3 = -(this.m01 * this.m22 - this.m02 * this.m21);
      float f4 = this.m00 * this.m22 - this.m02 * this.m20;
      float f5 = -(this.m00 * this.m21 - this.m01 * this.m20);
      float f6 = this.m01 * this.m12 - this.m02 * this.m11;
      float f7 = -(this.m00 * this.m12 - this.m02 * this.m10);
      float f8 = this.m00 * this.m11 - this.m01 * this.m10;
      float f9 = this.m00 * f + this.m01 * f1 + this.m02 * f2;
      this.m00 = f;
      this.m10 = f1;
      this.m20 = f2;
      this.m01 = f3;
      this.m11 = f4;
      this.m21 = f5;
      this.m02 = f6;
      this.m12 = f7;
      this.m22 = f8;
      return f9;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean invert() {
      float f = this.adjugateAndDet();
      if (Math.abs(f) > 1.0E-6F) {
         this.mul(f);
         return true;
      } else {
         return false;
      }
   }

   public void set(int p_232605_1_, int p_232605_2_, float pValue) {
      if (p_232605_1_ == 0) {
         if (p_232605_2_ == 0) {
            this.m00 = pValue;
         } else if (p_232605_2_ == 1) {
            this.m01 = pValue;
         } else {
            this.m02 = pValue;
         }
      } else if (p_232605_1_ == 1) {
         if (p_232605_2_ == 0) {
            this.m10 = pValue;
         } else if (p_232605_2_ == 1) {
            this.m11 = pValue;
         } else {
            this.m12 = pValue;
         }
      } else if (p_232605_2_ == 0) {
         this.m20 = pValue;
      } else if (p_232605_2_ == 1) {
         this.m21 = pValue;
      } else {
         this.m22 = pValue;
      }

   }

   public void mul(Matrix3f pOther) {
      float f = this.m00 * pOther.m00 + this.m01 * pOther.m10 + this.m02 * pOther.m20;
      float f1 = this.m00 * pOther.m01 + this.m01 * pOther.m11 + this.m02 * pOther.m21;
      float f2 = this.m00 * pOther.m02 + this.m01 * pOther.m12 + this.m02 * pOther.m22;
      float f3 = this.m10 * pOther.m00 + this.m11 * pOther.m10 + this.m12 * pOther.m20;
      float f4 = this.m10 * pOther.m01 + this.m11 * pOther.m11 + this.m12 * pOther.m21;
      float f5 = this.m10 * pOther.m02 + this.m11 * pOther.m12 + this.m12 * pOther.m22;
      float f6 = this.m20 * pOther.m00 + this.m21 * pOther.m10 + this.m22 * pOther.m20;
      float f7 = this.m20 * pOther.m01 + this.m21 * pOther.m11 + this.m22 * pOther.m21;
      float f8 = this.m20 * pOther.m02 + this.m21 * pOther.m12 + this.m22 * pOther.m22;
      this.m00 = f;
      this.m01 = f1;
      this.m02 = f2;
      this.m10 = f3;
      this.m11 = f4;
      this.m12 = f5;
      this.m20 = f6;
      this.m21 = f7;
      this.m22 = f8;
   }

   @OnlyIn(Dist.CLIENT)
   public void mul(Quaternion pQuaternion) {
      this.mul(new Matrix3f(pQuaternion));
   }

   @OnlyIn(Dist.CLIENT)
   public void mul(float pMultiplier) {
      this.m00 *= pMultiplier;
      this.m01 *= pMultiplier;
      this.m02 *= pMultiplier;
      this.m10 *= pMultiplier;
      this.m11 *= pMultiplier;
      this.m12 *= pMultiplier;
      this.m20 *= pMultiplier;
      this.m21 *= pMultiplier;
      this.m22 *= pMultiplier;
   }

   @OnlyIn(Dist.CLIENT)
   public Matrix3f copy() {
      return new Matrix3f(this);
   }

    // Forge start
    public void multiplyBackward(Matrix3f other) {
        Matrix3f copy = other.copy();
        copy.mul(this);
        this.load(copy);
    }
}
