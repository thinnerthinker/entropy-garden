package com.yallo.entropygarden.mobs.sandworm;

import com.yallo.entropygarden.EntityActionEvent;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Random;

public class SandwormActions extends EntityActionEvent<SpiderEntity, SandwormState> {
    public SandwormActions() {
        super(EntityType.SPIDER);
    }

    @Override
    public boolean filter(SpiderEntity entity, SandwormState state) {
        return entity.getName().getString().equals("Sandworm");
    }

    @Override
    public void takeAction(SpiderEntity entity, SandwormState state) {
        state.ticksIn++;

        var player = entity.getWorld().getEntitiesByType(EntityType.PLAYER,
                new Box(entity.getPos().add(-15, -15, -15), entity.getPos().add(15, 15, 15)),
                p -> true).stream().findFirst().orElse(null);

        Random random = new Random();

        entity.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);


        ParticleEffect particleEffect = new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.SAND.getDefaultState());
        for (int i = 0; i < 100; i++) {
            double offsetX = random.nextDouble() - 0.5;
            double offsetY = random.nextDouble() - 0.5;
            double offsetZ = random.nextDouble() - 0.5;
            ((ServerWorld)entity.getWorld()).spawnParticles(particleEffect,
                    entity.getPos().x, entity.getPos().y, entity.getPos().z,
                    10,
                    offsetX, offsetY, offsetZ,
                    1);
        }

        if (player == null) {
            return;
        }

        if (state.ticksIn < 40) {
            entity.setPosition(entity.getPos().add(player.getPos().
                    subtract(entity.getPos()).normalize().multiply(1/4f)));
        }
        if (state.ticksIn >= 40) {
            var dir = player.getPos(). subtract(entity.getPos()).normalize().multiply(1/4f);
            state.dir = state.dir.add(dir.multiply(0.1)).normalize().multiply(1/4f);
            entity.setPosition(entity.getPos().add(state.dir));
        }

        var eBlockPos = entity.getBlockPos();
        int y = eBlockPos.getY();
        BlockPos pos = eBlockPos, finalPos = eBlockPos;

        // Scan downward until a non-air block is found
        while (y > 0) {
            pos = new BlockPos(eBlockPos.getX(), y, eBlockPos.getZ());
            if (!entity.getWorld().getBlockState(pos).isAir()) {
                // Check if the block below is solid
                if (entity.getWorld().getBlockState(pos.down()).isSolidBlock(entity.getWorld(), pos.down())) {
                    finalPos = pos;
                    break;
                }
            }
            y--;
        }

        var entityPos = entity.getPos();
        entityPos = new Vec3d(entityPos.x, finalPos.getY() + 1, entityPos.z);
        entity.setPosition(entityPos);

        if (((ServerWorld) entity.getWorld()).getBlockState(eBlockPos).getBlock() == Blocks.TNT) {
            entity.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_SPIDER_DEATH, SoundCategory.MASTER, 1.0F, 1.0F);
            entity.getWorld().breakBlock(eBlockPos, false);

            entity.kill();
            states.remove(entity.getUuid());
            currentEntityUUIDs.remove(entity.getUuid());
        }

        Box searchBox = new Box(entity.getPos().add(-1, -1, -1), entity.getPos().add(1, 1, 1));
        List<Entity> nearbyEntities = entity.getWorld().getEntitiesByClass(Entity.class, searchBox, e -> e != entity);

        for (Entity e : nearbyEntities) {
            if (e instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) e;


                var entry = new RegistryEntry.Direct<DamageType>(new DamageType("Sandworm", 1));
                livingEntity.damage(new DamageSource(entry, player, entity), 6.0f);

                double knockbackIntensity = 0.5;
                Vec3d knockbackDirection = new Vec3d(0, 1, 0);
                livingEntity.addVelocity(knockbackDirection.x, knockbackDirection.y * knockbackIntensity, knockbackDirection.z);

                livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 1)); // 1 second of slowness
            }
        }

        if (state.ticksIn == 80) {
            state.ticksIn = 0;
        }
    }
}
