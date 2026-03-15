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
    private static int lastSlot = -1;

    public static void onTick() {
        if (!active) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null) return;

        // Auto refill hotbar from inventory when current slot runs out
        autoRefill(mc);

        int speed = FastBridgeConfig.speedTicks;

        // Reset every tick
        setKey(mc.options.sneakKey, false);
        setKey(mc.options.rightKey, false);
        setKey(mc.options.backKey, false);
        mc.options.useKey.setPressed(false);

        int cycle = tick % (speed * 2);

        if (cycle < speed) {
            // Phase 1: Shift + fast place
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            setKey(mc.options.sneakKey, true);
            if (tick % 2 == 0) {
                mc.options.useKey.setPressed(true);
            } else {
                mc.options.useKey.setPressed(false);
            }

        } else {
            // Phase 2: Remove shift + move + keep placing
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

        // Check if current hotbar slot is empty or not a block
        if (!heldStack.isEmpty() && heldStack.getItem() instanceof BlockItem) {
            lastSlot = selectedSlot;
            return; // all good, still has blocks
        }

        // Try to find blocks in the rest of the hotbar first
        for (int i = 0; i < 9; i++) {
            if (i == selectedSlot) continue;
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                // Switch to that hotbar slot
                inv.selectedSlot = i;
                return;
            }
        }

        // Hotbar is empty — search main inventory (slots 9–35)
        for (int i = 9; i < 36; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                // Move from inventory to current hotbar slot using screen handler
                if (mc.player.currentScreenHandler != null) {
                    // Swap inventory slot with hotbar slot
                    mc.interactionManager.clickSlot(
                        mc.player.currentScreenHandler.syncId,
                        i,           // inventory slot
                        selectedSlot, // hotbar slot number = swap hotkey number
                        SlotActionType.SWAP,
                        mc.player
                    );
                    return;
                }
            }
        }

        // Nothing found anywhere — deactivate to avoid placing air
        // Comment this out if you want it to keep running even with no blocks
        active = false;
        if (mc.player != null) {
            mc.player.sendMessage(
                net.minecraft.text.Text.literal("§c[FastBridge] No blocks left!"), true
            );
        }
    }

    private static void setKey(KeyBinding key, boolean pressed) {
        KeyBinding.setKeyPressed(key.getDefaultKey(), pressed);
    }

    public static void toggle() {
        active = !active;
        tick = 0;
        lastSlot = -1;
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
