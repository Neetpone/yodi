package org.appledash.noodel.screen.type;

import org.appledash.noodel.YodiGame;
import org.appledash.noodel.render.util.RenderUtil;
import org.appledash.noodel.render.Tesselator2D;
import org.appledash.noodel.screen.Screen;
import org.appledash.noodel.texture.Texture2D;
import org.appledash.noodel.render.util.RenderState;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glEnable;

public class GameOverScreen extends Screen {
    private static Texture2D GAME_OVER_TEXTURE;

    public GameOverScreen(YodiGame game) {
        super(game);
    }

    @Override
    public void init() {
        if (GAME_OVER_TEXTURE == null) {
            GAME_OVER_TEXTURE = Texture2D.fromResource("textures/game_over.png");
        }
    }

    @Override
    public void render() {
        glEnable(GL_TEXTURE_2D);
        RenderUtil.drawTexture2D(Tesselator2D.INSTANCE, GAME_OVER_TEXTURE, 0, 0, width, height);
        int halfWidth = width / 2;
        int halfHeight = height / 2;

        RenderState.pushMatrix();
        //RenderState.scale(2, 2, 1);
        this.game.getFontRenderer().drawCenteredString("Final score: " + this.game.getWorld().getScore(), halfWidth, halfHeight);
        RenderState.popMatrix();
    }

    @Override
    public void keyCallback(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.game.resetGame();
        }
    }
}
