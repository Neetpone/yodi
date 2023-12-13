package org.appledash.noodel.render;

import org.appledash.noodel.texture.Texture2D;

public class FontRenderer {
    private final TexturedQuadRenderer texture;

    public FontRenderer(String texturePath) {
        this.texture = new TexturedQuadRenderer(
                Texture2D.fromResource(texturePath),
                18, 28
        );
    }

    public void drawString(String string, int x, int y) {
        int currentX = x;
        int currentY = y;
        for (char c : string.toCharArray()) {
            if (c == '\n') {
                currentX = x;
                currentY += 8;
                continue;
            }
            this.texture.putQuad(currentX, currentY, 18, 28, c - ' ');
            currentX += 18;
        }

        this.texture.draw(this.texture.shader);
    }
}
