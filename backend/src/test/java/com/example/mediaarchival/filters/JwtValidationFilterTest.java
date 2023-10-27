package com.example.mediaarchival.filters;

import static org.junit.jupiter.api.Assertions.*;

import com.example.mediaarchival.models.UserModel;
import com.example.mediaarchival.utils.TokenUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

public class JwtValidationFilterTest {

  private JwtValidationFilter jwtValidationFilter;

  @BeforeEach
  public void setUp() {
    jwtValidationFilter = new JwtValidationFilter();
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void JwtValidationFilter_doFilterInternal_ShouldAuthenticateWithValidToken() throws ServletException, IOException {
    UserModel user = new UserModel("Ken", "test");
    String validJwt = TokenUtils.generateJwtToken(user);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Token", validJwt);

    MockHttpServletResponse response = new MockHttpServletResponse();

    FilterChain filterChain =
        (servletRequest, servletResponse) -> {
          assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        };

    jwtValidationFilter.doFilter(request, response, filterChain);
  }

  @Test
  public void JwtValidationFilter_doFilterInternal_ShouldNotAuthenticateWithInvalidToken() throws ServletException, IOException {
    String invalidJwt = "invalid-token";

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", invalidJwt);

    MockHttpServletResponse response = new MockHttpServletResponse();

    FilterChain filterChain =
        (servletRequest, servletResponse) -> {
          assertNull(SecurityContextHolder.getContext().getAuthentication());
        };

    // Process the filter
    jwtValidationFilter.doFilter(request, response, filterChain);
  }

  @Test
  public void JwtValidationFilter_doFilterInternal_ShouldNotAuthenticateWithoutToken() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();

    MockHttpServletResponse response = new MockHttpServletResponse();

    FilterChain filterChain =
        (servletRequest, servletResponse) -> {
          assertNull(SecurityContextHolder.getContext().getAuthentication());
        };

    jwtValidationFilter.doFilter(request, response, filterChain);
  }
}
