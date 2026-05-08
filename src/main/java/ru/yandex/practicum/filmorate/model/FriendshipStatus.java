package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum FriendshipStatus {
    PENDING(1, "Неподтвержденная"),
    CONFIRMED(2, "Подтвержденная");

    private final int id;
    private final String name;

    FriendshipStatus(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static FriendshipStatus valueOf(int id) {
        for (FriendshipStatus status : values()) {
            if (status.id == id) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown FriendshipStatus id: " + id);
    }

    @JsonCreator
    public static FriendshipStatus fromObject(@JsonProperty("id") int id) {
        return valueOf(id);
    }
}
