package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.StateHolder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.StringUtils;
import net.minecraft.util.UUIDCodec;
import net.minecraft.util.datafix.DefaultTypeReferences;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class NBTUtil {
   private static final Logger LOGGER = LogManager.getLogger();

   /**
    * Reads and returns a GameProfile that has been saved to the passed in NBTTagCompound
    */
   @Nullable
   public static GameProfile readGameProfile(CompoundNBT pTag) {
      String s = null;
      UUID uuid = null;
      if (pTag.contains("Name", 8)) {
         s = pTag.getString("Name");
      }

      if (pTag.hasUUID("Id")) {
         uuid = pTag.getUUID("Id");
      }

      try {
         GameProfile gameprofile = new GameProfile(uuid, s);
         if (pTag.contains("Properties", 10)) {
            CompoundNBT compoundnbt = pTag.getCompound("Properties");

            for(String s1 : compoundnbt.getAllKeys()) {
               ListNBT listnbt = compoundnbt.getList(s1, 10);

               for(int i = 0; i < listnbt.size(); ++i) {
                  CompoundNBT compoundnbt1 = listnbt.getCompound(i);
                  String s2 = compoundnbt1.getString("Value");
                  if (compoundnbt1.contains("Signature", 8)) {
                     gameprofile.getProperties().put(s1, new com.mojang.authlib.properties.Property(s1, s2, compoundnbt1.getString("Signature")));
                  } else {
                     gameprofile.getProperties().put(s1, new com.mojang.authlib.properties.Property(s1, s2));
                  }
               }
            }
         }

         return gameprofile;
      } catch (Throwable throwable) {
         return null;
      }
   }

   /**
    * Writes a {@code profile} to the given {@code tag}.
    */
   public static CompoundNBT writeGameProfile(CompoundNBT pTag, GameProfile pProfile) {
      if (!StringUtils.isNullOrEmpty(pProfile.getName())) {
         pTag.putString("Name", pProfile.getName());
      }

      if (pProfile.getId() != null) {
         pTag.putUUID("Id", pProfile.getId());
      }

      if (!pProfile.getProperties().isEmpty()) {
         CompoundNBT compoundnbt = new CompoundNBT();

         for(String s : pProfile.getProperties().keySet()) {
            ListNBT listnbt = new ListNBT();

            for(com.mojang.authlib.properties.Property property : pProfile.getProperties().get(s)) {
               CompoundNBT compoundnbt1 = new CompoundNBT();
               compoundnbt1.putString("Value", property.getValue());
               if (property.hasSignature()) {
                  compoundnbt1.putString("Signature", property.getSignature());
               }

               listnbt.add(compoundnbt1);
            }

            compoundnbt.put(s, listnbt);
         }

         pTag.put("Properties", compoundnbt);
      }

      return pTag;
   }

   @VisibleForTesting
   public static boolean compareNbt(@Nullable INBT pTag, @Nullable INBT pOther, boolean pCompareListTag) {
      if (pTag == pOther) {
         return true;
      } else if (pTag == null) {
         return true;
      } else if (pOther == null) {
         return false;
      } else if (!pTag.getClass().equals(pOther.getClass())) {
         return false;
      } else if (pTag instanceof CompoundNBT) {
         CompoundNBT compoundnbt = (CompoundNBT)pTag;
         CompoundNBT compoundnbt1 = (CompoundNBT)pOther;

         for(String s : compoundnbt.getAllKeys()) {
            INBT inbt1 = compoundnbt.get(s);
            if (!compareNbt(inbt1, compoundnbt1.get(s), pCompareListTag)) {
               return false;
            }
         }

         return true;
      } else if (pTag instanceof ListNBT && pCompareListTag) {
         ListNBT listnbt = (ListNBT)pTag;
         ListNBT listnbt1 = (ListNBT)pOther;
         if (listnbt.isEmpty()) {
            return listnbt1.isEmpty();
         } else {
            for(int i = 0; i < listnbt.size(); ++i) {
               INBT inbt = listnbt.get(i);
               boolean flag = false;

               for(int j = 0; j < listnbt1.size(); ++j) {
                  if (compareNbt(inbt, listnbt1.get(j), pCompareListTag)) {
                     flag = true;
                     break;
                  }
               }

               if (!flag) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return pTag.equals(pOther);
      }
   }

   public static IntArrayNBT createUUID(UUID pUuid) {
      return new IntArrayNBT(UUIDCodec.uuidToIntArray(pUuid));
   }

   /**
    * Reads a UUID from the passed NBTTagCompound.
    */
   public static UUID loadUUID(INBT pTag) {
      if (pTag.getType() != IntArrayNBT.TYPE) {
         throw new IllegalArgumentException("Expected UUID-Tag to be of type " + IntArrayNBT.TYPE.getName() + ", but found " + pTag.getType().getName() + ".");
      } else {
         int[] aint = ((IntArrayNBT)pTag).getAsIntArray();
         if (aint.length != 4) {
            throw new IllegalArgumentException("Expected UUID-Array to be of length 4, but found " + aint.length + ".");
         } else {
            return UUIDCodec.uuidFromIntArray(aint);
         }
      }
   }

   /**
    * Creates a BlockPos object from the data stored in the passed NBTTagCompound.
    */
   public static BlockPos readBlockPos(CompoundNBT pTag) {
      return new BlockPos(pTag.getInt("X"), pTag.getInt("Y"), pTag.getInt("Z"));
   }

   /**
    * Creates a new NBTTagCompound from a BlockPos.
    */
   public static CompoundNBT writeBlockPos(BlockPos pPos) {
      CompoundNBT compoundnbt = new CompoundNBT();
      compoundnbt.putInt("X", pPos.getX());
      compoundnbt.putInt("Y", pPos.getY());
      compoundnbt.putInt("Z", pPos.getZ());
      return compoundnbt;
   }

   /**
    * Reads a blockstate from the given tag.
    */
   public static BlockState readBlockState(CompoundNBT pTag) {
      if (!pTag.contains("Name", 8)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         Block block = Registry.BLOCK.get(new ResourceLocation(pTag.getString("Name")));
         BlockState blockstate = block.defaultBlockState();
         if (pTag.contains("Properties", 10)) {
            CompoundNBT compoundnbt = pTag.getCompound("Properties");
            StateContainer<Block, BlockState> statecontainer = block.getStateDefinition();

            for(String s : compoundnbt.getAllKeys()) {
               Property<?> property = statecontainer.getProperty(s);
               if (property != null) {
                  blockstate = setValueHelper(blockstate, property, s, compoundnbt, pTag);
               }
            }
         }

         return blockstate;
      }
   }

   private static <S extends StateHolder<?, S>, T extends Comparable<T>> S setValueHelper(S pStateHolder, Property<T> pProperty, String pPropertyName, CompoundNBT pPropertiesTag, CompoundNBT pBlockStateTag) {
      Optional<T> optional = pProperty.getValue(pPropertiesTag.getString(pPropertyName));
      if (optional.isPresent()) {
         return pStateHolder.setValue(pProperty, optional.get());
      } else {
         LOGGER.warn("Unable to read property: {} with value: {} for blockstate: {}", pPropertyName, pPropertiesTag.getString(pPropertyName), pBlockStateTag.toString());
         return pStateHolder;
      }
   }

   /**
    * Writes the given blockstate to the given tag.
    */
   public static CompoundNBT writeBlockState(BlockState pState) {
      CompoundNBT compoundnbt = new CompoundNBT();
      compoundnbt.putString("Name", Registry.BLOCK.getKey(pState.getBlock()).toString());
      ImmutableMap<Property<?>, Comparable<?>> immutablemap = pState.getValues();
      if (!immutablemap.isEmpty()) {
         CompoundNBT compoundnbt1 = new CompoundNBT();

         for(Entry<Property<?>, Comparable<?>> entry : immutablemap.entrySet()) {
            Property<?> property = entry.getKey();
            compoundnbt1.putString(property.getName(), getName(property, entry.getValue()));
         }

         compoundnbt.put("Properties", compoundnbt1);
      }

      return compoundnbt;
   }

   private static <T extends Comparable<T>> String getName(Property<T> pProperty, Comparable<?> pValue) {
      return pProperty.getName((T)pValue);
   }

   public static CompoundNBT update(DataFixer pDataFixer, DefaultTypeReferences pType, CompoundNBT pCompoundTag, int pVersion) {
      return update(pDataFixer, pType, pCompoundTag, pVersion, SharedConstants.getCurrentVersion().getWorldVersion());
   }

   public static CompoundNBT update(DataFixer pDataFixer, DefaultTypeReferences pType, CompoundNBT pCompoundTag, int pVersion, int pNewVersion) {
      return (CompoundNBT)pDataFixer.update(pType.getType(), new Dynamic<>(NBTDynamicOps.INSTANCE, pCompoundTag), pVersion, pNewVersion).getValue();
   }
}