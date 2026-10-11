package dev.meldrite.v1201.hook;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;

import java.util.UUID;

/** Common short names used by the 1.17-1.20 NMS player/entity implementations. */
@Mixin(Entity.class)
public abstract class LegacyEntityAliasesMixin {
    public int ae() {
        return ((Entity) (Object) this).getId();
    }

    public DataTracker al() {
        return ((Entity) (Object) this).getDataTracker();
    }

    public DataTracker au() {
        return ((Entity) (Object) this).getDataTracker();
    }

    public DataTracker ai() { return ((Entity) (Object) this).getDataTracker(); }
    public DataTracker an() { return ((Entity) (Object) this).getDataTracker(); }
    public DataTracker ap() { return ((Entity) (Object) this).getDataTracker(); }
    public DataTracker ar() { return ((Entity) (Object) this).getDataTracker(); }
    public DataTracker aC() { return ((Entity) (Object) this).getDataTracker(); }
    public DataTracker aD() { return ((Entity) (Object) this).getDataTracker(); }
    public DataTracker getDataWatcher() { return ((Entity) (Object) this).getDataTracker(); }

    public Vec3d dy() {
        return ((Entity) (Object) this).getVelocity();
    }

    public UUID cG() {
        return ((Entity) (Object) this).getUuid();
    }

    public UUID cs() {
        return ((Entity) (Object) this).getUuid();
    }

    /** Legacy NMS absolute position/rotation helper shared by every entity subtype. */
    public void absSnapTo(double x, double y, double z, float yaw, float pitch) {
        ((Entity) (Object) this).refreshPositionAndAngles(x, y, z, yaw, pitch);
    }
}
