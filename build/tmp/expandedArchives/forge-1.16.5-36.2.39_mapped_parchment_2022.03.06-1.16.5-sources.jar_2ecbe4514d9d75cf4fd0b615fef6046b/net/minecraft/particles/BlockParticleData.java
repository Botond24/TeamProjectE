package net.minecraft.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockParticleData implements IParticleData {
   public static final IParticleData.IDeserializer<BlockParticleData> DESERIALIZER = new IParticleData.IDeserializer<BlockParticleData>() {
      public BlockParticleData fromCommand(ParticleType<BlockParticleData> pParticleType, StringReader pReader) throws CommandSyntaxException {
         pReader.expect(' ');
         return new BlockParticleData(pParticleType, (new BlockStateParser(pReader, false)).parse(false).getState());
      }

      public BlockParticleData fromNetwork(ParticleType<BlockParticleData> pParticleType, PacketBuffer pBuffer) {
         return new BlockParticleData(pParticleType, Block.BLOCK_STATE_REGISTRY.byId(pBuffer.readVarInt()));
      }
   };
   private final ParticleType<BlockParticleData> type;
   private final BlockState state;

   public static Codec<BlockParticleData> codec(ParticleType<BlockParticleData> p_239800_0_) {
      return BlockState.CODEC.xmap((p_239801_1_) -> {
         return new BlockParticleData(p_239800_0_, p_239801_1_);
      }, (p_239799_0_) -> {
         return p_239799_0_.state;
      });
   }

   public BlockParticleData(ParticleType<BlockParticleData> p_i47953_1_, BlockState p_i47953_2_) {
      this.type = p_i47953_1_;
      this.state = p_i47953_2_;
   }

   public void writeToNetwork(PacketBuffer pBuffer) {
      pBuffer.writeVarInt(Block.BLOCK_STATE_REGISTRY.getId(this.state));
   }

   public String writeToString() {
      return Registry.PARTICLE_TYPE.getKey(this.getType()) + " " + BlockStateParser.serialize(this.state);
   }

   public ParticleType<BlockParticleData> getType() {
      return this.type;
   }

   @OnlyIn(Dist.CLIENT)
   public BlockState getState() {
      return this.state;
   }

   //FORGE: Add a source pos property, so we can provide models with additional model data
   private net.minecraft.util.math.BlockPos pos;
   public BlockParticleData setPos(net.minecraft.util.math.BlockPos pos) {
      this.pos = pos;
      return this;
   }

   public net.minecraft.util.math.BlockPos getPos() {
      return pos;
   }
}
