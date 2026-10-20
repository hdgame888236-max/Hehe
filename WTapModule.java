package dev.vapemace.client.module.impl;

import dev.vapemace.client.module.Category;
import dev.vapemace.client.module.Module;
import dev.vapemace.client.setting.BooleanSetting;
import dev.vapemace.client.setting.NumberSetting;
import dev.vapemace.client.util.MaceMathUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Random;

public class WTapModule extends Module {
    private final NumberSetting range = new NumberSetting("Trigger Range", "Max range for W-Tap activation", 3.2, 2.0, 4.5, 0.1);
    private final BooleanSetting organicVariation = new BooleanSetting("Organic Variation", "Zero-lag randomized tap duration for natural knockback", true);

    private final Random random = new Random();
    private int unpressTicks = 0;
    private boolean wasAttacking = false;

    public WTapModule() {
        super("WTap", "Peak HT2 organic sprint-reset (W-Tap)", Category.COMBAT, false);
        addSetting(range);
        addSetting(organicVariation);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        PlayerEntity target = MaceMathUtil.findNearestTarget(mc, range.getValue());
        if (target == null || mc.player == null) {
            unpressTicks = 0;
            return;
        }

        boolean isHitting = mc.options.attackKey.isPressed() && mc.player.distanceTo(target) <= range.getValue();
        if (isHitting && !wasAttacking) {
            mc.options.sprintKey.setPressed(false);
            int duration = 1;
            if (organicVariation.getValue()) {
                duration = random.nextDouble() < 0.4 ? 2 : 1;
            }
            unpressTicks = duration;
        }

        if (unpressTicks > 0) {
            unpressTicks--;
            if (unpressTicks == 0) {
                mc.options.sprintKey.setPressed(true);
            }
        }
        wasAttacking = isHitting;
    }

    @Override
    public String getHudStatus() {
        return unpressTicks > 0 ? "Reset" : "Ready";
    }
}
