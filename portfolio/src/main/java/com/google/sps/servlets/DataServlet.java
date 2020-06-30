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

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import java.util.List;
import com.google.gson.Gson;
import java.util.ArrayList;
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
      messages.add("How are you?");
      messages.add("How is the weather today?");
      messages.add("What are you doing?");
    }

    @Override 
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      // Converting Array List to JSON.
      String jsonVersion = convertToJson(messages);

      // Send JSON string.
      response.setContentType("application/json;");
      response.getWriter().println(jsonVersion);
    }

    private String convertToJson(List messages) {
      Gson gson = new Gson();
      return gson.toJson(messages);
    }
}