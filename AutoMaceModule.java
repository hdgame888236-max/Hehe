package dev.vapemace.client.module.impl;

import dev.vapemace.client.module.Category;
import dev.vapemace.client.module.Module;
import dev.vapemace.client.setting.BooleanSetting;
import dev.vapemace.client.setting.NumberSetting;
import dev.vapemace.client.util.MaceMathUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoMaceModule extends Module {
    private final NumberSetting minFallDistance = new NumberSetting("Min Fall Blocks", "Minimum fall distance for smash trigger", 2.5, 1.5, 15.0, 0.5);
    private final NumberSetting attackReach = new NumberSetting("Attack Reach", "Mace strike distance", 3.0, 2.0, 4.5, 0.1);
    private final BooleanSetting practiceAutoSlam = new BooleanSetting("Practice Auto-Slam", "Automatically swings Mace when in range", false);

    private double currentPotentialDamage = 0.0;
    private float attackCooldown = 0.0f;

    public AutoMaceModule() {
        super("AutoMace", "Mace cooldown, fall-damage multiplier, and slam trigger", Category.COMBAT, false);
        addSetting(minFallDistance);
        addSetting(attackReach);
        addSetting(practiceAutoSlam);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        currentPotentialDamage = MaceMathUtil.calculateMaceBonusDamage(mc.player.fallDistance);
        attackCooldown = mc.player.getAttackCooldownProgress(0.5f);

        if (!practiceAutoSlam.getValue() || mc.interactionManager == null) return;

        if (mc.player.fallDistance >= minFallDistance.getValue() && attackCooldown >= 0.9f) {
            PlayerEntity target = MaceMathUtil.findNearestTarget(mc, attackReach.getValue());
            int maceSlot = MaceMathUtil.findHotbarItem(mc.player, Items.MACE);

            if (target != null && maceSlot != -1 && !target.isBlocking()) {
                mc.player.getInventory().selectedSlot = maceSlot;
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }

    @Override
    public String getHudStatus() {
        return String.format("%.1f DMG | CD: %.0f%%", currentPotentialDamage, attackCooldown * 100.0f);
    }

    public double getCurrentPotentialDamage() { return currentPotentialDamage; }
    public float getAttackCooldown() { return attackCooldown; }
}
