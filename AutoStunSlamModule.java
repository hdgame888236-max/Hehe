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

public class AutoStunSlamModule extends Module {
    private final NumberSetting triggerRange = new NumberSetting("Trigger Range", "Shield stun reach", 3.1, 2.0, 4.5, 0.1);
    private final BooleanSetting practiceAutoExecute = new BooleanSetting("Practice Auto-Execute", "Executes Axe Stun -> Mace Slam automatically", false);

    private int comboStage = 0;
    private String stateLabel = "Idle";

    public AutoStunSlamModule() {
        super("AutoStunSlam", "Shield detection & Axe-to-Mace combo sequencer", Category.COMBAT, false);
        addSetting(triggerRange);
        addSetting(practiceAutoExecute);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        PlayerEntity target = MaceMathUtil.findNearestTarget(mc, triggerRange.getValue());
        if (target == null || mc.interactionManager == null) {
            stateLabel = "Scanning";
            comboStage = 0;
            return;
        }

        int axeSlot = MaceMathUtil.findHotbarAxe(mc.player);
        int maceSlot = MaceMathUtil.findHotbarItem(mc.player, Items.MACE);

        if (target.isBlocking()) {
            stateLabel = (axeSlot != -1 && maceSlot != -1) ? "SHIELD UP -> STUN READY" : "MISSING AXE/MACE";

            if (practiceAutoExecute.getValue() && axeSlot != -1 && maceSlot != -1 && comboStage == 0) {
                mc.player.getInventory().selectedSlot = axeSlot;
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
                comboStage = 1;
            }
        } else if (comboStage == 1 && practiceAutoExecute.getValue() && maceSlot != -1) {
            mc.player.getInventory().selectedSlot = maceSlot;
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            comboStage = 0;
            stateLabel = "SLAM EXECUTED";
        } else {
            comboStage = 0;
            stateLabel = "Target Open";
        }
    }

    @Override
    public String getHudStatus() {
        return stateLabel;
    }
}
