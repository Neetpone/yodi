package org.appledash.noodel.render.gl;

import org.appledash.noodel.render.util.RenderState;

import static org.lwjgl.opengl.GL20.*;

/**
 * Represents some standard uniform locations a shader program may or may not have.
 * <p>
 *     State values are pulled from {@link RenderState}.
 *     If you call {@link #upload()}, the current values in {@link RenderState} will be uploaded to the GPU.
 * </p>
 */
public class StandardUniforms {
    private final int modelViewMatrixLocation;
    private final int projectionMatrixLocation;
    private final int colorLocation;

    public StandardUniforms(int programId) {
        this.modelViewMatrixLocation = glGetUniformLocation(programId, "ModelViewMatrix");
        this.projectionMatrixLocation = glGetUniformLocation(programId, "ProjectionMatrix");
        this.colorLocation = glGetUniformLocation(programId, "Color");
    }

    public void upload() {
        if (this.modelViewMatrixLocation != -1) {
            glUniformMatrix4fv(this.modelViewMatrixLocation, false, RenderState.getModelViewMatrix());
        }

        if (this.projectionMatrixLocation != -1) {
            glUniformMatrix4fv(this.projectionMatrixLocation, false, RenderState.getProjectionMatrix());
        }

        if (this.colorLocation != -1) {
            glUniform4fv(this.colorLocation, RenderState.getColor());
        }
    }
}
