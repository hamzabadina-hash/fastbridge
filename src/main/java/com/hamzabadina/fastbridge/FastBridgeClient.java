package com.hamzabadina.fastbridge;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
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

        // Run when fast bridge is ON or autoclicker is enabled and right click held
        boolean shouldRun = FastBridgeController.active
            || (autoClickerEnabled && mc.options.useKey.isPressed());

        if (!shouldRun) {
            autoClickTick = 0;
            return;
        }

        int interval = Math.max(1, 20 / autoClickerCPS);
        autoClickTick++;

        if (autoClickTick >= interval) {
            autoClickTick = 0;
            performRightClick(mc);
        }
    }

    private static void performRightClick(MinecraftClient mc) {
        if (mc.player == null || mc.interactionManager == null || mc.world == null) return;

        ClientPlayerInteractionManager im = mc.interactionManager;

        // Check what player is looking at
        HitResult hit = mc.crosshairTarget;
        if (hit == null) return;

        if (hit.getType() == HitResult.Type.BLOCK) {
            // Looking at a block — place block on it
            BlockHitResult blockHit = (BlockHitResult) hit;
            im.interactBlock(mc.player, Hand.MAIN_HAND, blockHit);
            im.interactItem(mc.player, Hand.MAIN_HAND);
        } else if (hit.getType() == HitResult.Type.MISS) {
            // Looking at air — still try to use item
            im.interactItem(mc.player, Hand.MAIN_HAND);
        }
    }
}
