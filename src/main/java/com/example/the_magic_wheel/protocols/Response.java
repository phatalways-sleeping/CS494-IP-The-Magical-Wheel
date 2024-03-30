package com.example.the_magic_wheel.protocols;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class Response implements Serializable, ToBytes {
    private Header header;
    private Body body;

    public Response(Header header, Body body) {
        this.header = header;
        this.body = body;
    }

    public static Response deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
        ObjectInputStream objectStream = new ObjectInputStream(byteStream);
        return (Response) objectStream.readObject();
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "Response{" +
                "header=" + header +
                ", body=" + body +
                '}';
    }

    public static class Header implements Serializable {
        private Status status;
        private String serverName;
        private ContentType contentType;

        private Header(Builder builder) {
            this.status = builder.status;
            this.serverName = builder.serverName;
            this.contentType = builder.contentType;
        }

        public Status getStatus() {
            return status;
        }

        public String getServerName() {
            return serverName;
        }

        public ContentType getContentType() {
            return contentType;
        }

        @Override
        public String toString() {
            return "Header{" +
                    "status=" + status +
                    ", serverName='" + serverName + '\'' +
                    ", contentType=" + contentType +
                    '}';
        }
    }

    public static class Body implements Serializable {
        private String content;

        public Body(Builder builder) {
            this.content = builder.content;
        }

        public String getContent() {
            return content;
        }

        @Override
        public String toString() {
            return "Body{" +
                    "content='" + content + '\'' +
                    '}';
        }
    }

    public static class Builder {
        private Status status;
        private String serverName;
        private ContentType contentType;
        private String content;

        public Builder() {
        }

        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        public Builder serverName(String serverName) {
            this.serverName = serverName;
            return this;
        }

        public Builder contentType(ContentType contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Response build() {
            return new Response(new Header(this), new Body(this));
        }
    }

    public static enum Status {
        OK, ERROR
    }

    public static enum ContentType {
        RESULT,
        RANK,
        ACK,
    }
}
