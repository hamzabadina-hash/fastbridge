package com.hamzabadina.fastbridge;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

public class FastBridgeController {
    public static boolean active = false;
    private static int tick = 0;
    private static int placeDelay = 0;

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

        // Cycle:
        // Phase 1 (speed*5 ticks) : S+D+Shift held long — barely any sneak animation
        // Phase 2 (2 ticks)       : NO shift — short clean dip over edge
        // Phase 3 (3 ticks)       : Shift back + place 3 times = 3 cps rapid fire
        // Phase 4 (speed*2 ticks) : Recover with shift

        int totalCycle = (speed * 5) + 2 + 3 + (speed * 2);
        int cycle = tick % totalCycle;

        if (cycle < speed * 5) {
            // Phase 1: Long smooth sneak — shift held most of the time
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            setKey(mc.options.sneakKey, true);

        } else if (cycle < speed * 5 + 2) {
            // Phase 2: Shift OFF for only 2 ticks — quick clean dip
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            setKey(mc.options.sneakKey, false);

        } else if (cycle < speed * 5 + 2 + 3) {
            // Phase 3: Shift back ON + place block every tick = 3 cps
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            setKey(mc.options.sneakKey, true);
            mc.options.useKey.setPressed(true); // fires 3 ticks in a row = 3 cps

        } else {
            // Phase 4: Smooth recovery
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
        placeDelay = 0;
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
