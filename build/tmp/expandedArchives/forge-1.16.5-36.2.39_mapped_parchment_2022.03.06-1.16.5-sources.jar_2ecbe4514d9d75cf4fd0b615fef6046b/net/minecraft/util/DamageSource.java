package net.minecraft.util;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;

public class DamageSource {
   public static final DamageSource IN_FIRE = (new DamageSource("inFire")).bypassArmor().setIsFire();
   public static final DamageSource LIGHTNING_BOLT = new DamageSource("lightningBolt");
   public static final DamageSource ON_FIRE = (new DamageSource("onFire")).bypassArmor().setIsFire();
   public static final DamageSource LAVA = (new DamageSource("lava")).setIsFire();
   public static final DamageSource HOT_FLOOR = (new DamageSource("hotFloor")).setIsFire();
   public static final DamageSource IN_WALL = (new DamageSource("inWall")).bypassArmor();
   public static final DamageSource CRAMMING = (new DamageSource("cramming")).bypassArmor();
   public static final DamageSource DROWN = (new DamageSource("drown")).bypassArmor();
   public static final DamageSource STARVE = (new DamageSource("starve")).bypassArmor().bypassMagic();
   public static final DamageSource CACTUS = new DamageSource("cactus");
   public static final DamageSource FALL = (new DamageSource("fall")).bypassArmor();
   public static final DamageSource FLY_INTO_WALL = (new DamageSource("flyIntoWall")).bypassArmor();
   public static final DamageSource OUT_OF_WORLD = (new DamageSource("outOfWorld")).bypassArmor().bypassInvul();
   public static final DamageSource GENERIC = (new DamageSource("generic")).bypassArmor();
   public static final DamageSource MAGIC = (new DamageSource("magic")).bypassArmor().setMagic();
   public static final DamageSource WITHER = (new DamageSource("wither")).bypassArmor();
   public static final DamageSource ANVIL = new DamageSource("anvil");
   public static final DamageSource FALLING_BLOCK = new DamageSource("fallingBlock");
   public static final DamageSource DRAGON_BREATH = (new DamageSource("dragonBreath")).bypassArmor();
   public static final DamageSource DRY_OUT = new DamageSource("dryout");
   public static final DamageSource SWEET_BERRY_BUSH = new DamageSource("sweetBerryBush");
   private boolean bypassArmor;
   private boolean bypassInvul;
   /** Whether or not the damage ignores modification by potion effects or enchantments. */
   private boolean bypassMagic;
   private float exhaustion = 0.1F;
   private boolean isFireSource;
   private boolean isProjectile;
   private boolean scalesWithDifficulty;
   private boolean isMagic;
   private boolean isExplosion;
   public final String msgId;

   public static DamageSource sting(LivingEntity pBee) {
      return new EntityDamageSource("sting", pBee);
   }

   public static DamageSource mobAttack(LivingEntity pMob) {
      return new EntityDamageSource("mob", pMob);
   }

   public static DamageSource indirectMobAttack(Entity pSource, LivingEntity pIndirectEntity) {
      return new IndirectEntityDamageSource("mob", pSource, pIndirectEntity);
   }

   /**
    * returns an EntityDamageSource of type player
    */
   public static DamageSource playerAttack(PlayerEntity pPlayer) {
      return new EntityDamageSource("player", pPlayer);
   }

   /**
    * returns EntityDamageSourceIndirect of an arrow
    */
   public static DamageSource arrow(AbstractArrowEntity pArrow, @Nullable Entity pIndirectEntity) {
      return (new IndirectEntityDamageSource("arrow", pArrow, pIndirectEntity)).setProjectile();
   }

   public static DamageSource trident(Entity pSource, @Nullable Entity pIndirectEntity) {
      return (new IndirectEntityDamageSource("trident", pSource, pIndirectEntity)).setProjectile();
   }

   public static DamageSource fireworks(FireworkRocketEntity pFirework, @Nullable Entity pIndirectEntity) {
      return (new IndirectEntityDamageSource("fireworks", pFirework, pIndirectEntity)).setExplosion();
   }

   public static DamageSource fireball(AbstractFireballEntity pFireball, @Nullable Entity pIndirectEntity) {
      return pIndirectEntity == null ? (new IndirectEntityDamageSource("onFire", pFireball, pFireball)).setIsFire().setProjectile() : (new IndirectEntityDamageSource("fireball", pFireball, pIndirectEntity)).setIsFire().setProjectile();
   }

   public static DamageSource witherSkull(WitherSkullEntity pWitherSkull, Entity pIndirectEntity) {
      return (new IndirectEntityDamageSource("witherSkull", pWitherSkull, pIndirectEntity)).setProjectile();
   }

   public static DamageSource thrown(Entity pSource, @Nullable Entity pIndirectEntity) {
      return (new IndirectEntityDamageSource("thrown", pSource, pIndirectEntity)).setProjectile();
   }

   public static DamageSource indirectMagic(Entity pSource, @Nullable Entity pIndirectEntity) {
      return (new IndirectEntityDamageSource("indirectMagic", pSource, pIndirectEntity)).bypassArmor().setMagic();
   }

   /**
    * Returns the EntityDamageSource of the Thorns enchantment
    */
   public static DamageSource thorns(Entity pSource) {
      return (new EntityDamageSource("thorns", pSource)).setThorns().setMagic();
   }

   public static DamageSource explosion(@Nullable Explosion pExplosion) {
      return explosion(pExplosion != null ? pExplosion.getSourceMob() : null);
   }

   public static DamageSource explosion(@Nullable LivingEntity pLivingEntity) {
      return pLivingEntity != null ? (new EntityDamageSource("explosion.player", pLivingEntity)).setScalesWithDifficulty().setExplosion() : (new DamageSource("explosion")).setScalesWithDifficulty().setExplosion();
   }

   public static DamageSource badRespawnPointExplosion() {
      return new BedExplosionDamageSource();
   }

   public String toString() {
      return "DamageSource (" + this.msgId + ")";
   }

   /**
    * Returns true if the damage is projectile based.
    */
   public boolean isProjectile() {
      return this.isProjectile;
   }

   /**
    * Define the damage type as projectile based.
    */
   public DamageSource setProjectile() {
      this.isProjectile = true;
      return this;
   }

   public boolean isExplosion() {
      return this.isExplosion;
   }

   public DamageSource setExplosion() {
      this.isExplosion = true;
      return this;
   }

   public boolean isBypassArmor() {
      return this.bypassArmor;
   }

   /**
    * How much satiate(food) is consumed by this DamageSource
    */
   public float getFoodExhaustion() {
      return this.exhaustion;
   }

   public boolean isBypassInvul() {
      return this.bypassInvul;
   }

   /**
    * Whether or not the damage ignores modification by potion effects or enchantments.
    */
   public boolean isBypassMagic() {
      return this.bypassMagic;
   }

   public DamageSource(String pMessageId) {
      this.msgId = pMessageId;
   }

   /**
    * Retrieves the immediate causer of the damage, e.g. the arrow entity, not its shooter
    */
   @Nullable
   public Entity getDirectEntity() {
      return this.getEntity();
   }

   /**
    * Retrieves the true causer of the damage, e.g. the player who fired an arrow, the shulker who fired the bullet,
    * etc.
    */
   @Nullable
   public Entity getEntity() {
      return null;
   }

   public DamageSource bypassArmor() {
      this.bypassArmor = true;
      this.exhaustion = 0.0F;
      return this;
   }

   public DamageSource bypassInvul() {
      this.bypassInvul = true;
      return this;
   }

   /**
    * Sets a value indicating whether the damage is absolute (ignores modification by potion effects or enchantments),
    * and also clears out hunger damage.
    */
   public DamageSource bypassMagic() {
      this.bypassMagic = true;
      this.exhaustion = 0.0F;
      return this;
   }

   /**
    * Define the damage type as fire based.
    */
   public DamageSource setIsFire() {
      this.isFireSource = true;
      return this;
   }

   /**
    * Gets the death message that is displayed when the player dies
    */
   public ITextComponent getLocalizedDeathMessage(LivingEntity pLivingEntity) {
      LivingEntity livingentity = pLivingEntity.getKillCredit();
      String s = "death.attack." + this.msgId;
      String s1 = s + ".player";
      return livingentity != null ? new TranslationTextComponent(s1, pLivingEntity.getDisplayName(), livingentity.getDisplayName()) : new TranslationTextComponent(s, pLivingEntity.getDisplayName());
   }

   /**
    * Returns true if the damage is fire based.
    */
   public boolean isFire() {
      return this.isFireSource;
   }

   /**
    * Return the name of damage type.
    */
   public String getMsgId() {
      return this.msgId;
   }

   /**
    * Set whether this damage source will have its damage amount scaled based on the current difficulty.
    */
   public DamageSource setScalesWithDifficulty() {
      this.scalesWithDifficulty = true;
      return this;
   }

   /**
    * Return whether this damage source will have its damage amount scaled based on the current difficulty.
    */
   public boolean scalesWithDifficulty() {
      return this.scalesWithDifficulty;
   }

   /**
    * Returns true if the damage is magic based.
    */
   public boolean isMagic() {
      return this.isMagic;
   }

   /**
    * Define the damage type as magic based.
    */
   public DamageSource setMagic() {
      this.isMagic = true;
      return this;
   }

   public boolean isCreativePlayer() {
      Entity entity = this.getEntity();
      return entity instanceof PlayerEntity && ((PlayerEntity)entity).abilities.instabuild;
   }

   /**
    * Gets the location from which the damage originates.
    */
   @Nullable
   public Vector3d getSourcePosition() {
      return null;
   }
}