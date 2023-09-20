package net.minecraft.advancements;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;

public class AdvancementTreeNode {
   private final Advancement advancement;
   private final AdvancementTreeNode parent;
   private final AdvancementTreeNode previousSibling;
   private final int childIndex;
   private final List<AdvancementTreeNode> children = Lists.newArrayList();
   private AdvancementTreeNode ancestor;
   private AdvancementTreeNode thread;
   private int x;
   private float y;
   private float mod;
   private float change;
   private float shift;

   public AdvancementTreeNode(Advancement pAdvancement, @Nullable AdvancementTreeNode pParent, @Nullable AdvancementTreeNode pPreviousSibling, int pChildIndex, int pX) {
      if (pAdvancement.getDisplay() == null) {
         throw new IllegalArgumentException("Can't position an invisible advancement!");
      } else {
         this.advancement = pAdvancement;
         this.parent = pParent;
         this.previousSibling = pPreviousSibling;
         this.childIndex = pChildIndex;
         this.ancestor = this;
         this.x = pX;
         this.y = -1.0F;
         AdvancementTreeNode advancementtreenode = null;

         for(Advancement advancement : pAdvancement.getChildren()) {
            advancementtreenode = this.addChild(advancement, advancementtreenode);
         }

      }
   }

   @Nullable
   private AdvancementTreeNode addChild(Advancement pAdvancement, @Nullable AdvancementTreeNode pPrevious) {
      if (pAdvancement.getDisplay() != null) {
         pPrevious = new AdvancementTreeNode(pAdvancement, this, pPrevious, this.children.size() + 1, this.x + 1);
         this.children.add(pPrevious);
      } else {
         for(Advancement advancement : pAdvancement.getChildren()) {
            pPrevious = this.addChild(advancement, pPrevious);
         }
      }

      return pPrevious;
   }

   private void firstWalk() {
      if (this.children.isEmpty()) {
         if (this.previousSibling != null) {
            this.y = this.previousSibling.y + 1.0F;
         } else {
            this.y = 0.0F;
         }

      } else {
         AdvancementTreeNode advancementtreenode = null;

         for(AdvancementTreeNode advancementtreenode1 : this.children) {
            advancementtreenode1.firstWalk();
            advancementtreenode = advancementtreenode1.apportion(advancementtreenode == null ? advancementtreenode1 : advancementtreenode);
         }

         this.executeShifts();
         float f = ((this.children.get(0)).y + (this.children.get(this.children.size() - 1)).y) / 2.0F;
         if (this.previousSibling != null) {
            this.y = this.previousSibling.y + 1.0F;
            this.mod = this.y - f;
         } else {
            this.y = f;
         }

      }
   }

   private float secondWalk(float pOffsetY, int pColumnX, float pSubtreeTopY) {
      this.y += pOffsetY;
      this.x = pColumnX;
      if (this.y < pSubtreeTopY) {
         pSubtreeTopY = this.y;
      }

      for(AdvancementTreeNode advancementtreenode : this.children) {
         pSubtreeTopY = advancementtreenode.secondWalk(pOffsetY + this.mod, pColumnX + 1, pSubtreeTopY);
      }

      return pSubtreeTopY;
   }

   private void thirdWalk(float pY) {
      this.y += pY;

      for(AdvancementTreeNode advancementtreenode : this.children) {
         advancementtreenode.thirdWalk(pY);
      }

   }

   private void executeShifts() {
      float f = 0.0F;
      float f1 = 0.0F;

      for(int i = this.children.size() - 1; i >= 0; --i) {
         AdvancementTreeNode advancementtreenode = this.children.get(i);
         advancementtreenode.y += f;
         advancementtreenode.mod += f;
         f1 += advancementtreenode.change;
         f += advancementtreenode.shift + f1;
      }

   }

   @Nullable
   private AdvancementTreeNode previousOrThread() {
      if (this.thread != null) {
         return this.thread;
      } else {
         return !this.children.isEmpty() ? this.children.get(0) : null;
      }
   }

   @Nullable
   private AdvancementTreeNode nextOrThread() {
      if (this.thread != null) {
         return this.thread;
      } else {
         return !this.children.isEmpty() ? this.children.get(this.children.size() - 1) : null;
      }
   }

   private AdvancementTreeNode apportion(AdvancementTreeNode pNode) {
      if (this.previousSibling == null) {
         return pNode;
      } else {
         AdvancementTreeNode advancementtreenode = this;
         AdvancementTreeNode advancementtreenode1 = this;
         AdvancementTreeNode advancementtreenode2 = this.previousSibling;
         AdvancementTreeNode advancementtreenode3 = this.parent.children.get(0);
         float f = this.mod;
         float f1 = this.mod;
         float f2 = advancementtreenode2.mod;

         float f3;
         for(f3 = advancementtreenode3.mod; advancementtreenode2.nextOrThread() != null && advancementtreenode.previousOrThread() != null; f1 += advancementtreenode1.mod) {
            advancementtreenode2 = advancementtreenode2.nextOrThread();
            advancementtreenode = advancementtreenode.previousOrThread();
            advancementtreenode3 = advancementtreenode3.previousOrThread();
            advancementtreenode1 = advancementtreenode1.nextOrThread();
            advancementtreenode1.ancestor = this;
            float f4 = advancementtreenode2.y + f2 - (advancementtreenode.y + f) + 1.0F;
            if (f4 > 0.0F) {
               advancementtreenode2.getAncestor(this, pNode).moveSubtree(this, f4);
               f += f4;
               f1 += f4;
            }

            f2 += advancementtreenode2.mod;
            f += advancementtreenode.mod;
            f3 += advancementtreenode3.mod;
         }

         if (advancementtreenode2.nextOrThread() != null && advancementtreenode1.nextOrThread() == null) {
            advancementtreenode1.thread = advancementtreenode2.nextOrThread();
            advancementtreenode1.mod += f2 - f1;
         } else {
            if (advancementtreenode.previousOrThread() != null && advancementtreenode3.previousOrThread() == null) {
               advancementtreenode3.thread = advancementtreenode.previousOrThread();
               advancementtreenode3.mod += f - f3;
            }

            pNode = this;
         }

         return pNode;
      }
   }

   private void moveSubtree(AdvancementTreeNode pNode, float pShift) {
      float f = (float)(pNode.childIndex - this.childIndex);
      if (f != 0.0F) {
         pNode.change -= pShift / f;
         this.change += pShift / f;
      }

      pNode.shift += pShift;
      pNode.y += pShift;
      pNode.mod += pShift;
   }

   private AdvancementTreeNode getAncestor(AdvancementTreeNode pSelf, AdvancementTreeNode pOther) {
      return this.ancestor != null && pSelf.parent.children.contains(this.ancestor) ? this.ancestor : pOther;
   }

   private void finalizePosition() {
      if (this.advancement.getDisplay() != null) {
         this.advancement.getDisplay().setLocation((float)this.x, this.y);
      }

      if (!this.children.isEmpty()) {
         for(AdvancementTreeNode advancementtreenode : this.children) {
            advancementtreenode.finalizePosition();
         }
      }

   }

   public static void run(Advancement pRoot) {
      if (pRoot.getDisplay() == null) {
         throw new IllegalArgumentException("Can't position children of an invisible root!");
      } else {
         AdvancementTreeNode advancementtreenode = new AdvancementTreeNode(pRoot, (AdvancementTreeNode)null, (AdvancementTreeNode)null, 1, 0);
         advancementtreenode.firstWalk();
         float f = advancementtreenode.secondWalk(0.0F, 0, advancementtreenode.y);
         if (f < 0.0F) {
            advancementtreenode.thirdWalk(-f);
         }

         advancementtreenode.finalizePosition();
      }
   }
}