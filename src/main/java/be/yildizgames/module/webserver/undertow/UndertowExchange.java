/*
 * This file is part of the Yildiz-Engine project, licenced under the MIT License  (MIT)
 *
 *  Copyright (c) 2019 Grégory Van den Borre
 *
 *  More infos available: https://engine.yildiz-games.be
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 *  documentation files (the "Software"), to deal in the Software without restriction, including without
 *  limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 *  of the Software, and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all copies or substantial
 *  portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 *  WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 *  OR COPYRIGHT  HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE  SOFTWARE.
 *
 */
package be.yildizgames.module.webserver.undertow;

import be.yildizgames.module.webserver.HtmlPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.util.Headers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Deque;
import java.util.Optional;

public class UndertowExchange extends RoutingHandler {

    public void respondErrorInternal(HttpServerExchange exchange, String message) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        if (!exchange.isResponseStarted()) {
            exchange.setStatusCode(500);
        }
        exchange.getResponseSender().send(message);
    }

    public void respondErrorNotFound(HttpServerExchange exchange, String message) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.setStatusCode(404);
        exchange.getResponseSender().send(message);
    }

    public void respondHtml(HttpServerExchange exchange, String html) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
        exchange.setStatusCode(200);
        exchange.getResponseSender().send(html);
    }

    public void respondHtml(HttpServerExchange exchange, HtmlPage html) {
        this.respondHtml(exchange, html.render());
    }
    
    public void respondXml(HttpServerExchange exchange, String xml) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/xml");
        exchange.setStatusCode(200);
        exchange.getResponseSender().send(xml);
    }

    public void respondFile(HttpServerExchange exchange, Path file)  {
        try {
            if(Files.notExists(file)) {
                this.respondErrorNotFound(exchange, file + " not found");
            } else {
                if (exchange.isInIoThread()) {
                    exchange.dispatch(this);
                    return;
                }
                exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, String.valueOf(Files.size(file)));
                Optional.ofNullable(Files.probeContentType(file)).ifPresent(mime -> exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, mime));
                exchange.getResponseHeaders().put(Headers.CONTENT_DISPOSITION, "attachment; filename=" + file.getFileName().toString());
                exchange.setStatusCode(200);
                exchange.startBlocking();
                Files.copy(file, exchange.getOutputStream());
            }
        } catch (IOException e) {
            this.respondErrorInternal(exchange, e.getMessage());
        }
    }

    public void respondJson(HttpServerExchange exchange, Object o) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            if (exchange.isInIoThread()) {
                exchange.dispatch(this);
                return;
            }
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.setStatusCode(200);
            exchange.startBlocking();
            objectMapper.writeValue(exchange.getOutputStream(), o);
        } catch (IOException var5) {
            throw new IllegalStateException(var5);
        }
    }

    public String getPathParameter(HttpServerExchange exchange, String key) {
        Optional<Deque<String>> o =  Optional.ofNullable(exchange.getQueryParameters().get(key));
        if(o.isEmpty()) {
            return "";
        }
        return o.get().getFirst();
    }

    public void respondVideo(HttpServerExchange exchange, Path path) {
        try {
            if (exchange.isInIoThread()) {
                exchange.dispatch(this);
                return;
            }
            exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, Files.size(path));
            exchange.startBlocking();
            if (!exchange.isResponseStarted()) {
                exchange.setStatusCode(200);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "video/mp4");
                exchange.getResponseHeaders().put(Headers.ACCEPT_RANGES, "bytes");
                exchange.getResponseHeaders().put(Headers.EXPIRES, "0");
                exchange.getResponseHeaders().put(Headers.CACHE_CONTROL, "no-cache, no-store");
                exchange.getResponseHeaders().put(Headers.CONNECTION, "keep-alive");
                exchange.getResponseHeaders().put(Headers.CONTENT_TRANSFER_ENCODING, "binary");
            }
            Files.copy(path, exchange.getOutputStream());
        } catch (Exception e) {
            this.respondErrorInternal(exchange, "Cannot retrieve file " + path.toAbsolutePath());
        }
    }
}
