package org.appledash.noodel.render;

import org.appledash.noodel.util.ShaderProgram;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Tesselator2D {
    public static final Tesselator2D INSTANCE = new Tesselator2D();
    private static final int INITIAL_CAPACITY = 64; /* chosen by fair dice roll */
    private static final float GROW_FACTOR = 1.5f;

    private FloatBuffer buffer = MemoryUtil.memCallocFloat(INITIAL_CAPACITY);
    private int index = 0;
    private int cap = INITIAL_CAPACITY;
    private int vertexCount = 0;

    public void begin(VertexFormat format) {
        if (this.vertexCount != 0) {
            throw new IllegalStateException("Cannot begin while already beginned");
        }

        this.buffer.limit(this.cap);
    }

    public Tesselator2D vertex(float x, float y) {
        this.putFloat(x);
        this.putFloat(y);
        return this;
    }

    public Tesselator2D texture(float u, float v) {
        this.putFloat(u);
        this.putFloat(v);
        return this;
    }

    public Tesselator2D next() {
        this.vertexCount++;
        return this;
    }

    /**
     * Put some vertices with UVs into the buffer. The same number of UVs as vertices must be supplied.
     * UVs must be interpolated with vertices: [vertex, uv, vertex, uv]
     *
     * @param vertices Vertices with UVs
     */
    public void putVertices(float[] vertices) {
        this.ensureSpace(vertices.length);
        ///System.out.println("Putting " + vertices.length + " verticies into buffer with " + this.buffer.remaining() + " remaining" );
        this.buffer.put(this.index, vertices);
        this.index += vertices.length;
        this.vertexCount += vertices.length / 4;
    }

    public void draw(ShaderProgram shader) {
        VertexFormat vertexFormat = shader.getVertexFormat();
        int attributeCount = vertexFormat.attributeSizes.length;
        VertexBuffer buffer = vertexFormat.getBuffer();

        if (vertexFormat == VertexFormat.POSITION_TEXTURE_2D) {
            // glActiveTexture(GL_TEXTURE0);
            //this.spriteSheet.getTexture().bind();
        }

        shader.use();
        Tesselator2D.BuiltBuffer vertices = this.end();

        buffer.upload(vertexFormat, vertices.buffer());
        vertexFormat.setupState();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glDrawArrays(GL_TRIANGLES, 0, vertices.vertexCount());

        glDisable(GL_BLEND);

        vertexFormat.clearState();
    }

    /**
     * Get the raw vertex data in the buffer.
     *
     * @return Float array of vertex data.
     */
    public BuiltBuffer end() {
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
        this.index = 0;
    }

    private void putFloat(float f) {
        this.ensureSpace(1);
        this.buffer.put(this.index, f);
        this.index++;
    }

    private void ensureSpace(int howMuchMore) {
        if (this.index + howMuchMore >= this.cap) {
            this.cap = (int) (this.cap * GROW_FACTOR);
            FloatBuffer newBuffer = MemoryUtil.memCallocFloat(this.cap);
            this.buffer.limit(this.index);
            this.buffer.position(0);
            newBuffer.put(this.buffer);
            MemoryUtil.memFree(this.buffer);
            this.buffer = newBuffer;
            this.buffer.limit(this.cap);
            System.out.println("Set limit to " + this.cap);
        }
    }

    public record BuiltBuffer(FloatBuffer buffer, int vertexCount) {
    }
}
