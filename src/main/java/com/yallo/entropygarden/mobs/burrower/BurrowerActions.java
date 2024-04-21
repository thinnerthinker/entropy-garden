package com.yallo.entropygarden.mobs.burrower;

import com.yallo.entropygarden.EntityActionEvent;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;

import java.util.Random;

public class BurrowerActions extends EntityActionEvent<ZombieEntity, BurrowerState> {
    public BurrowerActions() {
        super(EntityType.ZOMBIE);
    }

    @Override
    public boolean filter(ZombieEntity entity, BurrowerState state) {
        return entity.getName().getString().equals("Burrower");
    }

    @Override
    public void takeAction(ZombieEntity entity, BurrowerState state) {
        state.ticksIn++;

        if (state.ticksIn >= 40) {

            if (state.ticksIn == state.order * 4L + 40) {
                if (Math.random() < 0.5) {

                    var players = entity.getWorld().getEntitiesByType(EntityType.PLAYER,
                            new Box(entity.getPos().add(-5, -5, -5), entity.getPos().add(5, 5, 5)),
                            p -> true);

                    for (var p : players) {
                        p.teleport(p.getPos().x, p.getPos().y - 1, p.getPos().z);
                        p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.BLOCK_SAND_BREAK, SoundCategory.MASTER, 10.0F, 1.0F);
                    }
                } else {
                    if (!state.dashing) {
                        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 2 * 20, 3, true, false));
                        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 2 * 20, 1, true, false));
                        state.dashing = true;
                    }
                }

                if (state.dashing) {
                    if (entity.hasStatusEffect(StatusEffects.INVISIBILITY)) {
                        Random random = new Random();

                        ParticleEffect particleEffect = new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.STONE.getDefaultState());
                        for (int i = 0; i < 100; i++) {
                            double offsetX = random.nextDouble() - 0.5;
                            double offsetY = random.nextDouble() - 0.5;
                            double offsetZ = random.nextDouble() - 0.5;
                            ((ServerWorld)entity.getWorld()).spawnParticles(particleEffect,
                                    entity.getPos().x, entity.getPos().y, entity.getPos().z,
                                    100,
                                    offsetX, offsetY, offsetZ,
                                    1);
                        }

                    } else {
                        state.dashing = false;
                    }
                }
            }
        }

        if (state.ticksIn == 61) {
            state.ticksIn = 0;
        }
    }
}
