package dev.meldrite.v1201.compat;

public interface NativeAnvilState {
    String meldrite$getRenameText();
    int meldrite$getRepairCostAmount();
    void meldrite$setRepairCostAmount(int amount);
    int meldrite$getRepairCost();
    void meldrite$setRepairCost(int cost);
    int meldrite$getMaximumRepairCost();
    void meldrite$setMaximumRepairCost(int cost);
}
