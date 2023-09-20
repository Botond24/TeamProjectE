package net.minecraft.nbt;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CompoundNBT implements INBT {
   public static final Codec<CompoundNBT> CODEC = Codec.PASSTHROUGH.comapFlatMap((p_240598_0_) -> {
      INBT inbt = p_240598_0_.convert(NBTDynamicOps.INSTANCE).getValue();
      return inbt instanceof CompoundNBT ? DataResult.success((CompoundNBT)inbt) : DataResult.error("Not a compound tag: " + inbt);
   }, (p_240599_0_) -> {
      return new Dynamic<>(NBTDynamicOps.INSTANCE, p_240599_0_);
   });
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
   public static final INBTType<CompoundNBT> TYPE = new INBTType<CompoundNBT>() {
      public CompoundNBT load(DataInput pInput, int pDepth, NBTSizeTracker pAccounter) throws IOException {
         pAccounter.accountBits(384L);
         if (pDepth > 512) {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
         } else {
            Map<String, INBT> map = Maps.newHashMap();

            byte b0;
            while((b0 = CompoundNBT.readNamedTagType(pInput, pAccounter)) != 0) {
               String s = CompoundNBT.readNamedTagName(pInput, pAccounter);
               pAccounter.accountBits((long)(224 + 16 * s.length()));
               pAccounter.accountBits(32); //Forge: 4 extra bytes for the object allocation.
               INBT inbt = CompoundNBT.readNamedTagData(NBTTypes.getType(b0), s, pInput, pDepth + 1, pAccounter);
               if (map.put(s, inbt) != null) {
                  pAccounter.accountBits(288L);
               }
            }

            return new CompoundNBT(map);
         }
      }

      public String getName() {
         return "COMPOUND";
      }

      public String getPrettyName() {
         return "TAG_Compound";
      }
   };
   private final Map<String, INBT> tags;

   protected CompoundNBT(Map<String, INBT> pTags) {
      this.tags = pTags;
   }

   public CompoundNBT() {
      this(Maps.newHashMap());
   }

   /**
    * Write the actual data contents of the tag, implemented in NBT extension classes
    */
   public void write(DataOutput pOutput) throws IOException {
      for(String s : this.tags.keySet()) {
         INBT inbt = this.tags.get(s);
         writeNamedTag(s, inbt, pOutput);
      }

      pOutput.writeByte(0);
   }

   /**
    * Gets a set with the names of the keys in the tag compound.
    */
   public Set<String> getAllKeys() {
      return this.tags.keySet();
   }

   /**
    * Gets the type byte for the tag.
    */
   public byte getId() {
      return 10;
   }

   public INBTType<CompoundNBT> getType() {
      return TYPE;
   }

   public int size() {
      return this.tags.size();
   }

   @Nullable
   public INBT put(String pKey, INBT pValue) {
      if (pValue == null) throw new IllegalArgumentException("Invalid null NBT value with key " + pKey);
      return this.tags.put(pKey, pValue);
   }

   /**
    * Stores a new NBTTagByte with the given byte value into the map with the given string key.
    */
   public void putByte(String pKey, byte pValue) {
      this.tags.put(pKey, ByteNBT.valueOf(pValue));
   }

   /**
    * Stores a new NBTTagShort with the given short value into the map with the given string key.
    */
   public void putShort(String pKey, short pValue) {
      this.tags.put(pKey, ShortNBT.valueOf(pValue));
   }

   /**
    * Stores a new NBTTagInt with the given integer value into the map with the given string key.
    */
   public void putInt(String pKey, int pValue) {
      this.tags.put(pKey, IntNBT.valueOf(pValue));
   }

   /**
    * Stores a new NBTTagLong with the given long value into the map with the given string key.
    */
   public void putLong(String pKey, long pValue) {
      this.tags.put(pKey, LongNBT.valueOf(pValue));
   }

   public void putUUID(String pKey, UUID pValue) {
      this.tags.put(pKey, NBTUtil.createUUID(pValue));
   }

   public UUID getUUID(String pKey) {
      return NBTUtil.loadUUID(this.get(pKey));
   }

   public boolean hasUUID(String pKey) {
      INBT inbt = this.get(pKey);
      return inbt != null && inbt.getType() == IntArrayNBT.TYPE && ((IntArrayNBT)inbt).getAsIntArray().length == 4;
   }

   /**
    * Stores a new NBTTagFloat with the given float value into the map with the given string key.
    */
   public void putFloat(String pKey, float pValue) {
      this.tags.put(pKey, FloatNBT.valueOf(pValue));
   }

   /**
    * Stores a new NBTTagDouble with the given double value into the map with the given string key.
    */
   public void putDouble(String pKey, double pValue) {
      this.tags.put(pKey, DoubleNBT.valueOf(pValue));
   }

   /**
    * Stores a new NBTTagString with the given string value into the map with the given string key.
    */
   public void putString(String pKey, String pValue) {
      this.tags.put(pKey, StringNBT.valueOf(pValue));
   }

   /**
    * Stores a new NBTTagByteArray with the given array as data into the map with the given string key.
    */
   public void putByteArray(String pKey, byte[] pValue) {
      this.tags.put(pKey, new ByteArrayNBT(pValue));
   }

   /**
    * Stores a new NBTTagIntArray with the given array as data into the map with the given string key.
    */
   public void putIntArray(String pKey, int[] pValue) {
      this.tags.put(pKey, new IntArrayNBT(pValue));
   }

   public void putIntArray(String pKey, List<Integer> pValue) {
      this.tags.put(pKey, new IntArrayNBT(pValue));
   }

   public void putLongArray(String pKey, long[] pValue) {
      this.tags.put(pKey, new LongArrayNBT(pValue));
   }

   public void putLongArray(String pKey, List<Long> pValue) {
      this.tags.put(pKey, new LongArrayNBT(pValue));
   }

   /**
    * Stores the given boolean value as a NBTTagByte, storing 1 for true and 0 for false, using the given string key.
    */
   public void putBoolean(String pKey, boolean pValue) {
      this.tags.put(pKey, ByteNBT.valueOf(pValue));
   }

   /**
    * gets a generic tag with the specified name
    */
   @Nullable
   public INBT get(String pKey) {
      return this.tags.get(pKey);
   }

   /**
    * Gets the byte identifier of the tag of the specified {@code key}, or {@code 0} if no tag exists for the {@code
    * key}.
    */
   public byte getTagType(String pKey) {
      INBT inbt = this.tags.get(pKey);
      return inbt == null ? 0 : inbt.getId();
   }

   /**
    * Returns whether the given string has been previously stored as a key in the map.
    */
   public boolean contains(String pKey) {
      return this.tags.containsKey(pKey);
   }

   /**
    * Returns whether the tag of the specified {@code key} is a particular {@code tagType}. If the {@code tagType} is
    * {@code 99}, all numeric tags will be checked against the type of the stored tag.
    */
   public boolean contains(String pKey, int pTagType) {
      int i = this.getTagType(pKey);
      if (i == pTagType) {
         return true;
      } else if (pTagType != 99) {
         return false;
      } else {
         return i == 1 || i == 2 || i == 3 || i == 4 || i == 5 || i == 6;
      }
   }

   /**
    * Retrieves a byte value using the specified key, or 0 if no such key was stored.
    */
   public byte getByte(String pKey) {
      try {
         if (this.contains(pKey, 99)) {
            return ((NumberNBT)this.tags.get(pKey)).getAsByte();
         }
      } catch (ClassCastException classcastexception) {
      }

      return 0;
   }

   /**
    * Retrieves a short value using the specified key, or 0 if no such key was stored.
    */
   public short getShort(String pKey) {
      try {
         if (this.contains(pKey, 99)) {
            return ((NumberNBT)this.tags.get(pKey)).getAsShort();
         }
      } catch (ClassCastException classcastexception) {
      }

      return 0;
   }

   /**
    * Retrieves an integer value using the specified key, or 0 if no such key was stored.
    */
   public int getInt(String pKey) {
      try {
         if (this.contains(pKey, 99)) {
            return ((NumberNBT)this.tags.get(pKey)).getAsInt();
         }
      } catch (ClassCastException classcastexception) {
      }

      return 0;
   }

   /**
    * Retrieves a long value using the specified key, or 0 if no such key was stored.
    */
   public long getLong(String pKey) {
      try {
         if (this.contains(pKey, 99)) {
            return ((NumberNBT)this.tags.get(pKey)).getAsLong();
         }
      } catch (ClassCastException classcastexception) {
      }

      return 0L;
   }

   /**
    * Retrieves a float value using the specified key, or 0 if no such key was stored.
    */
   public float getFloat(String pKey) {
      try {
         if (this.contains(pKey, 99)) {
            return ((NumberNBT)this.tags.get(pKey)).getAsFloat();
         }
      } catch (ClassCastException classcastexception) {
      }

      return 0.0F;
   }

   /**
    * Retrieves a double value using the specified key, or 0 if no such key was stored.
    */
   public double getDouble(String pKey) {
      try {
         if (this.contains(pKey, 99)) {
            return ((NumberNBT)this.tags.get(pKey)).getAsDouble();
         }
      } catch (ClassCastException classcastexception) {
      }

      return 0.0D;
   }

   /**
    * Retrieves a string value using the specified key, or an empty string if no such key was stored.
    */
   public String getString(String pKey) {
      try {
         if (this.contains(pKey, 8)) {
            return this.tags.get(pKey).getAsString();
         }
      } catch (ClassCastException classcastexception) {
      }

      return "";
   }

   /**
    * Retrieves a byte array using the specified key, or a zero-length array if no such key was stored.
    */
   public byte[] getByteArray(String pKey) {
      try {
         if (this.contains(pKey, 7)) {
            return ((ByteArrayNBT)this.tags.get(pKey)).getAsByteArray();
         }
      } catch (ClassCastException classcastexception) {
         throw new ReportedException(this.createReport(pKey, ByteArrayNBT.TYPE, classcastexception));
      }

      return new byte[0];
   }

   /**
    * Retrieves an int array using the specified key, or a zero-length array if no such key was stored.
    */
   public int[] getIntArray(String pKey) {
      try {
         if (this.contains(pKey, 11)) {
            return ((IntArrayNBT)this.tags.get(pKey)).getAsIntArray();
         }
      } catch (ClassCastException classcastexception) {
         throw new ReportedException(this.createReport(pKey, IntArrayNBT.TYPE, classcastexception));
      }

      return new int[0];
   }

   public long[] getLongArray(String pKey) {
      try {
         if (this.contains(pKey, 12)) {
            return ((LongArrayNBT)this.tags.get(pKey)).getAsLongArray();
         }
      } catch (ClassCastException classcastexception) {
         throw new ReportedException(this.createReport(pKey, LongArrayNBT.TYPE, classcastexception));
      }

      return new long[0];
   }

   /**
    * Retrieves a NBTTagCompound subtag matching the specified key, or a new empty NBTTagCompound if no such key was
    * stored.
    */
   public CompoundNBT getCompound(String pKey) {
      try {
         if (this.contains(pKey, 10)) {
            return (CompoundNBT)this.tags.get(pKey);
         }
      } catch (ClassCastException classcastexception) {
         throw new ReportedException(this.createReport(pKey, TYPE, classcastexception));
      }

      return new CompoundNBT();
   }

   /**
    * Gets the NBTTagList object with the given name.
    */
   public ListNBT getList(String pKey, int pTagType) {
      try {
         if (this.getTagType(pKey) == 9) {
            ListNBT listnbt = (ListNBT)this.tags.get(pKey);
            if (!listnbt.isEmpty() && listnbt.getElementType() != pTagType) {
               return new ListNBT();
            }

            return listnbt;
         }
      } catch (ClassCastException classcastexception) {
         throw new ReportedException(this.createReport(pKey, ListNBT.TYPE, classcastexception));
      }

      return new ListNBT();
   }

   /**
    * Retrieves a boolean value using the specified key, or false if no such key was stored. This uses the getByte
    * method.
    */
   public boolean getBoolean(String pKey) {
      return this.getByte(pKey) != 0;
   }

   /**
    * Remove the specified tag.
    */
   public void remove(String pKey) {
      this.tags.remove(pKey);
   }

   public String toString() {
      StringBuilder stringbuilder = new StringBuilder("{");
      Collection<String> collection = this.tags.keySet();
      if (LOGGER.isDebugEnabled()) {
         List<String> list = Lists.newArrayList(this.tags.keySet());
         Collections.sort(list);
         collection = list;
      }

      for(String s : collection) {
         if (stringbuilder.length() != 1) {
            stringbuilder.append(',');
         }

         stringbuilder.append(handleEscape(s)).append(':').append(this.tags.get(s));
      }

      return stringbuilder.append('}').toString();
   }

   public boolean isEmpty() {
      return this.tags.isEmpty();
   }

   private CrashReport createReport(String pTagName, INBTType<?> pType, ClassCastException pException) {
      CrashReport crashreport = CrashReport.forThrowable(pException, "Reading NBT data");
      CrashReportCategory crashreportcategory = crashreport.addCategory("Corrupt NBT tag", 1);
      crashreportcategory.setDetail("Tag type found", () -> {
         return this.tags.get(pTagName).getType().getName();
      });
      crashreportcategory.setDetail("Tag type expected", pType::getName);
      crashreportcategory.setDetail("Tag name", pTagName);
      return crashreport;
   }

   /**
    * Creates a clone of the tag.
    */
   public CompoundNBT copy() {
      Map<String, INBT> map = Maps.newHashMap(Maps.transformValues(this.tags, INBT::copy));
      return new CompoundNBT(map);
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else {
         return p_equals_1_ instanceof CompoundNBT && Objects.equals(this.tags, ((CompoundNBT)p_equals_1_).tags);
      }
   }

   public int hashCode() {
      return this.tags.hashCode();
   }

   private static void writeNamedTag(String pName, INBT pTag, DataOutput pOutput) throws IOException {
      pOutput.writeByte(pTag.getId());
      if (pTag.getId() != 0) {
         pOutput.writeUTF(pName);
         pTag.write(pOutput);
      }
   }

   private static byte readNamedTagType(DataInput pInput, NBTSizeTracker pAccounter) throws IOException {
      pAccounter.accountBits(8);
      return pInput.readByte();
   }

   private static String readNamedTagName(DataInput pInput, NBTSizeTracker pAccounter) throws IOException {
      return pAccounter.readUTF(pInput.readUTF());
   }

   private static INBT readNamedTagData(INBTType<?> pType, String pName, DataInput pInput, int pDepth, NBTSizeTracker pAccounter) {
      try {
         return pType.load(pInput, pDepth, pAccounter);
      } catch (IOException ioexception) {
         CrashReport crashreport = CrashReport.forThrowable(ioexception, "Loading NBT data");
         CrashReportCategory crashreportcategory = crashreport.addCategory("NBT Tag");
         crashreportcategory.setDetail("Tag name", pName);
         crashreportcategory.setDetail("Tag type", pType.getName());
         throw new ReportedException(crashreport);
      }
   }

   /**
    * Copies all the tags of {@code other} into this tag, then returns itself.
    * @see #copy()
    */
   public CompoundNBT merge(CompoundNBT pOther) {
      for(String s : pOther.tags.keySet()) {
         INBT inbt = pOther.tags.get(s);
         if (inbt.getId() == 10) {
            if (this.contains(s, 10)) {
               CompoundNBT compoundnbt = this.getCompound(s);
               compoundnbt.merge((CompoundNBT)inbt);
            } else {
               this.put(s, inbt.copy());
            }
         } else {
            this.put(s, inbt.copy());
         }
      }

      return this;
   }

   protected static String handleEscape(String p_193582_0_) {
      return SIMPLE_VALUE.matcher(p_193582_0_).matches() ? p_193582_0_ : StringNBT.quoteAndEscape(p_193582_0_);
   }

   protected static ITextComponent handleEscapePretty(String p_197642_0_) {
      if (SIMPLE_VALUE.matcher(p_197642_0_).matches()) {
         return (new StringTextComponent(p_197642_0_)).withStyle(SYNTAX_HIGHLIGHTING_KEY);
      } else {
         String s = StringNBT.quoteAndEscape(p_197642_0_);
         String s1 = s.substring(0, 1);
         ITextComponent itextcomponent = (new StringTextComponent(s.substring(1, s.length() - 1))).withStyle(SYNTAX_HIGHLIGHTING_KEY);
         return (new StringTextComponent(s1)).append(itextcomponent).append(s1);
      }
   }

   public ITextComponent getPrettyDisplay(String p_199850_1_, int p_199850_2_) {
      if (this.tags.isEmpty()) {
         return new StringTextComponent("{}");
      } else {
         IFormattableTextComponent iformattabletextcomponent = new StringTextComponent("{");
         Collection<String> collection = this.tags.keySet();
         if (LOGGER.isDebugEnabled()) {
            List<String> list = Lists.newArrayList(this.tags.keySet());
            Collections.sort(list);
            collection = list;
         }

         if (!p_199850_1_.isEmpty()) {
            iformattabletextcomponent.append("\n");
         }

         IFormattableTextComponent iformattabletextcomponent1;
         for(Iterator<String> iterator = collection.iterator(); iterator.hasNext(); iformattabletextcomponent.append(iformattabletextcomponent1)) {
            String s = iterator.next();
            iformattabletextcomponent1 = (new StringTextComponent(Strings.repeat(p_199850_1_, p_199850_2_ + 1))).append(handleEscapePretty(s)).append(String.valueOf(':')).append(" ").append(this.tags.get(s).getPrettyDisplay(p_199850_1_, p_199850_2_ + 1));
            if (iterator.hasNext()) {
               iformattabletextcomponent1.append(String.valueOf(',')).append(p_199850_1_.isEmpty() ? " " : "\n");
            }
         }

         if (!p_199850_1_.isEmpty()) {
            iformattabletextcomponent.append("\n").append(Strings.repeat(p_199850_1_, p_199850_2_));
         }

         iformattabletextcomponent.append("}");
         return iformattabletextcomponent;
      }
   }

   protected Map<String, INBT> entries() {
      return Collections.unmodifiableMap(this.tags);
   }
}
