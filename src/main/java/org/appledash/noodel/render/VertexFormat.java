package org.appledash.noodel.render;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL20.*;

public enum VertexFormat {
    POSITION_2D(new int[] { Float.BYTES * 2 }),
    POSITION_TEXTURE_2D(new int[] { Float.BYTES * 2, Float.BYTES * 2 }),
    POSITION_COLOR_2D(new int[] { Float.BYTES * 2, Float.BYTES * 4}),
    POSITION_COLOR_TEXTURE_2D(new int[] { Float.BYTES * 2, Float.BYTES * 4, Float.BYTES * 2 });

    private final VertexBuffer buffer = new VertexBuffer();
    public final int[] attributeSizes;

    VertexFormat(int[] attributeSizes) {
        this.attributeSizes = attributeSizes;
    }

    public void setupState() {
        int attributeCount = this.attributeSizes.length;
        int totalSize = 0;

        for (int i = 0; i < attributeCount; i++) {
            totalSize += this.attributeSizes[i];
        }

        int offset = 0;
        for (int i = 0; i < attributeCount; i++) {
            glEnableVertexAttribArray(i);
            glVertexAttribPointer(i, this.attributeSizes[i] / Float.BYTES, GL_FLOAT, false, totalSize, offset);

            offset = this.attributeSizes[i]; // FIXME: Do I want this to be +=?
        }
    }

    public void clearState() {
        int attributeCount = this.attributeSizes.length;
        for (int i = 0; i < attributeCount; i++) {
            glDisableVertexAttribArray(i);
        }
    }

    public VertexBuffer getBuffer() {
        return this.buffer;
    }
}
