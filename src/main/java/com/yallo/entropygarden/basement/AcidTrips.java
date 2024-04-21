package com.yallo.entropygarden.basement;

import com.yallo.entropygarden.StartServerTickEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AcidTrips implements StartServerTickEvent {
    private BasementAcidLeaf bal;

    public AcidTrips(BasementAcidLeaf bal) {
        this.bal = bal;
    }


    @Override
    public void onStartServerTick(MinecraftServer server) {
        long currentTime = System.currentTimeMillis();
        List<UUID> toRemove = new ArrayList<>();

        bal.playerAcidTrips.forEach((uuid, trips) -> {
            var trip = trips.get(trips.size() - 1);
            if (currentTime - trip.castAt >= 60000) { // 60000 ms = 1 minute
                PlayerEntity player = server.getPlayerManager().getPlayer(uuid);
                if (player != null) {
                    player.teleport(trip.from.getX(), trip.from.getY(), trip.from.getZ());
                    player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_FOX_TELEPORT, player.getSoundCategory(), 1.0F, 1.0F);

                    trips.remove(trips.size() - 1);

                    for (var t : trips) {
                        t.castAt += 60000;
                    }
                }
            }

            if (trips.isEmpty()) {
                toRemove.add(uuid);
            }
        });

        toRemove.forEach(bal.playerAcidTrips::remove);
    }
}
