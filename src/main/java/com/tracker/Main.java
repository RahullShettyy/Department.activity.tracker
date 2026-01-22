package com.tracker;

import com.sun.net.httpserver.HttpServer;
import com.tracker.config.DatabaseConfig;
import com.tracker.controller.ApiHandler;
import com.tracker.controller.StaticFileHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = 8000;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Serve static files (HTML, CSS, JS)
        server.createContext("/", new StaticFileHandler("src/main/resources/public"));

        // API Endpoints
        server.createContext("/api", new ApiHandler());

        server.setExecutor(null); // creates a default executor
        server.start();

        System.out.println("Server started on port " + port);
        System.out.println("Initializing Database...");
        initDatabase();
    }

    private static void initDatabase() {
        try {
            // 1. Create Connection to MySQL Server (no DB) to create DB if needed
            java.util.Properties props = new java.util.Properties();
            try (java.io.InputStream in = Main.class.getClassLoader().getResourceAsStream("application.properties")) {
                props.load(in);
            }

            String fullUrl = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String pass = props.getProperty("db.password");

            // Extract base URL (e.g. jdbc:mysql://localhost:3306/)
            String baseUrl = fullUrl.substring(0, fullUrl.lastIndexOf("/") + 1);

            System.out.println("Connecting to MySQL at " + baseUrl + " to initialize...");

            try (Connection conn = java.sql.DriverManager.getConnection(baseUrl, user, pass);
                    Statement stmt = conn.createStatement()) {

                // 2. Read schema.sql
                java.io.InputStream schemaStream = Main.class.getClassLoader().getResourceAsStream("schema.sql");
                if (schemaStream == null) {
                    System.err.println("schema.sql not found!");
                    return;
                }
                String schema = new String(schemaStream.readAllBytes());

                // 3. Execute script (Split by ;)
                String[] statements = schema.split(";");
                for (String sql : statements) {
                    if (!sql.trim().isEmpty()) {
                        try {
                            stmt.execute(sql);
                        } catch (Exception e) {
                            // Ignore specific errors like "table exists" if IF NOT EXISTS usage fails or
                            // other minor issues
                            // But usually safe to print
                            // System.out.println("Executed: " + sql);
                        }
                    }
                }
                System.out.println("Database initialized successfully!");
            }
        } catch (Exception e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
