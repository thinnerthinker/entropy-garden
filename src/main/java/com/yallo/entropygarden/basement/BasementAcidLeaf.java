package com.yallo.entropygarden.basement;

import com.yallo.entropygarden.UseItemEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class BasementAcidLeaf implements UseItemEvent {
    public HashMap<UUID, List<AcidTrip>> playerAcidTrips;

    public BasementAcidLeaf() {
        playerAcidTrips = new HashMap<>();
    }

    @Override
    public boolean onUseItem(ItemStack itemStack, String name, World world, PlayerEntity player) {
        if (!(itemStack.getItem() == Items.PAPER && name.equals("Acid Leaf"))) {
            return false;
        }
        // Decrease the item stack by one (consume the item)
        itemStack.decrement(1);

        Random random = new Random();
        BlockPos finalPos = null;

        int x = random.nextInt(10000) - 5000 + (int) player.getX();
        int z = random.nextInt(10000) - 5000 + (int) player.getZ();

        // Start from the highest possible y-coordinate, typically the world height
        int y = world.getHeight();
        BlockPos pos = new BlockPos(x, y, z);

        // Scan downward until a non-air block is found
        while (y > 0) {
            pos = new BlockPos(x, y, z);
            if (!world.getBlockState(pos).isAir()) {
                // Check if the block below is solid
                if (world.getBlockState(pos.down()).isSolidBlock(world, pos.down())) {
                    finalPos = pos;
                    break;
                }
            }
            y--;
        }

        if (!playerAcidTrips.containsKey(player.getUuid())) {
            ArrayList<AcidTrip> trips = new ArrayList<>();
            trips.add(new AcidTrip(System.currentTimeMillis(), player.getBlockPos(), finalPos));
            playerAcidTrips.put(player.getUuid(), trips);
        } else {
            playerAcidTrips.get(player.getUuid()).add(new AcidTrip(System.currentTimeMillis(), player.getBlockPos(), finalPos));
        }

        if (finalPos != null) {
            player.teleport(finalPos.getX(), finalPos.getY(), finalPos.getZ());
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_FOX_TELEPORT, SoundCategory.MASTER, 1.0F, 1.0F);
        }

        return true;
    }
}
