package dev.vapemace.client.module.impl;

import dev.vapemace.client.module.Category;
import dev.vapemace.client.module.Module;
import dev.vapemace.client.setting.BooleanSetting;
import dev.vapemace.client.util.MaceMathUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class AutoPearlCatchModule extends Module {
    private final BooleanSetting windChargeAssist = new BooleanSetting("Practice Wind-Boost", "Uses Wind Charge on self before pearl lands", false);

    private Vec3d predictedLanding = null;
    private int ticksToImpact = -1;

    public AutoPearlCatchModule() {
        super("AutoPearlCatch", "Tracks airborne Ender Pearls and alerts impact timing", Category.UTILITY, false);
        addSetting(windChargeAssist);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        predictedLanding = null;
        ticksToImpact = -1;

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof EnderPearlEntity pearl) {
                MaceMathUtil.PearlPrediction pred = MaceMathUtil.predictPearlLanding(pearl);
                predictedLanding = pred.landingPos();
                ticksToImpact = pred.ticksRemaining();

                if (windChargeAssist.getValue() && ticksToImpact == 4 && mc.interactionManager != null) {
                    int windSlot = MaceMathUtil.findHotbarItem(mc.player, Items.WIND_CHARGE);
                    if (windSlot != -1) {
                        mc.player.getInventory().selectedSlot = windSlot;
                        mc.player.setPitch(90.0f);
                        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    }
                }
                break;
            }
        }
    }

    @Override
    public String getHudStatus() {
        return ticksToImpact > 0 ? "Impact in " + ticksToImpact + "t" : "Scanning";
    }

    public Vec3d getPredictedLanding() { return predictedLanding; }
    public int getTicksToImpact() { return ticksToImpact; }
}
