package dev.meldrite.v1201.compat;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

/** Rewrites native world broadcasts using the player's effective environment. */
public final class NativePlayerEnvironment {
    private NativePlayerEnvironment() {}
    public static Packet<?> personalize(Packet<?> packet, NativePlayerState state, long worldTime, boolean daylightCycle) {
        if (packet instanceof net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket && state.viewDistance >= 0) return new net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket(state.viewDistance);
        if (packet instanceof net.minecraft.network.packet.s2c.play.SimulationDistanceS2CPacket && state.simulationDistance >= 0) return new net.minecraft.network.packet.s2c.play.SimulationDistanceS2CPacket(state.simulationDistance);
        if (packet instanceof WorldTimeUpdateS2CPacket time && (state.timeOffset != 0 || !state.relativeTime)) {
            return new WorldTimeUpdateS2CPacket(time.getTime(), state.playerTime(worldTime), state.relativeTime && daylightCycle);
        }
        if (state.weather != null && packet instanceof GameStateChangeS2CPacket game) {
            boolean rain = state.weather == org.bukkit.WeatherType.DOWNFALL; var reason = game.getReason();
            if (reason == GameStateChangeS2CPacket.RAIN_STARTED || reason == GameStateChangeS2CPacket.RAIN_STOPPED)
                return new GameStateChangeS2CPacket(rain ? GameStateChangeS2CPacket.RAIN_STARTED : GameStateChangeS2CPacket.RAIN_STOPPED, 0);
            if (reason == GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED) return new GameStateChangeS2CPacket(reason, rain ? 1 : 0);
            if (reason == GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED) return new GameStateChangeS2CPacket(reason, 0);
        }
        return packet;
    }
}
