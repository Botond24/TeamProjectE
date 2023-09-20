package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IRenderTypeBuffer {
   static IRenderTypeBuffer.Impl immediate(BufferBuilder pBuilder) {
      return immediateWithBuffers(ImmutableMap.of(), pBuilder);
   }

   static IRenderTypeBuffer.Impl immediateWithBuffers(Map<RenderType, BufferBuilder> pMapBuilders, BufferBuilder pBuilder) {
      return new IRenderTypeBuffer.Impl(pBuilder, pMapBuilders);
   }

   IVertexBuilder getBuffer(RenderType p_getBuffer_1_);

   @OnlyIn(Dist.CLIENT)
   public static class Impl implements IRenderTypeBuffer {
      protected final BufferBuilder builder;
      protected final Map<RenderType, BufferBuilder> fixedBuffers;
      protected Optional<RenderType> lastState = Optional.empty();
      protected final Set<BufferBuilder> startedBuffers = Sets.newHashSet();

      protected Impl(BufferBuilder p_i225969_1_, Map<RenderType, BufferBuilder> p_i225969_2_) {
         this.builder = p_i225969_1_;
         this.fixedBuffers = p_i225969_2_;
      }

      public IVertexBuilder getBuffer(RenderType p_getBuffer_1_) {
         Optional<RenderType> optional = p_getBuffer_1_.asOptional();
         BufferBuilder bufferbuilder = this.getBuilderRaw(p_getBuffer_1_);
         if (!Objects.equals(this.lastState, optional)) {
            if (this.lastState.isPresent()) {
               RenderType rendertype = this.lastState.get();
               if (!this.fixedBuffers.containsKey(rendertype)) {
                  this.endBatch(rendertype);
               }
            }

            if (this.startedBuffers.add(bufferbuilder)) {
               bufferbuilder.begin(p_getBuffer_1_.mode(), p_getBuffer_1_.format());
            }

            this.lastState = optional;
         }

         return bufferbuilder;
      }

      private BufferBuilder getBuilderRaw(RenderType pRenderType) {
         return this.fixedBuffers.getOrDefault(pRenderType, this.builder);
      }

      public void endBatch() {
         this.lastState.ifPresent((p_228464_1_) -> {
            IVertexBuilder ivertexbuilder = this.getBuffer(p_228464_1_);
            if (ivertexbuilder == this.builder) {
               this.endBatch(p_228464_1_);
            }

         });

         for(RenderType rendertype : this.fixedBuffers.keySet()) {
            this.endBatch(rendertype);
         }

      }

      public void endBatch(RenderType pRenderType) {
         BufferBuilder bufferbuilder = this.getBuilderRaw(pRenderType);
         boolean flag = Objects.equals(this.lastState, pRenderType.asOptional());
         if (flag || bufferbuilder != this.builder) {
            if (this.startedBuffers.remove(bufferbuilder)) {
               pRenderType.end(bufferbuilder, 0, 0, 0);
               if (flag) {
                  this.lastState = Optional.empty();
               }

            }
         }
      }
   }
}