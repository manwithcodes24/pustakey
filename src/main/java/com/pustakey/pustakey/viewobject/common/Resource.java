package com.pustakey.pustakey.viewobject.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import static com.pustakey.pustakey.viewobject.common.Status.SUCCESS;
import static com.pustakey.pustakey.viewobject.common.Status.ERROR;
import static com.pustakey.pustakey.viewobject.common.Status.LOADING;

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
public class Resource<T> {

    @NonNull
    public final Status status;

    @Nullable
    public final String message;

    @Nullable
    public final T data;

    public Resource(@NonNull Status status, @Nullable T data, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> Resource<T> success(@Nullable T data) {
        return new Resource<>(SUCCESS, data, null);
    }

    public static <T> Resource<T> successWithMsg(String msg, @Nullable T data) {
        return new Resource<>(SUCCESS, data, msg);
    }

    public static <T> Resource<T> error(String msg, @Nullable T data) {
        return new Resource<>(ERROR, data, msg);
    }

    public static <T> Resource<T> loading(@Nullable T data) {
        return new Resource<>(LOADING, data, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Resource<?> resource = (Resource<?>) o;

        return status == resource.status && (message != null ? message.equals(resource.message) : resource.message == null) && (data != null ? data.equals(resource.data) : resource.data == null);
    }

    @Override
    public int hashCode() {
        int result = status.hashCode();
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
