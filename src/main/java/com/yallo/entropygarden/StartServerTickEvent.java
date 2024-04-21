package com.yallo.entropygarden;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public interface StartServerTickEvent {
    void onStartServerTick(MinecraftServer server);
}
