package org.appledash.noodel.util;

import org.appledash.noodel.Snake;
import org.jetbrains.annotations.NotNull;

public final class Mth {
    private Mth() {
    }

    public static int ceil(float f) {
        return (int) Math.ceil(f);
    }

    // Yeah, this is in here, because I'm too lazy to make a new class for it.
    public static @NotNull Snake.Direction adjacency(Vec2 a, Vec2 b) {
        if (a.x() == b.x()) {
            if (b.y() > a.y()) {
                return Snake.Direction.UP;
            } else {
                return Snake.Direction.DOWN;
            }
        } else if (a.y() == b.y()) {
            if (b.x() > a.x()) {
                return Snake.Direction.RIGHT;
            } else {
                return Snake.Direction.LEFT;
            }
        }

        throw new IllegalArgumentException("Vectors are not adjacent");
    }
}
