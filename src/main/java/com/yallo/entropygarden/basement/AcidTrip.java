package com.yallo.entropygarden.basement;

import net.minecraft.util.math.BlockPos;

public class AcidTrip {
    public long castAt;
    public BlockPos from, to;

    public AcidTrip(long castAt, BlockPos from, BlockPos to) {
        this.castAt = castAt;
        this.from = from;
        this.to = to;
    }
}
