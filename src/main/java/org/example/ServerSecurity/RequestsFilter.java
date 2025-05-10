package org.example.ServerSecurity;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RequestsFilter extends HttpFilter {
    private static final Logger logger = LoggerFactory.getLogger(RequestsFilter.class);
    private static final String USER_HEART_BEAT_PATH = "/user-heartbeat";
    private static final String GUEST_HEART_BEAT_PATH = "/guest-heartbeat";

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException, java.io.IOException {
        addCorsHeaders(response);

        String path = request.getRequestURI();
        String sessionToken = request.getHeader("sessionToken");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        if (path.contains("heartbeat")) {
            addCorsHeaders(response);
            if (path.equals(USER_HEART_BEAT_PATH)) {
                if (SessionsManager.updateSessionHeartbeat(sessionToken)) {
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                }
            } else if (path.equals(GUEST_HEART_BEAT_PATH)) {
                response.setStatus(HttpServletResponse.SC_OK);
            }
            return;
        }
        logger.info("path: {}", path);
        chain.doFilter(request, response);
    }

    private void addCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", Constants.FRONTEND_PATH);
        response.setHeader("Access-Control-Allow-Methods", String.join(", ", Constants.ALLOWED_HTTP_METHODS));
        response.setHeader("Access-Control-Allow-Headers", String.join(", ", Constants.ALLOWED_HTTP_HEADERS));
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }
}
