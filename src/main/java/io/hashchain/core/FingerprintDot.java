package io.hashchain.core;

public class FingerprintDot {
    private int x_position;
    private int y_position;
    private FingerprintType type;
    private int quality;

    public FingerprintDot(int x_position, int y_position, FingerprintType type, int quality) {
        this.x_position = x_position;
        this.y_position = y_position;
        this.type = type;
        this.quality = quality;
    }

    public int getX_position() { return x_position; }
    public int getY_position() { return y_position; }
    public FingerprintType getType() { return type; }
    public int getQuality() { return quality; }

    public void setX_position(int x_position) { this.x_position = x_position; }
    public void setY_position(int y_position) { this.y_position = y_position; }
    public void setType(FingerprintType type) { this.type = type; }
    public void setQuality(int quality) { this.quality = quality; }

    @Override
    public String toString() {
        return String.format("Fingerprint[x=%d, y=%d, type=%s, quality=%d]", x_position, y_position, type, quality);
    }
}
