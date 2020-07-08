package com.google.sps.servlets;

import com.google.sps.data.Location;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/interactive")
public class Interactive extends HttpServlet {

  private Collection<Location> locations;

  @Override
  public void init() {
      
    List<Object> locations = new ArrayList<>();

    Scanner scanner = new Scanner(getServletContext().getResourceAsStream("/WEB-INF/favorites.csv"));
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      String[] cells = line.split(",");

      double latitude = Double.parseDouble(cells[0]);
      double longitude = Double.parseDouble(cells[1]);

      locations.add(new Location(latitude, longitude));
    }
    scanner.close();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String json = new Gson().toJson(locations);
    response.setContentType("application/json");
    response.getWriter().println(json);
  }
}
