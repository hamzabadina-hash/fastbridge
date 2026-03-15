package com.hamzabadina.fastbridge;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

public class FastBridgeController {
    public static boolean active = false;
    private static int tick = 0;

    public static void onTick() {
        if (!active) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int speed = FastBridgeConfig.speedTicks;

        // Auto refill hotbar slot with blocks from inventory
        autoRefillBlocks(mc);

        // Reset keys every tick
        setKey(mc.options.sneakKey, false);
        setKey(mc.options.rightKey, false);
        setKey(mc.options.backKey, false);
        mc.options.useKey.setPressed(false);

        // Cycle:
        // Phase 1 (speed*8 ticks) : S+D, NO shift at all — walking freely toward edge
        // Phase 2 (speed*3 ticks) : S+D+Shift — only sneak when close to edge
        // Phase 3 (2 ticks)       : NO shift — dip over edge
        // Phase 4 (3 ticks)       : Shift back ON immediately — catch yourself
        // Right click is ALWAYS on regardless of phase

        int totalCycle = (speed * 8) + (speed * 3) + 2 + 3;
        int cycle = tick % totalCycle;

        if (cycle < speed * 8) {
            // Phase 1: Walk freely, no sneak — player moves naturally
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            setKey(mc.options.sneakKey, false); // no shift

        } else if (cycle < speed * 8 + speed * 3) {
            // Phase 2: Getting close to edge — start sneaking
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            setKey(mc.options.sneakKey, true); // sneak on

        } else if (cycle < speed * 8 + speed * 3 + 2) {
            // Phase 3: Release shift — dip over edge for 2 ticks
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            setKey(mc.options.sneakKey, false); // shift OFF

        } else {
            // Phase 4: Catch yourself — sneak back on fast
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            setKey(mc.options.sneakKey, true); // shift back ON
        }

        // Always right click — constant block placement
        mc.options.useKey.setPressed(true);

        tick++;
    }

    private static void autoRefillBlocks(MinecraftClient mc) {
        if (mc.player == null) return;

        int hotbarSlot = mc.player.getInventory().selectedSlot;
        ItemStack held = mc.player.getInventory().getStack(hotbarSlot);

        // Check if current hotbar slot is empty or not a block
        if (held.isEmpty() || !(held.getItem() instanceof BlockItem)) {
            // Search hotbar first for another block slot
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                    mc.player.getInventory().selectedSlot = i;
                    return;
                }
            }

            // Nothing in hotbar — search main inventory (slots 9-35)
            for (int i = 9; i < 36; i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                    // Find empty hotbar slot to move it to
                    for (int h = 0; h < 9; h++) {
                        if (mc.player.getInventory().getStack(h).isEmpty()) {
                            // Swap inventory slot to hotbar
                            mc.player.getInventory().setStack(h, stack.copy());
                            mc.player.getInventory().setStack(i, ItemStack.EMPTY);
                            mc.player.getInventory().selectedSlot = h;
                            return;
                        }
                    }
                    // No empty hotbar slot — just overwrite slot 0
                    mc.player.getInventory().setStack(0, stack.copy());
                    mc.player.getInventory().setStack(i, ItemStack.EMPTY);
                    mc.player.getInventory().selectedSlot = 0;
                    return;
                }
            }
        }
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
