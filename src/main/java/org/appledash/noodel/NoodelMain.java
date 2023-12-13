package org.appledash.noodel;

import org.appledash.noodel.render.FontRenderer;
import org.appledash.noodel.render.RenderUtil;
import org.appledash.noodel.render.Tesselator2D;
import org.appledash.noodel.render.VertexFormat;
import org.appledash.noodel.texture.SpriteSheet;
import org.appledash.noodel.texture.Terrain;
import org.appledash.noodel.texture.Texture2D;
import org.appledash.noodel.util.FrameCounter;
import org.appledash.noodel.util.Mth;
import org.appledash.noodel.util.RenderState;
import org.appledash.noodel.util.Vec2;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public final class NoodelMain {
    /* the actual window size */
    private static final int DEFAULT_WIDTH = 1600;
    private static final int DEFAULT_HEIGHT = 1200;

    /* correspond to the values mapped in the shader */
    private static final int SCALED_WIDTH = 640;
    private static final int SCALED_HEIGHT = 480;

    private static final int TILE_SIZE = 16;
    private static final int TILES_X = SCALED_WIDTH / TILE_SIZE;
    private static final int TILES_Y = SCALED_HEIGHT / TILE_SIZE;
    private static final int UPDATE_INTERVAL = 150; /* in milliseconds */

    private final World world = new World(TILES_X, TILES_Y);
    private final FrameCounter frameCounter = new FrameCounter();

    private GameWindow window;
    private Texture2D gameOverTexture;

    private boolean paused;
    private boolean gameOver;
    private long lastUpdate = -1;
    private long updateCount;
    private SpriteSheet spriteSheet;
    private FontRenderer fontRenderer;

    private void init() {
        this.window = new GameWindow(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        this.window.setKeyCallback(this::keyCallback);
        this.window.setSizeCallback((window, width, height) ->
                glViewport(0, 0, width, height)
        );

        this.window.centerOnScreen();
        this.window.makeContextCurrent();
        glfwSwapInterval(1); // vsync
        this.window.show();

        GL.createCapabilities();

        glBindVertexArray(glGenVertexArrays());

        this.spriteSheet = new SpriteSheet(Texture2D.fromResource("textures/terrain.png"), 16, 16);
        this.gameOverTexture = Texture2D.fromResource("textures/game_over.png");
        this.fontRenderer = new FontRenderer(new Font("Verdana", Font.PLAIN, 9 * 4));
        this.world.reset();

        glUniform1i(RenderUtil.POSITION_TEXTURE.getUniformLocation("textureSampler"), 0);
    }

    private void mainLoop() {
        Tesselator2D tess = Tesselator2D.INSTANCE;

        glClearColor(158 / 255F, 203 / 255F, 145 / 255F, 1.0F);

        while (!this.window.shouldClose()) {
            long frameStart = System.currentTimeMillis();

            if (((frameStart - this.lastUpdate) >= UPDATE_INTERVAL) && !this.paused) {
                this.world.update();

                if (this.world.wantsReset()) {
                    this.gameOver = true;
                    this.paused = true;
                }
                this.lastUpdate = frameStart;
                this.updateCount++;
            }

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glEnable(GL_TEXTURE_2D);
            glActiveTexture(GL_TEXTURE0);

            if (this.gameOver) {
                RenderUtil.drawTexture2D(tess, this.gameOverTexture, 0, 0, SCALED_WIDTH, SCALED_HEIGHT);
            } else {
                tess.begin(VertexFormat.POSITION_TEXTURE_2D);

                // Top and bottom border
                for (int x = 0; x < TILES_X; x++) {
                    this.drawTile(tess, x, 0, Terrain.GRASS);
                    this.drawTile(tess, x, TILES_Y - 1, Terrain.GRASS);
                }

                // Left and right border
                for (int y = 1; y < TILES_Y - 1; y++) {
                    this.drawTile(tess, 0, y, Terrain.GRASS);
                    this.drawTile(tess, TILES_X - 1, y, Terrain.GRASS);
                }

                // Set the color for the border and render the border
                RenderState.color(158, 203, 145, 255); // This is a nice green
                this.spriteSheet.getTexture().bind();
                tess.draw(GL_TRIANGLES, RenderUtil.POSITION_TEXTURE);

                tess.begin(VertexFormat.POSITION_TEXTURE_2D);

                this.drawTiles(tess, this.world.getApples(), Terrain.BREAD);
                this.drawYoditax(tess, this.world.getSnake());

                // Set the color back to normal
                RenderState.color(1.0F, 1.0F, 1.0F, 1.0F);
                tess.draw(GL_TRIANGLES, RenderUtil.POSITION_TEXTURE);
                tess.reset();

                this.fontRenderer.drawString("FPS: " + this.frameCounter.getAverageFPS(), 0, 0);

                ///RenderUtil.drawTexture2D(tess, this.fontRenderer.getTexture(), 0, 0, SCALED_WIDTH, SCALED_HEIGHT);
            }

            glfwSwapBuffers(this.window.getWindowId());
            glfwPollEvents();

            this.frameCounter.update(System.currentTimeMillis() - frameStart);
        }

        // this.positionTextureShader.delete();
        this.gameOverTexture.delete();
    }

    private void drawYoditax(Tesselator2D tess, Snake yodi) {
        List<Vec2> segments = new ArrayList<>(yodi.getPath());
        Vec2 headPos = segments.get(0);
        Snake.Direction facing = Mth.adjacency(headPos, segments.get(1));

        int count = segments.size();
        int bodyCount = 0;
        // Use the other animation frame every other update
        int textureSubtrahend = (this.updateCount % 2 == 0) ? 0 : 64;

        int headTexture = switch (facing) {
            case UP -> Terrain.YODI_HEAD_D;
            case DOWN -> Terrain.YODI_HEAD_U;
            case LEFT -> Terrain.YODI_HEAD_R;
            case RIGHT -> Terrain.YODI_HEAD_L;
        };
        this.drawTile(tess, headPos.x(), headPos.y(), headTexture - textureSubtrahend);

        // Draw the middle segments
        for (int i = 1; i < count - 1; i++) {
            Vec2 segment = segments.get(i);
            Vec2 nextSegment = segments.get(i + 1);

            Snake.Direction nextFacing = Mth.adjacency(segment, nextSegment);

            if (nextFacing == null) {
                continue;
            }

            int texture = this.computeTexture(facing, nextFacing);

            // This deals with the flipping of the middle segments
            texture = switch (texture) {
                case Terrain.YODI_MIDDLE_R -> ((bodyCount % 2) == 0) ? Terrain.YODI_MIDDLE_R : Terrain.YODI_MIDDLE_L;
                case Terrain.YODI_MIDDLE_U -> ((bodyCount % 2) == 0) ? Terrain.YODI_MIDDLE_U : Terrain.YODI_MIDDLE_D;
                default -> texture;
            };

            this.drawTile(tess, segment.x(), segment.y(), texture - textureSubtrahend);

            facing = nextFacing;
            bodyCount++;
        }

        // Deal with the tail
        int tailTexture = switch (facing) {
            case UP -> Terrain.YODI_TAIL_D;
            case DOWN -> Terrain.YODI_TAIL_U;
            case LEFT -> Terrain.YODI_TAIL_R;
            case RIGHT -> Terrain.YODI_TAIL_L;
        };

        this.drawTile(tess, segments.get(count - 1).x(), segments.get(count - 1).y(), tailTexture - textureSubtrahend);
    }

    // This is kind of WTF because the directions of the args are the directions
    // the snake is going, but the directions of the angled textures are the directions they connect on.
    private int computeTexture(Snake.Direction incoming, Snake.Direction outgoing) {
        if (incoming == Snake.Direction.UP) {
            if (outgoing == Snake.Direction.UP) {
                return Terrain.YODI_MIDDLE_U;
            } else if (outgoing == Snake.Direction.RIGHT) {
                return Terrain.YODI_ANGLE_DR;
            } else if (outgoing == Snake.Direction.LEFT) {
                return Terrain.YODI_ANGLE_DL;
            }
        } else if (incoming == Snake.Direction.DOWN) {
            if (outgoing == Snake.Direction.DOWN) {
                return Terrain.YODI_MIDDLE_U;
            } else if (outgoing == Snake.Direction.RIGHT) {
                return Terrain.YODI_ANGLE_UR;
            } else if (outgoing == Snake.Direction.LEFT) {
                return Terrain.YODI_ANGLE_UL;
            }
        } else if (incoming == Snake.Direction.RIGHT) {
            if (outgoing == Snake.Direction.LEFT) {
                return Terrain.YODI_MIDDLE_R;
            } else if (outgoing == Snake.Direction.UP) {
                return Terrain.YODI_ANGLE_UL;
            } else if (outgoing == Snake.Direction.DOWN) {
                return Terrain.YODI_ANGLE_DL;
            }
        } else if (incoming == Snake.Direction.LEFT) {
            if (outgoing == Snake.Direction.RIGHT) {
                return Terrain.YODI_MIDDLE_R;
            } else if (outgoing == Snake.Direction.UP) {
                return Terrain.YODI_ANGLE_UR;
            } else if (outgoing == Snake.Direction.DOWN) {
                return Terrain.YODI_ANGLE_DR;
            }
        }

        return Terrain.YODI_MIDDLE_R;
    }

    private void drawTile(Tesselator2D tess, int tileX, int tileY, int blockID) {
        int uv = this.spriteSheet.getSpriteUV(blockID);
        int u = (uv >> Short.SIZE) & Short.MAX_VALUE;
        int v = uv & Short.MAX_VALUE;
        int x = tileX * TILE_SIZE;
        int y = tileY * TILE_SIZE;

        float tW = this.spriteSheet.getTexture().getWidth();
        float sW = this.spriteSheet.getSpriteWidth();
        float tH = this.spriteSheet.getTexture().getHeight();
        float sH = this.spriteSheet.getSpriteHeight();

        // Draw as two triangles
        tess.vertex(x, y).texture(u / tW, (v + sH) / tH).next();
        tess.vertex(x + TILE_SIZE, y).texture((u + sW) / tW, (v + sH) / tH).next();
        tess.vertex(x, y + TILE_SIZE).texture(u / tW, v / tH).next();

        tess.vertex(x, y + TILE_SIZE).texture(u / tW, v / tH).next();
        tess.vertex(x + TILE_SIZE, y).texture((u + sW) / tW, (v + sH) / tH).next();
        tess.vertex(x + TILE_SIZE, y + TILE_SIZE).texture((u + sW) / tW, v / tH).next();
    }

    private void drawTiles(Tesselator2D tess, Iterable<Vec2> tiles, int blockId) {
        for (Vec2 pos : tiles) {
            this.drawTile(tess, pos.x(), pos.y(), blockId);
        }
    }

    private void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
            if (this.gameOver) {
                this.gameOver = false;
            }

            this.paused = !this.paused;
        }

        if (action != GLFW_PRESS) {
            return;
        }

        if (this.paused) {
            return;
        }

        Snake snake = this.world.getSnake();

        Snake.Direction desiredDir = switch (key) {
            case GLFW_KEY_W -> Snake.Direction.UP;
            case GLFW_KEY_A -> Snake.Direction.LEFT;
            case GLFW_KEY_S -> Snake.Direction.DOWN;
            case GLFW_KEY_D -> Snake.Direction.RIGHT;
            default -> snake.direction;
        };

        if (desiredDir != snake.direction && desiredDir != snake.prevDirection.reverse()) {
            snake.setDirection(desiredDir);
        }
    }

    public static void main(String[] args) {
        NoodelMain noodelMain = new NoodelMain();
        try {
            GLFWErrorCallback.createPrint(System.err).set();

            if (!glfwInit()) {
                throw new IllegalStateException("Failed to initialize GLFW");
            }

            noodelMain.init();
            noodelMain.mainLoop();
        } catch (Exception e) {
            e.printStackTrace();
            alertError("Error!", e.getMessage());
        } finally {
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }

    private static void alertError(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }
}
