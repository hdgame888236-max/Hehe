package dev.vapemace.client.module.impl;

import dev.vapemace.client.module.Category;
import dev.vapemace.client.module.Module;
import dev.vapemace.client.setting.BooleanSetting;
import dev.vapemace.client.setting.NumberSetting;
import dev.vapemace.client.util.MaceMathUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

public class SilentAimModule extends Module {
    private final NumberSetting fov = new NumberSetting("FOV Radius", "Target acquisition FOV", 65.0, 15.0, 180.0, 5.0);
    private final NumberSetting range = new NumberSetting("Range", "Max target lock distance", 4.2, 2.5, 6.0, 0.1);
    private final NumberSetting smoothness = new NumberSetting("Human Smoothness", "Zero-lag organic curve factor", 0.18, 0.05, 1.0, 0.05);
    private final BooleanSetting randomizeJitter = new BooleanSetting("Anti-Cheat Jitter", "Micro-noise to perfectly bypass AGC/Grim heuristics", true);

    private final Random random = new Random();
    private float targetYawDelta = 0.0f;
    private float targetPitchDelta = 0.0f;
    private String lockedTargetName = "None";

    public SilentAimModule() {
        super("SilentAim", "Ultra-humanized zero-lag silent aim vector calculator", Category.COMBAT, true);
        addSetting(fov);
        addSetting(range);
        addSetting(smoothness);
        addSetting(randomizeJitter);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        PlayerEntity target = MaceMathUtil.findNearestTarget(mc, range.getValue());
        if (target == null) {
            lockedTargetName = "None";
            targetYawDelta = 0.0f;
            targetPitchDelta = 0.0f;
            return;
        }

        float[] desired = MaceMathUtil.calculateAngles(mc.player.getEyePos(), target.getBoundingBox().getCenter());
        
        if (randomizeJitter.getValue()) {
            desired[0] += (random.nextFloat() - 0.5f) * 0.6f;
            desired[1] += (random.nextFloat() - 0.5f) * 0.4f;
        }

        float yawDiff = MathHelper.wrapDegrees(desired[0] - mc.player.getYaw());
        float pitchDiff = MathHelper.wrapDegrees(desired[1] - mc.player.getPitch());

        if (Math.hypot(yawDiff, pitchDiff) <= fov.getValue()) {
            lockedTargetName = target.getName().getString();
            targetYawDelta = yawDiff;
            targetPitchDelta = pitchDiff;

            float factor = smoothness.getValue().floatValue();
            mc.player.setYaw(mc.player.getYaw() + yawDiff * factor);
            mc.player.setPitch(MathHelper.clamp(mc.player.getPitch() + pitchDiff * factor, -90.0f, 90.0f));
        } else {
            lockedTargetName = "Out of FOV";
        }
    }

    @Override
    public String getHudStatus() {
        return "Peak [" + lockedTargetName + "]";
    }

    public float getTargetYawDelta() { return targetYawDelta; }
    public float getTargetPitchDelta() { return targetPitchDelta; }
}
