package org.appledash.noodel.screen;

import lombok.Setter;
import org.appledash.noodel.YodiGame;

public abstract class Screen {
    /* Yeah, these are in here. These will always just be the width and height of the viewport. */
    @Setter
    protected static int width;
    @Setter
    protected static int height;
    protected final YodiGame game;

    protected Screen(YodiGame game) {
        this.game = game;
        this.init();
    }

    /**
     * Called when the screen is created/shown.
     */
    public void init() { }

    /**
     * Called every game update.
     */
    public void update() { }

    /**
     * Called to render the screen.
     */
    public abstract void render();

    /**
     * Called whenever a key is pressed on the keyboard.
     * @param keyCode The GLFW key code of the key that was pressed.
     */
    public void keyCallback(int keyCode) { }

    /**
     * @return True if the game should be rendered under the screen.
     */
    public boolean isTranslucent() {
        return false;
    }
}
