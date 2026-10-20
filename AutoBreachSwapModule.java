package dev.vapemace.client.module.impl;

import dev.vapemace.client.module.Category;
import dev.vapemace.client.module.Module;
import dev.vapemace.client.setting.BooleanSetting;
import dev.vapemace.client.setting.NumberSetting;
import dev.vapemace.client.util.MaceMathUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

public class AutoBreachSwapModule extends Module {
    private final NumberSetting fallThreshold = new NumberSetting("Density Fall Min", "Fall blocks before Density beats Breach", 7.0, 3.0, 20.0, 0.5);
    private final BooleanSetting autoHotbarSwap = new BooleanSetting("Auto Hotbar Swap", "Automatically selects recommended Mace slot", false);

    private String recommendation = "None";

    public AutoBreachSwapModule() {
        super("AutoBreachSwap", "Recommends or swaps between Breach IV and Density V Maces", Category.COMBAT, false);
        addSetting(fallThreshold);
        addSetting(autoHotbarSwap);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        PlayerEntity target = MaceMathUtil.findNearestTarget(mc, 4.5);
        int breachSlot = MaceMathUtil.findHotbarMaceByEnchantHint(mc.player, "breach");
        int densitySlot = MaceMathUtil.findHotbarMaceByEnchantHint(mc.player, "density");

        if (breachSlot == -1 && densitySlot == -1) {
            recommendation = "No Mace in Hotbar";
            return;
        }

        boolean useDensity = mc.player.fallDistance >= fallThreshold.getValue();
        int bestSlot = useDensity && densitySlot != -1 ? densitySlot : (breachSlot != -1 ? breachSlot : densitySlot);

        recommendation = (useDensity ? "DENSITY" : "BREACH") + " [Slot " + (bestSlot + 1) + "]";

        if (autoHotbarSwap.getValue() && target != null && bestSlot != -1 && mc.player.fallDistance > 1.5f) {
            mc.player.getInventory().selectedSlot = bestSlot;
        }
    }

    @Override
    public String getHudStatus() {
        return recommendation;
    }
}
