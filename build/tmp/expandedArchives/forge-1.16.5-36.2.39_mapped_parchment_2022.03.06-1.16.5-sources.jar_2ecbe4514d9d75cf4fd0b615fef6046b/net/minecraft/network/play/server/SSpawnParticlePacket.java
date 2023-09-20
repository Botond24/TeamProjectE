package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SSpawnParticlePacket implements IPacket<IClientPlayNetHandler> {
   private double x;
   private double y;
   private double z;
   private float xDist;
   private float yDist;
   private float zDist;
   private float maxSpeed;
   private int count;
   private boolean overrideLimiter;
   private IParticleData particle;

   public SSpawnParticlePacket() {
   }

   public <T extends IParticleData> SSpawnParticlePacket(T pParticle, boolean pOverrideLimiter, double pX, double pY, double pZ, float pXDist, float pYDist, float pZDist, float pMaxSpeed, int pCount) {
      this.particle = pParticle;
      this.overrideLimiter = pOverrideLimiter;
      this.x = pX;
      this.y = pY;
      this.z = pZ;
      this.xDist = pXDist;
      this.yDist = pYDist;
      this.zDist = pZDist;
      this.maxSpeed = pMaxSpeed;
      this.count = pCount;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      ParticleType<?> particletype = Registry.PARTICLE_TYPE.byId(p_148837_1_.readInt());
      if (particletype == null) {
         particletype = ParticleTypes.BARRIER;
      }

      this.overrideLimiter = p_148837_1_.readBoolean();
      this.x = p_148837_1_.readDouble();
      this.y = p_148837_1_.readDouble();
      this.z = p_148837_1_.readDouble();
      this.xDist = p_148837_1_.readFloat();
      this.yDist = p_148837_1_.readFloat();
      this.zDist = p_148837_1_.readFloat();
      this.maxSpeed = p_148837_1_.readFloat();
      this.count = p_148837_1_.readInt();
      this.particle = this.readParticle(p_148837_1_, particletype);
   }

   private <T extends IParticleData> T readParticle(PacketBuffer pBuffer, ParticleType<T> pParticleType) {
      return pParticleType.getDeserializer().fromNetwork(pParticleType, pBuffer);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeInt(Registry.PARTICLE_TYPE.getId(this.particle.getType()));
      pBuffer.writeBoolean(this.overrideLimiter);
      pBuffer.writeDouble(this.x);
      pBuffer.writeDouble(this.y);
      pBuffer.writeDouble(this.z);
      pBuffer.writeFloat(this.xDist);
      pBuffer.writeFloat(this.yDist);
      pBuffer.writeFloat(this.zDist);
      pBuffer.writeFloat(this.maxSpeed);
      pBuffer.writeInt(this.count);
      this.particle.writeToNetwork(pBuffer);
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isOverrideLimiter() {
      return this.overrideLimiter;
   }

   /**
    * Gets the x coordinate to spawn the particle.
    */
   @OnlyIn(Dist.CLIENT)
   public double getX() {
      return this.x;
   }

   /**
    * Gets the y coordinate to spawn the particle.
    */
   @OnlyIn(Dist.CLIENT)
   public double getY() {
      return this.y;
   }

   /**
    * Gets the z coordinate to spawn the particle.
    */
   @OnlyIn(Dist.CLIENT)
   public double getZ() {
      return this.z;
   }

   /**
    * Gets the x coordinate offset for the particle. The particle may use the offset for particle spread.
    */
   @OnlyIn(Dist.CLIENT)
   public float getXDist() {
      return this.xDist;
   }

   /**
    * Gets the y coordinate offset for the particle. The particle may use the offset for particle spread.
    */
   @OnlyIn(Dist.CLIENT)
   public float getYDist() {
      return this.yDist;
   }

   /**
    * Gets the z coordinate offset for the particle. The particle may use the offset for particle spread.
    */
   @OnlyIn(Dist.CLIENT)
   public float getZDist() {
      return this.zDist;
   }

   /**
    * Gets the speed of the particle animation (used in client side rendering).
    */
   @OnlyIn(Dist.CLIENT)
   public float getMaxSpeed() {
      return this.maxSpeed;
   }

   /**
    * Gets the amount of particles to spawn
    */
   @OnlyIn(Dist.CLIENT)
   public int getCount() {
      return this.count;
   }

   @OnlyIn(Dist.CLIENT)
   public IParticleData getParticle() {
      return this.particle;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleParticleEvent(this);
   }
}