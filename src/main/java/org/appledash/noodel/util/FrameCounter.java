package org.appledash.noodel.util;

import lombok.Getter;

public class FrameCounter {
    private static final long WINDOW = 10;

    private int updateCount;
    private long runningTotal;
    @Getter
    private int averageFrameTime = -1;

    public void update(long lastFrameTime) {
        this.runningTotal += lastFrameTime;
        this.updateCount++;

        if (this.updateCount == WINDOW) {
            this.averageFrameTime = Math.toIntExact(this.runningTotal / WINDOW);
            this.updateCount = 0;
            this.runningTotal = 0;
        }
    }

    public int getAverageFPS() {
        return 1000 / this.averageFrameTime;
    }
}
