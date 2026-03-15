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

    private static boolean lastRightClickState = false;
    private static int multiClickTicks = 0;
    private static final int MULTI_CLICK_AMOUNT = 4;

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

            handleClickMultiplier(client);
            FastBridgeController.onTick();
        });
    }

    private static void handleClickMultiplier(MinecraftClient mc) {
        if (mc.player == null) return;

        boolean rightClickNow = mc.options.useKey.isPressed();

        if (rightClickNow && !lastRightClickState) {
            multiClickTicks = MULTI_CLICK_AMOUNT * 2;
        }

        lastRightClickState = rightClickNow;

        if (multiClickTicks > 0) {
            if (multiClickTicks % 2 == 0) {
                mc.options.useKey.setPressed(true);
            } else {
                mc.options.useKey.setPressed(false);
            }
            multiClickTicks--;
        }
    }
}
