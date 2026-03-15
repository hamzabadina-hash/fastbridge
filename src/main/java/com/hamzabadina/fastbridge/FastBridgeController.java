package com.hamzabadina.fastbridge;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class FastBridgeController {
    public static boolean active = false;
    private static int tick = 0;

    // How many ticks each phase lasts
    private static final int SNEAK_TICKS = 3;   // shift ON + place
    private static final int MOVE_TICKS  = 3;   // shift OFF + move
    private static final int TOTAL       = SNEAK_TICKS + MOVE_TICKS;

    public static void onTick() {
        if (!active) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null) return;

        // Safety check — if player is falling, force sneak immediately
        if (mc.player.fallDistance > 0.1f) {
            setKey(mc.options.sneakKey, true);
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            mc.options.useKey.setPressed(true);
            // Reset cycle back to sneak phase so we dont immediately release again
            tick = 0;
            return;
        }

        // Auto refill blocks
        autoRefill(mc);

        // Reset every tick
        setKey(mc.options.sneakKey, false);
        setKey(mc.options.rightKey, false);
        setKey(mc.options.backKey, false);
        mc.options.useKey.setPressed(false);

        int cycle = tick % TOTAL;

        if (cycle < SNEAK_TICKS) {
            // Phase 1: Sneak + Place
            // Shift is ON the whole phase
            // Right click alternates for fast placement
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            setKey(mc.options.sneakKey, true);
            if (tick % 2 == 0) {
                mc.options.useKey.setPressed(true);
            } else {
                mc.options.useKey.setPressed(false);
            }

        } else {
            // Phase 2: Move — shift OFF
            // Still placing while moving
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            setKey(mc.options.sneakKey, false);
            if (tick % 2 == 0) {
                mc.options.useKey.setPressed(true);
            } else {
                mc.options.useKey.setPressed(false);
            }
        }

        tick++;
    }

    private static void autoRefill(MinecraftClient mc) {
        PlayerInventory inv = mc.player.getInventory();
        int selectedSlot = inv.selectedSlot;
        ItemStack heldStack = inv.getStack(selectedSlot);

        // Current slot still has blocks — no need to refill
        if (!heldStack.isEmpty() && heldStack.getItem() instanceof BlockItem) {
            return;
        }

        // Scan rest of hotbar
        for (int i = 0; i < 9; i++) {
            if (i == selectedSlot) continue;
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                inv.selectedSlot = i;
                return;
            }
        }

        // Scan main inventory
        for (int i = 9; i < 36; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                if (mc.player.currentScreenHandler != null) {
                    mc.interactionManager.clickSlot(
                        mc.player.currentScreenHandler.syncId,
                        i,
                        selectedSlot,
                        SlotActionType.SWAP,
                        mc.player
                    );
                    return;
                }
            }
        }

        // No blocks anywhere — stop
        active = false;
        mc.player.sendMessage(
            net.minecraft.text.Text.literal("§c[FastBridge] No blocks left!"), true
        );
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
