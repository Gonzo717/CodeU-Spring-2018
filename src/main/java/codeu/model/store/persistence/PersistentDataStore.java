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

package codeu.model.store.persistence;

import codeu.model.data.Conversation;
import codeu.model.data.Message;
import codeu.model.data.User;
import codeu.model.data.Activity;
import codeu.model.data.Activity.ActivityType;
import codeu.model.data.Group;
import codeu.model.store.persistence.PersistentDataStoreException;
import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.lang.InterruptedException;
import java.util.concurrent.ExecutionException;

/**
 * This class handles all interactions with Google App Engine's Datastore service. On startup it
 * sets the state of the applications's data objects from the current contents of its Datastore. It
 * also performs writes of new of modified objects back to the Datastore.
 */
public class PersistentDataStore {

  // Handle to Google AppEngine's Datastore service.
  private AsyncDatastoreService datastore;

  /**
   * Constructs a new PersistentDataStore and sets up its state to begin loading objects from the
   * Datastore service.
   */
  public PersistentDataStore() {
    datastore = DatastoreServiceFactory.getAsyncDatastoreService();
  }

  /**
   * Loads all User objects from the Datastore service and returns them in a List.
   *
   * @throws PersistentDataStoreException if an error was detected during the load from the
   *     Datastore service
   */
  public List<User> loadUsers() throws PersistentDataStoreException {

    List<User> users = new ArrayList<>();

    // Retrieve all users from the datastore.
    Query query = new Query("chat-users");
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      try {
        UUID uuid = UUID.fromString((String) entity.getProperty("uuid"));
        UUID profileId = UUID.fromString((String) entity.getProperty("profile_uuid"));
        String userName = (String) entity.getProperty("username");
        String passwordHash = (String) entity.getProperty("password_hash");
        Boolean is_admin = Boolean.parseBoolean(String.valueOf(entity.getProperty("is_admin")));
        Instant creationTime = Instant.parse((String) entity.getProperty("creation_time"));
        User user = new User(uuid, profileId, userName, passwordHash, false, creationTime);
        users.add(user);
      } catch (Exception e) {
        // In a production environment, errors should be very rare. Errors which may
        // occur include network errors, Datastore service errors, authorization errors,
        // database entity definition mismatches, or service mismatches.
        throw new PersistentDataStoreException(e);
      }
    }

    return users;
  }

  /**
   * Loads all Conversation objects from the Datastore service and returns them in a List, sorted in
   * ascending order by creation time.
   *
   * @throws PersistentDataStoreException if an error was detected during the load from the
   *     Datastore service
   */
  public List<Conversation> loadConversations() throws PersistentDataStoreException {

    List<Conversation> conversations = new ArrayList<>();

    // Retrieve all conversations from the datastore.
    Query query = new Query("chat-conversations").addSort("creation_time", SortDirection.ASCENDING);
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      try {
        UUID uuid = UUID.fromString((String) entity.getProperty("uuid"));
        UUID ownerUuid = UUID.fromString((String) entity.getProperty("owner_uuid"));
        String title = (String) entity.getProperty("title");
        Instant creationTime = Instant.parse((String) entity.getProperty("creation_time"));
        Conversation conversation = new Conversation(uuid, ownerUuid, title, creationTime);
        conversations.add(conversation);
      } catch (Exception e) {
        // In a production environment, errors should be very rare. Errors which may
        // occur include network errors, Datastore service errors, authorization errors,
        // database entity definition mismatches, or service mismatches.
        throw new PersistentDataStoreException(e);
      }
    }

    return conversations;
  }

  /**
   * Loads all Group Conversation objects from the Datastore service and returns them in a List, sorted in
   * ascending order by creation time.
   *
   * @throws PersistentDataStoreException if an error was detected during the load from the
   *     Datastore service
   */
  public List<Group> loadGroupConversations() throws PersistentDataStoreException {

    List<Group> groupConversations = new ArrayList<>();

    // Retrieve all conversations from the datastore.
    Query query = new Query("chat-group").addSort("creation_time", SortDirection.ASCENDING);
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      try {
        UUID uuid = UUID.fromString((String) entity.getProperty("UUID"));
        UUID ownerUuid = UUID.fromString((String) entity.getProperty("owner_UUID"));
        String title = (String) entity.getProperty("Title");
        Instant creationTime = Instant.parse((String) entity.getProperty("creation"));
		    HashSet<User> users = (HashSet) entity.getProperty("users");
		    Group group = new Group(uuid, ownerUuid, title, creationTime, users);
        groupConversations.add(group);
      } catch (Exception e) {
        // In a production environment, errors should be very rare. Errors which may
        // occur include network errors, Datastore service errors, authorization errors,
        // database entity definition mismatches, or service mismatches.
        throw new PersistentDataStoreException(e);
      }
    }

    return groupConversations;
  }

  /**
   * Loads all Message objects from the Datastore service and returns them in a List, sorted in
   * ascending order by creation time.
   *
   * @throws PersistentDataStoreException if an error was detected during the load from the
   *     Datastore service
   */
  public List<Message> loadMessages() throws PersistentDataStoreException {

    List<Message> messages = new ArrayList<>();

    // Retrieve all messages from the datastore.
    Query query = new Query("chat-messages").addSort("creation_time", SortDirection.ASCENDING);
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      try {
        UUID uuid = UUID.fromString((String) entity.getProperty("uuid"));
        UUID conversationUuid = UUID.fromString((String) entity.getProperty("conv_uuid"));
        UUID authorUuid = UUID.fromString((String) entity.getProperty("author_uuid"));
        Instant creationTime = Instant.parse((String) entity.getProperty("creation_time"));
        String content = (String) entity.getProperty("content");
        Message message = new Message(uuid, conversationUuid, authorUuid, content, creationTime);
        messages.add(message);
      } catch (Exception e) {
        // In a production environment, errors should be very rare. Errors which may
        // occur include network errors, Datastore service errors, authorization errors,
        // database entity definition mismatches, or service mismatches.
        throw new PersistentDataStoreException(e);
      }
    }

    return messages;
  }

  /**
	 * Loads all Activity objects from the Datastore service and returns them in a List, sorted in
	 * ascending order by creation time.
	 * @throws PersistentDataStoreException if an error was detected during the load from the
	 *			Datastore service
	 */

	public List<Activity> loadActivities() throws PersistentDataStoreException {
	List<Activity> activities = new ArrayList<>();

		// Retrieve all activities from the datastore.
		Query query = new Query("chat-activities").addSort("creation_time", SortDirection.DESCENDING);
		PreparedQuery results = datastore.prepare(query);

		for(Entity entity : results.asIterable()) {
			try {
				ActivityType type = ActivityType.valueOf((String) entity.getProperty("activity_type"));
				UUID uuid = UUID.fromString((String) entity.getProperty("uuid"));
				UUID ownerId = UUID.fromString((String) entity.getProperty("ownerId"));
        UUID activityId = UUID.fromString((String) entity.getProperty("activityId"));
				Instant creationTime = Instant.parse((String) entity.getProperty("creation_time"));
				Activity activity = new Activity(type, uuid, ownerId, activityId, creationTime);
				activities.add(activity);
			} catch (Exception e) {
				throw new PersistentDataStoreException(e);
			}
		}
		return activities;
	}

  /** Write a User object to the Datastore service. */
  public void writeThrough(User user) throws InterruptedException, ExecutionException {
    Entity userEntity = new Entity("chat-users", user.getId().toString());
    userEntity.setProperty("uuid", user.getId().toString());
    userEntity.setProperty("profile_uuid", user.getProfileID().toString());
    userEntity.setProperty("username", user.getName());
    userEntity.setProperty("password_hash", user.getPasswordHash());
    userEntity.setProperty("is_admin", String.valueOf(user.getType()));
    userEntity.setProperty("creation_time", user.getCreationTime().toString());
    Future result = datastore.put(userEntity);
    result.get();
  }

  /** Write a Message object to the Datastore service. */
  public void writeThrough(Message message) throws InterruptedException, ExecutionException {
    Entity messageEntity = new Entity("chat-messages", message.getId().toString());
    messageEntity.setProperty("uuid", message.getId().toString());
    messageEntity.setProperty("conv_uuid", message.getConversationId().toString());
    messageEntity.setProperty("author_uuid", message.getAuthorId().toString());
    messageEntity.setProperty("content", message.getContent());
    messageEntity.setProperty("creation_time", message.getCreationTime().toString());
    Future result = datastore.put(messageEntity);
    result.get();
  }

  /** Write a Group object to the Datastore service. */
  public void writeThrough(Group group) throws InterruptedException, ExecutionException {
    Entity groupEntity = new Entity("chat-group", group.getId().toString());
    groupEntity.setProperty("UUID", group.getId().toString());
    groupEntity.setProperty("owner", group.getOwnerId().toString());
    groupEntity.setProperty("Title", group.getTitle());
    groupEntity.setProperty("creation", group.getCreationTime().toString());
	  groupEntity.setProperty("users", group.getAllUsers().toString());
    Future result = datastore.put(groupEntity);
    result.get();
  }


  /** Write a Conversation object to the Datastore service. */
  public void writeThrough(Conversation conversation) throws InterruptedException, ExecutionException {
    Entity conversationEntity = new Entity("chat-conversations", conversation.getId().toString());
    conversationEntity.setProperty("uuid", conversation.getId().toString());
    conversationEntity.setProperty("owner_uuid", conversation.getOwnerId().toString());
    conversationEntity.setProperty("title", conversation.getTitle());
    conversationEntity.setProperty("creation_time", conversation.getCreationTime().toString());
    Future result = datastore.put(conversationEntity);
    result.get();
  }

  /** Write an Activity object to the Datastore service. */
  public void writeThrough(Activity activity) throws InterruptedException, ExecutionException {
	  Entity activityEntity = new Entity("chat-activities", activity.getId().toString());
	  activityEntity.setProperty("activity_type", activity.getType().toString());
	  activityEntity.setProperty("uuid", activity.getId().toString());
	  activityEntity.setProperty("ownerId", activity.getOwnerId().toString());
    activityEntity.setProperty("activityId", activity.getActivityId().toString());
	  activityEntity.setProperty("creation_time", activity.getCreationTime().toString());
	  Future result = datastore.put(activityEntity);
    result.get();
  }
}
