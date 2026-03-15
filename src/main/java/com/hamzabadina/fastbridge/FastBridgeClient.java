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
    private static int autoClickTick = 0;

    // 12 CPS pattern over 20 ticks
    // 12 clicks in 20 ticks = click on ticks: 0,1,3,4,6,7,9,10,12,13,15,16
    // pattern: ON ON OFF ON ON OFF ON ON OFF ON ON OFF ON ON OFF ON ON OFF ON OFF
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

            handleClickMultiplier(client);
            FastBridgeController.onTick();
        });
    }

    private static void handleClickMultiplier(MinecraftClient mc) {
        if (mc.player == null) return;

        boolean rightClickNow = mc.options.useKey.isPressed();

        if (rightClickNow) {
            // Fire 12 CPS pattern continuously while right click held
            boolean shouldClick = CPS_12_PATTERN[autoClickTick % CPS_12_PATTERN.length];
            mc.options.useKey.setPressed(shouldClick);
            autoClickTick++;
        } else {
            // Reset pattern when not clicking
            autoClickTick = 0;
            mc.options.useKey.setPressed(false);
        }

        lastRightClickState = rightClickNow;
    }
}
