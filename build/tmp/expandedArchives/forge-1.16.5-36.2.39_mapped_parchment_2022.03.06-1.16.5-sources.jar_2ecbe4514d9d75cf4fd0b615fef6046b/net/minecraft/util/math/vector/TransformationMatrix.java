package net.minecraft.util.math.vector;

import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Triple;

@OnlyIn(Dist.CLIENT)
public final class TransformationMatrix implements net.minecraftforge.client.extensions.IForgeTransformationMatrix {
   private final Matrix4f matrix;
   private boolean decomposed;
   @Nullable
   private Vector3f translation;
   @Nullable
   private Quaternion leftRotation;
   @Nullable
   private Vector3f scale;
   @Nullable
   private Quaternion rightRotation;
   private static final TransformationMatrix IDENTITY = Util.make(() -> {
      Matrix4f matrix4f = new Matrix4f();
      matrix4f.setIdentity();
      TransformationMatrix transformationmatrix = new TransformationMatrix(matrix4f);
      transformationmatrix.getLeftRotation();
      return transformationmatrix;
   });

   public TransformationMatrix(@Nullable Matrix4f p_i225915_1_) {
      if (p_i225915_1_ == null) {
         this.matrix = IDENTITY.matrix;
      } else {
         this.matrix = p_i225915_1_;
      }

   }

   public TransformationMatrix(@Nullable Vector3f p_i225916_1_, @Nullable Quaternion p_i225916_2_, @Nullable Vector3f p_i225916_3_, @Nullable Quaternion p_i225916_4_) {
      this.matrix = compose(p_i225916_1_, p_i225916_2_, p_i225916_3_, p_i225916_4_);
      this.translation = p_i225916_1_ != null ? p_i225916_1_ : new Vector3f();
      this.leftRotation = p_i225916_2_ != null ? p_i225916_2_ : Quaternion.ONE.copy();
      this.scale = p_i225916_3_ != null ? p_i225916_3_ : new Vector3f(1.0F, 1.0F, 1.0F);
      this.rightRotation = p_i225916_4_ != null ? p_i225916_4_ : Quaternion.ONE.copy();
      this.decomposed = true;
   }

   public static TransformationMatrix identity() {
      return IDENTITY;
   }

   public TransformationMatrix compose(TransformationMatrix pOther) {
      Matrix4f matrix4f = this.getMatrix();
      matrix4f.multiply(pOther.getMatrix());
      return new TransformationMatrix(matrix4f);
   }

   @Nullable
   public TransformationMatrix inverse() {
      if (this == IDENTITY) {
         return this;
      } else {
         Matrix4f matrix4f = this.getMatrix();
         return matrix4f.invert() ? new TransformationMatrix(matrix4f) : null;
      }
   }

   private void ensureDecomposed() {
      if (!this.decomposed) {
         Pair<Matrix3f, Vector3f> pair = toAffine(this.matrix);
         Triple<Quaternion, Vector3f, Quaternion> triple = pair.getFirst().svdDecompose();
         this.translation = pair.getSecond();
         this.leftRotation = triple.getLeft();
         this.scale = triple.getMiddle();
         this.rightRotation = triple.getRight();
         this.decomposed = true;
      }

   }

   private static Matrix4f compose(@Nullable Vector3f pTranslation, @Nullable Quaternion pRotationLeft, @Nullable Vector3f pScale, @Nullable Quaternion pRotationRight) {
      Matrix4f matrix4f = new Matrix4f();
      matrix4f.setIdentity();
      if (pRotationLeft != null) {
         matrix4f.multiply(new Matrix4f(pRotationLeft));
      }

      if (pScale != null) {
         matrix4f.multiply(Matrix4f.createScaleMatrix(pScale.x(), pScale.y(), pScale.z()));
      }

      if (pRotationRight != null) {
         matrix4f.multiply(new Matrix4f(pRotationRight));
      }

      if (pTranslation != null) {
         matrix4f.m03 = pTranslation.x();
         matrix4f.m13 = pTranslation.y();
         matrix4f.m23 = pTranslation.z();
      }

      return matrix4f;
   }

   public static Pair<Matrix3f, Vector3f> toAffine(Matrix4f pMatrix) {
      pMatrix.multiply(1.0F / pMatrix.m33);
      Vector3f vector3f = new Vector3f(pMatrix.m03, pMatrix.m13, pMatrix.m23);
      Matrix3f matrix3f = new Matrix3f(pMatrix);
      return Pair.of(matrix3f, vector3f);
   }

   public Matrix4f getMatrix() {
      return this.matrix.copy();
   }

   public Quaternion getLeftRotation() {
      this.ensureDecomposed();
      return this.leftRotation.copy();
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
         TransformationMatrix transformationmatrix = (TransformationMatrix)p_equals_1_;
         return Objects.equals(this.matrix, transformationmatrix.matrix);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(this.matrix);
   }

    // FORGE START
    public Vector3f getTranslation() {
        ensureDecomposed();
        return translation.copy();
    }
    public Vector3f getScale() {
        ensureDecomposed();
        return scale.copy();
    }

    public Quaternion getRightRot() {
        ensureDecomposed();
        return rightRotation.copy();
    }

    private Matrix3f normalTransform = null;
    public Matrix3f getNormalMatrix() {
        checkNormalTransform();
        return normalTransform;
    }
    private void checkNormalTransform() {
        if (normalTransform == null) {
            normalTransform = new Matrix3f(matrix);
            normalTransform.invert();
            normalTransform.transpose();
        }
    }
}
