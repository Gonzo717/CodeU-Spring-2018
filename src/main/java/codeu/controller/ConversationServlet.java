// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//		http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package codeu.controller;

import codeu.model.data.Conversation;
import codeu.model.data.User;
import codeu.model.data.Group;
import codeu.model.store.basic.ConversationStore;
import codeu.model.data.Activity;
import codeu.model.data.Activity.ActivityType;
import codeu.model.store.basic.GroupConversationStore;
import codeu.model.store.basic.UserStore;
import codeu.model.store.basic.ActivityStore;
import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.PostPut;
import com.google.appengine.api.datastore.PutContext;
import com.google.appengine.api.datastore.Entity;

/** Servlet class responsible for the conversations page. */
public class ConversationServlet extends HttpServlet {

  /** Store class that gives access to Users. */
  private UserStore userStore;

  /** Store class that gives access to Conversations. */
  private ConversationStore conversationStore;

  /** Store class that gives access to Group Conversations. */
  private GroupConversationStore groupConversationStore;

  /** Store class that gives access to Activity */
  private ActivityStore activityStore;

  /**
  * Set up state for handling conversation-related requests. This method is only called when
  * running in a server, not when running in a test.
  */
  @Override
  public void init() throws ServletException {
    super.init();
    setUserStore(UserStore.getInstance());
    setConversationStore(ConversationStore.getInstance());
    setGroupConversationStore(GroupConversationStore.getInstance());
    setActivityStore(ActivityStore.getInstance());
  }

  /**
  * Sets the UserStore used by this servlet. This function provides a common setup method for use
  * by the test framework or the servlet's init() function.
  */
  void setUserStore(UserStore userStore) {
    this.userStore = userStore;
  }

  /**
  * Sets the ConversationStore used by this servlet. This function provides a common setup method
  * for use by the test framework or the servlet's init() function.
  */
  void setConversationStore(ConversationStore conversationStore) {
    this.conversationStore = conversationStore;
  }


  void setGroupConversationStore(GroupConversationStore groupConversationStore) {
    this.groupConversationStore = groupConversationStore;
  }

  void setActivityStore(ActivityStore activityStore) {
    this.activityStore = activityStore;
  }

  /**
  * This function fires when a user navigates to the conversations page. It gets all of the
  * conversations from the model and forwards to conversations.jsp for rendering the list.
  */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
  throws IOException, ServletException {
    List<Conversation> conversations = conversationStore.getAllConversations();
    List<Group> groups = groupConversationStore.getAllGroupConversations();
    request.setAttribute("conversations", conversations);
    request.setAttribute("groups", groups);
    request.getRequestDispatcher("/WEB-INF/view/conversations.jsp").forward(request, response);
  }

  /**
  * This function fires when a user submits the form on the conversations page. It gets the
  * logged-in username from the session and the new conversation title from the submitted form
  * data. It uses this to create a new Conversation object that it adds to the model.
  */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
  throws IOException, ServletException {

    String username = (String) request.getSession().getAttribute("user");
    if (username == null) {
      // user is not logged in, don't let them create a conversation
      response.sendRedirect("/conversations");
      return;
    }

    User user = userStore.getUser(username);

    if (user == null) {
      // user was not found, don't let them create a conversation
      System.out.println("User not found: " + username);
      response.sendRedirect("/conversations");
      return;
    }

    String conversationTitle = "";
    if(request.getParameter("conversationTitle") == null){
      int counter = 0;
      conversationTitle = (String) request.getSession().getAttribute("user") + "sGroup";
      conversationTitle = (String) request.getSession().getAttribute("user") + "sGroup" + counter;
      counter++;
    }else{
      conversationTitle = request.getParameter("conversationTitle");
    }

    if (!conversationTitle.matches("[\\w*]*")) {
      request.setAttribute("error", "Please enter only letters and numbers.");
      request.getRequestDispatcher("/WEB-INF/view/conversations.jsp").forward(request, response);
      return;
    }

    if (conversationStore.isTitleTaken(conversationTitle)) {
      // conversation title is already taken, just go into that conversation instead of creating a
      // new one
      response.sendRedirect("/chat/" + conversationTitle);
      return;
    }

    if (groupConversationStore.isTitleTaken(conversationTitle)){
      response.sendRedirect("/chat/" + conversationTitle);
      return;
    }

    if(request.getParameter("group") != null){
      //create a Private Group Message
      HashSet<User> users = new HashSet<User>();
      String name = (String) request.getSession().getAttribute("user");
      User addUser = userStore.getUser(name);
      users.add(addUser);
      Group group = new Group(UUID.randomUUID(), user.getId(), conversationTitle, Instant.now(), users);
      request.setAttribute("group", group);
      groupConversationStore.addGroup(group);
      response.sendRedirect("/chat/" + conversationTitle);
    } else if(request.getParameter("conversation") != null){
      //Create a public Conversation
      Conversation conversation = new Conversation(UUID.randomUUID(), user.getId(), conversationTitle, Instant.now());
      conversationStore.addConversation(conversation);
      request.setAttribute("conversation", conversation);

      // old way to add convo activity to ActivityStore, keeping for ref
      Activity convoActivity = new Activity(ActivityType.CONVERSATION, UUID.randomUUID(), conversation.getOwnerId(), conversation.getId(), conversation.getCreationTime());
      activityStore.addActivity(convoActivity);

      response.sendRedirect("/chat/" + conversationTitle);
    }
  }

  //PostPut runs when the user datastore has a user put into it
  @PostPut(kinds = {"chat-conversations"}) // Only applies to chat-convos query
  void addActivity(PutContext context) {
    //adds activity into activityStore
    // System.out.println("PostPut running for new conversation");
    // Entity convo = context.getCurrentElement();
    // Activity newActivity = new Activity(ActivityType.CONVERSATION, UUID.randomUUID(), UUID.fromString((String) convo.getProperty("owner_uuid")), UUID.fromString((String) convo.getProperty("uuid")), Instant.parse((String) convo.getProperty("creation_time")));
    // activityStore.addActivity(newActivity);
  }

}
