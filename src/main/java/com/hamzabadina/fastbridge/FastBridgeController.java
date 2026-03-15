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

        setKey(mc.options.sneakKey, false);
        setKey(mc.options.rightKey, false);
        setKey(mc.options.leftKey, false);
        setKey(mc.options.backKey, false);
        mc.options.useKey.setPressed(false);

        int cycle = tick % (speed * 4);

        if (cycle < speed) {
            // Phase 1: S (walk backward)
            setKey(mc.options.backKey, true);

        } else if (cycle < speed * 2) {
            // Phase 2: Shift + Right Click
            setKey(mc.options.sneakKey, true);
            if (cycle == speed) {
                mc.options.useKey.setPressed(true);
            }

        } else if (cycle < speed * 3) {
            // Phase 3: D (strafe right)
            setKey(mc.options.rightKey, true);

        } else {
            // Phase 4: Shift + Right Click
            setKey(mc.options.sneakKey, true);
            if (cycle == speed * 3) {
                mc.options.useKey.setPressed(true);
            }
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
                setKey(mc.options.leftKey, false);
                setKey(mc.options.backKey, false);
                mc.options.useKey.setPressed(false);
            }
        }
    }
}
