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

    // Phase lengths in ticks
    private static final int SNEAK_TICKS = 3;
    private static final int MOVE_TICKS  = 3;
    private static final int TOTAL       = SNEAK_TICKS + MOVE_TICKS;

    // 12 CPS = click every 1.67 ticks at 20tps
    // We simulate this by clicking 2 out of every 3 ticks = ~13 CPS
    // Pattern: CLICK, CLICK, RELEASE, CLICK, CLICK, RELEASE...
    private static final boolean[] CPS_PATTERN = {
        true, true, false,  // 2 clicks every 3 ticks = ~13 CPS
        true, true, false,
        true, true, false,
        true, true, false
    };

    public static void onTick() {
        if (!active) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null) return;

        // Fall safety — immediately sneak + place if falling
        if (mc.player.fallDistance > 0.1f) {
            setKey(mc.options.sneakKey, true);
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            mc.options.useKey.setPressed(true);
            tick = 0;
            clickTick = 0;
            return;
        }

        // Auto refill blocks from inventory
        autoRefill(mc);

        // Reset every tick
        setKey(mc.options.sneakKey, false);
        setKey(mc.options.rightKey, false);
        setKey(mc.options.backKey, false);
        mc.options.useKey.setPressed(false);

        // Get current click state from pattern
        boolean shouldClick = CPS_PATTERN[clickTick % CPS_PATTERN.length];
        clickTick++;

        int cycle = tick % TOTAL;

        if (cycle < SNEAK_TICKS) {
            // Phase 1: Sneak + rapid place
            setKey(mc.options.backKey, true);
            setKey(mc.options.rightKey, true);
            setKey(mc.options.sneakKey, true);
            mc.options.useKey.setPressed(shouldClick);

        } else {
            // Phase 2: Move + keep rapid placing
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

        // Scan hotbar
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

        // No blocks — stop
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
```

---

## How 12 CPS works:

Minecraft runs at **20 ticks per second**. To get 12 CPS you need a click every 1.67 ticks which isn't a whole number, so the pattern approximates it:
```
Tick:    1    2    3    4    5    6    7    8    9   10...
Click:   ON   ON  OFF   ON   ON  OFF   ON   ON  OFF  ON...
