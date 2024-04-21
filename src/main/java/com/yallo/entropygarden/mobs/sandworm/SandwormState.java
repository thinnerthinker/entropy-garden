package com.yallo.entropygarden.mobs.sandworm;

import net.minecraft.util.math.Vec3d;

public class SandwormState {
    public long ticksIn;
    public Vec3d pos, dir;

    public SandwormState(long ticksIn, Vec3d pos, Vec3d dir) {
        this.ticksIn = ticksIn;
        this.pos = pos;
        this.dir = dir;
    }
}
