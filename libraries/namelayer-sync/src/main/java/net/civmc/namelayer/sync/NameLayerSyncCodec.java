package net.civmc.namelayer.sync;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class NameLayerSyncCodec {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private NameLayerSyncCodec() {
    }

    public static byte[] encodeWriteRequest(final NameLayerWriteRequest request) {
        return encode(request);
    }

    public static NameLayerWriteRequest decodeWriteRequest(final byte[] body) {
        return decode(body, NameLayerWriteRequest.class);
    }

    public static byte[] encodeWriteResponse(final NameLayerWriteResponse response) {
        return encode(response);
    }

    public static NameLayerWriteResponse decodeWriteResponse(final byte[] body) {
        return decode(body, NameLayerWriteResponse.class);
    }

    public static byte[] encodeInvalidation(final NameLayerInvalidationMessage invalidation) {
        return encode(invalidation);
    }

    public static NameLayerInvalidationMessage decodeInvalidation(final byte[] body) {
        return decode(body, NameLayerInvalidationMessage.class);
    }

    private static byte[] encode(final Object message) {
        Objects.requireNonNull(message, "message");
        return GSON.toJson(message).getBytes(StandardCharsets.UTF_8);
    }

    private static <T> T decode(final byte[] body, final Class<T> messageType) {
        Objects.requireNonNull(body, "body");
        Objects.requireNonNull(messageType, "messageType");
        try {
            return GSON.fromJson(new String(body, StandardCharsets.UTF_8), messageType);
        } catch (final JsonParseException exception) {
            throw new IllegalArgumentException("Invalid NameLayer synchronization message", exception);
        }
    }
}
