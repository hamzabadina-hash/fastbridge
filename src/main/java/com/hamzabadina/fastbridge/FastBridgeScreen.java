package com.hamzabadina.fastbridge;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class FastBridgeScreen extends Screen {

    private ButtonWidget cpsLabel;

    public FastBridgeScreen() {
        super(Text.literal("Fast Bridge Settings"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int sy = this.height / 2 - 110;

        // Fast Bridge ON/OFF
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Fast Bridge: " + (FastBridgeController.active ? "§aON" : "§cOFF")),
            btn -> {
                FastBridgeController.toggle();
                btn.setMessage(Text.literal("Fast Bridge: " +
                    (FastBridgeController.active ? "§aON" : "§cOFF")));
            }
        ).dimensions(cx - 80, sy, 160, 24).build());

        // Auto Clicker ON/OFF
        addDrawableChild(ButtonWidget.builder(
            Text.literal("Auto Clicker: " + (FastBridgeClient.autoClickerEnabled ? "§aON" : "§cOFF")),
            btn -> {
                FastBridgeClient.autoClickerEnabled = !FastBridgeClient.autoClickerEnabled;
                btn.setMessage(Text.literal("Auto Clicker: " +
                    (FastBridgeClient.autoClickerEnabled ? "§aON" : "§cOFF")));
            }
        ).dimensions(cx - 80, sy + 32, 160, 24).build());

        // CPS minus
        addDrawableChild(ButtonWidget.builder(Text.literal("-"), btn -> {
            if (FastBridgeClient.autoClickerCPS > 1) {
                FastBridgeClient.autoClickerCPS--;
                cpsLabel.setMessage(Text.literal("CPS: " + FastBridgeClient.autoClickerCPS));
            }
        }).dimensions(cx - 80, sy + 64, 40, 24).build());

        // CPS label (disabled, just display)
        cpsLabel = ButtonWidget.builder(
            Text.literal("CPS: " + FastBridgeClient.autoClickerCPS),
            btn -> {}
        ).dimensions(cx - 35, sy + 64, 70, 24).build();
        cpsLabel.active = false;
        addDrawableChild(cpsLabel);

        // CPS plus
        addDrawableChild(ButtonWidget.builder(Text.literal("+"), btn -> {
            if (FastBridgeClient.autoClickerCPS < 20) {
                FastBridgeClient.autoClickerCPS++;
                cpsLabel.setMessage(Text.literal("CPS: " + FastBridgeClient.autoClickerCPS));
            }
        }).dimensions(cx + 40, sy + 64, 40, 24).build());

        // Bridge speed
        addDrawableChild(ButtonWidget.builder(Text.literal("Speed: Fast (2)"), btn -> {
            FastBridgeConfig.speedTicks = 2;
            btn.setMessage(Text.literal("§aSpeed: Fast (2)"));
        }).dimensions(cx - 80, sy + 104, 50, 24).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Speed: Mid (4)"), btn -> {
            FastBridgeConfig.speedTicks = 4;
            btn.setMessage(Text.literal("§aSpeed: Mid (4)"));
        }).dimensions(cx - 25, sy + 104, 50, 24).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Speed: Slow (6)"), btn -> {
            FastBridgeConfig.speedTicks = 6;
            btn.setMessage(Text.literal("§aSpeed: Slow (6)"));
        }).dimensions(cx + 30, sy + 104, 50, 24).build());

        // Close
        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), btn -> close())
            .dimensions(cx - 40, sy + 145, 80, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        // Title
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("§6§lFast Bridge"),
            this.width / 2, this.height / 2 - 128, 0xFFFFFF);

        // Status bar
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("§7Status: " + (FastBridgeController.active ? "§aBridging" : "§cIdle") +
                "   §7Clicker: " + (FastBridgeClient.autoClickerEnabled ? "§aON" : "§cOFF") +
                "   §7CPS: §f" + FastBridgeClient.autoClickerCPS),
            this.width / 2, this.height / 2 + 55, 0xAAAAAA);

        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("§7Bridge Speed: §f" + FastBridgeConfig.speedTicks + " ticks"),
            this.width / 2, this.height / 2 + 68, 0xAAAAAA);

        // Credits
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("§8Made by §7SG_Mafia_"),
            this.width / 2, this.height / 2 + 90, 0x555555);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
