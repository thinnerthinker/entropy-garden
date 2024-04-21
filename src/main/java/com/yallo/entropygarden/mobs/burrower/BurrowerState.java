package com.yallo.entropygarden.mobs.burrower;

import net.minecraft.util.math.BlockPos;

public class BurrowerState {
    public long ticksIn;
    public int order;
    public BlockPos spawnedAt;
    public boolean dashing;

    public BurrowerState(long ticksIn, int order, BlockPos spawnedAt) {
        this.ticksIn = ticksIn;
        this.order = order;
        this.spawnedAt = spawnedAt;
        this.dashing = false;
    }
}
