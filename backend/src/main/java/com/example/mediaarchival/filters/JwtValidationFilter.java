package com.example.mediaarchival.filters;

import com.example.mediaarchival.utils.TokenUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Custom filter for JWT validation on incoming HTTP requests.
 * This filter checks for the presence of a JWT in the HTTP request header and
 * attempts to validate it. If the JWT is valid, it sets the corresponding
 * Authentication object in the Spring Security context.
 **/
public class JwtValidationFilter extends OncePerRequestFilter {

  // It might be useful to make this a constant or configurable property.
  private static final String TOKEN_HEADER = "token";

  /**
   * Processes an HTTP request to validate the JWT.
   *
   * @param request     the servlet request
   * @param response    the servlet response
   * @param filterChain the filter chain
   * @throws ServletException if an error occurs during request processing
   * @throws IOException      if an I/O error occurs during request processing
   */
  @Override
  protected void doFilterInternal(
          @NonNull HttpServletRequest request,
          @NonNull HttpServletResponse response,
          @NonNull FilterChain filterChain)
          throws ServletException, IOException {
    String jwt = request.getHeader(TOKEN_HEADER);
    if (jwt != null && TokenUtils.validateJwtToken(jwt)) {
      Authentication authentication = TokenUtils.getAuthenticationFromJwt(jwt);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    try {
      filterChain.doFilter(request, response);
    } finally {
      SecurityContextHolder.clearContext();
    }
  }
}

