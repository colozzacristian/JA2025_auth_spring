package it.eforhum.authModule.filters;

import java.io.IOException;

import it.eforhum.authModule.utils.RateLimitingUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter("/*")
public class RateLimitFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        String ipAddress = request.getRemoteAddr();

        if (RateLimitingUtils.isBlocked(ipAddress)) {
            ( (HttpServletResponse) response).setStatus(401);
            return;
        }

        chain.doFilter(request, response);
    }
    
}
