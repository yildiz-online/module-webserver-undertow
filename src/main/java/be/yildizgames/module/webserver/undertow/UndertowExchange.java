/*
 * This file is part of the Yildiz-Engine project, licenced under the MIT License  (MIT)
 *  Copyright (c) 2020-2024 Grégory Van den Borre
 *  More infos available: https://engine.yildiz-games.be
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 *  documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright
 *  notice and this permission notice shall be included in all copies or substantial portions of the  Software.
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 *  WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 *  OR COPYRIGHT  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 *  OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package be.yildizgames.module.webserver.undertow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Deque;
import java.util.Optional;

/**
 * @author Grégory Van den Borre
 */
class UndertowExchange {

    private final String serverName;

    UndertowExchange(String serverName) {
        this.serverName = serverName;
    }

    void respondErrorInternal(HttpServerExchange e, String message) {
        var headers = e.getResponseHeaders();
        e.setStatusCode(200);
        headers.put(Headers.CONTENT_TYPE, "text/html");
        headers.put(Headers.DATE, new Date().toString());
        headers.put(Headers.SERVER, this.serverName);
        headers.put(Headers.CONTENT_LENGTH, message.length());
        if (!e.isResponseStarted()) {
            e.setStatusCode(500);
        }
        e.getResponseSender().send(message);
    }

    void respondErrorNotFound(HttpServerExchange e, String message) {
        var headers = e.getResponseHeaders();
        e.setStatusCode(404);
        headers.put(Headers.CONTENT_TYPE, "text/html");
        headers.put(Headers.DATE, new Date().toString());
        headers.put(Headers.SERVER, this.serverName);
        headers.put(Headers.CONTENT_LENGTH, message.length());
        e.getResponseSender().send(message);
    }

    void respondHtml(HttpServerExchange e, String html) {
        sendText(html, "text/html", e);
    }

    void respondXml(HttpServerExchange e, String xml) {
        sendText(xml, "application/xml", e);
    }

    void respondText(HttpServerExchange e, String body) {
        sendText(body, "text/plain", e);
    }

    void respondJson(HttpServerExchange e, Object o) {
        try {
            var content = new ObjectMapper().writeValueAsString(o);
            sendText(content, "application/json", e);
        } catch (IOException ex) {
            System.getLogger(UndertowExchange.class.getName()).log(System.Logger.Level.ERROR, "", ex);
            this.respondErrorInternal(e, "An error has occurred.");
        }
    }

    void respondYaml(HttpServerExchange e, Object o) {
        try {
            var content = new ObjectMapper(new YAMLFactory()).writeValueAsString(o);
            sendText(content, "application/yaml", e);
        } catch (IOException ex) {
            System.getLogger(UndertowExchange.class.getName()).log(System.Logger.Level.ERROR, "", ex);
            this.respondErrorInternal(e, "An error has occurred.");
        }
    }

    void respondFile(HttpServerExchange e, Path file) {
        try {
            var headers = e.getResponseHeaders();
            e.setStatusCode(200);
            headers.put(Headers.CONTENT_TYPE, "application/octet-stream;charset=UTF-8");
            headers.put(Headers.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"");
            headers.put(Headers.DATE, new Date().toString());
            headers.put(Headers.SERVER, this.serverName);
            headers.put(Headers.CONTENT_LENGTH, Files.size(file));
            var bytes = Files.readAllBytes(file);
            var buffer = ByteBuffer.allocate(bytes.length);
            buffer.clear();
            buffer.put(bytes);
            buffer.flip();
            e.getResponseSender().send(buffer);
        } catch (IOException ex) {
            System.getLogger(UndertowExchange.class.getName()).log(System.Logger.Level.ERROR, "", ex);
            this.respondErrorInternal(e, "The file cannot be transmitted.");
        }
    }

    String getPathParameter(HttpServerExchange exchange, String key) {
        Optional<Deque<String>> o = Optional.ofNullable(exchange.getQueryParameters().get(key));
        if (o.isEmpty()) {
            return "";
        }
        return o.get().getFirst();
    }

    void respondVideo(HttpServerExchange e, Path path) {
        if (e.isInIoThread()) {
            e.dispatch(e.getIoThread());
            return;
        }
        try (var blocking = e.startBlocking()) {
            var headers = e.getResponseHeaders();
            headers.put(Headers.CONTENT_LENGTH, Files.size(path));
            if (!e.isResponseStarted()) {
                e.setStatusCode(200);
                headers.put(Headers.CONTENT_TYPE, "video/mp4");
                headers.put(Headers.ACCEPT_RANGES, "bytes");
                headers.put(Headers.EXPIRES, "0");
                headers.put(Headers.CACHE_CONTROL, "no-cache, no-store");
                headers.put(Headers.CONNECTION, "keep-alive");
                headers.put(Headers.CONTENT_TRANSFER_ENCODING, "binary");
            }
            Files.copy(path, blocking.getOutputStream());
        } catch (Exception ex) {
            System.getLogger(UndertowExchange.class.getName()).log(System.Logger.Level.ERROR, "", ex);
            this.respondErrorInternal(e, "An error has occurred.");
        }
    }

    private void sendText(String content, String mime, HttpServerExchange e) {
        var headers = e.getResponseHeaders();
        e.setStatusCode(200);
        headers.put(Headers.CONTENT_TYPE, mime);
        headers.put(Headers.DATE, new Date().toString());
        headers.put(Headers.SERVER, this.serverName);
        headers.put(Headers.CONTENT_LENGTH, content.length());
        e.getResponseSender().send(content);
    }
}
