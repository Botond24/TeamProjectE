package net.minecraft.world.storage;

import java.io.File;
import java.io.IOException;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.SharedConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class WorldSavedData implements net.minecraftforge.common.util.INBTSerializable<CompoundNBT> {
   private static final Logger LOGGER = LogManager.getLogger();
   private final String id;
   private boolean dirty;

   public WorldSavedData(String p_i2141_1_) {
      this.id = p_i2141_1_;
   }

   public abstract void load(CompoundNBT p_76184_1_);

   /**
    * Used to save the {@code SavedData} to a {@code CompoundTag}
    * @param pCompound the {@code CompoundTag} to save the {@code SavedData} to
    */
   public abstract CompoundNBT save(CompoundNBT pCompound);

   /**
    * Marks this {@code SavedData} dirty, to be saved to disk when the level next saves.
    */
   public void setDirty() {
      this.setDirty(true);
   }

   /**
    * Sets the dirty state of this {@code SavedData}, whether it needs saving to disk.
    */
   public void setDirty(boolean pDirty) {
      this.dirty = pDirty;
   }

   /**
    * Whether this {@code SavedData} needs saving to disk.
    */
   public boolean isDirty() {
      return this.dirty;
   }

   public String getId() {
      return this.id;
   }

   /**
    * Saves the {@code SavedData} to disc
    * @param pFile the passed {@code java.io.File} to write the {@code SavedData} to
    */
   public void save(File pFile) {
      if (this.isDirty()) {
         CompoundNBT compoundnbt = new CompoundNBT();
         compoundnbt.put("data", this.save(new CompoundNBT()));
         compoundnbt.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());

         try {
            CompressedStreamTools.writeCompressed(compoundnbt, pFile);
         } catch (IOException ioexception) {
            LOGGER.error("Could not save data {}", this, ioexception);
         }

         this.setDirty(false);
      }
   }

   @Override
   public void deserializeNBT(CompoundNBT nbt) {
      load(nbt);
   }

   @Override
   public CompoundNBT serializeNBT() {
      return save(new CompoundNBT());
   }
}
