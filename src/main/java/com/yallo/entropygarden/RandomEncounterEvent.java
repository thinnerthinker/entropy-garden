package com.yallo.entropygarden;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.UUID;

public abstract class RandomEncounterEvent implements StartServerTickEvent {
    protected HashMap<UUID, Long> activePlayers;
    public int delay;

    protected RandomEncounterEvent(int delay) {
        activePlayers = new HashMap<>();
        this.delay = delay;
    }

    public abstract boolean condition(PlayerEntity player, World world);
    public abstract void duringEncounter(PlayerEntity player, World world, long ticksIn);

    @Override
    public void onStartServerTick(MinecraftServer server) {
        for (var player : server.getPlayerManager().getPlayerList()) {
            boolean c = condition(player, player.getWorld());

            if (c && !activePlayers.containsKey(player.getUuid())) {
                activePlayers.put(player.getUuid(), System.currentTimeMillis());
            }

            if (!c) {
                activePlayers.remove(player.getUuid());
            }

            if (c) {
                if (System.currentTimeMillis() - activePlayers.get(player.getUuid()) > delay) {
                    duringEncounter(player, player.getWorld(), (System.currentTimeMillis() - activePlayers.get(player.getUuid()) - delay) * 20 / 1000);
                }
            }
        }
    }
}
