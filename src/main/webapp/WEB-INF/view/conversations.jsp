<%--
  Copyright 2017 Google Inc.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
--%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.UUID" %>
<%@ page import="codeu.model.data.Conversation" %>
<%@ page import="codeu.model.data.Conversation.Type" %>
<%@ page import="codeu.model.data.Conversation.Visibility" %>
<%@ page import="codeu.model.data.Group" %>



<!DOCTYPE html>
<html>
<head>
	<title>Trill</title>
	<link rel="stylesheet" href="/css/main.css">
	<link rel="shortcut icon" href="/images/JavaChipsLogo.png" />
	<link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">
	<link rel="stylesheet" href="https://code.getmdl.io/1.3.0/material.cyan-yellow.min.css">
	<script defer src="https://code.getmdl.io/1.3.0/material.min.js"></script>
	<style>
	.content-grid {
		width:960px;
		max-width: 960px;
	}
	.page-content{
		width: 800px;
		margin-top: 12%;
		margin-left: auto;
	    margin-right: auto;
	}
	#view-source {
	  position: fixed;
	  display: block;
	  right: 0;
	  bottom: 0;
	  margin-right: 40px;
	  margin-bottom: 40px;

	  z-index: 900;
	}
	</style>

</head>
<body>
	<div class="mdl-layout mdl-js-layout mdl-layout--fixed-header">
	  <div class="android-header mdl-layout__header mdl-layout__header--waterfall">
		<div class="mdl-layout__header-row">
			<span class="mdl-layout-title">Trill</span>
			<!-- Image card -->
				  <!-- Add spacer, to align navigation to the right in desktop -->
			<div class="android-header-spacer mdl-layout-spacer"></div>
				<div class="android-search-box mdl-textfield mdl-js-textfield mdl-textfield--expandable mdl-textfield--floating-label mdl-textfield--align-right mdl-textfield--full-width">
					<label class="mdl-button mdl-js-button mdl-button--icon" for="search-field">
						<i class="material-icons">search</i>
					</label>
					<div class="mdl-textfield__expandable-holder">
						<input class="mdl-textfield__input" type="text" id="search-field">
					</div>
				</div>
				  <!-- Navigation -->
				<div class="android-navigation-container">
					<nav class="android-navigation mdl-navigation">
						<a class="mdl-navigation__link mdl-typography--text-uppercase" href="/conversations">Conversations</a>
						<% if(request.getSession().getAttribute("user") != null){ %>
								<a class="mdl-navigation__link mdl-typography--text-uppercase">Hello <%= request.getSession().getAttribute("user") %>!</a>
							<a></a>
						<% } else{ %>
							<a class="mdl-navigation__link mdl-typography--text-uppercase" href="/login">Login</a>
						<% } %>
						<% if(request.getSession().getAttribute("admin") != null) { %>
							<a class="mdl-navigation__link mdl-typography--text-uppercase" href="/admin">Admin</a>
						<% } %>
						<a class="mdl-navigation__link mdl-typography--text-uppercase" href="/activityfeed">Activity Feed</a>
						<% if(request.getSession().getAttribute("user") != null){ %>
						<a class="mdl-navigation__link mdl-typography--text-uppercase" href ="/user/<%=request.getSession().getAttribute("user")%>">My Profile</a>
						<% } %>
						<% if(request.getSession().getAttribute("user") != null){ %>
							<a class="mdl-navigation__link mdl-typography--text-uppercase" href="/logout">Logout</a>
						<% } %>
					</nav>
				</div>
				<span class="android-mobile-title mdl-layout-title">
					<a class="mdl-navigation__link mdl-typography--text-uppercase" href="/about.jsp">
					<img class="android-logo-image" src="/images/JavaChipsLogoMenu.png">
					</a>
				</span>
				<button class="android-more-button mdl-button mdl-js-button mdl-button--icon mdl-js-ripple-effect" id="more-button">
					<i class="material-icons">more_vert</i>
				</button>
				<ul class="mdl-menu mdl-js-menu mdl-menu--bottom-right mdl-js-ripple-effect" for="more-button">
					<li class="mdl-menu__item">Add something here!</li>
					<li class="mdl-menu__item">Perhaps another?</li>
					<li disabled class="mdl-menu__item">Another one</li>
					<li class="mdl-menu__item">Anotha 1</li>
				</ul>
			</div>
		</div>
	</div>

<main class="mdl-layout__content">
	<div class="content-grid">
		<div class="page-content">
		  <div id="container">
		    <% if ((request.getSession().getAttribute("user") == null) && (request.getSession().getAttribute("admin") == null)){ %>
		       <h3 class="mdl-typography--text-center mdl-typography--display-1" >
				   Login to start a conversation
			       </br>
				   <a class="mdl-navigation__link" href="/login">
					   <button class="mdl-button mdl-js-button mdl-button--raised mdl-button--accent">Login</button>
				   </a>
		       </h3>
		    <% } %>

		    <% if(request.getAttribute("error") != null){ %>
		        <h2 style="color:red"><%= request.getAttribute("error") %></h2>
		    <% } %>

		    <% if(request.getSession().getAttribute("user") != null){ %>
		      <h1>New Conversation</h1>
		      <form action="/conversations" method="POST" enctype="multipart/form-data">
					  <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label">
						  <input class="mdl-textfield__input" type="text" name="conversationTitle" id="title">
						  <label class="mdl-textfield__label" for="">Conversation Title...</label> <!-- sample3 -->
						</div>
						</br>
						<div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label">
							<input class="mdl-textfield__input" type="text" name="conversationType" id="type">
							<label class="mdl-textfield__label" for="sample3">Type...</label>
						</div>
						</br>
						<!-- ALL THE OPTIONS FOR VISIBILITY -->
						<h3>Select Conversation Visibility: </h3>
						<label class="mdl-radio mdl-js-radio mdl-js-ripple-effect" for="DIRECT">
						  <input checked class="mdl-radio__button" id="DIRECT" name="DIRECT" type="radio" value="auto">
						  <span class="mdl-radio__label">Direct Message</span>
						</label>
						<label class="mdl-radio mdl-js-radio mdl-js-ripple-effect" for="ßPUBLIC">
						  <input class="mdl-radio__button" id="PUBLIC" name="PUBLIC" type="radio" value="auto">
						  <span class="mdl-radio__label">Public Conversation</span>
						</label>
						<label class="mdl-radio mdl-js-radio mdl-js-ripple-effect" for="GROUP">
						  <input class="mdl-radio__button" id="GROUP" name="GROUP" type="radio" value="auto">
						  <span class="mdl-radio__label">Group Message</span>
						</label>
						<%-- <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label">
							<input class="mdl-textfield__input" type="text" name="conversationVisibility" id="visibility">
							<label class="mdl-textfield__label" for="sample3">Visibility...</label>
						</div> --%>

						</br>
						<!-- THE DROPDOWN FOR THE VALID TIME -->
						<h3>Select Valid Time for the Conversation: </h3>
						<!-- DROPDOWN FOR THE NUMBERS -->
						<div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label getmdl-select">
				        <input type="text" value="" class="mdl-textfield__input" id="conversationValidTimeUnit" readonly>
				        <input type="hidden" value="" name="conversationValidTimeUnit">
				        <label for="conversationValidTimeUnit" class="mdl-textfield__label">Digit</label>
				        <ul for="conversationValidTimeUnit" class="mdl-menu mdl-menu--bottom-left mdl-js-menu">
				            <li class="mdl-menu__item" data-val="1">1</li>
				            <li class="mdl-menu__item" data-val="2">2</li>
				            <li class="mdl-menu__item" data-val="3">3</li>
										<li class="mdl-menu__item" data-val="4">4</li>
				            <li class="mdl-menu__item" data-val="5">5</li>
				            <li class="mdl-menu__item" data-val="6">6</li>
										<li class="mdl-menu__item" data-val="7">7</li>
				            <li class="mdl-menu__item" data-val="8">8</li>
				            <li class="mdl-menu__item" data-val="9">9</li>
										<li class="mdl-menu__item" data-val="10">10</li>
				        </ul>
				    </div>
						<!-- DROPDOWN FOR THE CHRONOUNITS -->
						<div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label getmdl-select">
				        <input type="text" value="" class="mdl-textfield__input" id="conversationValidTimeChronoUnit" readonly>
				        <input type="hidden" value="" name="conversationValidTimeChronoUnit">
				        <label for="conversationValidTimeChronoUnit" class="mdl-textfield__label">Unit of Time</label>
				        <ul for="conversationValidTimeChronoUnit" class="mdl-menu mdl-menu--bottom-left mdl-js-menu">
										<li class="mdl-menu__item" data-val="DECADES">Forever</li>
				            <li class="mdl-menu__item" data-val="SECONDS">Seconds</li>
				            <li class="mdl-menu__item" data-val="MINUTES">Minutes</li>
				            <li class="mdl-menu__item" data-val="HOURS">Hours</li>
										<li class="mdl-menu__item" data-val="DAYS">Days</li>
										<li class="mdl-menu__item" data-val="WEEKS">Weeks</li>
										<li class="mdl-menu__item" data-val="MONTHS">Months</li>
										<li class="mdl-menu__item" data-val="YEARS">Years</li>
				        </ul>
				    </div>

						</br>
						<!-- Pre-selected value -->
						    <div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label getmdl-select getmdl-select__fix-height">
						        <input type="text" value="" class="mdl-textfield__input" id="sample6" readonly>
						        <input type="hidden" value="" name="sample6">
						        <i class="mdl-icon-toggle__label material-icons">keyboard_arrow_down</i>
						        <label for="sample6" class="mdl-textfield__label">Country</label>
						        <ul for="sample6" class="mdl-menu mdl-menu--bottom-left mdl-js-menu">
						            <li class="mdl-menu__item" data-val="BY" data-selected="true">Belarus</li>
						            <li class="mdl-menu__item" data-val="BR">Brazil</li>
						            <li class="mdl-menu__item" data-val="ES">Estonia</li>
						            <li class="mdl-menu__item" data-val="FI">Finland</li>
						            <li class="mdl-menu__item" data-val="FR">France</li>
						            <li class="mdl-menu__item" data-val="DE">Germany</li>
						            <li class="mdl-menu__item" data-val="PL">Poland</li>
						            <li class="mdl-menu__item" data-val="RU">Russia</li>
						        </ul>
						    </div>

						</br>

						<div class="mdl-textfield mdl-js-textfield mdl-textfield--floating-label">
							<input class="mdl-textfield__input" type="text" name="conversationDescription" id="description">
							<label class="mdl-textfield__label" for="description">Short Description...</label>
						</div>
						</br>
						<button name="conversation" value="conversation" class="mdl-button mdl-js-button mdl-button--raised" type="submit">Create</button>
		      </form>

			  <form action="/conversations" method="POST">
				  <h1>Group Message</h1>
				  <button type="submit" name="group" value="group">Private Group</button>
			   </form>
		      <hr/>
		    <% } %>

		    <h1>Conversations</h1>

		    <%
		    List<Conversation> conversations =
		      (List<Conversation>) request.getAttribute("conversations");
			List<Group> groups =
			  (List<Group>) request.getAttribute("groups");
		    if(conversations == null || conversations.isEmpty()){
		    %>
		      <p>Create a conversation to get started.</p>
		    <%
		    }
		    else if(conversations != null){
		    %>
		      <ul class="mdl-list">
		    <% //Display 10 conversations at a time
		    for(Conversation conversation : conversations){
		    %>
			<li class="mdl-list__item">
				<span class="mdl-list__item-primary-content">
					<i class="material-icons mdl-list__item-avatar">group</i>
					<a class="mdl-navigation__link" href="/chat/<%= conversation.getTitle() %>">
 					   <button class="mdl-button mdl-js-button mdl-button--raised mdl-button--accent"><%= conversation.getTitle() %></button>
 				   </a>
					<%-- <a class="mdl-navigation__link" href="/chat/<%= conversation.getTitle() %>">
					<%= conversation.getTitle() %></a> --%>
				</span>
			</li>
		    <%
		      }
		    %>
		      </ul>
		    <%
		    }
			%>
			</hr>
			<% if(request.getSession().getAttribute("uuid") != null){ // check if signed in!
				UUID id = (UUID) request.getSession().getAttribute("uuid"); %>
				<h1>Private Messages</h1>
				<% if(groups != null){
				%>
					<ul class="mdl-list">
			    <%
			       for(Group group : groups){
						if(group.isAccessAllowed(id)){
					//only display the private conversation that the user is a part of
			    %>
			        <li class="mdl-list__item">
						<span class="mdl-list__item-primary-content">
							<i class="material-icons mdl-list__item-avatar">group</i>
							<a class="mdl-navigation__link" href="/chat/<%= group.getTitle() %>">
				          	<%= group.getTitle() %></a>
						</span>
				  	</li>
			    <%
			        	}
					}
			    %>
			        </ul>
				<% } %>
			<% } %>
		    <hr/>
		  </div>
	  </div>
</div>
</main>
</body>
</html>
