package com.velox.framework.id.api.codec;

public interface IdCodec {

    String getName();

    String encode(String sourceId);

    String decode(String encodedId);
}
