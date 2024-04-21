package com.yallo.entropygarden;

import com.yallo.entropygarden.basement.AcidTrips;
import com.yallo.entropygarden.basement.BasementAcidLeaf;
import com.yallo.entropygarden.basement.BasementKush;
import com.yallo.entropygarden.basement.BasementTight;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.*;

public class EntropyGarden implements ModInitializer {
    private List<ParticleEffect> allParticles = new ArrayList<>();

    private List<UseItemEvent> useItemEvents;
    private List<StartServerTickEvent> startServerTickEvents;

    public void initParticles(World world) {
        // Populate the list with all registered particles
        allParticles.add(ParticleTypes.BUBBLE);
        allParticles.add(ParticleTypes.SOUL_FIRE_FLAME);
        allParticles.add(ParticleTypes.ENTITY_EFFECT);
    }

    @Override
    public void onInitialize() {
        useItemEvents = new ArrayList<>();
        startServerTickEvents = new ArrayList<>();

        useItemEvents.add(new BasementTight());
        useItemEvents.add(new BasementKush());
        var basementAcidLeaf = new BasementAcidLeaf();
        useItemEvents.add(basementAcidLeaf);

        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack itemStack = player.getStackInHand(hand);
            if (!itemStack.hasCustomName()) {
                return TypedActionResult.pass(itemStack);
            }

            var name = itemStack.getName().getString();

            for (var e : useItemEvents) {
                if (e.onUseItem(itemStack, name, world, player)) {
                    return TypedActionResult.pass(itemStack);
                }
            }

            return TypedActionResult.fail(itemStack);
        });


        startServerTickEvents.add(new AcidTrips(basementAcidLeaf));

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (var e : startServerTickEvents) {
                e.onStartServerTick(server);
            }
        });
    }


}
