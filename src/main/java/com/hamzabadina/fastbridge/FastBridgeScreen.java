package com.hamzabadina.fastbridge;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class FastBridgeScreen extends Screen {

    public FastBridgeScreen() {
        super(Text.literal("Fast Bridge Settings"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int sy = this.height / 2 - 80;

        // Fast Bridge toggle
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Fast Bridge: " + (FastBridgeController.active ? "§aON" : "§cOFF")),
            btn -> {
                FastBridgeController.toggle();
                btn.setMessage(Text.literal("Fast Bridge: " + (FastBridgeController.active ? "§aON" : "§cOFF")));
            }
        ).dimensions(cx - 80, sy, 160, 24).build());

        // Auto Clicker toggle
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Auto Clicker: " + (FastBridgeClient.autoClickerEnabled ? "§aON" : "§cOFF")),
            btn -> {
                FastBridgeClient.autoClickerEnabled = !FastBridgeClient.autoClickerEnabled;
                btn.setMessage(Text.literal("Auto Clicker: " + (FastBridgeClient.autoClickerEnabled ? "§aON" : "§cOFF")));
            }
        ).dimensions(cx - 80, sy + 32, 160, 24).build());

        // Speed buttons
        addDrawableChild(ButtonWidget.builder(Text.literal("Speed: Fast (2 ticks)"), btn -> {
            FastBridgeConfig.speedTicks = 2;
            close();
        }).dimensions(cx - 80, sy + 72, 160, 24).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Speed: Medium (4 ticks)"), btn -> {
            FastBridgeConfig.speedTicks = 4;
            close();
        }).dimensions(cx - 80, sy + 104, 160, 24).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Speed: Slow (6 ticks)"), btn -> {
            FastBridgeConfig.speedTicks = 6;
            close();
        }).dimensions(cx - 80, sy + 136, 160, 24).build());

        // Close
        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), btn -> close())
            .dimensions(cx - 40, sy + 176, 80, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("§6§lFast Bridge §r§7Settings"),
            this.width / 2, this.height / 2 - 105, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("§7Speed: §f" + FastBridgeConfig.speedTicks + " ticks"),
            this.width / 2, this.height / 2 + 90, 0xAAAAAA);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
