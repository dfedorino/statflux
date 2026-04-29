package com.rmrf.statflux.domain.constant;

import lombok.Getter;

/**
 * Enum поддерживаемых платформ для видео
 */
@Getter
public enum Platform {
    YOUTUBE("YouTube"), VK("VK"), RUTUBE("RuTube");

    private final String displayName;

    Platform(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
