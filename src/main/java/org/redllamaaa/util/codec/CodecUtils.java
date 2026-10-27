package org.redllamaaa.util.codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stream codec helpers for Optional and List values on a plain ByteBuf.
 * The wire format is a boolean presence flag for Optional, and an int length prefix for List.
 */
public final class CodecUtils {

    private CodecUtils() {
    }

    public static <U> StreamCodec<ByteBuf, Optional<U>> optionalStreamCodecByteBuf(StreamCodec<ByteBuf, U> codec) {
        return new StreamCodec<>() {
            @Override
            public Optional<U> decode(ByteBuf buf) {
                if (buf.readBoolean()) {
                    return Optional.of(codec.decode(buf));
                }
                return Optional.empty();
            }

            @Override
            public void encode(ByteBuf buf, Optional<U> value) {
                if (value.isPresent()) {
                    buf.writeBoolean(true);
                    codec.encode(buf, value.get());
                } else {
                    buf.writeBoolean(false);
                }
            }
        };
    }

    public static <T> StreamCodec<ByteBuf, List<T>> listStreamCodecByteBuf(StreamCodec<ByteBuf, T> codec) {
        return new StreamCodec<>() {
            @Override
            public List<T> decode(ByteBuf buf) {
                int length = buf.readInt();
                List<T> list = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    list.add(codec.decode(buf));
                }
                return list;
            }

            @Override
            public void encode(ByteBuf buf, List<T> list) {
                buf.writeInt(list.size());
                for (T t : list) {
                    codec.encode(buf, t);
                }
            }
        };
    }

}
