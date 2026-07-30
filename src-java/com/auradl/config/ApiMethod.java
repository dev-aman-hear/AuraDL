package com.auradl.config;

public enum ApiMethod {
    COOKIES("cookies-file"),
    MEDIA_USER_TOKEN("media-user-token"),
    WRAPPER("wrapper");

    private final String value;

    ApiMethod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ApiMethod fromString(String text) {
        if (text != null) {
            for (ApiMethod method : ApiMethod.values()) {
                if (method.value.equalsIgnoreCase(text) || text.equalsIgnoreCase("cookies") || text.equalsIgnoreCase("browser-cookies")) {
                    return method;
                }
            }
        }
        return COOKIES;
    }
}
