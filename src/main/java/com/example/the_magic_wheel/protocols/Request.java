package com.example.the_magic_wheel.protocols;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Date;

public class Request implements Serializable, ToBytes {
    private Header header;
    private Body body;

    public Request(Header header, Body body) {
        this.header = header;
        this.body = body;
    }

    public static Request deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
        ObjectInputStream objectStream = new ObjectInputStream(byteStream);
        return (Request) objectStream.readObject();
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
        return "Request{" +
                "header=" + header +
                ", body=" + body +
                '}';
    }

    public static class Header implements Serializable {
        private Method method;
        private ContentType contentType;
        private String clientName;
        private Date date;

        private Header(Builder builder) {
            this.method = builder.method;
            this.clientName = builder.clientName;
            this.date = builder.date;
            this.contentType = builder.contentType;
        }

        public Method getMethod() {
            return method;
        }

        public String getClientName() {
            return clientName;
        }

        public ContentType getContentType() {
            return contentType;
        }

        public Date getDate() {
            return date;
        }

        @Override
        public String toString() {
            return "Header{" +
                    "method=" + method +
                    ", contentType=" + contentType +
                    ", clientName='" + clientName + '\'' +
                    ", date=" + date +
                    '}';
        }
    }

    public static class Body implements Serializable {
        private String content;

        private Body(Builder builder) {
            this.content = builder.content;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return "Body{" +
                    "content='" + content + '\'' +
                    '}';
        }
    }

    public static class Builder {
        private Method method;
        private ContentType contentType;
        private Date date;
        private String clientName;
        private String content;

        public Builder() {
            this.date = new Date();
        }

        public Builder method(Method method) {
            this.method = method;
            return this;
        }

        public Builder clientName(String clientName) {
            this.clientName = clientName;
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

        public Request build() {
            return new Request(new Header(this), new Body(this));
        }
    }

    public static enum Method {
        SPAWN, // request for new socket channel
        READ, // request for reading data from socket channel
        WRITE, // request for writing data to socket channel
        CLOSE // request for closing socket channel
    }

    public static enum ContentType {
        USERNAME, // username
        GUESS_CHAR, // guessed character
        GUESS_WORD, // guessed word with the character
        CLOSE, // close the connection
    }

}
