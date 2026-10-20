package dev.vapemace.client.gui;

import dev.vapemace.client.VapeMaceClient;
import dev.vapemace.client.module.Category;
import dev.vapemace.client.module.Module;
import dev.vapemace.client.setting.BooleanSetting;
import dev.vapemace.client.setting.NumberSetting;
import dev.vapemace.client.setting.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ClickGuiScreen extends Screen {
    private static final int PANEL_BG = 0xE6141418;
    private static final int HEADER_BG = 0xFF1E1E24;
    private static final int ACCENT_GREEN = 0xFF00C896;
    private static final int PROTOTYPE_AMBER = 0xFFF5A623;
    private static final int MODULE_HOVER = 0xFF25252D;

    private Module bindingModule = null;

    public ClickGuiScreen() {
        super(Text.literal("VapeMace Ultra-Human ClickGUI"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x7008080C);

        int panelX = 24;
        int panelY = 28;
        int panelWidth = 155;

        for (Category category : Category.values()) {
            renderCategoryPanel(context, category, panelX, panelY, panelWidth, mouseX, mouseY, delta);
            panelX += panelWidth + 16;
        }

        String footer = bindingModule != null
                ? "Press any key to bind [" + bindingModule.getName() + "] (ESC to clear)"
                : "VapeMace v6.0 (Ultra-Human) | Left-Click: Toggle | Right-Click: Settings | Middle-Click: Keybind";
        context.drawTextWithShadow(this.textRenderer, footer, 24, this.height - 18, 0xFFAAAAAA);
    }

    private void renderCategoryPanel(DrawContext context, Category category, int x, int y, int width, int mouseX, int mouseY, float delta) {
        context.fill(x, y, x + width, y + 20, HEADER_BG);
        context.fill(x, y, x + width, y + 2, ACCENT_GREEN);
        context.drawTextWithShadow(this.textRenderer, category.getDisplayName(), x + 8, y + 6, 0xFFFFFFFF);

        int currentY = y + 20;
        List<Module> modules = VapeMaceClient.INSTANCE.getModuleManager().getModulesByCategory(category);

        for (Module module : modules) {
            boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= currentY && mouseY <= currentY + 18;
            float targetAnim = module.isEnabled() ? 1.0f : 0.0f;
            module.setAnimProgress(MathHelper.lerp(delta * 0.25f, module.getAnimProgress(), targetAnim));

            int bg = hovered ? MODULE_HOVER : PANEL_BG;
            context.fill(x, currentY, x + width, currentY + 18, bg);

            int barWidth = (int) (3 * module.getAnimProgress());
            if (barWidth > 0) {
                context.fill(x, currentY, x + barWidth, currentY + 18,
                        module.isPrototypeOnly() ? PROTOTYPE_AMBER : ACCENT_GREEN);
            }

            int textColor = module.isEnabled() ? ACCENT_GREEN : 0xFFCCCCCC;
            String label = module.getName() + (module.isPrototypeOnly() ? " [P]" : "");
            context.drawTextWithShadow(this.textRenderer, label, x + 8, currentY + 5, textColor);

            if (module.getKeyCode() != GLFW.GLFW_KEY_UNKNOWN) {
                String keyName = InputUtil.fromKeyCode(module.getKeyCode(), 0).getLocalizedText().getString();
                int kw = this.textRenderer.getWidth(keyName);
                context.drawTextWithShadow(this.textRenderer, "[" + keyName + "]", x + width - kw - 12, currentY + 5, 0xFF777785);
            }

            currentY += 18;

            if (module.isExpanded()) {
                for (Setting<?> setting : module.getSettings()) {
                    context.fill(x, currentY, x + width, currentY + 16, 0xF0101014);
                    if (setting instanceof BooleanSetting boolSet) {
                        int boxColor = boolSet.getValue() ? ACCENT_GREEN : 0xFF3A3A44;
                        context.fill(x + 8, currentY + 4, x + 16, currentY + 12, boxColor);
                        context.drawTextWithShadow(this.textRenderer, boolSet.getName(), x + 22, currentY + 4, 0xFFDDDDDD);
                    } else if (setting instanceof NumberSetting numSet) {
                        double pct = (numSet.getValue() - numSet.getMin()) / (numSet.getMax() - numSet.getMin());
                        int sliderW = (int) ((width - 16) * pct);
                        context.fill(x + 8, currentY + 12, x + width - 8, currentY + 14, 0xFF33333D);
                        context.fill(x + 8, currentY + 12, x + 8 + sliderW, currentY + 14, ACCENT_GREEN);
                        String txt = numSet.getName() + ": " + String.format("%.2f", numSet.getValue());
                        context.drawTextWithShadow(this.textRenderer, txt, x + 8, currentY + 2, 0xFFDDDDDD);
                    }
                    currentY += 16;
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int panelX = 24;
        int panelY = 28;
        int panelWidth = 155;

        for (Category category : Category.values()) {
            int currentY = panelY + 20;
            for (Module module : VapeMaceClient.INSTANCE.getModuleManager().getModulesByCategory(category)) {
                if (mouseX >= panelX && mouseX <= panelX + panelWidth && mouseY >= currentY && mouseY <= currentY + 18) {
                    if (button == 0) module.toggle();
                    else if (button == 1) module.setExpanded(!module.isExpanded());
                    else if (button == 2) bindingModule = module;
                    return true;
                }
                currentY += 18;

                if (module.isExpanded()) {
                    for (Setting<?> setting : module.getSettings()) {
                        if (mouseX >= panelX && mouseX <= panelX + panelWidth && mouseY >= currentY && mouseY <= currentY + 16) {
                            if (setting instanceof BooleanSetting boolSet && button == 0) {
                                boolSet.toggle();
                            } else if (setting instanceof NumberSetting numSet && button == 0) {
                                double ratio = MathHelper.clamp((mouseX - (panelX + 8)) / (panelWidth - 16), 0.0, 1.0);
                                numSet.setValue(numSet.getMin() + ratio * (numSet.getMax() - numSet.getMin()));
                            }
                            return true;
                        }
                        currentY += 16;
                    }
                }
            }
            panelX += panelWidth + 16;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (bindingModule != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE) {
                bindingModule.setKeyCode(GLFW.GLFW_KEY_UNKNOWN);
            } else {
                bindingModule.setKeyCode(keyCode);
            }
            bindingModule = null;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
