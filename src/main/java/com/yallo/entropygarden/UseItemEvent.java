package com.yallo.entropygarden;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface UseItemEvent {
    boolean onUseItem(ItemStack itemStack, String name, World world, PlayerEntity player);
}
