package com.yallo.entropygarden.basement;

import com.yallo.entropygarden.UseItemEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class BasementTight implements UseItemEvent {
    @Override
    public boolean onUseItem(ItemStack itemStack, String name, World world, PlayerEntity player) {
        if (!(itemStack.getItem() == Items.BONE_MEAL && name.equals("Tight"))) {
            return false;
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ENDER_DRAGON_HURT, player.getSoundCategory(), 1.0F, 1.0F);

        Vec3d targetPos = teleportPlayer(player);  // Calculate the new position and teleport the player
        itemStack.decrement(1);  // Consume the bone meal

        // Play dragon hurt sound at the target position
        world.playSound(null, targetPos.x, targetPos.y, targetPos.z, SoundEvents.ENTITY_ENDER_DRAGON_HURT, player.getSoundCategory(), 1.0F, 1.0F);

        // Create a box around the target position to find entities within a 10-block radius
        Box damageArea = new Box(targetPos.subtract(10, 10, 10), targetPos.add(10, 10, 10));
        // Apply damage and knockback to all entities within the box
        List<Entity> nearbyEntities = world.getEntitiesByClass(Entity.class, damageArea, e -> e != player);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                var entry = new RegistryEntry.Direct<DamageType>(new DamageType("Tight", 1));

                // Apply damage
                livingEntity.damage(new DamageSource(entry, player, player), 6.0f);

                // Apply knockback effect
                double dx = entity.getX() - targetPos.x;
                double dz = entity.getZ() - targetPos.z;
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > 0.0) {
                    entity.addVelocity(dx / distance * 4.0, 0.4, dz / distance * 4.0);
                }
            }
        }

        return true;  // Return a success result
    }

    private Vec3d teleportPlayer(PlayerEntity player) {
        // Calculate the horizontal and vertical components of the direction vector based on yaw and pitch
        double yawRadians = Math.toRadians(-player.getYaw());
        double pitchRadians = Math.toRadians(-player.getPitch());

        double x = Math.cos(pitchRadians) * Math.sin(yawRadians);
        double y = Math.sin(pitchRadians);
        double z = Math.cos(pitchRadians) * Math.cos(yawRadians);

        Vec3d direction = new Vec3d(x, y, z).normalize();
        Vec3d newPos = player.getPos().add(direction.multiply(10));  // Move the player 10 blocks in the facing direction

        player.teleport(newPos.x, newPos.y, newPos.z);  // Teleport the player to the new position
        return newPos;
    }
}
