package com.google.sps.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import com.google.sps.data.Cases;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/chart")
public class ChartServlet extends HttpServlet {

  private Collection<Cases> covidCases;
  
  @Override
  public void init() {

    covidCases = new ArrayList<>();

    Scanner scanner = new Scanner(getServletContext().getResourceAsStream("/WEB-INF/covid.csv"));
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      String[] cells = line.split(",");
      
      // Cases and deaths of every state on July 8th 2020.
      String state = String.valueOf(cells[1]);
      Integer cases = Integer.valueOf(cells[3]);
      Integer deaths = Integer.valueOf(cells[4]);    

      covidCases.add(new Cases(state, cases, deaths));
    }
    scanner.close();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    String json = new Gson().toJson(covidCases);
    response.getWriter().println(json);
  }
}
