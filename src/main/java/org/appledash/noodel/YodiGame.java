package org.appledash.noodel;

import lombok.Getter;
import org.appledash.noodel.render.FontRenderer;
import org.appledash.noodel.render.Tesselator2D;
import org.appledash.noodel.render.gl.VertexFormat;
import org.appledash.noodel.render.util.RenderState;
import org.appledash.noodel.render.util.RenderUtil;
import org.appledash.noodel.screen.Screen;
import org.appledash.noodel.screen.type.GameOverScreen;
import org.appledash.noodel.screen.type.PauseScreen;
import org.appledash.noodel.texture.SpriteSheet;
import org.appledash.noodel.texture.Terrain;
import org.appledash.noodel.texture.Texture2D;
import org.appledash.noodel.util.FrameCounter;
import org.appledash.noodel.util.Mth;
import org.appledash.noodel.util.Vec2;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public final class YodiGame implements AutoCloseable {
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

    @Getter
    private final World world = new World(TILES_X, TILES_Y);
    private final FrameCounter frameCounter = new FrameCounter();

    private final GameWindow window;
    private final SpriteSheet spriteSheet;
    @Getter
    private final FontRenderer fontRenderer;
    private Screen currentScreen;

    private long lastUpdate = -1;
    private long updateCount;

    public YodiGame() {
        this.window = new GameWindow(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        this.window.setKeyCallback(this::keyCallback);
        this.window.setSizeCallback(this::resizeCallback);

        this.window.centerOnScreen();
        this.window.makeContextCurrent();
        this.window.show();

        glfwSwapInterval(0); // vsync
        GL.createCapabilities();
        glBindVertexArray(glGenVertexArrays());

        this.resizeCallback(this.window.getWindowId(), DEFAULT_WIDTH, DEFAULT_HEIGHT);

        this.spriteSheet = new SpriteSheet(Texture2D.fromResource("textures/terrain.png"), 16, 16);
        this.fontRenderer = new FontRenderer(new Font("Verdana", Font.PLAIN, 36));
        this.world.reset();

        glUniform1i(RenderUtil.POSITION_TEXTURE.getUniformLocation("textureSampler"), 0);
    }

    @Override
    public void close() {
        Tesselator2D.INSTANCE.delete();
        this.spriteSheet.delete();
        this.window.delete();
    }

    private void mainLoop() {
        // A nice green, for the background
        glClearColor(158 / 255F, 203 / 255F, 145 / 255F, 1.0F);

        while (!this.window.shouldClose()) {
            long frameStart = System.nanoTime();

            if (((frameStart - this.lastUpdate) >= (UPDATE_INTERVAL * 1.0e6))) {
                if (this.currentScreen != null) {
                    this.currentScreen.update();
                } else {
                    this.update();
                }
                this.lastUpdate = frameStart;
            }

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            RenderState.resetTranslations();

            if (this.currentScreen == null || this.currentScreen.isTranslucent()) {
                this.renderGame();
            }

            if (this.currentScreen != null) {
                this.currentScreen.render();
            }

            glfwSwapBuffers(this.window.getWindowId());
            glfwPollEvents();

            this.frameCounter.update(System.nanoTime() - frameStart);
        }
    }

    private void update() {
        this.world.update();

        if (this.world.wantsReset() && this.currentScreen == null) {
            this.currentScreen = new GameOverScreen(this);
        }

        this.updateCount++;
    }

    private void renderGame() {
        Tesselator2D tess = Tesselator2D.INSTANCE;
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        /* Render the border */
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

        RenderState.color(158, 203, 145, 255); // This is a nice green
        this.spriteSheet.getTexture().bind();
        tess.draw(GL_TRIANGLES, RenderUtil.POSITION_TEXTURE);

        /* Render the apples and the Yodi */
        tess.begin(VertexFormat.POSITION_TEXTURE_2D);

        this.drawTiles(tess, this.world.getApples(), Terrain.BREAD);
        this.drawYoditax(tess, this.world.getSnake());

        RenderState.color(1.0F, 1.0F, 1.0F, 1.0F);
        tess.draw(GL_TRIANGLES, RenderUtil.POSITION_TEXTURE);

        /* Render the score and FPS */
        RenderState.pushMatrix();
        // RenderState.translate(2, SCALED_HEIGHT - FontRenderer.SCALED_FONT_HEIGHT - 2, 0);
        RenderState.scale(2, 2, 1);

        this.fontRenderer.drawString("FPS: " + this.frameCounter.getAverageFPS(), 2, 0);
        this.fontRenderer.drawString("Score: " + this.world.getScore(), 2, FontRenderer.SCALED_FONT_HEIGHT);
        RenderState.popMatrix();
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
            case UP -> Terrain.YODI_HEAD_U;
            case DOWN -> Terrain.YODI_HEAD_D;
            case LEFT -> Terrain.YODI_HEAD_R;
            case RIGHT -> Terrain.YODI_HEAD_L;
        };
        this.drawTile(tess, headPos.x(), headPos.y(), headTexture - textureSubtrahend);

        // Draw the middle segments
        for (int i = 1; i < count - 1; i++) {
            Vec2 segment = segments.get(i);
            Vec2 nextSegment = segments.get(i + 1);

            Snake.Direction nextFacing = Mth.adjacency(segment, nextSegment);

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
            case UP -> Terrain.YODI_TAIL_U;
            case DOWN -> Terrain.YODI_TAIL_D;
            case LEFT -> Terrain.YODI_TAIL_R;
            case RIGHT -> Terrain.YODI_TAIL_L;
        };

        // this.drawTile(tess, 0, 0, Terrain.TNT);

        this.drawTile(tess, segments.get(count - 1).x(), segments.get(count - 1).y(), tailTexture - textureSubtrahend);
    }

    // This is kind of WTF because the directions of the args are the directions
    // the snake is going, but the directions of the angled textures are the directions they connect on.
    private int computeTexture(Snake.Direction incoming, Snake.Direction outgoing) {
        if (incoming == Snake.Direction.UP) {
            if (outgoing == Snake.Direction.UP) {
                return Terrain.YODI_MIDDLE_U;
            } else if (outgoing == Snake.Direction.RIGHT) {
                return Terrain.YODI_ANGLE_UR;
            } else if (outgoing == Snake.Direction.LEFT) {
                return Terrain.YODI_ANGLE_UL;
            }
        } else if (incoming == Snake.Direction.DOWN) {
            if (outgoing == Snake.Direction.DOWN) {
                return Terrain.YODI_MIDDLE_U;
            } else if (outgoing == Snake.Direction.RIGHT) {
                return Terrain.YODI_ANGLE_DR;
            } else if (outgoing == Snake.Direction.LEFT) {
                return Terrain.YODI_ANGLE_DL;
            }
        } else if (incoming == Snake.Direction.RIGHT) {
            if (outgoing == Snake.Direction.LEFT) {
                return Terrain.YODI_MIDDLE_R;
            } else if (outgoing == Snake.Direction.UP) {
                return Terrain.YODI_ANGLE_DL;
            } else if (outgoing == Snake.Direction.DOWN) {
                return Terrain.YODI_ANGLE_UL;
            }
        } else if (incoming == Snake.Direction.LEFT) {
            if (outgoing == Snake.Direction.RIGHT) {
                return Terrain.YODI_MIDDLE_R;
            } else if (outgoing == Snake.Direction.UP) {
                return Terrain.YODI_ANGLE_DR;
            } else if (outgoing == Snake.Direction.DOWN) {
                return Terrain.YODI_ANGLE_UR;
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
        tess.vertex(x, y).texture(u / tW, (v) / tH).next();
        tess.vertex(x + TILE_SIZE, y).texture((u + sW) / tW, (v) / tH).next();
        tess.vertex(x, y + TILE_SIZE).texture(u / tW, (v+ sH) / tH).next();

        tess.vertex(x, y + TILE_SIZE).texture(u / tW, (v+ sH) / tH).next();
        tess.vertex(x + TILE_SIZE, y).texture((u + sW) / tW, (v) / tH).next();
        tess.vertex(x + TILE_SIZE, y + TILE_SIZE).texture((u + sW) / tW, (v+ sH) / tH).next();
    }

    private void drawTiles(Tesselator2D tess, Iterable<Vec2> tiles, int blockId) {
        for (Vec2 pos : tiles) {
            this.drawTile(tess, pos.x(), pos.y(), blockId);
        }
    }

    private void resizeCallback(long window, int width, int height) {
        glViewport(0, 0, width, height);
        RenderState.ortho(0, SCALED_WIDTH, SCALED_HEIGHT, 0, -1, 1);
        Screen.setWidth(SCALED_WIDTH);
        Screen.setHeight(SCALED_HEIGHT);
    }

    private void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (action != GLFW_PRESS) {
            return;
        }

        if (this.currentScreen != null) {
            this.currentScreen.keyCallback(key);
            return;
        }

        if (key == GLFW_KEY_ESCAPE) {
            this.currentScreen = new PauseScreen(this);
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

    public void resetGame() {
        this.world.reset();
        this.currentScreen = null;
    }

    public void closeScreen() {
        this.currentScreen = null;
    }

    public static void main(String[] args) {
        try {
            GLFWErrorCallback.createPrint(System.err).set();

            if (!glfwInit()) {
                throw new IllegalStateException("Failed to initialize GLFW");
            }

            try (YodiGame yodiGame = new YodiGame()) {
                yodiGame.mainLoop();
            }
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
