package com.tracker.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class StaticFileHandler implements HttpHandler {
    private final String baseDir;

    public StaticFileHandler(String baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String uri = exchange.getRequestURI().getPath();
        if (uri.equals("/")) {
            uri = "/index.html";
        }

        File file = new File(baseDir + uri);
        if (file.exists() && file.isFile()) {
            String mimeType = Files.probeContentType(file.toPath());
            if (uri.endsWith(".css"))
                mimeType = "text/css";
            if (uri.endsWith(".js"))
                mimeType = "application/javascript";

            exchange.getResponseHeaders().set("Content-Type", mimeType != null ? mimeType : "application/octet-stream");
            exchange.sendResponseHeaders(200, file.length());
            try (OutputStream os = exchange.getResponseBody();
                    FileInputStream fs = new FileInputStream(file)) {
                fs.transferTo(os);
            }
        } else {
            String response = "404 (Not Found)\n";
            exchange.sendResponseHeaders(404, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}
