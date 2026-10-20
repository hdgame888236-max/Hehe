package dev.vapemace.client.module;

import dev.vapemace.client.setting.Setting;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;
    private final boolean prototypeOnly;

    private boolean enabled;
    private boolean expanded;
    private int keyCode = GLFW.GLFW_KEY_UNKNOWN;
    private boolean keyDown = false;
    private float animProgress = 0.0f;

    private final List<Setting<?>> settings = new ArrayList<>();

    public Module(String name, String description, Category category, boolean prototypeOnly) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.prototypeOnly = prototypeOnly;
    }

    protected void addSetting(Setting<?> setting) {
        this.settings.add(setting);
    }

    public void toggle() {
        this.enabled = !this.enabled;
        if (this.enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public void onEnable() {}
    public void onDisable() {}
    public abstract void onTick(MinecraftClient mc);

    public String getHudStatus() {
        return "";
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public boolean isPrototypeOnly() { return prototypeOnly; }
    public boolean isEnabled() { return enabled; }
    public boolean isExpanded() { return expanded; }
    public void setExpanded(boolean expanded) { this.expanded = expanded; }
    public int getKeyCode() { return keyCode; }
    public void setKeyCode(int keyCode) { this.keyCode = keyCode; }
    public boolean wasKeyDown() { return keyDown; }
    public void setKeyDown(boolean keyDown) { this.keyDown = keyDown; }
    public float getAnimProgress() { return animProgress; }
    public void setAnimProgress(float animProgress) { this.animProgress = animProgress; }
    public List<Setting<?>> getSettings() { return settings; }
}
