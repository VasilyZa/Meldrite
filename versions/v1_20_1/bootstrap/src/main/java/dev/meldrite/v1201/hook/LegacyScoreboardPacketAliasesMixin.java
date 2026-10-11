package dev.meldrite.v1201.hook;

import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Packet aliases shared by the old CraftBukkit scoreboard/NMS paths. */
public final class LegacyScoreboardPacketAliasesMixin {
    private LegacyScoreboardPacketAliasesMixin() {}

    @Mixin(ScoreboardDisplayS2CPacket.class)
    public abstract static class Display {
        public int b() { return ((ScoreboardDisplayS2CPacket) (Object) this).getSlot(); }
        public String e() { return ((ScoreboardDisplayS2CPacket) (Object) this).getName(); }
    }

    @Mixin(ScoreboardObjectiveUpdateS2CPacket.class)
    public abstract static class Objective {
        public String b() { return ((ScoreboardObjectiveUpdateS2CPacket) (Object) this).getName(); }
        public int f() { return ((ScoreboardObjectiveUpdateS2CPacket) (Object) this).getMode(); }
    }

    @Mixin(PlayerRemoveS2CPacket.class)
    public abstract static class Remove {
        public List<UUID> b() { return ((PlayerRemoveS2CPacket) (Object) this).profileIds(); }
    }

    @Mixin(TeamS2CPacket.class)
    public abstract static class Team {
        public TeamS2CPacket.Operation e() { return ((TeamS2CPacket) (Object) this).getTeamOperation(); }
        public String f() { return ((TeamS2CPacket) (Object) this).getTeamName(); }
        public Collection<String> g() { return ((TeamS2CPacket) (Object) this).getPlayerNames(); }
        public Optional<TeamS2CPacket.SerializableTeam> h() { return ((TeamS2CPacket) (Object) this).getTeam(); }
        public TeamS2CPacket.Operation b() { return ((TeamS2CPacket) (Object) this).getPlayerListOperation(); }
    }
}
