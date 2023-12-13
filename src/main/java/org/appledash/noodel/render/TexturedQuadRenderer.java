package org.appledash.noodel.render;

import org.appledash.noodel.texture.SpriteSheet;
import org.appledash.noodel.texture.Texture2D;
import org.appledash.noodel.util.ShaderProgram;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;

public class TexturedQuadRenderer {
    private final int vertexBufferId;
    private final int uvBufferId;
    public final ShaderProgram shader;
    public final ShaderProgram shader2;

    private final SpriteSheet spriteSheet;
    private final Tesselator2D tesselator2D = new Tesselator2D();

    public TexturedQuadRenderer(Texture2D texture, int spriteWidth, int spriteHeight) {
        this.spriteSheet = new SpriteSheet(texture, spriteWidth, spriteHeight);
        this.vertexBufferId = glGenBuffers();
        this.uvBufferId = glGenBuffers();

        this.shader = ShaderProgram.loadFromResources("shaders/2dtexture", VertexFormat.POSITION_TEXTURE_2D);
        this.shader.use();
        glUniform1i(this.shader.getUniformLocation("textureSampler"), 0);

        this.shader2 = ShaderProgram.loadFromResources("shaders/2d", VertexFormat.POSITION_COLOR_2D);
    }

    public void putQuad(int x, int y, int w, int h, int spriteIndex) {
        int uv = this.spriteSheet.getSpriteUV(spriteIndex);
        int u = (uv >> Short.SIZE) & Short.MAX_VALUE;
        int v = uv & Short.MAX_VALUE;

        this.tesselator2D.putVertices(
                VertexFormat.POSITION_TEXTURE_2D,
                this.generateQuadVertices(x, y, w, h, u, v)
        );
    }

    public void putColoredQuad(int x, int y, int w, int h, float r, float g, float b, float a) {
        this.tesselator2D.putVertices(
                VertexFormat.POSITION_COLOR_2D,
                this.generateColoredQuadVertices(x, y, w, h, r, g, b, a)
        );
    }
    private float[] generateColoredQuadVertices(int x, int y, int w, int h, float r, float g, float b, float a) {
        return new float[] {
                x, y,       // bottom left
                r, g, b, a,

                (x + w), y, // bottom right
                r, g, b, a,

                x, (y + h), // top left
                r, g, b, a,

                x, (y + h),  // top left
                r, g, b, a,

                (x + w), (y + h), // top right
                r, g, b, a,

                (x + w), y, // bottom right
                r, g, b, a
        };
    }


    public void draw(ShaderProgram shader) {
        VertexFormat vertexFormat = shader.getVertexFormat();
        int attributeCount = vertexFormat.attributeSizes.length;
        VertexBuffer buffer = vertexFormat.getBuffer();

        if (vertexFormat == VertexFormat.POSITION_TEXTURE_2D) {
            // glActiveTexture(GL_TEXTURE0);
            this.spriteSheet.getTexture().bind();
        }

        shader.use();
        FloatBuffer vertices = this.tesselator2D.getVertices(vertexFormat);

        buffer.upload(vertexFormat, vertices);
        vertexFormat.setupState();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glDrawArrays(GL_TRIANGLES, 0, vertices.limit() / attributeCount);

        glDisable(GL_BLEND);

        vertexFormat.clearState();
    }

    public void reset() {
        this.tesselator2D.reset();
    }

    private int sizeof(ShaderProgram shader) {
        return switch (shader.getVertexFormat()) {
            case POSITION_COLOR_2D -> (Float.BYTES * 2) + (Float.BYTES * 4);
            case POSITION_TEXTURE_2D -> Float.BYTES * 4;
            default -> throw new IllegalStateException("yall usin a weird vertex format bro");
        };
    }

    public void delete() {
        glDeleteBuffers(this.vertexBufferId);
        glDeleteBuffers(this.uvBufferId);
        this.spriteSheet.delete();

        if (this.shader != null) {
            this.shader.delete();
        }
    }

    private float[] generateQuadVertices(int x, int y, int w, int h, int u, int v) {
        float tW = this.spriteSheet.getTexture().getWidth();
        float sW = this.spriteSheet.getSpriteWidth();
        float tH = this.spriteSheet.getTexture().getHeight();
        float sH = this.spriteSheet.getSpriteHeight();

        return new float[]{
                // triangle0
                x, y,       // bottom left
                (u / tW), (v + sH) / tH,

                (x + w), y, // bottom right
                (u + sW) / tW, (v + sH) / tH,

                x, (y + h), // top left
                (u / tW), (v / tH),

                // triangle1
                x, (y + h),  // top left
                (u / tW), (v / tH),

                (x + w), y, // bottom right
                (u + sW) / tW, (v + sH) / tH,

                (x + w), (y + h), // top right
                (u + sW) / tW, v / tH,
        };
    }
}
