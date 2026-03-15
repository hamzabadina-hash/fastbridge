package com.hamzabadina.fastbridge;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

public class FastBridgeController {
    public static boolean active = false;
    private static int tick = 0;

    public static void onTick() {
        if (!active) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int speed = FastBridgeConfig.speedTicks;

        // Always reset every tick
        setKey(mc.options.sneakKey, false);
        setKey(mc.options.rightKey, false);
        setKey(mc.options.backKey, false);
        mc.options.useKey.setPressed(false);

        // Real fast bridge mechanic:
        // - S + D always held (moving diagonally)
        // - Right click ALWAYS held (placing constantly)
        // - Sneak tapped for only 2 ticks per cycle (just enough to not fall)
        // - Sneak releases for the rest — looks and feels like a real player

        int cycle = tick % (speed * 6);

        // Always moving + always placing
        setKey(mc.options.backKey, true);
        setKey(mc.options.rightKey, true);
        mc.options.useKey.setPressed(true);

        if (cycle < 2) {
            // Sneak tap — just 2 ticks, catches the player at the edge
            setKey(mc.options.sneakKey, true);
        } else {
            // Rest of cycle — no sneak, walking freely
            setKey(mc.options.sneakKey, false);
        }

        tick++;
    }

    private static void setKey(KeyBinding key, boolean pressed) {
        KeyBinding.setKeyPressed(key.getDefaultKey(), pressed);
    }

    public static void toggle() {
        active = !active;
        tick = 0;
        if (!active) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.options != null) {
                setKey(mc.options.sneakKey, false);
                setKey(mc.options.rightKey, false);
                setKey(mc.options.backKey, false);
                mc.options.useKey.setPressed(false);
            }
        }
    }
}
