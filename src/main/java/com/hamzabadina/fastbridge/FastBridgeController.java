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

    // Click multiplier state
    private static boolean lastRightClickState = false;
    private static int multiClickTicks = 0;
    private static final int MULTI_CLICK_AMOUNT = 4; // 1 click = 4 clicks

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

            // Toggle fast bridge
            while (toggleKey.wasPressed()) {
                FastBridgeController.toggle();
                if (client.player != null) {
                    client.player.sendMessage(
                        Text.literal(FastBridgeController.active
                            ? "§a[FastBridge] Enabled"
                            : "§c[FastBridge] Disabled"), true);
                }
            }

            // Open menu
            while (menuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new FastBridgeScreen());
                }
            }

            // Click multiplier — fires 4 rapid clicks for every 1 real right click
            handleClickMultiplier(client);

            // Run fast bridge automation
            FastBridgeController.onTick();
        });
    }

    private static void handleClickMultiplier(MinecraftClient mc) {
        if (mc.player == null) return;

        boolean rightClickNow = mc.options.useKey.isPressed();

        // Detect fresh click (was not pressed before, now is)
        if (rightClickNow && !lastRightClickState) {
            // New click detected — start firing 4 clicks
            multiClickTicks = MULTI_CLICK_AMOUNT * 2; // x2 because on/off each tick
        }

        lastRightClickState = rightClickNow;

        // Fire extra clicks
        if (multiClickTicks > 0) {
            // Alternate on/off to simulate separate clicks
            if (multiClickTicks % 2 == 0) {
                mc.options.useKey.setPressed(true);
            } else {
                mc.options.useKey.setPressed(false);
            }
            multiClickTicks--;
        }
    }
}
