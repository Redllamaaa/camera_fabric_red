
package org.redllamaaa.util.inventory;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ScreenBase<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

    public static final int FONT_COLOR = 0xFF404040;

    protected Identifier texture;
    protected List<HoverArea> hoverAreas;

    public ScreenBase(
            Identifier texture,
            T container,
            Inventory playerInventory,
            Component title,
            int width,
            int height
    ) {
        super(container, playerInventory, title, width, height);
        this.texture = texture;
        this.hoverAreas = new ArrayList<>();
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor guiGraphics,
            int mouseX,
            int mouseY,
            float partialTicks
    ) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        extractHoverAreas(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void extractBackground(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTicks
    ) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                leftPos,
                topPos,
                0,
                0,
                imageWidth,
                imageHeight,
                256,
                256
        );
    }

    @Override
    protected void extractLabels(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
    }

    public void extractHoverAreas(
            GuiGraphicsExtractor guiGraphics,
            int mouseX,
            int mouseY
    ) {
        for (HoverArea hoverArea : hoverAreas) {
            if (hoverArea.tooltip != null
                    && hoverArea.isHovered(leftPos, topPos, mouseX, mouseY)) {
                guiGraphics.setComponentTooltipForNextFrame(
                        font,
                        hoverArea.tooltip.get(),
                        mouseX,
                        mouseY
                );
            }
        }
    }

    public int getBlitSize(int amount, int max, int size) {
        return size - (int) (((float) amount / max) * size);
    }

    public void drawCentered(
            GuiGraphicsExtractor guiGraphics,
            Component text,
            int y,
            int color
    ) {
        drawCentered(guiGraphics, text, imageWidth / 2, y, color);
    }

    public void drawCentered(
            GuiGraphicsExtractor guiGraphics,
            Component text,
            int x,
            int y,
            int color
    ) {
        drawCentered(font, guiGraphics, text, x, y, color);
    }

    public static void drawCentered(
            Font font,
            GuiGraphicsExtractor guiGraphics,
            Component text,
            int x,
            int y,
            int color
    ) {
        int width = font.width(text);
        guiGraphics.text(font, text, x - width / 2, y, color, false);
    }

    public static class HoverArea {

        private final int posX;
        private final int posY;
        private final int width;
        private final int height;

        @Nullable
        private final Supplier<List<Component>> tooltip;

        public HoverArea(int posX, int posY, int width, int height) {
            this(posX, posY, width, height, null);
        }

        public HoverArea(
                int posX,
                int posY,
                int width,
                int height,
                @Nullable Supplier<List<Component>> tooltip
        ) {
            this.posX = posX;
            this.posY = posY;
            this.width = width;
            this.height = height;
            this.tooltip = tooltip;
        }

        public int getPosX() {
            return posX;
        }

        public int getPosY() {
            return posY;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        @Nullable
        public Supplier<List<Component>> getTooltip() {
            return tooltip;
        }

        public boolean isHovered(
                int guiLeft,
                int guiTop,
                int mouseX,
                int mouseY
        ) {
            return mouseX >= guiLeft + posX
                    && mouseX < guiLeft + posX + width
                    && mouseY >= guiTop + posY
                    && mouseY < guiTop + posY + height;
        }
    }
}
