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

    // Cycle:
    // MOVE  phase: no sneak, walk fast S+D        (short)
    // SNEAK phase: sneak just before falling edge  (short quick tap)
    // repeat

    private static final int MOVE_TICKS  = 4; // walk freely toward edge
    private static final int SNEAK_TICKS = 2; // quick sneak tap right before falling
    private static final int TOTAL       = MOVE_TICKS + SNEAK_TICKS;

    public static void onTick() {
        if (!active) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null) return;

        // Emergency fall catch — if falling snap sneak on immediately
        if (mc.player.fallDistance > 0.05f) {
            setKey(mc.options.sneakKey, true);
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            tick = 0;
            return;
        }

        autoRefill(mc);

        // Reset all keys every tick
        setKey(mc.options.sneakKey, false);
        setKey(mc.options.backKey, false);
        setKey(mc.options.rightKey, false);

        int cycle = tick % TOTAL;

        if (cycle < MOVE_TICKS) {
            // MOVE phase: walk fast, no sneak
            // Player moves toward edge freely
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            setKey(mc.options.sneakKey, false);

        } else {
            // SNEAK phase: quick tap sneak right before edge
            // Just 2 ticks — barely noticeable but catches player
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            setKey(mc.options.sneakKey, true);
        }

        tick++;
    }

    private static void autoRefill(MinecraftClient mc) {
        PlayerInventory inv = mc.player.getInventory();
        int selectedSlot = inv.selectedSlot;
        ItemStack heldStack = inv.getStack(selectedSlot);

        if (!heldStack.isEmpty() && heldStack.getItem() instanceof BlockItem) {
            return;
        }

        for (int i = 0; i < 9; i++) {
            if (i == selectedSlot) continue;
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                inv.selectedSlot = i;
                return;
            }
        }

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
                setKey(mc.options.backKey, false);
                setKey(mc.options.rightKey, false);
            }
        }
    }
}
