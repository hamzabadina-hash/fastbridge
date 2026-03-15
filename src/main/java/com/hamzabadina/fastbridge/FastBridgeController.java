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
    private static int clickTick = 0;

    private static final int SNEAK_TICKS = 3;
    private static final int MOVE_TICKS  = 3;
    private static final int TOTAL       = SNEAK_TICKS + MOVE_TICKS;

    private static final boolean[] CPS_PATTERN = {
        true, false, true, false, true, false,
        true, false, true, false, true, false,
        true, false, true, false, true, false,
        true, false
    };

    public static void onTick() {
        if (!active) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null) return;

        if (mc.player.fallDistance > 0.1f) {
            setKey(mc.options.sneakKey, true);
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            mc.options.useKey.setPressed(true);
            tick = 0;
            clickTick = 0;
            return;
        }

        autoRefill(mc);

        setKey(mc.options.sneakKey, false);
        setKey(mc.options.rightKey, false);
        setKey(mc.options.backKey, false);
        mc.options.useKey.setPressed(false);

        boolean shouldClick = CPS_PATTERN[clickTick % CPS_PATTERN.length];
        clickTick++;

        int cycle = tick % TOTAL;

        if (cycle < SNEAK_TICKS) {
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            setKey(mc.options.sneakKey, true);
            mc.options.useKey.setPressed(shouldClick);
        } else {
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            setKey(mc.options.sneakKey, false);
            mc.options.useKey.setPressed(shouldClick);
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
        clickTick = 0;
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
