package me.hypherionmc.sdlinklib.utils;

public class Result {

    enum Type {
        ERROR,
        SUCCESS
    }

    private final Type type;
    private final String message;

    private Result(Type type, String message) {
        this.type = type;
        this.message = message;
    }

    public static Result success(String message) {
        return new Result(Type.SUCCESS, message);
    }

    public static Result error(String message) {
        return new Result(Type.ERROR, message);
    }

    public boolean isError() {
        return this.type == Type.ERROR;
    }

    public String getMessage() {
        return message;
    }
}
