package org.appledash.noodel.render;

import lombok.Getter;
import lombok.SneakyThrows;
import org.appledash.noodel.texture.Texture2D;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class FontRenderer {
    private static final float SCALED_FONT_HEIGHT = 9.0F;
    private static final int PADDING = 1;
    public static final Map<RenderingHints.Key, Object> RENDERING_HINTS = Map.of(
            RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON,
            RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY,
            RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
            RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE,
            RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY,
            RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON
    );

    private final Font font;
    private final float scale;
    private final float scaleInv;
    private final float offsetY;
    private final float baseline;
    private final int charHeight;
    private final int charWidth;
    private final int[] charWidths = new int['~' - ' ' + 1];
    @Getter
    private Texture2D texture;

    public FontRenderer(Font font) {
        this.font = font;

        this.scale = this.font.getSize2D() / FontRenderer.SCALED_FONT_HEIGHT;
        this.scaleInv = 1 / this.scale;

        Graphics2D graphics2D = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
        graphics2D.setFont(this.font);
        FontRenderContext fontRenderContext = graphics2D.getFontRenderContext();
        LineMetrics lineMetrics = this.font.getLineMetrics("", fontRenderContext);

        this.offsetY = -lineMetrics.getDescent() * this.scaleInv;
        this.baseline = lineMetrics.getAscent() + lineMetrics.getLeading();

        this.charHeight = (int) Math.ceil(this.baseline + lineMetrics.getDescent());

        int maxWidth = -1;
        for (char c = ' '; c <= '~'; c++) {
            Rectangle2D bounds = graphics2D.getFontMetrics().getStringBounds(Character.toString(c), graphics2D);

            int width = (int) Math.ceil(bounds.getWidth() + 1);
            this.charWidths[c - ' '] = width;

            if (width > maxWidth) {
                maxWidth = width;
            }
        }

        maxWidth += PADDING * 2;

        this.charWidth = maxWidth;

        this.createAtlas();

        graphics2D.dispose();
    }

    public void drawString(String str, float x, float y) {
        Tesselator2D tess = Tesselator2D.INSTANCE;
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        this.texture.bind();
        tess.begin(VertexFormat.POSITION_TEXTURE_2D);
        float uPerCharacter = this.charWidth / (float) this.texture.getWidth();
        float vPerCharacter = this.charHeight / (float) this.texture.getHeight();
        float scale = this.scale;

        for (char c : str.toCharArray()) {
            int row = (c - ' ') / 10;
            int col = (c - ' ') % 10;
            float u = col * uPerCharacter;
            float v = row * vPerCharacter;

            tess.vertex(x, y).texture(u, v + vPerCharacter).next();
            tess.vertex(x + this.charWidth / scale, y).texture(u + uPerCharacter, v + vPerCharacter).next();
            tess.vertex(x + this.charWidth / scale, y + this.charHeight / scale).texture(u + uPerCharacter, v).next();
            tess.vertex(x, y + this.charHeight / scale).texture(u, v).next();

            x += this.charWidths[c - ' '] / scale;
        }

        tess.draw(GL_QUADS, RenderUtil.POSITION_TEXTURE);
        tess.reset();
    }

    @SneakyThrows
    private void createAtlas() {
        final int totalCols = 10;
        final int totalRows = 10;
        final int fontSize = this.font.getSize();
        BufferedImage bufferedImage = new BufferedImage(totalCols * this.charWidth, totalRows * this.charHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setFont(this.font);
        graphics2D.setRenderingHints(FontRenderer.RENDERING_HINTS);

        for (char c = ' '; c <= '~'; c++) {
            int col = (c - ' ') % totalCols;
            int row = (c - ' ') / totalCols;

            graphics2D.drawString(Character.toString(c), col * this.charWidth, ((row + 1) * this.charHeight) + (this.offsetY * this.scale));
        }

        ImageIO.write(bufferedImage, "png", new File("font.png"));

        graphics2D.dispose();

        this.texture = new Texture2D(bufferedImage);
    }

    public static void main(String[] args) {
        new FontRenderer(new Font("Arial", Font.PLAIN, 9 * 4));
    }
}
