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

        // Reset every tick
        setKey(mc.options.sneakKey, false);
        setKey(mc.options.rightKey, false);
        setKey(mc.options.backKey, false);
        mc.options.useKey.setPressed(false);

        // Cycle:
        // Phase 1: SHIFT + PLACE (sneak and right click)
        // Phase 2: MOVE (no sneak, just walk S+D)
        // Repeat

        int cycle = tick % (speed * 2);

        if (cycle < speed) {
            // Phase 1: Shift + Place
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            setKey(mc.options.sneakKey, true);
            mc.options.useKey.setPressed(true);

        } else {
            // Phase 2: Remove shift + move
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
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
