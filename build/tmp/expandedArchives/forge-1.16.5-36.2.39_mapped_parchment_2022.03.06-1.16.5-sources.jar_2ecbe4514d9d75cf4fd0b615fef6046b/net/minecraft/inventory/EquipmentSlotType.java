package net.minecraft.inventory;

public enum EquipmentSlotType {
   MAINHAND(EquipmentSlotType.Group.HAND, 0, 0, "mainhand"),
   OFFHAND(EquipmentSlotType.Group.HAND, 1, 5, "offhand"),
   FEET(EquipmentSlotType.Group.ARMOR, 0, 1, "feet"),
   LEGS(EquipmentSlotType.Group.ARMOR, 1, 2, "legs"),
   CHEST(EquipmentSlotType.Group.ARMOR, 2, 3, "chest"),
   HEAD(EquipmentSlotType.Group.ARMOR, 3, 4, "head");

   private final EquipmentSlotType.Group type;
   private final int index;
   private final int filterFlag;
   private final String name;

   private EquipmentSlotType(EquipmentSlotType.Group pType, int pIndex, int pFilterFlag, String pName) {
      this.type = pType;
      this.index = pIndex;
      this.filterFlag = pFilterFlag;
      this.name = pName;
   }

   public EquipmentSlotType.Group getType() {
      return this.type;
   }

   public int getIndex() {
      return this.index;
   }

   /**
    * Gets the actual slot index.
    */
   public int getFilterFlag() {
      return this.filterFlag;
   }

   public String getName() {
      return this.name;
   }

   public static EquipmentSlotType byName(String pTargetName) {
      for(EquipmentSlotType equipmentslottype : values()) {
         if (equipmentslottype.getName().equals(pTargetName)) {
            return equipmentslottype;
         }
      }

      throw new IllegalArgumentException("Invalid slot '" + pTargetName + "'");
   }

   /**
    * Returns the slot type based on the slot group and the index inside of that group.
    */
   public static EquipmentSlotType byTypeAndIndex(EquipmentSlotType.Group pSlotType, int pSlotIndex) {
      for(EquipmentSlotType equipmentslottype : values()) {
         if (equipmentslottype.getType() == pSlotType && equipmentslottype.getIndex() == pSlotIndex) {
            return equipmentslottype;
         }
      }

      throw new IllegalArgumentException("Invalid slot '" + pSlotType + "': " + pSlotIndex);
   }

   public static enum Group {
      HAND,
      ARMOR;
   }
}