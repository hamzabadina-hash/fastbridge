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

        // Reset all keys every tick
        setKey(mc.options.sneakKey, false);
        setKey(mc.options.rightKey, false);
        setKey(mc.options.backKey, false);
        mc.options.useKey.setPressed(false);

        // Full cycle = speed * 4 ticks
        // Phase 1: S+D+Shift (walking to edge safely)
        // Phase 2: S+D, NO shift for 1 tick (walk over edge slightly)
        // Phase 3: S+D+Shift+RightClick (place block while sneaking back)
        // Phase 4: S+D+Shift (recover, hold position)

        int cycle = tick % (speed * 4);

        if (cycle < speed) {
            // Phase 1: Move S+D with sneak — safe movement toward edge
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            setKey(mc.options.sneakKey, true);

        } else if (cycle < speed + 1) {
            // Phase 2: Release shift for exactly 1 tick
            // Player steps slightly over the edge
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            setKey(mc.options.sneakKey, false); // <-- shift released!

        } else if (cycle < speed * 2 + 1) {
            // Phase 3: Immediately sneak again + place block
            // This is the "catch yourself" moment
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            setKey(mc.options.sneakKey, true); // <-- shift back on!
            mc.options.useKey.setPressed(true); // place block

        } else {
            // Phase 4: Keep sneaking, recover position
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            setKey(mc.options.sneakKey, true);
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
