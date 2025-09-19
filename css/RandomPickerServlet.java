package com.example.servlets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet("/pick")
public class RandomPickerServlet extends HttpServlet {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8"); // handle unicode input
        resp.setContentType("text/html; charset=UTF-8");

        String raw = Optional.ofNullable(req.getParameter("choices")).orElse("");
        String countParam = Optional.ofNullable(req.getParameter("count")).orElse("1");

        // Parse requested number of picks safely
        int picksRequested = 1;
        try {
            picksRequested = Math.max(1, Integer.parseInt(countParam));
        } catch (NumberFormatException ignored) { }

        // Split on commas, semicolons, or newlines. Then trim and filter empties.
        String[] rawParts = raw.split("[,;\\r?\\n]+");
        List<String> choices = Arrays.stream(rawParts)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());

        try (PrintWriter out = resp.getWriter()) {
            out.println("<!doctype html><html><head><meta charset='utf-8'><title>Pick Result</title>");
            out.println("<style>body{font-family:Arial,Helvetica,sans-serif;padding:24px;background:#f7fafc} .box{background:#fff;padding:20px;border-radius:10px;max-width:700px;margin:auto;box-shadow:0 8px 30px rgba(2,6,23,0.06)} h2{margin-top:0} .choice{padding:8px 12px;margin:6px 0;border-radius:8px;background:#eef2ff;display:inline-block}</style>");
            out.println("</head><body><div class='box'>");
            out.println("<h2>Random Picker Result</h2>");

            if (choices.isEmpty()) {
                out.println("<p>No valid choices provided. Please enter items (one per line or comma-separated).</p>");
                out.println("<p><a href='index.html'>Back to input</a></p>");
                out.println("</div></body></html>");
                return;
            }

            // If picksRequested >= number of choices -> return all choices in random order
            List<String> result = new ArrayList<>();
            if (picksRequested >= choices.size()) {
                // shuffle and return all
                Collections.shuffle(choices, RANDOM);
                result.addAll(choices);
            } else {
                // choose unique picks: shuffle and take first N
                List<String> temp = new ArrayList<>(choices);
                Collections.shuffle(temp, RANDOM);
                result.addAll(temp.subList(0, picksRequested));
            }

            out.println("<p>Input had <strong>" + choices.size() + "</strong> valid choices.</p>");
            out.println("<p>Picks requested: <strong>" + picksRequested + "</strong></p>");
            out.println("<div>");
            for (String r : result) {
                out.println("<span class='choice'>" + escapeHtml(r) + "</span>");
            }
            out.println("</div>");

            out.println("<hr>");
            out.println("<p><a href='index.html'>Pick again</a></p>");
            out.println("</div></body></html>");
        }
    }

    // Simple HTML escaper to avoid HTML injection
    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#x27;");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect("index.html");
    }
}
