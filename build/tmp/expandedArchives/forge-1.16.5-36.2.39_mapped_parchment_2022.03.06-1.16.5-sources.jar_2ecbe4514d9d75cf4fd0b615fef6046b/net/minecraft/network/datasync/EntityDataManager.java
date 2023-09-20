package net.minecraft.network.datasync;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nullable;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Keeps data in sync from server to client for an entity.
 * A maximum of 254 parameters per entity class can be registered. The system then ensures that these values are updated
 * on the client whenever they change on the server.
 * 
 * Use {@link #defineId} to register a piece of data for your entity class.
 * Use {@link #define} during {@link Entity#defineSynchedData} to set the default value for a given parameter.
 */
public class EntityDataManager {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Map<Class<? extends Entity>, Integer> ENTITY_ID_POOL = Maps.newHashMap();
   private final Entity entity;
   private final Map<Integer, EntityDataManager.DataEntry<?>> itemsById = Maps.newHashMap();
   private final ReadWriteLock lock = new ReentrantReadWriteLock();
   private boolean isEmpty = true;
   private boolean isDirty;

   public EntityDataManager(Entity p_i46840_1_) {
      this.entity = p_i46840_1_;
   }

   /**
    * Register a piece of data to be kept in sync for an entity class.
    * This method must be called during a static initializer of an entity class and the first parameter of this method
    * must be that entity class.
    */
   public static <T> DataParameter<T> defineId(Class<? extends Entity> pClazz, IDataSerializer<T> pSerializer) {
      if (true || LOGGER.isDebugEnabled()) { // Forge: This is very useful for mods that register keys on classes that are not their own
         try {
            Class<?> oclass = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());
            if (!oclass.equals(pClazz)) {
               // Forge: log at warn, mods should not add to classes that they don't own, and only add stacktrace when in debug is enabled as it is mostly not needed and consumes time
               if (LOGGER.isDebugEnabled()) LOGGER.warn("defineId called for: {} from {}", pClazz, oclass, new RuntimeException());
               else LOGGER.warn("defineId called for: {} from {}", pClazz, oclass);
            }
         } catch (ClassNotFoundException classnotfoundexception) {
         }
      }

      int j;
      if (ENTITY_ID_POOL.containsKey(pClazz)) {
         j = ENTITY_ID_POOL.get(pClazz) + 1;
      } else {
         int i = 0;
         Class<?> oclass1 = pClazz;

         while(oclass1 != Entity.class) {
            oclass1 = oclass1.getSuperclass();
            if (ENTITY_ID_POOL.containsKey(oclass1)) {
               i = ENTITY_ID_POOL.get(oclass1) + 1;
               break;
            }
         }

         j = i;
      }

      if (j > 254) {
         throw new IllegalArgumentException("Data value id is too big with " + j + "! (Max is " + 254 + ")");
      } else {
         ENTITY_ID_POOL.put(pClazz, j);
         return pSerializer.createAccessor(j);
      }
   }

   /**
    * Set the default value for a data parameter. Call this during {@link Entity#defineSynchedData}.
    */
   public <T> void define(DataParameter<T> pKey, T pValue) {
      int i = pKey.getId();
      if (i > 254) {
         throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is " + 254 + ")");
      } else if (this.itemsById.containsKey(i)) {
         throw new IllegalArgumentException("Duplicate id value for " + i + "!");
      } else if (DataSerializers.getSerializedId(pKey.getSerializer()) < 0) {
         throw new IllegalArgumentException("Unregistered serializer " + pKey.getSerializer() + " for " + i + "!");
      } else {
         this.createDataItem(pKey, pValue);
      }
   }

   private <T> void createDataItem(DataParameter<T> pKey, T pValue) {
      EntityDataManager.DataEntry<T> dataentry = new EntityDataManager.DataEntry<>(pKey, pValue);
      this.lock.writeLock().lock();
      this.itemsById.put(pKey.getId(), dataentry);
      this.isEmpty = false;
      this.lock.writeLock().unlock();
   }

   private <T> EntityDataManager.DataEntry<T> getItem(DataParameter<T> pKey) {
      this.lock.readLock().lock();

      EntityDataManager.DataEntry<T> dataentry;
      try {
         dataentry = (DataEntry<T>) this.itemsById.get(pKey.getId());
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Getting synched entity data");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Synched entity data");
         crashreportcategory.setDetail("Data ID", pKey);
         throw new ReportedException(crashreport);
      } finally {
         this.lock.readLock().unlock();
      }

      return dataentry;
   }

   /**
    * Get the value of the given key for this entity.
    */
   public <T> T get(DataParameter<T> pKey) {
      return this.getItem(pKey).getValue();
   }

   /**
    * Set the value of the given key for this entity.
    */
   public <T> void set(DataParameter<T> pKey, T pValue) {
      EntityDataManager.DataEntry<T> dataentry = this.getItem(pKey);
      if (ObjectUtils.notEqual(pValue, dataentry.getValue())) {
         dataentry.setValue(pValue);
         this.entity.onSyncedDataUpdated(pKey);
         dataentry.setDirty(true);
         this.isDirty = true;
      }

   }

   /**
    * Whether any keys have changed since the last synchronization packet to the client.
    */
   public boolean isDirty() {
      return this.isDirty;
   }

   /**
    * Encode the given data entries into the buffer.
    */
   public static void pack(List<EntityDataManager.DataEntry<?>> pEntries, PacketBuffer pBuffer) throws IOException {
      if (pEntries != null) {
         int i = 0;

         for(int j = pEntries.size(); i < j; ++i) {
            writeDataItem(pBuffer, pEntries.get(i));
         }
      }

      pBuffer.writeByte(255);
   }

   /**
    * Gets all data entries which have changed since the last check and clears their dirty flag.
    */
   @Nullable
   public List<EntityDataManager.DataEntry<?>> packDirty() {
      List<EntityDataManager.DataEntry<?>> list = null;
      if (this.isDirty) {
         this.lock.readLock().lock();

         for(EntityDataManager.DataEntry<?> dataentry : this.itemsById.values()) {
            if (dataentry.isDirty()) {
               dataentry.setDirty(false);
               if (list == null) {
                  list = Lists.newArrayList();
               }

               list.add(dataentry.copy());
            }
         }

         this.lock.readLock().unlock();
      }

      this.isDirty = false;
      return list;
   }

   /**
    * Get all values from the data entries.
    */
   @Nullable
   public List<EntityDataManager.DataEntry<?>> getAll() {
      List<EntityDataManager.DataEntry<?>> list = null;
      this.lock.readLock().lock();

      for(EntityDataManager.DataEntry<?> dataentry : this.itemsById.values()) {
         if (list == null) {
            list = Lists.newArrayList();
         }

         list.add(dataentry.copy());
      }

      this.lock.readLock().unlock();
      return list;
   }

   private static <T> void writeDataItem(PacketBuffer pBuffer, EntityDataManager.DataEntry<T> pEntry) throws IOException {
      DataParameter<T> dataparameter = pEntry.getAccessor();
      int i = DataSerializers.getSerializedId(dataparameter.getSerializer());
      if (i < 0) {
         throw new EncoderException("Unknown serializer type " + dataparameter.getSerializer());
      } else {
         pBuffer.writeByte(dataparameter.getId());
         pBuffer.writeVarInt(i);
         dataparameter.getSerializer().write(pBuffer, pEntry.getValue());
      }
   }

   /**
    * Decode the data written by {@link #pack}.
    */
   @Nullable
   public static List<EntityDataManager.DataEntry<?>> unpack(PacketBuffer pBuffer) throws IOException {
      List<EntityDataManager.DataEntry<?>> list = null;

      int i;
      while((i = pBuffer.readUnsignedByte()) != 255) {
         if (list == null) {
            list = Lists.newArrayList();
         }

         int j = pBuffer.readVarInt();
         IDataSerializer<?> idataserializer = DataSerializers.getSerializer(j);
         if (idataserializer == null) {
            throw new DecoderException("Unknown serializer type " + j);
         }

         list.add(genericHelper(pBuffer, i, idataserializer));
      }

      return list;
   }

   private static <T> EntityDataManager.DataEntry<T> genericHelper(PacketBuffer pBuffer, int pId, IDataSerializer<T> pSerializer) {
      return new EntityDataManager.DataEntry<>(pSerializer.createAccessor(pId), pSerializer.read(pBuffer));
   }

   /**
    * Updates the data using the given entries. Used on the client when the update packet is received.
    */
   @OnlyIn(Dist.CLIENT)
   public void assignValues(List<EntityDataManager.DataEntry<?>> pEntries) {
      this.lock.writeLock().lock();

      for(EntityDataManager.DataEntry<?> dataentry : pEntries) {
         EntityDataManager.DataEntry<?> dataentry1 = this.itemsById.get(dataentry.getAccessor().getId());
         if (dataentry1 != null) {
            this.assignValue(dataentry1, dataentry);
            this.entity.onSyncedDataUpdated(dataentry.getAccessor());
         }
      }

      this.lock.writeLock().unlock();
      this.isDirty = true;
   }

   @OnlyIn(Dist.CLIENT)
   private <T> void assignValue(EntityDataManager.DataEntry<T> pTarget, EntityDataManager.DataEntry<?> pSource) {
      if (!Objects.equals(pSource.accessor.getSerializer(), pTarget.accessor.getSerializer())) {
         throw new IllegalStateException(String.format("Invalid entity data item type for field %d on entity %s: old=%s(%s), new=%s(%s)", pTarget.accessor.getId(), this.entity, pTarget.value, pTarget.value.getClass(), pSource.value, pSource.value.getClass()));
      } else {
         pTarget.setValue((T)pSource.getValue());
      }
   }

   public boolean isEmpty() {
      return this.isEmpty;
   }

   /**
    * Clears the dirty flag, marking all entries as unchanged.
    */
   public void clearDirty() {
      this.isDirty = false;
      this.lock.readLock().lock();

      for(EntityDataManager.DataEntry<?> dataentry : this.itemsById.values()) {
         dataentry.setDirty(false);
      }

      this.lock.readLock().unlock();
   }

   public static class DataEntry<T> {
      private final DataParameter<T> accessor;
      private T value;
      private boolean dirty;

      public DataEntry(DataParameter<T> pAccessor, T pValue) {
         this.accessor = pAccessor;
         this.value = pValue;
         this.dirty = true;
      }

      public DataParameter<T> getAccessor() {
         return this.accessor;
      }

      public void setValue(T pValue) {
         this.value = pValue;
      }

      public T getValue() {
         return this.value;
      }

      public boolean isDirty() {
         return this.dirty;
      }

      public void setDirty(boolean pDirty) {
         this.dirty = pDirty;
      }

      public EntityDataManager.DataEntry<T> copy() {
         return new EntityDataManager.DataEntry<>(this.accessor, this.accessor.getSerializer().copy(this.value));
      }
   }
}
