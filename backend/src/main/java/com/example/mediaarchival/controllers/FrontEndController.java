package com.example.mediaarchival.controllers;

import com.example.mediaarchival.errors.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller responsible for handling non-API requests in the application.
 * This controller primarily deals with routing frontend application requests.
 */
@Controller
public class FrontEndController {
  private static final String FORWARD_TO_INDEX = "forward:/index.html";

  /**
   * Handles all non-API requests and forwards them to the index.html of the frontend application.
   * If the request starts with '/api/', a ResourceNotFoundException is thrown.
   *
   * @param request The incoming HTTP request.
   * @return A string indicating the path to forward to (index.html).
   * @throws ResourceNotFoundException if the request URI starts with '/api/'.
   */
  @RequestMapping(
          value = {"/", "/{path:[^\\.]*}"},
          method = RequestMethod.GET)
  public String allNonApiRequests(HttpServletRequest request) {
    if (request.getRequestURI().startsWith("/api/")) {
      throw new ResourceNotFoundException("endpoint does not exist");
    }
    return FORWARD_TO_INDEX;
  }
}

