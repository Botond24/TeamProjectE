package net.minecraft.network.datasync;

import net.minecraft.network.PacketBuffer;

/**
 * Handles encoding and decoding of data for {@link SynchedEntityData}.
 * Note that mods cannot add new serializers, because this is not a managed registry and the serializer ID is limited to
 * 16.
 */
public interface IDataSerializer<T> {
   void write(PacketBuffer pBuffer, T pValue);

   T read(PacketBuffer pBuffer);

   default DataParameter<T> createAccessor(int pId) {
      return new DataParameter<>(pId, this);
   }

   T copy(T pValue);
}