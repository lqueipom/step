// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.sps.data.Task;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private List<String> messages;

    @Override
    public void init() {
      messages = new ArrayList<String>();
    }

    @Override 
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      // Converting Array List to JSON.
      String jsonVersion = convertToJson(messages);

      Query query = new Query("Comments").addSort("comment", SortDirection.ASCENDING);
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      PreparedQuery results = datastore.prepare(query);

      for (Entity entity : results.asIterable()) {
        String comment = (String) entity.getProperty("comment");

        Task task = new Task(comment);
        jsonVersion.add(task);
      }

      // Send JSON string.
      response.setContentType("application/json;");
      response.getWriter().println(jsonVersion);
    }

    private String convertToJson(List messages) {
      Gson gson = new Gson();
      return gson.toJson(messages);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      // Get the input from the form.
      String text = getParameter(request, "word-input", "");
      boolean positive = Boolean.parseBoolean(getParameter(request, "positive", "false"));
      boolean negative = Boolean.parseBoolean(getParameter(request, "negative", "false"));
      boolean mixed = Boolean.parseBoolean(getParameter(request, "mixed", "false"));
      
      Entity taskEntity = new Entity("Comments");
      taskEntity.setProperty("comment", text);

      // Storing comments in their respective bins.
      if (positive) {
        taskEntity.setProperty("status", "positive");
      } 
      
      if (negative) {
        taskEntity.setProperty("status", "negative");
      }

      if (mixed) {
        taskEntity.setProperty("status", "mixed");
      }

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(taskEntity);

      // Respond with the result.
      response.setContentType("text/html;");
      response.getWriter().println(text);
}
  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
    private String getParameter(HttpServletRequest request, String name, String defaultValue) {
      String value = request.getParameter(name);
      return value != null ? value : defaultValue;
  }
}
