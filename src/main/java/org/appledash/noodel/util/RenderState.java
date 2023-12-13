package org.appledash.noodel.util;

import lombok.Getter;
import lombok.experimental.UtilityClass;

/**
 * Class to store global render state, like the old OpenGL fixed function pipeline.
 */
@UtilityClass
public class RenderState {
    @Getter
    private static final float[] color = new float[]{1, 1, 1, 1};

    public static void color(float r, float g, float b, float a) {
        color[0] = r;
        color[1] = g;
        color[2] = b;
        color[3] = a;
    }

    public static void color(int r, int g, int b, int a) {
        color(r / 255f, g / 255f, b / 255f, a / 255f);
    }
}
