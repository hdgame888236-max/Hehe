package dev.vapemace.client.gui;

import dev.vapemace.client.VapeMaceClient;
import dev.vapemace.client.module.Module;
import dev.vapemace.client.module.impl.AutoPearlCatchModule;
import dev.vapemace.client.module.impl.SilentAimModule;
import dev.vapemace.client.util.MaceMathUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;

public class HudRenderer {
    private static final int ACCENT = 0xFF00C896;
    private static final int BG_DARK = 0xAA121216;

    public static void render(DrawContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.options.hudHidden) return;

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        String watermark = VapeMaceClient.CLIENT_NAME + " " + VapeMaceClient.CLIENT_VERSION;
        context.fill(6, 6, 16 + mc.textRenderer.getWidth(watermark), 22, BG_DARK);
        context.fill(6, 6, 8, 22, ACCENT);
        context.drawTextWithShadow(mc.textRenderer, watermark, 12, 10, 0xFFFFFFFF);

        float fall = mc.player.fallDistance;
        double bonusDmg = MaceMathUtil.calculateMaceBonusDamage(fall);
        String fallText = String.format("Fall: %.1fm | Mace Smash: %.1f DMG", fall, bonusDmg);
        context.fill(6, 25, 16 + mc.textRenderer.getWidth(fallText), 39, BG_DARK);
        context.drawTextWithShadow(mc.textRenderer, fallText, 10, 28, fall > 1.5f ? ACCENT : 0xFFAAAAAA);

        List<Module> active = VapeMaceClient.INSTANCE.getModuleManager().getModules().stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingInt((Module m) ->
                        mc.textRenderer.getWidth(m.getName() + " " + m.getHudStatus())).reversed())
                .toList();

        int yOffset = 6;
        for (Module mod : active) {
            String status = mod.getHudStatus();
            String full = mod.getName() + (status.isEmpty() ? "" : " [" + status + "]");
            int tw = mc.textRenderer.getWidth(full);
            context.fill(sw - tw - 12, yOffset, sw - 4, yOffset + 13, BG_DARK);
            context.fill(sw - 6, yOffset, sw - 4, yOffset + 13, mod.isPrototypeOnly() ? 0xFFF5A623 : ACCENT);
            context.drawTextWithShadow(mc.textRenderer, full, sw - tw - 9, yOffset + 3, 0xFFEFEFEF);
            yOffset += 14;
        }

        PlayerEntity target = MaceMathUtil.findNearestTarget(mc, 6.0);
        if (target != null) {
            int tx = sw / 2 + 18;
            int ty = sh / 2 + 12;
            context.fill(tx, ty, tx + 135, ty + 42, BG_DARK);
            context.fill(tx, ty, tx + 135, ty + 2, target.isBlocking() ? 0xFFFF5555 : ACCENT);

            context.drawTextWithShadow(mc.textRenderer, target.getName().getString(), tx + 6, ty + 6, 0xFFFFFFFF);
            String shieldTag = target.isBlocking() ? "SHIELD: BLOCKING" : "SHIELD: OPEN";
            context.drawTextWithShadow(mc.textRenderer, shieldTag, tx + 6, ty + 17,
                    target.isBlocking() ? 0xFFFF5555 : 0xFF55FF55);

            float hpPct = Math.min(1.0f, target.getHealth() / target.getMaxHealth());
            context.fill(tx + 6, ty + 30, tx + 129, ty + 36, 0xFF2A2A32);
            context.fill(tx + 6, ty + 30, tx + 6 + (int) (123 * hpPct), ty + 36, ACCENT);
        }

        SilentAimModule silentAim = VapeMaceClient.INSTANCE.getModuleManager().getModule(SilentAimModule.class);
        if (silentAim != null && silentAim.isEnabled()) {
            String aimTelemetry = String.format("Ultra-Human Jitter -> dYaw: %.1f | dPitch: %.1f",
                    silentAim.getTargetYawDelta(), silentAim.getTargetPitchDelta());
            int aw = mc.textRenderer.getWidth(aimTelemetry);
            context.drawTextWithShadow(mc.textRenderer, aimTelemetry, (sw - aw) / 2, sh / 2 - 22, 0xFF00C896);
        }

        AutoPearlCatchModule pearlMod = VapeMaceClient.INSTANCE.getModuleManager().getModule(AutoPearlCatchModule.class);
        if (pearlMod != null && pearlMod.isEnabled() && pearlMod.getPredictedLanding() != null) {
            Vec3d p = pearlMod.getPredictedLanding();
            String pearlAlert = String.format("PEARL LANDING: (%.1f, %.1f, %.1f) in %d ticks",
                    p.x, p.y, p.z, pearlMod.getTicksToImpact());
            int pw = mc.textRenderer.getWidth(pearlAlert);
            context.fill((sw - pw) / 2 - 6, 48, (sw + pw) / 2 + 6, 62, 0xCC281038);
            context.drawTextWithShadow(mc.textRenderer, pearlAlert, (sw - pw) / 2, 51, 0xFFE088FF);
        }
    }
}
