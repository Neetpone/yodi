package org.appledash.noodel.render.util;

import lombok.experimental.UtilityClass;
import org.appledash.noodel.render.Tesselator2D;
import org.appledash.noodel.render.gl.ShaderProgram;
import org.appledash.noodel.render.gl.VertexFormat;
import org.appledash.noodel.texture.Texture2D;

import static org.lwjgl.opengl.GL11.*;

@UtilityClass
public class RenderUtil {
    public static final ShaderProgram POSITION_TEXTURE = ShaderProgram.loadFromResources("shaders/2dtexture", VertexFormat.POSITION_TEXTURE_2D);

    public static void drawTexture2D(Tesselator2D tess, Texture2D texture, int startX, int startY, int width, int height) {
        glEnable(GL_TEXTURE_2D);
        texture.bind();

        tess.begin(VertexFormat.POSITION_TEXTURE_2D);

        tess.vertex(startX, startY).texture(0, 0).next();
        tess.vertex(startX + width, startY).texture(1, 0).next();
        tess.vertex(startX, startY + height).texture(0, 1).next();
        tess.vertex(startX + width, startY + height).texture(1, 1).next();

        tess.draw(GL_TRIANGLE_STRIP, POSITION_TEXTURE);
    }
}
