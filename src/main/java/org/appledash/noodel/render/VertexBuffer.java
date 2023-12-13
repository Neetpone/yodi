package org.appledash.noodel.render;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;

public class VertexBuffer {
    private int vertexBufferId;

    public VertexBuffer() {
        this.vertexBufferId = glGenBuffers();
    }

    public void upload(VertexFormat format, FloatBuffer vertices) {
        glBindBuffer(GL_ARRAY_BUFFER, this.vertexBufferId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
    }
}
