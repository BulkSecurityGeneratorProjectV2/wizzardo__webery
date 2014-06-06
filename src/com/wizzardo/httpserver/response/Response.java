package com.wizzardo.httpserver.response;

import com.wizzardo.epoll.readable.ReadableData;
import com.wizzardo.httpserver.ReadableBuilder;
import com.wizzardo.httpserver.request.Header;

import java.util.Arrays;

/**
 * @author: wizzardo
 * Date: 3/31/14
 */
public class Response {
    private static final byte[] LINE_SEPARATOR = "\r\n".getBytes();
    private static final byte[] HEADER_SEPARATOR = ": ".getBytes();

    private byte[][] headers = new byte[20][];
    private int headersCount = 0;

    private Status status = Status._200;
    private byte[] body;

    public Response setBody(byte[] body) {
        this.body = body;
        setHeader(Header.KEY_CONTENT_LENGTH, String.valueOf(body.length));
        return this;
    }

    public Response setStatus(Status status) {
        this.status = status;
        return this;
    }

    public Response setHeader(String key, String value) {
        return setHeader(key.getBytes(), value.getBytes());
    }

    public Response setHeader(Header key, String value) {
        return setHeader(key.bytes, value.getBytes());
    }

    public Response setHeader(Header key, Header value) {
        return setHeader(key.bytes, value.bytes);
    }

    public Response setHeader(byte[] key, byte[] value) {
        for (int i = 0; i < headersCount; i += 2) {
            if (Arrays.equals(key, headers[i])) {
                headers[i + 1] = value;
                return this;
            }
        }
        appendHeader(key, value);
        return this;
    }

    public Response appendHeader(String key, String value) {
        return appendHeader(key.getBytes(), value.getBytes());
    }

    public Response appendHeader(Header key, String value) {
        return appendHeader(key.bytes, value.getBytes());
    }

    public Response appendHeader(Header key, Header value) {
        return appendHeader(key.bytes, value.bytes);
    }

    public Response appendHeader(byte[] key, byte[] value) {
        if (headersCount + 1 >= headers.length)
            increaseHeadersSize();

        headers[headersCount++] = key;
        headers[headersCount++] = value;

        return this;
    }

    private void increaseHeadersSize() {
        byte[][] temp = new byte[headers.length * 3 / 2][];
        System.arraycopy(headers, 0, temp, 0, headers.length);
        headers = temp;
    }

    public ReadableData toReadableBytes() {
        ReadableBuilder builder = new ReadableBuilder(status.header);
        for (int i = 0; i < headersCount; i += 2) {
            builder.append(headers[i])
                    .append(HEADER_SEPARATOR)
                    .append(headers[i + 1])
                    .append(LINE_SEPARATOR);
        }

        builder.append(LINE_SEPARATOR);
        builder.append(body);
        return builder;
    }
}
