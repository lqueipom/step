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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.gson.Gson;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** An item on a todo list. */

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private final List<Object> json = new ArrayList<>();

  @Override 
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int quantity = Integer.parseInt(request.getParameter("amount"));
    Query query = new Query("Comments").addSort("comment", SortDirection.ASCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(quantity));
    
    for (Entity entity : results) {
      json.add(entity.getProperty("comment"));
    }

    // Send JSON string.
    String jsonVersion = new Gson().toJson(json);
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
    String text = request.getParameter("word-input");
    String status = request.getParameter("status");
    String language = request.getParameter("languages");

    Translate translate = TranslateOptions.getDefaultInstance().getService();
    Translation translation = translate.translate(text, Translate.TranslateOption.targetLanguage(language));
    String finalText = translation.getTranslatedText();
    System.out.println(language);
    
    Entity taskEntity = new Entity("Comments");
    taskEntity.setProperty("comment", finalText);

    // Storing comments in their respective bins.
    if (status.equals("positive")) {
      taskEntity.setProperty("status", "positive");
    } else if (status.equals("negative")) {
      taskEntity.setProperty("status", "negative");
    } else if (status.equals("mixed")) {
      taskEntity.setProperty("status", "mixed");
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(taskEntity);
    
    response.setContentType("text/html; charset=UTF-8");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().println(finalText);
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
