package dev.meldrite.v1201.compat;

public interface ArmorStandNativeAccess {
    void meldrite$setSmall(boolean small);
    void meldrite$setMarker(boolean marker);
    int meldrite$getDisabledSlots();
    void meldrite$setDisabledSlots(int slots);
    boolean meldrite$isSlotDisabled(net.minecraft.entity.EquipmentSlot slot);
    boolean meldrite$canMove();
    void meldrite$setCanMove(boolean move);
    boolean meldrite$canTick();
    void meldrite$setCanTick(boolean tick);
}
