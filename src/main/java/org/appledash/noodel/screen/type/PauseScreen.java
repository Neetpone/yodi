package org.appledash.noodel.screen.type;

import org.appledash.noodel.YodiGame;
import org.appledash.noodel.screen.Screen;
import org.lwjgl.glfw.GLFW;

public class PauseScreen extends Screen {
    public PauseScreen(YodiGame game) {
        super(game);
    }

    @Override
    public void render() {
    }

    @Override
    public void keyCallback(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.game.closeScreen();
        }
    }

    @Override
    public boolean isTranslucent() {
        return true;
    }
}
