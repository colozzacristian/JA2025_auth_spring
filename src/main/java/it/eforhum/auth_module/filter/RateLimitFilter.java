package it.eforhum.auth_module.filter;

import java.io.IOException;

import org.springframework.stereotype.Component;

import it.eforhum.auth_module.service.RateLimitingUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;


@WebFilter("/*")
@Component
public class RateLimitFilter implements Filter {

    private final RateLimitingUtils rateLimitingUtils;

    public RateLimitFilter(RateLimitingUtils rateLimitingUtils) {
        this.rateLimitingUtils = rateLimitingUtils;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        String ipAddress = request.getRemoteAddr();

        if (rateLimitingUtils.isBlocked(ipAddress)) {
            ( (HttpServletResponse) response).setStatus(401);
            return;
        }

        chain.doFilter(request, response);
    }
    
}
