package org.appledash.noodel.util;

import lombok.Getter;

public class FrameCounter {
    private static final float WINDOW = 10.0F;

    private int updateCount;
    private long runningTotal;
    @Getter
    private float averageFrameTime = -1;

    public void update(long lastFrameTime) {
        this.runningTotal += lastFrameTime;
        this.updateCount++;

        if (this.updateCount == WINDOW) {
            this.averageFrameTime = this.runningTotal / WINDOW;
            this.updateCount = 0;
            this.runningTotal = 0;
        }
    }

    public int getAverageFPS() {
        return (int) (1.0e9 / this.averageFrameTime);
    }
}
