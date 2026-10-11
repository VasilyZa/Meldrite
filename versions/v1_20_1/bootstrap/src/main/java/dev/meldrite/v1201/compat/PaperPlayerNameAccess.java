package dev.meldrite.v1201.compat;
public interface PaperPlayerNameAccess {
    record Names(net.kyori.adventure.text.Component display, String legacyDisplay, net.minecraft.text.Text list) { }
    Names meldrite$names();
    net.kyori.adventure.text.Component meldrite$getDisplayName();
    void meldrite$setDisplayName(net.kyori.adventure.text.Component name, String legacy);
    void meldrite$setPlayerListName(net.minecraft.text.Text name);
}
