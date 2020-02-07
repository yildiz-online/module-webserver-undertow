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
import be.yildizgames.module.webserver.RequestHandler;
import io.undertow.server.HttpServerExchange;

import java.nio.file.Path;

public class UndertowController {

    final UndertowExchange undertowExchange = new UndertowExchange();

    public UndertowController() {
        super();
    }

    public UndertowController get(String s, RequestHandler h) {
        this.undertowExchange.get(s, h);
        return this;
    }

    public UndertowController post(String s, RequestHandler h) {
        this.undertowExchange.post(s, h);
        return this;
    }

    public UndertowController put(String s, RequestHandler h) {
        this.undertowExchange.put(s, h);
        return this;
    }

    protected void respondJson(HttpServerExchange exchange, Object o) {
        this.undertowExchange.respondJson(exchange, o);
    }

    protected String getPathParameter(HttpServerExchange exchange, String p) {
        return this.undertowExchange.getPathParameter(exchange, p);
    }

    protected void respondHtml(HttpServerExchange exchange, HtmlPage page) {
        this.undertowExchange.respondHtml(exchange, page);
    }

    protected void respondFile(HttpServerExchange exchange, Path file) {
        this.undertowExchange.respondFile(exchange, file);
    }

    protected void respondErrorInternal(HttpServerExchange exchange, String message) {
        this.undertowExchange.respondErrorInternal(exchange, message);
    }

    protected void respondErrorNotFound(HttpServerExchange exchange, String message) {
        this.undertowExchange.respondErrorNotFound(exchange, message);
    }

    protected void respondVideo(HttpServerExchange exchange, Path p) {
        this.undertowExchange.respondVideo(exchange, p);
    }

    protected void respondXml(HttpServerExchange exchange, String xml) {
        this.undertowExchange.respondXml(exchange, xml);
    }
}
