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

        // Cycle breakdown (speed = ticks per phase):
        // Phase 1 (speed*3 ticks) : S+D+Shift held — smooth stable movement
        // Phase 2 (3 ticks)       : S+D, NO shift — hang over edge long enough to feel natural
        // Phase 3 (2 ticks)       : S+D+Shift+Click x2 — rapid double place on the way back
        // Phase 4 (speed ticks)   : S+D+Shift — recover before next cycle

        int totalCycle = (speed * 3) + 3 + 2 + speed;
        int cycle = tick % totalCycle;

        if (cycle < speed * 3) {
            // Phase 1: Smooth sneak walk toward edge
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            setKey(mc.options.sneakKey, true);

        } else if (cycle < speed * 3 + 3) {
            // Phase 2: Release shift — stay off edge for 3 ticks (smooth, not shaky)
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            setKey(mc.options.sneakKey, false); // shift OFF — hang over edge

        } else if (cycle < speed * 3 + 3 + 2) {
            // Phase 3: Sneak back ON + place block fast (2 rapid ticks)
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            setKey(mc.options.sneakKey, true);  // shift back ON
            mc.options.useKey.setPressed(true); // place block every tick = fast placement

        } else {
            // Phase 4: Recover with sneak held before repeating
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
