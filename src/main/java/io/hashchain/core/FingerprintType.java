package io.hashchain.core;

public enum FingerprintType {
    DOT("Точка"),
    CORE("Ядро"),
    DELTA("Дельта");

    private final String description;

    FingerprintType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
