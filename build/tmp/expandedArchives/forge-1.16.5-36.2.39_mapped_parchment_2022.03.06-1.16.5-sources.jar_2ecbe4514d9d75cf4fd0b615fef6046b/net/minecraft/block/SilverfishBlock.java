package net.minecraft.block;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.SilverfishEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class SilverfishBlock extends Block {
   private final Block hostBlock;
   private static final Map<Block, Block> BLOCK_BY_HOST_BLOCK = Maps.newIdentityHashMap();

   public SilverfishBlock(Block pHostBlock, AbstractBlock.Properties pProperties) {
      super(pProperties);
      this.hostBlock = pHostBlock;
      BLOCK_BY_HOST_BLOCK.put(pHostBlock, this);
   }

   public Block getHostBlock() {
      return this.hostBlock;
   }

   public static boolean isCompatibleHostBlock(BlockState pState) {
      return BLOCK_BY_HOST_BLOCK.containsKey(pState.getBlock());
   }

   private void spawnInfestation(ServerWorld pLevel, BlockPos pPos) {
      SilverfishEntity silverfishentity = EntityType.SILVERFISH.create(pLevel);
      silverfishentity.moveTo((double)pPos.getX() + 0.5D, (double)pPos.getY(), (double)pPos.getZ() + 0.5D, 0.0F, 0.0F);
      pLevel.addFreshEntity(silverfishentity);
      silverfishentity.spawnAnim();
   }

   /**
    * Perform side-effects from block dropping, such as creating silverfish
    */
   public void spawnAfterBreak(BlockState pState, ServerWorld pLevel, BlockPos pPos, ItemStack pStack) {
      super.spawnAfterBreak(pState, pLevel, pPos, pStack);
      if (pLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, pStack) == 0) {
         this.spawnInfestation(pLevel, pPos);
      }

   }

   /**
    * Called when this Block is destroyed by an Explosion
    */
   public void wasExploded(World pLevel, BlockPos pPos, Explosion pExplosion) {
      if (pLevel instanceof ServerWorld) {
         this.spawnInfestation((ServerWorld)pLevel, pPos);
      }

   }

   public static BlockState stateByHostBlock(Block p_196467_0_) {
      return BLOCK_BY_HOST_BLOCK.get(p_196467_0_).defaultBlockState();
   }
}