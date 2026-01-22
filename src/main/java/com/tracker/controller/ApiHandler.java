package com.tracker.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tracker.dao.EventDAO;
import com.tracker.model.Event;
import com.tracker.dao.RegistrationDAO;
import com.tracker.model.Registration;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiHandler implements HttpHandler {
    private EventDAO eventDAO = new EventDAO();
    private RegistrationDAO registrationDAO = new RegistrationDAO();
    // Allow standard ISO format (yyyy-MM-dd'T'HH:mm) as sent by HTML5
    // datetime-local
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm").create();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Add CORS Headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization,Origin,Accept");

        String method = exchange.getRequestMethod();
        if ("OPTIONS".equals(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();

        try {
            if ("GET".equals(method) && path.endsWith("/events")) {
                handleGetEvents(exchange);
            } else if ("POST".equals(method) && path.endsWith("/events")) {
                handleAddEvent(exchange);
            } else if ("POST".equals(method) && path.endsWith("/registrations")) {
                handleRegistration(exchange);
            } else if ("GET".equals(method) && path.contains("/registrations")) {
                handleGetRegistrations(exchange);
            } else if ("POST".equals(method) && path.endsWith("/attendance")) {
                handleAttendance(exchange);
            } else if ("GET".equals(method) && path.endsWith("/departments")) {
                handleGetDepartments(exchange);
            } else if ("GET".equals(method) && path.endsWith("/dashboard/stats")) {
                handleDashboardStats(exchange);
            } else {
                sendResponse(exchange, 404, "Endpoint not found");
            }
        } catch (java.sql.SQLException e) {
            sendResponse(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleGetDepartments(HttpExchange exchange) throws IOException, java.sql.SQLException {
        com.tracker.dao.DepartmentDAO deptDAO = new com.tracker.dao.DepartmentDAO();
        java.util.List<com.tracker.model.Department> depts = deptDAO.getAllDepartments();
        String json = gson.toJson(depts);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        sendResponse(exchange, 200, json);
    }

    private void handleGetEvents(HttpExchange exchange) throws IOException, java.sql.SQLException {
        List<Event> events = eventDAO.getAllEvents();
        String json = gson.toJson(events);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        sendResponse(exchange, 200, json);
    }

    private void handleAddEvent(HttpExchange exchange) throws IOException, java.sql.SQLException {
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        Event event = gson.fromJson(body, Event.class);
        eventDAO.addEvent(event);
        sendResponse(exchange, 201, "{\"message\": " + "\"Event created\"}");
    }

    private void handleRegistration(HttpExchange exchange) throws IOException, java.sql.SQLException {
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        Registration reg = gson.fromJson(body, Registration.class);
        registrationDAO.registerStudent(reg);
        sendResponse(exchange, 201, "{\"message\": \"Registration successful\"}");
    }

    private void handleGetRegistrations(HttpExchange exchange) throws IOException, java.sql.SQLException {
        String query = exchange.getRequestURI().getQuery();
        int eventId = 0;
        if (query != null && query.contains("eventId=")) {
            eventId = Integer.parseInt(query.split("eventId=")[1].split("&")[0]);
        }

        List<Registration> regs = registrationDAO.getRegistrationsForEvent(eventId);
        String json = gson.toJson(regs);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        sendResponse(exchange, 200, json);
    }

    private void handleAttendance(HttpExchange exchange) throws IOException, java.sql.SQLException {
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        // Expects { "registrationId": 1, "attended": true }
        Registration update = gson.fromJson(body, Registration.class); // Reusing model for simplicity

        // We really just need an ID and boolean. Let's assume the Registration.id is
        // set and attended is set
        registrationDAO.updateAttendance(update.getId(), update.isAttended());

        sendResponse(exchange, 200, "{\"message\": \"Attendance updated\"}");
    }

    private void handleDashboardStats(HttpExchange exchange) throws IOException, java.sql.SQLException {
        List<Event> events = eventDAO.getAllEvents();
        int upcoming = 0;
        int completed = 0;

        for (Event e : events) {
            // Using basic string checks. Should ideally use Enums or constants.
            if ("SCHEDULED".equalsIgnoreCase(e.getStatus())) {
                upcoming++;
            } else if ("COMPLETED".equalsIgnoreCase(e.getStatus())) {
                completed++;
            }
        }

        int totalAttendance = registrationDAO.getTotalAttendance();

        Map<String, Object> stats = new HashMap<>();
        stats.put("upcomingEvents", upcoming);
        stats.put("completedEvents", completed);
        stats.put("totalAttendance", totalAttendance);

        String json = gson.toJson(stats);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        sendResponse(exchange, 200, json);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
