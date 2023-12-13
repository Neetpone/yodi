package org.appledash.noodel.render.gl;

import lombok.Getter;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL20.*;

public enum VertexFormat {
    POSITION_2D(Element.POSITION),
    POSITION_TEXTURE_2D(Element.POSITION, Element.TEXTURE);

    @Getter
    private final VertexBuffer buffer = new VertexBuffer();
    public final Element[] elements;

    VertexFormat(Element... elements) {
        this.elements = elements;
    }

    public void setupState() {
        int attributeCount = this.elements.length;
        int totalSize = 0;

        for (int i = 0; i < attributeCount; i++) {
            totalSize += this.elements[i].size;
        }

        int offset = 0;
        for (int i = 0; i < attributeCount; i++) {
            glEnableVertexAttribArray(i);
            glVertexAttribPointer(i, this.elements[i].size / Float.BYTES, GL_FLOAT, false, totalSize, offset);

            offset = this.elements[i].size; // FIXME: Do I want this to be +=?
        }
    }

    public void clearState() {
        int attributeCount = this.elements.length;
        for (int i = 0; i < attributeCount; i++) {
            glDisableVertexAttribArray(i);
        }
    }

    public enum Element {
        POSITION(2),
        TEXTURE(2);

        private final int size;

        Element(int size) {
            this.size = size * Float.BYTES;
        }
    }
}
