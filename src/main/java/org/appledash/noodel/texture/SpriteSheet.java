package org.appledash.noodel.texture;

import lombok.Getter;

import java.awt.image.BufferedImage;

public class SpriteSheet {
    @Getter
    private final Texture2D texture;
    private final boolean textureOwned; /* if true, we made the Texture2D and we need to delete it. */
    @Getter
    private final int spriteWidth;
    @Getter
    private final int spriteHeight;
    private final int xCount;
    private final int yCount;

    public SpriteSheet(BufferedImage bufferedImage, int spriteWidth, int spriteHeight) {
        this(new Texture2D(bufferedImage), true, spriteWidth, spriteHeight);
    }

    public SpriteSheet(Texture2D texture, int spriteWidth, int spriteHeight) {
        this(texture, false, spriteWidth, spriteHeight);
    }

    private SpriteSheet(Texture2D texture, boolean textureOwned, int spriteWidth, int spriteHeight) {
        this.texture = texture;
        this.textureOwned = textureOwned;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        this.xCount = texture.getWidth() / spriteWidth;
        this.yCount = texture.getHeight() / spriteHeight;
    }

    // upper 16 bits = U, lower 16 bits = V
    public int getSpriteUV(int spriteIndex) {
        int row = spriteIndex % this.xCount;
        int col = spriteIndex / this.yCount;

        return ((row * this.spriteWidth) << Short.SIZE) | (col * this.spriteHeight);
    }

    public void delete() {
        if (this.textureOwned) {
            this.texture.delete();
        }
    }
}
