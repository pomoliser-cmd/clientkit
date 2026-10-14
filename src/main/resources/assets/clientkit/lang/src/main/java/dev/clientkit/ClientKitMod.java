package dev.clientkit;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class ClientKitMod implements ClientModInitializer {
    public static final String MOD_ID = "clientkit";

    private static final int ZOOM_FOV = 30;
    private static KeyMapping zoomKey, hudKey, sprintKey;

    private static boolean hudOn = true;
    private static boolean sprintOn = false;
    private static boolean zooming = false;
    private static int savedFov = 70;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category =
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));

        zoomKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.clientkit.zoom", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, category));
        hudKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.clientkit.hud", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, category));
        sprintKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.clientkit.sprint", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, category));

        ClientTickEvents.END_CLIENT_TICK.register(ClientKitMod::tick);

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath(MOD_ID, "info"),
                ClientKitMod::renderHud);
    }

    private static void tick(Minecraft mc) {
        while (hudKey.consumeClick()) hudOn = !hudOn;
        while (sprintKey.consumeClick()) sprintOn = !sprintOn;

        if (zoomKey.isDown()) {
            if (!zooming) {
                savedFov = mc.options.fov().get();
                zooming = true;
            }
            mc.options.fov().set(ZOOM_FOV);
        } else if (zooming) {
            mc.options.fov().set(savedFov);
            zooming = false;
        }

        if (sprintOn && mc.player != null && mc.options.keyUp.isDown()
                && !mc.player.horizontalCollision && !mc.player.isUsingItem()) {
            mc.player.setSprinting(true);
        }
    }

    private static void renderHud(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (!hudOn || mc.player == null || mc.options.hideGui) return;

        int y = 4;
        String[] lines = {
                "FPS: " + mc.getFps(),
                String.format("XYZ: %.1f / %.1f / %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ()),
                "Sprint: " + (sprintOn ? "ON" : "OFF")
        };
        for (String line : lines) {
            g.drawString(mc.font, line, 4, y, 0xFFFFFFFF, true);
            y += 10;
        }
    }
}
