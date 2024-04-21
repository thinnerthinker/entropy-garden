package com.yallo.entropygarden.mobs.burrower;

import com.yallo.entropygarden.RandomEncounterEvent;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class BurrowerPosse extends RandomEncounterEvent {
    private BurrowerActions actions;
    private HashMap<UUID, BlockPos> playerSummonPos;

    public BurrowerPosse(BurrowerActions actions) {
        super(30 * 1000);
        this.actions = actions;
        playerSummonPos = new HashMap<>();
    }

    @Override
    public boolean condition(PlayerEntity player, World world) {
        BlockPos playerPos = player.getBlockPos();

        BlockPos blockBeneathPos = playerPos.down();
        BlockState blockBeneathState = world.getBlockState(blockBeneathPos);

        return blockBeneathState.isOf(Blocks.STONE);
    }

    private BlockPos findRandomSurfacePosition(ServerWorld world, BlockPos center, int radius) {
        Random random = new Random();
        BlockPos randomPos = null;
        int attempts = 0;
        while (attempts < 100) {  // Limit the number of attempts to avoid an infinite loop
            int x = center.getX() + random.nextInt(radius * 2) - radius;
            int z = center.getZ() + random.nextInt(radius * 2) - radius;
            int y = center.getY();
            randomPos = new BlockPos(x, y, z);

            if (world.getBlockState(randomPos).isAir()) {
                return randomPos;
            }

            attempts++;
        }
        return randomPos;
    }


    @Override
    public void duringEncounter(PlayerEntity player, World world, long ticksIn) {
        if (!playerSummonPos.containsKey(player.getUuid())) {
            Random random = new Random();
            BlockPos playerPos = player.getBlockPos();

            // Attempt to find a random surface position around the player
            BlockPos randomSurfacePos = findRandomSurfacePosition((ServerWorld)world, playerPos, 3);
            playerSummonPos.put(player.getUuid(), randomSurfacePos);
        }

        if (ticksIn < 50) {
            Vec3d playerPos = player.getPos();
            if (ticksIn % 10 == 0) {
                ZombieEntity zombie = new ZombieEntity(world);
                BlockPos spawnPos = playerSummonPos.get(player.getUuid());

                zombie.refreshPositionAndAngles(spawnPos, 0.0F, 0.0F);
                zombie.setBaby(true);
                zombie.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
                zombie.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
                zombie.equipStack(EquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
                zombie.equipStack(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));

                // Set the custom name for the zombie
                MutableText name = Text.literal("Burrower").formatted(Formatting.BOLD, Formatting.GOLD);
                zombie.setCustomName(name);
                zombie.setCustomNameVisible(true); // Name will always be visible

                zombie.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, Integer.MAX_VALUE, 1, true, true)); // Duration, Amplifier (Speed 2)

                // Spawn the zombie in the world
                world.spawnEntity(zombie);
                actions.currentEntityUUIDs.add(zombie.getUuid());
                actions.states.put(zombie.getUuid(), new BurrowerState(0L, (int) (ticksIn / 10), playerSummonPos.get(player.getUuid())));
            }

            world.playSound(null, playerPos.getX(), playerPos.getY(), playerPos.getZ(), SoundEvents.BLOCK_GRAVEL_STEP, SoundCategory.BLOCKS, 1.0F, 1.0F);
            Random random = new Random();
            ParticleEffect particleEffect = new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.STONE.getDefaultState());

            ServerWorld serverWorld = (ServerWorld) player.getWorld();
            // Emit particles around the player
            for (int i = 0; i < 100; i++) {
                double offsetX = random.nextDouble() - 0.5;
                double offsetY = random.nextDouble() - 0.5;
                double offsetZ = random.nextDouble() - 0.5;
                var pos = playerSummonPos.get(player.getUuid());
                serverWorld.spawnParticles(particleEffect,
                        pos.getX(), pos.getY(), pos.getZ(), // Position
                        100, // Count
                        offsetX, offsetY, offsetZ, // Random offset
                        1); // Speed
            }
        } else {
            activePlayers.remove(player.getUuid());
        }
    }
}
