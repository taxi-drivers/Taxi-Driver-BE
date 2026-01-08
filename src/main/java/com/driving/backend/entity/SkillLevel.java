package com.driving.backend.entity;

/**
 * 사용자 숙련도 레벨 enum
 */
public enum SkillLevel {
    BEGINNER("초보"),
    INTERMEDIATE("중급"),
    ADVANCED("숙련");

    private final String description;

    SkillLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
