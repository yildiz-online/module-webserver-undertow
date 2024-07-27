/*
 * This file is part of the Yildiz-Engine project, licenced under the MIT License  (MIT)
 *  Copyright (c) 2024 Grégory Van den Borre
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

import be.yildizgames.module.webserver.FileResponse;
import be.yildizgames.module.webserver.HtmlPageResponse;
import be.yildizgames.module.webserver.PlainTextResponse;
import be.yildizgames.module.webserver.WebServer;
import be.yildizgames.module.webserver.WebServerBuilder;
import be.yildizgames.module.webserver.WebServerConfiguration;
import be.yildizgames.module.webserver.YamlTextResponse;
import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;

/**
 * @author Grégory Van den Borre
 */
public class UndertowWebServerBuilder implements WebServerBuilder {

    private final Undertow.Builder builder;

    private final RoutingHandler router;
    private final UndertowExchange exchange;


    public UndertowWebServerBuilder(WebServerConfiguration configuration) {
        this.builder = Undertow.builder();
        this.builder.addHttpListener(configuration.webServerPort(), "0.0.0.0");
        this.router = new RoutingHandler();
        this.exchange = new UndertowExchange(configuration.serverName());
        this.builder.setHandler(this.router)
                .setIoThreads(4)
                .setWorkerThreads(10);
    }

    @Override
    public WebServerBuilder addRoute(String path, HtmlPageResponse response) {
        this.router.get(path, httpServerExchange -> this.exchange.respondHtml(httpServerExchange, response.htmlResponse(new UndertowExchangeData(httpServerExchange))));
        return this;
    }

    @Override
    public WebServerBuilder addRoute(String path, PlainTextResponse response) {
        this.router.get(path, httpServerExchange -> this.exchange.respondText(httpServerExchange, response.textResponse(new UndertowExchangeData(httpServerExchange))));
        return this;
    }

    @Override
    public WebServerBuilder addRoute(String path, YamlTextResponse response) {
        this.router.get(path, httpServerExchange -> this.exchange.respondYaml(httpServerExchange, response.yamlResponse(new UndertowExchangeData(httpServerExchange))));
        return this;
    }

    @Override
    public WebServerBuilder addRoute(String path, FileResponse response) {
        this.router.get(path, httpServerExchange -> this.exchange.respondFile(httpServerExchange, response.fileResponse(new UndertowExchangeData(httpServerExchange))));
        return this;
    }

    @Override
    public WebServer build() {
        return new UndertowWebServer(this.builder.build());
    }
}
