package vg.civcraft.mc.citadel.model;

public record WorldBorderBuffers(double centerX, double centerZ, Shape borderShape, double bufferSize, boolean decay) {

    public enum Shape {
        CIRCLE,
        SQUARE;
    }

    private boolean checkIfOutsideCircle(double x, double z) {
        return (x - centerX) * (x - centerX) + (z - centerZ) * (z - centerZ) > bufferSize * bufferSize;
    }

    private boolean checkIfOutsideSquare(double x, double z) {
        return Math.abs(x) > (centerX + bufferSize) || Math.abs(z) > (centerZ + bufferSize);
    }

    public boolean checkIfOutside(double x, double z) {
        switch (this.borderShape) {
            case CIRCLE -> {
                return checkIfOutsideCircle(x, z);
            }
            case SQUARE -> {
                return checkIfOutsideSquare(x, z);
            }
        }
        return false;
    }
}


