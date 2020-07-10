package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Checking login status. 
    UserService userService = UserServiceFactory.getUserService();
    
    if (userService.isUserLoggedIn()) {
      String email = userService.getCurrentUser().getEmail();
      String urlToRedirectToAfterLogOut = "/login";
      String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterLogOut);

      response.getWriter().println("<p>Hello " + email + "!</p>");
      response.getWriter().println("<p>Logout <a href=\"" + logoutUrl + "\">here</a>.</p>");

    } else {
      String urlToRedirectToAfterLogIn = "/data";
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterLogIn);
      
      response.setContentType("text/html");
      response.getWriter().println("<p>Hello user.</p>");
      response.getWriter().println("<p>Login <a href=\"" + loginUrl + "\">here</a>.</p>");
    }
  }
}
