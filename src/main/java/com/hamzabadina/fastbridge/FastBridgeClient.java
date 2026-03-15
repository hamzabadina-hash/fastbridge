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
    private static int autoClickTick = 0;

    private static final boolean[] CPS_12_PATTERN = {
        true,  true,  false,
        true,  true,  false,
        true,  true,  false,
        true,  true,  false,
        true,  true,  false,
        true,  true,  false,
        true,  false
    };

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

            handleAutoClicker(client);
            FastBridgeController.onTick();
        });
    }

    private static void handleAutoClicker(MinecraftClient mc) {
        if (mc.player == null) return;

        // Auto clicker runs when:
        // - fast bridge is ON (always fires right click)
        // - OR player is manually holding right click with auto clicker enabled
        boolean shouldRun = FastBridgeController.active
            || (autoClickerEnabled && mc.options.useKey.isPressed());

        if (shouldRun) {
            boolean shouldClick = CPS_12_PATTERN[autoClickTick % CPS_12_PATTERN.length];
            mc.options.useKey.setPressed(shouldClick);
            autoClickTick++;
        } else {
            autoClickTick = 0;
        }
    }
}
