package net.minecraft.command.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.command.CommandSource;
import net.minecraft.nbt.CollectionNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class NBTPathArgument implements ArgumentType<NBTPathArgument.NBTPath> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo.bar", "foo[0]", "[0]", "[]", "{foo=bar}");
   public static final SimpleCommandExceptionType ERROR_INVALID_NODE = new SimpleCommandExceptionType(new TranslationTextComponent("arguments.nbtpath.node.invalid"));
   public static final DynamicCommandExceptionType ERROR_NOTHING_FOUND = new DynamicCommandExceptionType((p_208665_0_) -> {
      return new TranslationTextComponent("arguments.nbtpath.nothing_found", p_208665_0_);
   });

   public static NBTPathArgument nbtPath() {
      return new NBTPathArgument();
   }

   public static NBTPathArgument.NBTPath getPath(CommandContext<CommandSource> pContext, String pName) {
      return pContext.getArgument(pName, NBTPathArgument.NBTPath.class);
   }

   public NBTPathArgument.NBTPath parse(StringReader p_parse_1_) throws CommandSyntaxException {
      List<NBTPathArgument.INode> list = Lists.newArrayList();
      int i = p_parse_1_.getCursor();
      Object2IntMap<NBTPathArgument.INode> object2intmap = new Object2IntOpenHashMap<>();
      boolean flag = true;

      while(p_parse_1_.canRead() && p_parse_1_.peek() != ' ') {
         NBTPathArgument.INode nbtpathargument$inode = parseNode(p_parse_1_, flag);
         list.add(nbtpathargument$inode);
         object2intmap.put(nbtpathargument$inode, p_parse_1_.getCursor() - i);
         flag = false;
         if (p_parse_1_.canRead()) {
            char c0 = p_parse_1_.peek();
            if (c0 != ' ' && c0 != '[' && c0 != '{') {
               p_parse_1_.expect('.');
            }
         }
      }

      return new NBTPathArgument.NBTPath(p_parse_1_.getString().substring(i, p_parse_1_.getCursor()), list.toArray(new NBTPathArgument.INode[0]), object2intmap);
   }

   private static NBTPathArgument.INode parseNode(StringReader pReader, boolean pFirst) throws CommandSyntaxException {
      switch(pReader.peek()) {
      case '"':
         String s = pReader.readString();
         return readObjectNode(pReader, s);
      case '[':
         pReader.skip();
         int j = pReader.peek();
         if (j == 123) {
            CompoundNBT compoundnbt1 = (new JsonToNBT(pReader)).readStruct();
            pReader.expect(']');
            return new NBTPathArgument.ListNode(compoundnbt1);
         } else {
            if (j == 93) {
               pReader.skip();
               return NBTPathArgument.EmptyListNode.INSTANCE;
            }

            int i = pReader.readInt();
            pReader.expect(']');
            return new NBTPathArgument.CollectionNode(i);
         }
      case '{':
         if (!pFirst) {
            throw ERROR_INVALID_NODE.createWithContext(pReader);
         }

         CompoundNBT compoundnbt = (new JsonToNBT(pReader)).readStruct();
         return new NBTPathArgument.CompoundNode(compoundnbt);
      default:
         String s1 = readUnquotedName(pReader);
         return readObjectNode(pReader, s1);
      }
   }

   private static NBTPathArgument.INode readObjectNode(StringReader pReader, String pName) throws CommandSyntaxException {
      if (pReader.canRead() && pReader.peek() == '{') {
         CompoundNBT compoundnbt = (new JsonToNBT(pReader)).readStruct();
         return new NBTPathArgument.JsonNode(pName, compoundnbt);
      } else {
         return new NBTPathArgument.StringNode(pName);
      }
   }

   /**
    * Reads a tag name until the next special character. Throws if the result would be a 0-length string. Does not
    * handle quoted tag names.
    */
   private static String readUnquotedName(StringReader pReader) throws CommandSyntaxException {
      int i = pReader.getCursor();

      while(pReader.canRead() && isAllowedInUnquotedName(pReader.peek())) {
         pReader.skip();
      }

      if (pReader.getCursor() == i) {
         throw ERROR_INVALID_NODE.createWithContext(pReader);
      } else {
         return pReader.getString().substring(i, pReader.getCursor());
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   /**
    * @return {@code true} if the given character is normal for a tag name; otherwise {@code false} if it has special
    * meaning for paths.
    */
   private static boolean isAllowedInUnquotedName(char pCh) {
      return pCh != ' ' && pCh != '"' && pCh != '[' && pCh != ']' && pCh != '.' && pCh != '{' && pCh != '}';
   }

   private static Predicate<INBT> createTagPredicate(CompoundNBT pTag) {
      return (p_218081_1_) -> {
         return NBTUtil.compareNbt(pTag, p_218081_1_, true);
      };
   }

   static class CollectionNode implements NBTPathArgument.INode {
      private final int index;

      public CollectionNode(int pIndex) {
         this.index = pIndex;
      }

      public void getTag(INBT pTag, List<INBT> pTags) {
         if (pTag instanceof CollectionNBT) {
            CollectionNBT<?> collectionnbt = (CollectionNBT)pTag;
            int i = collectionnbt.size();
            int j = this.index < 0 ? i + this.index : this.index;
            if (0 <= j && j < i) {
               pTags.add(collectionnbt.get(j));
            }
         }

      }

      public void getOrCreateTag(INBT pTag, Supplier<INBT> pSupplier, List<INBT> pTags) {
         this.getTag(pTag, pTags);
      }

      /**
       * Creates an empty element of the type read by this node.
       */
      public INBT createPreferredParentTag() {
         return new ListNBT();
      }

      public int setTag(INBT pTag, Supplier<INBT> pSupplier) {
         if (pTag instanceof CollectionNBT) {
            CollectionNBT<?> collectionnbt = (CollectionNBT)pTag;
            int i = collectionnbt.size();
            int j = this.index < 0 ? i + this.index : this.index;
            if (0 <= j && j < i) {
               INBT inbt = collectionnbt.get(j);
               INBT inbt1 = pSupplier.get();
               if (!inbt1.equals(inbt) && collectionnbt.setTag(j, inbt1)) {
                  return 1;
               }
            }
         }

         return 0;
      }

      public int removeTag(INBT pTag) {
         if (pTag instanceof CollectionNBT) {
            CollectionNBT<?> collectionnbt = (CollectionNBT)pTag;
            int i = collectionnbt.size();
            int j = this.index < 0 ? i + this.index : this.index;
            if (0 <= j && j < i) {
               collectionnbt.remove(j);
               return 1;
            }
         }

         return 0;
      }
   }

   static class CompoundNode implements NBTPathArgument.INode {
      private final Predicate<INBT> predicate;

      public CompoundNode(CompoundNBT pTag) {
         this.predicate = NBTPathArgument.createTagPredicate(pTag);
      }

      public void getTag(INBT pTag, List<INBT> pTags) {
         if (pTag instanceof CompoundNBT && this.predicate.test(pTag)) {
            pTags.add(pTag);
         }

      }

      public void getOrCreateTag(INBT pTag, Supplier<INBT> pSupplier, List<INBT> pTags) {
         this.getTag(pTag, pTags);
      }

      /**
       * Creates an empty element of the type read by this node.
       */
      public INBT createPreferredParentTag() {
         return new CompoundNBT();
      }

      public int setTag(INBT pTag, Supplier<INBT> pSupplier) {
         return 0;
      }

      public int removeTag(INBT pTag) {
         return 0;
      }
   }

   static class EmptyListNode implements NBTPathArgument.INode {
      public static final NBTPathArgument.EmptyListNode INSTANCE = new NBTPathArgument.EmptyListNode();

      private EmptyListNode() {
      }

      public void getTag(INBT pTag, List<INBT> pTags) {
         if (pTag instanceof CollectionNBT) {
            pTags.addAll((CollectionNBT)pTag);
         }

      }

      public void getOrCreateTag(INBT pTag, Supplier<INBT> pSupplier, List<INBT> pTags) {
         if (pTag instanceof CollectionNBT) {
            CollectionNBT<?> collectionnbt = (CollectionNBT)pTag;
            if (collectionnbt.isEmpty()) {
               INBT inbt = pSupplier.get();
               if (collectionnbt.addTag(0, inbt)) {
                  pTags.add(inbt);
               }
            } else {
               pTags.addAll(collectionnbt);
            }
         }

      }

      /**
       * Creates an empty element of the type read by this node.
       */
      public INBT createPreferredParentTag() {
         return new ListNBT();
      }

      public int setTag(INBT pTag, Supplier<INBT> pSupplier) {
         if (!(pTag instanceof CollectionNBT)) {
            return 0;
         } else {
            CollectionNBT<?> collectionnbt = (CollectionNBT)pTag;
            int i = collectionnbt.size();
            if (i == 0) {
               collectionnbt.addTag(0, pSupplier.get());
               return 1;
            } else {
               INBT inbt = pSupplier.get();
               int j = i - (int)collectionnbt.stream().filter(inbt::equals).count();
               if (j == 0) {
                  return 0;
               } else {
                  collectionnbt.clear();
                  if (!collectionnbt.addTag(0, inbt)) {
                     return 0;
                  } else {
                     for(int k = 1; k < i; ++k) {
                        collectionnbt.addTag(k, pSupplier.get());
                     }

                     return j;
                  }
               }
            }
         }
      }

      public int removeTag(INBT pTag) {
         if (pTag instanceof CollectionNBT) {
            CollectionNBT<?> collectionnbt = (CollectionNBT)pTag;
            int i = collectionnbt.size();
            if (i > 0) {
               collectionnbt.clear();
               return i;
            }
         }

         return 0;
      }
   }

   interface INode {
      void getTag(INBT pTag, List<INBT> pTags);

      void getOrCreateTag(INBT pTag, Supplier<INBT> pSupplier, List<INBT> pTags);

      /**
       * Creates an empty element of the type read by this node.
       */
      INBT createPreferredParentTag();

      int setTag(INBT pTag, Supplier<INBT> pSupplier);

      int removeTag(INBT pTag);

      default List<INBT> get(List<INBT> pTags) {
         return this.collect(pTags, this::getTag);
      }

      default List<INBT> getOrCreate(List<INBT> pTags, Supplier<INBT> pSupplier) {
         return this.collect(pTags, (p_218055_2_, p_218055_3_) -> {
            this.getOrCreateTag(p_218055_2_, pSupplier, p_218055_3_);
         });
      }

      default List<INBT> collect(List<INBT> pTags, BiConsumer<INBT, List<INBT>> pConsumer) {
         List<INBT> list = Lists.newArrayList();

         for(INBT inbt : pTags) {
            pConsumer.accept(inbt, list);
         }

         return list;
      }
   }

   static class JsonNode implements NBTPathArgument.INode {
      private final String name;
      private final CompoundNBT pattern;
      private final Predicate<INBT> predicate;

      public JsonNode(String pName, CompoundNBT pPattern) {
         this.name = pName;
         this.pattern = pPattern;
         this.predicate = NBTPathArgument.createTagPredicate(pPattern);
      }

      public void getTag(INBT pTag, List<INBT> pTags) {
         if (pTag instanceof CompoundNBT) {
            INBT inbt = ((CompoundNBT)pTag).get(this.name);
            if (this.predicate.test(inbt)) {
               pTags.add(inbt);
            }
         }

      }

      public void getOrCreateTag(INBT pTag, Supplier<INBT> pSupplier, List<INBT> pTags) {
         if (pTag instanceof CompoundNBT) {
            CompoundNBT compoundnbt = (CompoundNBT)pTag;
            INBT inbt = compoundnbt.get(this.name);
            if (inbt == null) {
               INBT compoundnbt1 = this.pattern.copy();
               compoundnbt.put(this.name, compoundnbt1);
               pTags.add(compoundnbt1);
            } else if (this.predicate.test(inbt)) {
               pTags.add(inbt);
            }
         }

      }

      /**
       * Creates an empty element of the type read by this node.
       */
      public INBT createPreferredParentTag() {
         return new CompoundNBT();
      }

      public int setTag(INBT pTag, Supplier<INBT> pSupplier) {
         if (pTag instanceof CompoundNBT) {
            CompoundNBT compoundnbt = (CompoundNBT)pTag;
            INBT inbt = compoundnbt.get(this.name);
            if (this.predicate.test(inbt)) {
               INBT inbt1 = pSupplier.get();
               if (!inbt1.equals(inbt)) {
                  compoundnbt.put(this.name, inbt1);
                  return 1;
               }
            }
         }

         return 0;
      }

      public int removeTag(INBT pTag) {
         if (pTag instanceof CompoundNBT) {
            CompoundNBT compoundnbt = (CompoundNBT)pTag;
            INBT inbt = compoundnbt.get(this.name);
            if (this.predicate.test(inbt)) {
               compoundnbt.remove(this.name);
               return 1;
            }
         }

         return 0;
      }
   }

   static class ListNode implements NBTPathArgument.INode {
      private final CompoundNBT pattern;
      private final Predicate<INBT> predicate;

      public ListNode(CompoundNBT pPattern) {
         this.pattern = pPattern;
         this.predicate = NBTPathArgument.createTagPredicate(pPattern);
      }

      public void getTag(INBT pTag, List<INBT> pTags) {
         if (pTag instanceof ListNBT) {
            ListNBT listnbt = (ListNBT)pTag;
            listnbt.stream().filter(this.predicate).forEach(pTags::add);
         }

      }

      public void getOrCreateTag(INBT pTag, Supplier<INBT> pSupplier, List<INBT> pTags) {
         MutableBoolean mutableboolean = new MutableBoolean();
         if (pTag instanceof ListNBT) {
            ListNBT listnbt = (ListNBT)pTag;
            listnbt.stream().filter(this.predicate).forEach((p_218060_2_) -> {
               pTags.add(p_218060_2_);
               mutableboolean.setTrue();
            });
            if (mutableboolean.isFalse()) {
               CompoundNBT compoundnbt = this.pattern.copy();
               listnbt.add(compoundnbt);
               pTags.add(compoundnbt);
            }
         }

      }

      /**
       * Creates an empty element of the type read by this node.
       */
      public INBT createPreferredParentTag() {
         return new ListNBT();
      }

      public int setTag(INBT pTag, Supplier<INBT> pSupplier) {
         int i = 0;
         if (pTag instanceof ListNBT) {
            ListNBT listnbt = (ListNBT)pTag;
            int j = listnbt.size();
            if (j == 0) {
               listnbt.add(pSupplier.get());
               ++i;
            } else {
               for(int k = 0; k < j; ++k) {
                  INBT inbt = listnbt.get(k);
                  if (this.predicate.test(inbt)) {
                     INBT inbt1 = pSupplier.get();
                     if (!inbt1.equals(inbt) && listnbt.setTag(k, inbt1)) {
                        ++i;
                     }
                  }
               }
            }
         }

         return i;
      }

      public int removeTag(INBT pTag) {
         int i = 0;
         if (pTag instanceof ListNBT) {
            ListNBT listnbt = (ListNBT)pTag;

            for(int j = listnbt.size() - 1; j >= 0; --j) {
               if (this.predicate.test(listnbt.get(j))) {
                  listnbt.remove(j);
                  ++i;
               }
            }
         }

         return i;
      }
   }

   public static class NBTPath {
      private final String original;
      private final Object2IntMap<NBTPathArgument.INode> nodeToOriginalPosition;
      private final NBTPathArgument.INode[] nodes;

      public NBTPath(String pOriginal, NBTPathArgument.INode[] pNodes, Object2IntMap<NBTPathArgument.INode> pNodeToOriginPosition) {
         this.original = pOriginal;
         this.nodes = pNodes;
         this.nodeToOriginalPosition = pNodeToOriginPosition;
      }

      public List<INBT> get(INBT pTag) throws CommandSyntaxException {
         List<INBT> list = Collections.singletonList(pTag);

         for(NBTPathArgument.INode nbtpathargument$inode : this.nodes) {
            list = nbtpathargument$inode.get(list);
            if (list.isEmpty()) {
               throw this.createNotFoundException(nbtpathargument$inode);
            }
         }

         return list;
      }

      public int countMatching(INBT pTag) {
         List<INBT> list = Collections.singletonList(pTag);

         for(NBTPathArgument.INode nbtpathargument$inode : this.nodes) {
            list = nbtpathargument$inode.get(list);
            if (list.isEmpty()) {
               return 0;
            }
         }

         return list.size();
      }

      private List<INBT> getOrCreateParents(INBT pTag) throws CommandSyntaxException {
         List<INBT> list = Collections.singletonList(pTag);

         for(int i = 0; i < this.nodes.length - 1; ++i) {
            NBTPathArgument.INode nbtpathargument$inode = this.nodes[i];
            int j = i + 1;
            list = nbtpathargument$inode.getOrCreate(list, this.nodes[j]::createPreferredParentTag);
            if (list.isEmpty()) {
               throw this.createNotFoundException(nbtpathargument$inode);
            }
         }

         return list;
      }

      public List<INBT> getOrCreate(INBT pTag, Supplier<INBT> pSupplier) throws CommandSyntaxException {
         List<INBT> list = this.getOrCreateParents(pTag);
         NBTPathArgument.INode nbtpathargument$inode = this.nodes[this.nodes.length - 1];
         return nbtpathargument$inode.getOrCreate(list, pSupplier);
      }

      private static int apply(List<INBT> pTags, Function<INBT, Integer> pFunction) {
         return pTags.stream().map(pFunction).reduce(0, (p_218074_0_, p_218074_1_) -> {
            return p_218074_0_ + p_218074_1_;
         });
      }

      public int set(INBT pTag, Supplier<INBT> pSupplier) throws CommandSyntaxException {
         List<INBT> list = this.getOrCreateParents(pTag);
         NBTPathArgument.INode nbtpathargument$inode = this.nodes[this.nodes.length - 1];
         return apply(list, (p_218077_2_) -> {
            return nbtpathargument$inode.setTag(p_218077_2_, pSupplier);
         });
      }

      public int remove(INBT pTag) {
         List<INBT> list = Collections.singletonList(pTag);

         for(int i = 0; i < this.nodes.length - 1; ++i) {
            list = this.nodes[i].get(list);
         }

         NBTPathArgument.INode nbtpathargument$inode = this.nodes[this.nodes.length - 1];
         return apply(list, nbtpathargument$inode::removeTag);
      }

      private CommandSyntaxException createNotFoundException(NBTPathArgument.INode pNode) {
         int i = this.nodeToOriginalPosition.getInt(pNode);
         return NBTPathArgument.ERROR_NOTHING_FOUND.create(this.original.substring(0, i));
      }

      public String toString() {
         return this.original;
      }
   }

   static class StringNode implements NBTPathArgument.INode {
      private final String name;

      public StringNode(String pName) {
         this.name = pName;
      }

      public void getTag(INBT pTag, List<INBT> pTags) {
         if (pTag instanceof CompoundNBT) {
            INBT inbt = ((CompoundNBT)pTag).get(this.name);
            if (inbt != null) {
               pTags.add(inbt);
            }
         }

      }

      public void getOrCreateTag(INBT pTag, Supplier<INBT> pSupplier, List<INBT> pTags) {
         if (pTag instanceof CompoundNBT) {
            CompoundNBT compoundnbt = (CompoundNBT)pTag;
            INBT inbt;
            if (compoundnbt.contains(this.name)) {
               inbt = compoundnbt.get(this.name);
            } else {
               inbt = pSupplier.get();
               compoundnbt.put(this.name, inbt);
            }

            pTags.add(inbt);
         }

      }

      /**
       * Creates an empty element of the type read by this node.
       */
      public INBT createPreferredParentTag() {
         return new CompoundNBT();
      }

      public int setTag(INBT pTag, Supplier<INBT> pSupplier) {
         if (pTag instanceof CompoundNBT) {
            CompoundNBT compoundnbt = (CompoundNBT)pTag;
            INBT inbt = pSupplier.get();
            INBT inbt1 = compoundnbt.put(this.name, inbt);
            if (!inbt.equals(inbt1)) {
               return 1;
            }
         }

         return 0;
      }

      public int removeTag(INBT pTag) {
         if (pTag instanceof CompoundNBT) {
            CompoundNBT compoundnbt = (CompoundNBT)pTag;
            if (compoundnbt.contains(this.name)) {
               compoundnbt.remove(this.name);
               return 1;
            }
         }

         return 0;
      }
   }
}