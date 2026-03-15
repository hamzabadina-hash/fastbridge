package com.hamzabadina.fastbridge;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class FastBridgeClient implements ClientModInitializer {

    public static KeyBinding toggleKey;
    public static KeyBinding menuKey;

    public static boolean autoClickerEnabled = false;
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

            FastBridgeController.onTick();
            handleAutoClicker(client);
        });
    }

    public static void handleAutoClicker(MinecraftClient mc) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Auto clicker runs when:
        // fast bridge ON always fires
        // auto clicker ON fires when holding right click
        boolean shouldRun = FastBridgeController.active
            || (autoClickerEnabled && mc.options.useKey.isPressed());

        if (!shouldRun) {
            autoClickTick = 0;
            return;
        }

        // ticks per click — CPS 20 = 1 tick, CPS 1 = 20 ticks
        int interval = Math.max(1, 20 / autoClickerCPS);
        autoClickTick++;

        if (autoClickTick >= interval) {
            autoClickTick = 0;
            // right click = place block
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
    }
}
