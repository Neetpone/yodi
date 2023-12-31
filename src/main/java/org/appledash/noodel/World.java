package org.appledash.noodel;

import lombok.Getter;
import org.appledash.noodel.util.Vec2;

import java.util.*;

public class World {
    private final int tilesX;
    private final int tilesY;

    private final Random random = new Random();
    private final Set<Vec2> apples = new HashSet<>();

    private boolean wantReset;
    @Getter
    private Snake snake;
    @Getter
    private int score;

    public World(int tilesX, int tilesY) {
        this.tilesX = tilesX;
        this.tilesY = tilesY;
    }

    public void update() {
        if (this.wantReset) {
            this.reset();
            this.wantReset = false;
        }

        Vec2 nextPos = this.snake.getNextPos();

        /* About to collide with the border */
        if (nextPos.x() == 0 || nextPos.x() == (this.tilesX - 1) || nextPos.y() == 0 || nextPos.y() == (this.tilesY - 1)) {
            this.wantReset = true;
            return;
        }

        boolean hasEaten = false;
        for (Iterator<Vec2> iter = this.apples.iterator(); iter.hasNext();) {
            Vec2 applePos = iter.next();
            if (this.snake.isIntersectingWith(applePos)) {
                iter.remove();
                hasEaten = true;
                break;
            }
        }

        this.snake.move(hasEaten);

        if (hasEaten) {
            this.score += 50;
            this.spawnApple();
        }

        /* this is a little weird, but the reason I do it this way is that the snake's path is a linked list,
         * and iterating like this is faster than using a bounded indexed for loop. */
        int pathSize = this.snake.getPath().size();
        if (this.snake.getPath()
                .stream()
                .skip(1).limit(pathSize - 2)
                .anyMatch(pos -> pos.equals(nextPos))) {
            this.wantReset = true;
        }
    }

    public void reset() {
        this.score = 0;
        this.snake = new Snake(Vec2.of(this.tilesX / 2, this.tilesY / 2));
        this.apples.clear();
        for (int i = 0; i < 5; i++) {
            this.spawnApple();
        }
    }

    public Set<Vec2> getApples() {
        return Collections.unmodifiableSet(this.apples);
    }

    public boolean wantsReset() {
        return this.wantReset;
    }

    private Vec2 findApplePos() {
        Vec2 pos;

        // FIXME: This will infinite loop if there are no more spaces to place an apple
        do {
            pos = new Vec2((1 + this.random.nextInt(this.tilesX - 2)), (1 + this.random.nextInt(this.tilesY - 2)));
        } while (this.apples.contains(pos) && this.snake.getPath().contains(pos)); /* Avoid placing an apple where there is already an apple or the snake */

        return pos;
    }

    private void spawnApple() {
        this.apples.add(this.findApplePos());
    }
}
