package com.yallo.entropygarden.mobs.sandworm;

import com.yallo.entropygarden.RandomEncounterEvent;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class SandwormEncounter extends RandomEncounterEvent {
    private SandwormActions actions;
    private HashMap<UUID, BlockPos> playerSummonPos;

    public SandwormEncounter(SandwormActions actions) {
        super(30 * 1000);
        this.actions = actions;
        playerSummonPos = new HashMap<>();
    }
    @Override
    public boolean condition(PlayerEntity player, World world) {
        BlockPos playerPos = player.getBlockPos();
        var biome = world.getBiome(playerPos);

        BlockPos blockBeneathPos = playerPos.down();
        BlockState blockBeneathState = world.getBlockState(blockBeneathPos);

        return biome.matchesKey(BiomeKeys.DESERT) && (blockBeneathState.isOf(Blocks.SAND) ||
                blockBeneathState.isOf(Blocks.CACTUS) ||blockBeneathState.isOf(Blocks.AIR) ||
                blockBeneathState.isOf(Blocks.SANDSTONE));
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
        if (player == null) {
            return;
        }

        if (!playerSummonPos.containsKey(player.getUuid())) {
            BlockPos playerPos = player.getBlockPos();

            // Attempt to find a random surface position around the player
            BlockPos randomSurfacePos = findRandomSurfacePosition((ServerWorld)world, playerPos, 5);
            playerSummonPos.put(player.getUuid(), randomSurfacePos);
        }

        Vec3d playerPos = player.getPos();

        SpiderEntity spider = new SpiderEntity(EntityType.SPIDER, world);
        BlockPos spawnPos = playerSummonPos.get(player.getUuid());

        spider.refreshPositionAndAngles(spawnPos, 0.0F, 0.0F);
        spider.setInvulnerable(true);
        spider.setSilent(true);
        spider.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, Integer.MAX_VALUE, 1, true, false));
        spider.setAiDisabled(true);

        // Set the custom name for the spider
        MutableText name = Text.literal("Sandworm").formatted(Formatting.BOLD, Formatting.GOLD);
        spider.setCustomName(name);
        spider.setCustomNameVisible(true);

        // Spawn the spider in the world
        world.spawnEntity(spider);
        actions.currentEntityUUIDs.add(spider.getUuid());
        actions.states.put(spider.getUuid(), new SandwormState(0L, playerSummonPos.get(player.getUuid()).toCenterPos(), new Vec3d(1, 0, 0)));

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

        activePlayers.remove(player.getUuid());

    }
}
