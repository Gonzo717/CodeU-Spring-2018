// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package codeu.model.store.basic;

import codeu.model.data.User;
import codeu.model.store.persistence.PersistentStorageAgent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Store class that uses in-memory data structures to hold values and automatically loads from and
 * saves to PersistentStorageAgent. It's a singleton so all servlet classes can access the same
 * instance.
 */
public class UserStore {

  /** Singleton instance of UserStore. */
  private static UserStore instance;

  /**
   * Returns the singleton instance of UserStore that should be shared between all servlet classes.
   * Do not call this function from a test; use getTestInstance() instead.
   */
  public static UserStore getInstance() {
    if (instance == null) {
      instance = new UserStore(PersistentStorageAgent.getInstance());
    }
    return instance;
  }

  /**
   * Instance getter function used for testing. Supply a mock for PersistentStorageAgent.
   *
   * @param persistentStorageAgent a mock used for testing
   */
  public static UserStore getTestInstance(PersistentStorageAgent persistentStorageAgent) {
    return new UserStore(persistentStorageAgent);
  }

  /**
   * The PersistentStorageAgent responsible for loading Users from and saving Users to Datastore.
   */
  private PersistentStorageAgent persistentStorageAgent;

  /** The in-memory list of Users. */
  private List<User> users;
  /** The in-memory list of Admins. */
  private List<String> adminUsernames;

  /** This class is a singleton, so its constructor is private. Call getInstance() instead. */
  private UserStore(PersistentStorageAgent persistentStorageAgent) {
    this.persistentStorageAgent = persistentStorageAgent;
    users = new ArrayList<>();

    adminUsernames = new ArrayList<>();
    adminUsernames.add("Claguemeister");
    adminUsernames.add("Cristameister");
    adminUsernames.add("Gonzalomeister");
  }

  /**
   * Access the User object with the given name.
   *
   * @return null if username does not match any existing User.
   */
  public User getUser(String username) {
    // This approach will be pretty slow if we have many users.
    for (User user : users) {
      if (user.getName().equals(username)) {
        return user;
      }
    }
    return null;
  }

  public List<User> getUsers(){
    /*
    Returns the total users in the UserStore for admin purposes
    */
    return users;
  }

  /**
   * Access the User object with the given UUID.
   *
   * @return null if the UUID does not match any existing User.
   */
  public User getUser(UUID id) {
    for (User user : users) {
      if (user.getId().equals(id)) {
        return user;
      }
    }
    return null;
  }

  /**
   * Add a new user to the current set of users known to the application. This should only be called
   * to add a new user, not to update an existing user.
   */
  public void addUser(User user) {
    users.add(user);
    persistentStorageAgent.writeThrough(user);
  }

  /**
   * Update an existing User.
   */
  public void updateUser(User user) {
    persistentStorageAgent.writeThrough(user);
  }

  /** Return true if the given username is known to the application. */
  public boolean isUserRegistered(String username) {
    for (User user : users) {
      if (user.getName().equals(username)) {
        return true;
      }
    }
    return false;
  }

  /* Return true if the given username is known to be an admin. */
  public boolean isUserAdmin(String username){
    for(String name : adminUsernames){
      if(name.equals(username)) {
        return true;
      }
    }
    return false;
  }

/* Adds another admin */
  public void addAdmin(String username){
    adminUsernames.add(username);
  }

  //The fact that this is a public method makes me skeptical
  public List getAdmins(){
    return adminUsernames;
  }
  /**
   * Sets the List of Users stored by this UserStore. This should only be called once, when the data
   * is loaded from Datastore.
   */
  public void setUsers(List<User> users) {
    this.users = users;
  }
}
