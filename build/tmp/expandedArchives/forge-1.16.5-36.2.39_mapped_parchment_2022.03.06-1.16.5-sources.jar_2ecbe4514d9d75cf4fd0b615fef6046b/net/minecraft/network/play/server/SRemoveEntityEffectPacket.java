package net.minecraft.network.play.server;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Effect;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SRemoveEntityEffectPacket implements IPacket<IClientPlayNetHandler> {
   private int entityId;
   private Effect effect;

   public SRemoveEntityEffectPacket() {
   }

   public SRemoveEntityEffectPacket(int pEntityId, Effect pEffect) {
      this.entityId = pEntityId;
      this.effect = pEffect;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.entityId = p_148837_1_.readVarInt();
      this.effect = Effect.byId(p_148837_1_.readUnsignedByte());
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.entityId);
      pBuffer.writeByte(Effect.getId(this.effect));
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleRemoveMobEffect(this);
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public Entity getEntity(World pLevel) {
      return pLevel.getEntity(this.entityId);
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public Effect getEffect() {
      return this.effect;
   }
}