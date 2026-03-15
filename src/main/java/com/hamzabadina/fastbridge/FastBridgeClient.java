package com.hamzabadina.fastbridge;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class FastBridgeClient implements ClientModInitializer {

    public static KeyBinding toggleKey;
    public static KeyBinding menuKey;

    public static boolean autoClickerEnabled = true;
    public static int autoClickerCPS = 12;
    private static int autoClickTick = 0;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.fastbridge.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "category.fastbridge"
        ));

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.fastbridge.menu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "category.fastbridge"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                FastBridgeController.toggle();
                if (client.player != null) {
                    client.player.sendMessage(
                        Text.literal(FastBridgeController.active
                            ? "§a[FastBridge] Enabled"
                            : "§c[FastBridge] Disabled"), true);
                }
            }

            while (menuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new FastBridgeScreen());
                }
            }

            // Run fast bridge automation first
            FastBridgeController.onTick();

            // Then handle auto clicker on top
            handleAutoClicker(client);
        });
    }

    public static void handleAutoClicker(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        if (!autoClickerEnabled) {
            autoClickTick = 0;
            return;
        }

        // Auto clicker fires when fast bridge is ON or player holds right click
        boolean shouldRun = FastBridgeController.active || mc.options.useKey.isPressed();
        if (!shouldRun) {
            autoClickTick = 0;
            return;
        }

        // Calculate interval in ticks between each click
        // CPS 1 = every 20 ticks, CPS 20 = every 1 tick
        int intervalTicks = Math.max(1, 20 / autoClickerCPS);

        autoClickTick++;

        if (autoClickTick >= intervalTicks) {
            autoClickTick = 0;
            // Simulate a right click directly through the interaction manager
            if (mc.interactionManager != null) {
                mc.options.useKey.setPressed(true);
                mc.interactionManager.interactItem(
                    mc.player,
                    net.minecraft.util.Hand.MAIN_HAND
                );
                mc.options.useKey.setPressed(false);
            }
        }
    }
}
