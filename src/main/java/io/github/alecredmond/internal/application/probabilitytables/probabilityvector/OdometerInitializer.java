package io.github.alecredmond.internal.application.probabilitytables.probabilityvector;

import lombok.Data;

@Data
public class OdometerInitializer {
    private boolean[] lockedPositions;
    private int fastestPosition;
    private boolean fireOnlyOnce;
    private int baseStride;
    private int[] strideIfLocked;
    private int initialIndex;
}
