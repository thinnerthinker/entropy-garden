package com.yallo.entropygarden;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;

import java.util.*;
import java.util.stream.StreamSupport;

public abstract class EntityActionEvent<E extends Entity, S> implements StartServerTickEvent {
    public abstract boolean filter(E entity, S state);
    public abstract void takeAction(E entity, S state);
    EntityType<E> entityType;
    public HashSet<UUID> currentEntityUUIDs;


    public HashMap<UUID, S> states;

    protected EntityActionEvent(EntityType<E> type) {
        this.entityType = type;
        this.states = new HashMap<>();
        currentEntityUUIDs = new HashSet<>();
    }

    @Override
    public void onStartServerTick(MinecraftServer server) {
        states.keySet().removeIf(id -> !currentEntityUUIDs.contains(id));

        for (UUID uuid : new ArrayList<>(states.keySet())) {
            E entity = (E)StreamSupport.stream(Spliterators.spliteratorUnknownSize(server.getWorlds().iterator(), Spliterator.ORDERED), false)
                    .map(world -> world.getEntity(uuid))
                    .filter(e -> e != null)
                    .findFirst().orElse(null);

            if (entity != null) {
                S state = states.get(uuid);

                if (state != null && filter(entity,  state)) {
                    takeAction(entity, state);
                }
            } else {
                states.remove(uuid);
            }
        }
    }
}
