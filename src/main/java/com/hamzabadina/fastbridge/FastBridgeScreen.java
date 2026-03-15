package com.hamzabadina.fastbridge;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class FastBridgeScreen extends Screen {

    public FastBridgeScreen() {
        super(Text.literal("Fast Bridge Settings"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int sy = this.height / 2 - 50;

        addDrawableChild(ButtonWidget.builder(Text.literal("Fast (2 ticks)"), btn -> {
            FastBridgeConfig.speedTicks = 2;
            close();
        }).dimensions(cx - 80, sy, 160, 24).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Medium (4 ticks)"), btn -> {
            FastBridgeConfig.speedTicks = 4;
            close();
        }).dimensions(cx - 80, sy + 32, 160, 24).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Slow (6 ticks)"), btn -> {
            FastBridgeConfig.speedTicks = 6;
            close();
        }).dimensions(cx - 80, sy + 64, 160, 24).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), btn -> close())
            .dimensions(cx - 40, sy + 104, 80, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("Fast Bridge - Speed Settings"),
            this.width / 2, this.height / 2 - 85, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("Current: " + FastBridgeConfig.speedTicks + " ticks  |  Status: " +
                (FastBridgeController.active ? "§aON" : "§cOFF")),
            this.width / 2, this.height / 2 - 70, 0xAAAAAA);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
```

---

## ✅ Final folder structure should look like this:
```
fast-bridge-mod/
├── build.gradle
├── gradle.properties
├── settings.gradle
└── src/main/
    ├── java/com/hamzabadina/fastbridge/
    │   ├── FastBridgeMod.java
    │   ├── FastBridgeClient.java
    │   ├── FastBridgeController.java
    │   ├── FastBridgeConfig.java
    │   └── FastBridgeScreen.java
    └── resources/
        ├── fabric.mod.json
        └── assets/fastbridge/lang/en_us.json
