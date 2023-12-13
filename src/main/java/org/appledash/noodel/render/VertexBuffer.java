package org.appledash.noodel.render;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;

/**
 * Represents an OpenGL Vertex Buffer Object.
 */
public class VertexBuffer {
    private final int vertexBufferId;

    public VertexBuffer() {
        this.vertexBufferId = glGenBuffers();
    }

    /**
     * Upload some vertices to the buffer. This will bind the buffer.
     *
     * @param format Vertex format.
     * @param vertices Vertices to upload.
     */
    public void upload(VertexFormat format, FloatBuffer vertices) {
        glBindBuffer(GL_ARRAY_BUFFER, this.vertexBufferId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
    }

    /**
     * Delete the OpenGL vertex buffer. The buffer will be unusable after this.
     */
    public void delete() {
        glDeleteBuffers(this.vertexBufferId);
    }
}
