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

package codeu.model.data;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import java.io.Serializable;
import java.time.Duration;
import java.lang.Object;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;


/**
 * Class representing a conversation, which can be thought of as a chat room. Conversations are
 * created by a User and contain Messages.
 */
public class Conversation {
  public final UUID id;
  public final UUID ownerId;
  public final Instant creationTime;
  public String title;
  public HashSet<UUID> members;

  public enum Type {
	  //denotes a hybrid conversation or only one that allows either messages only or img only
	  TEXT("Text"), IMG("Image"), HYBRID("Hybrid");

		private final String typeDescription;

		private Type(String value){
			this.typeDescription = value;
		}

		public String toString(){
			return typeDescription;
		}
  };

  public enum Visibility {
	  //specifies the scope of the conversation object
	  PUBLIC("Public"), GROUP("Group"), DIRECT("Direct");

		private final String visibilityDescription;

		private Visibility(String value){
			this.visibilityDescription = value;
		}

		public String toString(){
			return visibilityDescription;
		}
  };

  public final Type type;
  public final Visibility visibility;
  public String avatarImageURL;
  public boolean isActive;
  public final ChronoUnit validTime;
  public final Instant deletionInstant;
  public int totalPoints;
  public String description;
	public HashSet<UUID> haveVoted;


  /**
   * Constructs a new Conversation.
   *
   * @param id the ID of this Conversation
   * @param ownerId the ID of the User who created this Conversation
   * @param creationTime the creation time of this Conversation
   * @param title a String of text containing the conversation Title
   * @param members a HashSet containing all user uuids that are members of this Conversation
   * @param type an enum that denotes the Conversation limitations (public, group, or direct)
   * @param visibility an enum object that also denotes the conversation limitations of who can see this particular Conversations
   * @param avatarImageURL a BlobKey object that holds a particular conversation's reference to the actual Image
   * @param isActive a boolean variable that determines if a conversation is closed or still able to edit
   * @param deletionInstant an Instant variable that specifies the instant where this conversation becomes inactive
   * @param validTime the length of time that this conversation is valid for (user specified)
   * @param totalPoints an int variable that keeps track of the net rating of a conversation
   * @param description a Blob object that holds a short description of what the Conversation is about
	 * @param haveVoted a HashSet containing the UUID objects of the Users that have voted
   *
   */
  public Conversation(UUID id, UUID ownerId, String title, Instant creationTime,
  						HashSet<UUID> members, Type type, Visibility visibility,
					 		String avatarImageURL, ChronoUnit validTime, String description) {

    this.id = id;
    this.ownerId = ownerId;
    this.title = title;
		this.creationTime = creationTime;
		this.members = members;
		this.type = type;
		this.visibility = visibility;
		this.avatarImageURL = avatarImageURL;
		this.isActive = true;
		this.validTime = validTime;
		this.deletionInstant = creationTime.plus(validTime.getDuration());
		this.totalPoints = 0;
		this.description = description;
		this.haveVoted = new HashSet<UUID>();

  }

  /** Returns the ID of this Conversation. */
  public UUID getId() {
    return id;
  }

  /** Returns the ID of the User who created this Conversation. */
  public UUID getOwnerId() {
    return ownerId;
  }

  /** Returns the title of this Conversation. */
  public String getTitle() {
	//have to query for the blob and change the blob back into String
    return title;
  }

  /** Returns the creation time of this Conversation. */
  public Instant getCreationTime() {
    return creationTime;
  }

  /** Returns the arraylist members of a conversation **/
  public HashSet<UUID> getMembers() {
		return members;
  }

  public void addMember(User user){
		UUID id = user.getId();
		members.add(id);
  }

  public void removeMember(User user){
		UUID id = user.getId();
		members.remove(id);
  }

	public void setMembers(HashSet newMembers){
		members = newMembers;
	}

	public String getDescription(){
		return description;
	}

  // how to deal with enums??
  // for type and Visibility
	public Type getConversationType(){
		return type; //I dont even know if it has a toString method... I dont think so.
	}

	public Visibility getConversationVisibility(){
		return visibility;
	}

	public String getAvatarImageURL() {
		return avatarImageURL;
	}

	/* This stuff deals with the time-restrictions/validity of a conversation */

  public int timeRemaining(Instant now){
		// Here is where I will return the amount of time remaining for a conversation's validity
		// Returns the amount of time in seconds rounded down.
		// Dont know if I want to parse it to a human readable form here or do it later.
		// TODO: I'll just parse it here and return an instant
		long gap = Duration.between(deletionInstant, now).getSeconds();
		int parsedGap = (int) (gap/1000);
		return parsedGap;
  }

  public boolean checkActive(Instant now){
		if(now.isAfter(deletionInstant)){
		  isActive = false;
	  }
	  return isActive;
  }

	public boolean isActive(){
		return isActive;
	}

  public Instant getDeletionInstant(){
	  return deletionInstant;
  }

	public ChronoUnit getValidTime(){
		return validTime;
	}

	/* This stuff deals with the points of a conversation */
	public int getTotalPoints() {
		return totalPoints;
	}

	public void setTotalPoints(int points){
		totalPoints = points;
	}

	public HashSet getVoters(){
		return haveVoted;
	}

	public void upVote(UUID id){
		// How to make it so that every user has only one point?
		// make a HashSet called haveVoted, update that with the id's of people that have have voted
		if(!haveVoted.contains(id)){
			totalPoints++;
			haveVoted.add(id);
		}
	}

	public void downVote(UUID id){
		if(haveVoted.contains(id)){
			totalPoints--;
			haveVoted.remove(id);
		}
	}


}
