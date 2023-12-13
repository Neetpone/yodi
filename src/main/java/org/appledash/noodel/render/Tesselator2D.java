package org.appledash.noodel.render;

import org.appledash.noodel.util.ShaderProgram;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Tesselator2D {
    public static final Tesselator2D INSTANCE = new Tesselator2D(4096);
    private static final float GROW_FACTOR = 1.5f;

    private FloatBuffer buffer;

    private @Nullable VertexFormat vertexFormat; /* Current vertex format we might be drawing */
    private int index; /* Current index in the buffer */
    private int vertexCount; /* Current number of vertices in the buffer */
    private int elementIndex; /* Current element index in the vertex format */

    public Tesselator2D(int initialCapacity) {
        this.buffer = MemoryUtil.memCallocFloat(initialCapacity);
    }

    /**
     * Start building vertices in the given VertexFormat.
     *
     * @param format The VertexFormat used.
     */
    public void begin(VertexFormat format) {
        if (this.vertexFormat != null) {
            throw new IllegalStateException("Cannot begin while already beginned");
        }

        this.vertexFormat = format;
        this.buffer.limit(this.buffer.capacity());
    }

    /**
     * Put a 2D vertex into the buffer.
     *
     * @param x X-coordinate of the vertex.
     * @param y Y-coordinate of the vertex.
     * @return this
     */
    public Tesselator2D vertex(float x, float y) {
        this.putFloat(x);
        this.putFloat(y);
        this.elementIndex++;
        return this;
    }

    /**
     * Put a 2D texture coordinate into the buffer.
     *
     * @param u U-coordinate of the texture.
     * @param v V-coordinate of the texture.
     * @return this
     */
    public Tesselator2D texture(float u, float v) {
        this.putFloat(u);
        this.putFloat(v);
        this.elementIndex++;
        return this;
    }

    /**
     * Finish the current vertex. This validates that all elements of the vertex have been filled.
     */
    public void next() {
        if (this.elementIndex != this.vertexFormat.elements.length) {
            throw new IllegalStateException("not filled all elements of the vertex (expected " + this.vertexFormat.elements.length + ", got " + this.elementIndex + ")");
        }

        this.vertexCount++;
        this.elementIndex = 0;
    }

    public void draw(int mode, ShaderProgram shader) {
        VertexFormat vertexFormat = shader.getVertexFormat();
        VertexBuffer buffer = vertexFormat.getBuffer();
        Tesselator2D.BuiltBuffer vertices = this.end();

        shader.use();
        shader.uploadStandardUniforms();

        buffer.upload(vertexFormat, vertices.buffer());
        vertexFormat.setupState();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glDrawArrays(mode, 0, vertices.vertexCount());

        glDisable(GL_BLEND);

        vertexFormat.clearState();
    }

    /**
     * Finish building, and return the raw vertex data in the buffer.
     *
     * @return {@code BuiltBuffer} containing the raw vertex data and the number of vertices.
     */
    public BuiltBuffer end() {
        if (this.elementIndex != 0) {
            throw new IllegalStateException("not filled all elements of the vertex");
        }

        BuiltBuffer builtBuffer = new BuiltBuffer(this.buffer, this.vertexCount);
        this.reset();
        return builtBuffer;
    }

    /**
     * Reset the Tesselator, readying it for new vertices.
     */
    public void reset() {
        this.buffer.rewind();
        this.vertexCount = 0;
        this.vertexFormat = null;
        this.index = 0;
    }

    /**
     * Free the memory used by the Tesselator. The Tesselator cannot be used after this.
     */
    public void delete() {
        MemoryUtil.memFree(this.buffer);
        this.buffer = null;
    }

    private void putFloat(float f) {
        this.ensureBeginned();
        this.ensureSpace(1);
        this.buffer.put(this.index, f);
        this.index++;
    }

    private void ensureBeginned() {
        if (this.vertexFormat == null) {
            throw new IllegalStateException("cannot add vertex while not beginned");
        }
    }

    private void ensureSpace(int increment) {
        if (this.index + increment >= this.buffer.capacity()) {
            int cap = (int) (this.buffer.capacity() * GROW_FACTOR);
            FloatBuffer newBuffer = MemoryUtil.memCallocFloat(cap);
            this.buffer.limit(this.index);
            this.buffer.position(0);
            newBuffer.put(this.buffer);
            newBuffer.limit(cap);
            MemoryUtil.memFree(this.buffer);

            this.buffer = newBuffer;
        }
    }

    /**
     * Represents a built buffer.
     * @param buffer FloatBuffer with raw vertex data.
     * @param vertexCount Number of vertices in the buffer.
     */
    public record BuiltBuffer(FloatBuffer buffer, int vertexCount) {
    }
}
