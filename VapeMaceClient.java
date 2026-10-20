package dev.vapemace.client;

import dev.vapemace.client.gui.ClickGuiScreen;
import dev.vapemace.client.module.Module;
import dev.vapemace.client.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class VapeMaceClient implements ClientModInitializer {
    public static final String CLIENT_NAME = "VapeMace";
    public static final String CLIENT_VERSION = "v6.0 (Ultra-Human)";
    public static VapeMaceClient INSTANCE;

    private ModuleManager moduleManager;
    private ClickGuiScreen clickGuiScreen;
    private KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        this.moduleManager = new ModuleManager();
        this.clickGuiScreen = new ClickGuiScreen();

        this.openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.vapemace.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.vapemace.client"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;

        while (openGuiKey.wasPressed()) {
            mc.setScreen(clickGuiScreen);
        }

        for (Module module : moduleManager.getModules()) {
            if (module.getKeyCode() != GLFW.GLFW_KEY_UNKNOWN
                    && InputUtil.isKeyPressed(mc.getWindow().getHandle(), module.getKeyCode())) {
                if (!module.wasKeyDown()) {
                    module.toggle();
                    module.setKeyDown(true);
                }
            } else {
                module.setKeyDown(false);
            }

            if (module.isEnabled()) {
                module.onTick(mc);
            }
        }
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }
}
