package org.appledash.noodel.render.util;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.appledash.noodel.render.gl.ShaderProgram;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

/**
 * Class to store global render state, like the old OpenGL fixed function pipeline.
 * Some of us are more comfortable with this, and it's easier to use than passing around a bunch of parameters.
 * {@link ShaderProgram} pulls the values from here to set uniforms.
 * <p>
 * e.g: If you call {@code RenderState.color(1, 0, 0, 1)}, then the next time you call {@code ShaderProgram#draw()},
 * the uniform {@code Color}, if it exists in the shader, will be set to {@code vec4(1, 0, 0, 1)}.
 */
@UtilityClass
@SuppressWarnings("MismatchedReadAndWriteOfArray") /* IntelliJ is too stupid to see through @Getter */
public class RenderState {
    @Getter
    private static final float[] color = new float[]{1, 1, 1, 1};

    private static final FloatBuffer PROJECTION_MATRIX_BUFFER = BufferUtils.createFloatBuffer(16);
    private static final Matrix4f projectionMatrix = new Matrix4f().identity();
    private static final FloatBuffer MODEL_VIEW_MATRIX_BUFFER = BufferUtils.createFloatBuffer(16);
    private static final Matrix4fStack modelViewMatrixStack = new Matrix4fStack(8);

    public static void color(float r, float g, float b, float a) {
        color[0] = r;
        color[1] = g;
        color[2] = b;
        color[3] = a;
    }

    public static void color(int r, int g, int b, int a) {
        color(r / 255f, g / 255f, b / 255f, a / 255f);
    }

    public static void ortho(float left, float right, float bottom, float top, float near, float far) {
        projectionMatrix.identity().ortho(left, right, bottom, top, near, far);
    }

    public static FloatBuffer getProjectionMatrix() {
        return projectionMatrix.get(PROJECTION_MATRIX_BUFFER);
    }

    public static void pushMatrix() {
        modelViewMatrixStack.pushMatrix();
    }

    public static void resetTranslations() {
        modelViewMatrixStack.identity();
    }

    public static void translate(float x, float y, float z) {
        modelViewMatrixStack.translate(x, y, z);
    }

    public static void scale(float x, float y, float z) {
        modelViewMatrixStack.scale(x, y, z);
    }

    public static void popMatrix() {
        modelViewMatrixStack.popMatrix();
    }

    public static FloatBuffer getModelViewMatrix() {
        return modelViewMatrixStack.get(MODEL_VIEW_MATRIX_BUFFER);
    }
}
