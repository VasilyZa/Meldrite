package dev.meldrite.v1201.compat;

public interface PaperDamageSourceAccess {
    boolean isSweep();
    boolean isMelting();
    boolean isPoison();
    boolean isCritical();
    void meldrite$setPoison();
    org.bukkit.block.BlockState meldrite$getExplodedBlockState();
}
