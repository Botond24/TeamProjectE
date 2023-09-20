package net.minecraft.client.renderer.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemModelGenerator {
   public static final List<String> LAYERS = Lists.newArrayList("layer0", "layer1", "layer2", "layer3", "layer4");

   public BlockModel generateBlockModel(Function<RenderMaterial, TextureAtlasSprite> pTextureGetter, BlockModel pBlockModel) {
      Map<String, Either<RenderMaterial, String>> map = Maps.newHashMap();
      List<BlockPart> list = Lists.newArrayList();

      for(int i = 0; i < LAYERS.size(); ++i) {
         String s = LAYERS.get(i);
         if (!pBlockModel.hasTexture(s)) {
            break;
         }

         RenderMaterial rendermaterial = pBlockModel.getMaterial(s);
         map.put(s, Either.left(rendermaterial));
         TextureAtlasSprite textureatlassprite = pTextureGetter.apply(rendermaterial);
         list.addAll(this.processFrames(i, s, textureatlassprite));
      }

      map.put("particle", pBlockModel.hasTexture("particle") ? Either.left(pBlockModel.getMaterial("particle")) : map.get("layer0"));
      BlockModel blockmodel = new BlockModel((ResourceLocation)null, list, map, false, pBlockModel.getGuiLight(), pBlockModel.getTransforms(), pBlockModel.getOverrides());
      blockmodel.name = pBlockModel.name;
      blockmodel.customData.copyFrom(pBlockModel.customData);
      return blockmodel;
   }

   private List<BlockPart> processFrames(int pTintIndex, String pTexture, TextureAtlasSprite pSprite) {
      Map<Direction, BlockPartFace> map = Maps.newHashMap();
      map.put(Direction.SOUTH, new BlockPartFace((Direction)null, pTintIndex, pTexture, new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0)));
      map.put(Direction.NORTH, new BlockPartFace((Direction)null, pTintIndex, pTexture, new BlockFaceUV(new float[]{16.0F, 0.0F, 0.0F, 16.0F}, 0)));
      List<BlockPart> list = Lists.newArrayList();
      list.add(new BlockPart(new Vector3f(0.0F, 0.0F, 7.5F), new Vector3f(16.0F, 16.0F, 8.5F), map, (BlockPartRotation)null, true));
      list.addAll(this.createSideElements(pSprite, pTexture, pTintIndex));
      return list;
   }

   private List<BlockPart> createSideElements(TextureAtlasSprite pSprite, String pTexture, int pTintIndex) {
      float f = (float)pSprite.getWidth();
      float f1 = (float)pSprite.getHeight();
      List<BlockPart> list = Lists.newArrayList();

      for(ItemModelGenerator.Span itemmodelgenerator$span : this.getSpans(pSprite)) {
         float f2 = 0.0F;
         float f3 = 0.0F;
         float f4 = 0.0F;
         float f5 = 0.0F;
         float f6 = 0.0F;
         float f7 = 0.0F;
         float f8 = 0.0F;
         float f9 = 0.0F;
         float f10 = 16.0F / f;
         float f11 = 16.0F / f1;
         float f12 = (float)itemmodelgenerator$span.getMin();
         float f13 = (float)itemmodelgenerator$span.getMax();
         float f14 = (float)itemmodelgenerator$span.getAnchor();
         ItemModelGenerator.SpanFacing itemmodelgenerator$spanfacing = itemmodelgenerator$span.getFacing();
         switch(itemmodelgenerator$spanfacing) {
         case UP:
            f6 = f12;
            f2 = f12;
            f4 = f7 = f13 + 1.0F;
            f8 = f14;
            f3 = f14;
            f5 = f14;
            f9 = f14 + 1.0F;
            break;
         case DOWN:
            f8 = f14;
            f9 = f14 + 1.0F;
            f6 = f12;
            f2 = f12;
            f4 = f7 = f13 + 1.0F;
            f3 = f14 + 1.0F;
            f5 = f14 + 1.0F;
            break;
         case LEFT:
            f6 = f14;
            f2 = f14;
            f4 = f14;
            f7 = f14 + 1.0F;
            f9 = f12;
            f3 = f12;
            f5 = f8 = f13 + 1.0F;
            break;
         case RIGHT:
            f6 = f14;
            f7 = f14 + 1.0F;
            f2 = f14 + 1.0F;
            f4 = f14 + 1.0F;
            f9 = f12;
            f3 = f12;
            f5 = f8 = f13 + 1.0F;
         }

         f2 = f2 * f10;
         f4 = f4 * f10;
         f3 = f3 * f11;
         f5 = f5 * f11;
         f3 = 16.0F - f3;
         f5 = 16.0F - f5;
         f6 = f6 * f10;
         f7 = f7 * f10;
         f8 = f8 * f11;
         f9 = f9 * f11;
         Map<Direction, BlockPartFace> map = Maps.newHashMap();
         map.put(itemmodelgenerator$spanfacing.getDirection(), new BlockPartFace((Direction)null, pTintIndex, pTexture, new BlockFaceUV(new float[]{f6, f8, f7, f9}, 0)));
         switch(itemmodelgenerator$spanfacing) {
         case UP:
            list.add(new BlockPart(new Vector3f(f2, f3, 7.5F), new Vector3f(f4, f3, 8.5F), map, (BlockPartRotation)null, true));
            break;
         case DOWN:
            list.add(new BlockPart(new Vector3f(f2, f5, 7.5F), new Vector3f(f4, f5, 8.5F), map, (BlockPartRotation)null, true));
            break;
         case LEFT:
            list.add(new BlockPart(new Vector3f(f2, f3, 7.5F), new Vector3f(f2, f5, 8.5F), map, (BlockPartRotation)null, true));
            break;
         case RIGHT:
            list.add(new BlockPart(new Vector3f(f4, f3, 7.5F), new Vector3f(f4, f5, 8.5F), map, (BlockPartRotation)null, true));
         }
      }

      return list;
   }

   private List<ItemModelGenerator.Span> getSpans(TextureAtlasSprite pSprite) {
      int i = pSprite.getWidth();
      int j = pSprite.getHeight();
      List<ItemModelGenerator.Span> list = Lists.newArrayList();

      for(int k = 0; k < pSprite.getFrameCount(); ++k) {
         for(int l = 0; l < j; ++l) {
            for(int i1 = 0; i1 < i; ++i1) {
               boolean flag = !this.isTransparent(pSprite, k, i1, l, i, j);
               this.checkTransition(ItemModelGenerator.SpanFacing.UP, list, pSprite, k, i1, l, i, j, flag);
               this.checkTransition(ItemModelGenerator.SpanFacing.DOWN, list, pSprite, k, i1, l, i, j, flag);
               this.checkTransition(ItemModelGenerator.SpanFacing.LEFT, list, pSprite, k, i1, l, i, j, flag);
               this.checkTransition(ItemModelGenerator.SpanFacing.RIGHT, list, pSprite, k, i1, l, i, j, flag);
            }
         }
      }

      return list;
   }

   private void checkTransition(ItemModelGenerator.SpanFacing pSpanFacing, List<ItemModelGenerator.Span> pListSpans, TextureAtlasSprite pSprite, int pFrameIndex, int pPixelX, int pPixelY, int pSpiteWidth, int pSpriteHeight, boolean pTransparent) {
      boolean flag = this.isTransparent(pSprite, pFrameIndex, pPixelX + pSpanFacing.getXOffset(), pPixelY + pSpanFacing.getYOffset(), pSpiteWidth, pSpriteHeight) && pTransparent;
      if (flag) {
         this.createOrExpandSpan(pListSpans, pSpanFacing, pPixelX, pPixelY);
      }

   }

   private void createOrExpandSpan(List<ItemModelGenerator.Span> pListSpans, ItemModelGenerator.SpanFacing pSpanFacing, int pPixelX, int pPixelY) {
      ItemModelGenerator.Span itemmodelgenerator$span = null;

      for(ItemModelGenerator.Span itemmodelgenerator$span1 : pListSpans) {
         if (itemmodelgenerator$span1.getFacing() == pSpanFacing) {
            int i = pSpanFacing.isHorizontal() ? pPixelY : pPixelX;
            if (itemmodelgenerator$span1.getAnchor() == i) {
               itemmodelgenerator$span = itemmodelgenerator$span1;
               break;
            }
         }
      }

      int j = pSpanFacing.isHorizontal() ? pPixelY : pPixelX;
      int k = pSpanFacing.isHorizontal() ? pPixelX : pPixelY;
      if (itemmodelgenerator$span == null) {
         pListSpans.add(new ItemModelGenerator.Span(pSpanFacing, k, j));
      } else {
         itemmodelgenerator$span.expand(k);
      }

   }

   private boolean isTransparent(TextureAtlasSprite pSprite, int pFrameIndex, int pPixelX, int pPixelY, int pSpiteWidth, int pSpriteHeight) {
      return pPixelX >= 0 && pPixelY >= 0 && pPixelX < pSpiteWidth && pPixelY < pSpriteHeight ? pSprite.isTransparent(pFrameIndex, pPixelX, pPixelY) : true;
   }

   @OnlyIn(Dist.CLIENT)
   static class Span {
      private final ItemModelGenerator.SpanFacing facing;
      private int min;
      private int max;
      private final int anchor;

      public Span(ItemModelGenerator.SpanFacing p_i46216_1_, int p_i46216_2_, int p_i46216_3_) {
         this.facing = p_i46216_1_;
         this.min = p_i46216_2_;
         this.max = p_i46216_2_;
         this.anchor = p_i46216_3_;
      }

      public void expand(int pPos) {
         if (pPos < this.min) {
            this.min = pPos;
         } else if (pPos > this.max) {
            this.max = pPos;
         }

      }

      public ItemModelGenerator.SpanFacing getFacing() {
         return this.facing;
      }

      public int getMin() {
         return this.min;
      }

      public int getMax() {
         return this.max;
      }

      public int getAnchor() {
         return this.anchor;
      }
   }

   @OnlyIn(Dist.CLIENT)
   static enum SpanFacing {
      UP(Direction.UP, 0, -1),
      DOWN(Direction.DOWN, 0, 1),
      LEFT(Direction.EAST, -1, 0),
      RIGHT(Direction.WEST, 1, 0);

      private final Direction direction;
      private final int xOffset;
      private final int yOffset;

      private SpanFacing(Direction p_i46215_3_, int p_i46215_4_, int p_i46215_5_) {
         this.direction = p_i46215_3_;
         this.xOffset = p_i46215_4_;
         this.yOffset = p_i46215_5_;
      }

      /**
       * Gets the direction of the block's facing.
       */
      public Direction getDirection() {
         return this.direction;
      }

      public int getXOffset() {
         return this.xOffset;
      }

      public int getYOffset() {
         return this.yOffset;
      }

      private boolean isHorizontal() {
         return this == DOWN || this == UP;
      }
   }
}