package com.yallo.entropygarden.basement;

import com.yallo.entropygarden.UseItemEvent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class BasementKush implements UseItemEvent {
    @Override
    public boolean onUseItem(ItemStack itemStack, String name, World world, PlayerEntity player) {
        if (!(itemStack.getItem() == Items.GREEN_DYE && name.equals("Kush"))) {
            return false;
        }

        // Play a pleasant cave ambiance sound at the player's location
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMBIENT_CAVE.value(), player.getSoundCategory(), 1.0F, 1.0F);

        // Decrease the item stack by one (consume the item)
        itemStack.decrement(1);

        // Generate particles around the player
        for (int i = 0; i < 10; i++) {
            world.addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, player.getX(), player.getY() + 1.0D, player.getZ(), 0, 0.1, 0);
        }

        // Add levitation effect
        StatusEffectInstance levitation = new StatusEffectInstance(StatusEffects.LEVITATION, 100, 0); // 100 ticks duration, Level 1
        player.addStatusEffect(levitation);

        // Add a mild nausea effect
        StatusEffectInstance nausea = new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0); // 200 ticks duration, Level 1
        player.addStatusEffect(nausea);

        return true;

    }
}
