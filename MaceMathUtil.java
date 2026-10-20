package dev.vapemace.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;

public class MaceMathUtil {

    public static double calculateMaceBonusDamage(float fallDistance) {
        if (fallDistance <= 1.5f) return 0.0;
        double firstTier = Math.min(fallDistance, 3.0f) * 4.0;
        double secondTier = Math.max(0.0, Math.min(fallDistance - 3.0f, 5.0f)) * 2.0;
        double thirdTier = Math.max(0.0, fallDistance - 8.0f) * 1.0;
        return 6.0 + firstTier + secondTier + thirdTier;
    }

    public static PlayerEntity findNearestTarget(MinecraftClient mc, double maxRange) {
        if (mc.world == null || mc.player == null) return null;
        return mc.world.getPlayers().stream()
                .filter(p -> p != mc.player && !p.isSpectator() && p.isAlive())
                .filter(p -> mc.player.distanceTo(p) <= maxRange)
                .min(Comparator.comparingDouble(p -> mc.player.distanceTo(p)))
                .orElse(null);
    }

    public static float[] calculateAngles(Vec3d from, Vec3d to) {
        double diffX = to.x - from.x;
        double diffY = to.y - from.y;
        double diffZ = to.z - from.z;
        double distXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, distXZ));

        return new float[]{
                MathHelper.wrapDegrees(yaw),
                MathHelper.clamp(MathHelper.wrapDegrees(pitch), -90.0f, 90.0f)
        };
    }

    public static int findHotbarItem(PlayerEntity player, Item targetItem) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(targetItem)) {
                return i;
            }
        }
        return -1;
    }

    public static int findHotbarAxe(PlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isIn(ItemTags.AXES)) {
                return i;
            }
        }
        return -1;
    }

    public static int findHotbarMaceByEnchantHint(PlayerEntity player, String enchantKeyword) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(Items.MACE)) {
                String enchants = stack.getEnchantments().toString().toLowerCase();
                if (enchants.contains(enchantKeyword.toLowerCase())) {
                    return i;
                }
            }
        }
        return findHotbarItem(player, Items.MACE);
    }

    public static PearlPrediction predictPearlLanding(EnderPearlEntity pearl) {
        Vec3d pos = pearl.getPos();
        Vec3d vel = pearl.getVelocity();
        for (int tick = 1; tick <= 120; tick++) {
            pos = pos.add(vel);
            vel = vel.multiply(0.99).subtract(0, 0.03, 0);
            if (!pearl.getWorld().getBlockState(net.minecraft.util.math.BlockPos.ofFloored(pos)).isAir()) {
                return new PearlPrediction(pos, tick);
            }
        }
        return new PearlPrediction(pos, 120);
    }

    public record PearlPrediction(Vec3d landingPos, int ticksRemaining) {}
}
