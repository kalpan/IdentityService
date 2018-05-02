package com.identityservice.controller;

import java.security.Principal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.identityservice.dto.User;
import com.identityservice.service.UserService;

/*
 * REST Controller for the User Identity Service
 * 
 * Example use cases with cURL:
 * 
 * Create user:
 * curl -v -u 'admin:admin' http://localhost:8080/api/user -H
 * "Content-Type: application/json" -X POST -d '{"firstName":"john",
 * "lastName":"doe", "userName":"jdoe"}'
 * 
 * Get user:
 * curl -v http://localhost:8080/api/user/zoe -H
 * "Content-Type: application/json" -u 'john:doe'
 * 
 * Asynchronous get user:
 * curl -v http://localhost:8080/api/async/user/zoe -H
 * "Content-Type: application/json" -u 'john:doe'
 * 
 */
@RestController
@RequestMapping("/api")
public class UserController {

	public static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	UserService userService;

	private final InMemoryUserDetailsManager inMemoryUserDetailsManager;

	@Autowired
	public UserController(InMemoryUserDetailsManager inMemoryUserDetailsManager) {
		this.inMemoryUserDetailsManager = inMemoryUserDetailsManager;
	}

	/**
	 * createUser
	 * 	- save user in cache/db
	 *  - adds user info (username, password, role) into inMemoryUserDetailsManager
	 * 
	 * @param principal
	 * @param user
	 * @param uriComponentsBuilder
	 * @return
	 */
	@RequestMapping(value = "/admin/user", method = RequestMethod.POST)
	public ResponseEntity<Void> createUser(Principal principal, @Valid @RequestBody User user,
			UriComponentsBuilder uriComponentsBuilder) {
		Authentication authentication = (Authentication) principal;
		org.springframework.security.core.userdetails.User adminUser = (org.springframework.security.core.userdetails.User) authentication
				.getPrincipal();
		logger.debug("POST/createUser requesting user: " + adminUser.toString());
		logger.debug("POST User " + user.getUserName());

		if (userService.isUserExist(user)) {
			logger.debug("User with username " + user.getUserName() + " already exists.");
			return new ResponseEntity<Void>(HttpStatus.CONFLICT);
		}

		userService.saveUser(user);
		inMemoryUserDetailsManager.createUser(org.springframework.security.core.userdetails.User
				.withUsername(user.getUserName()).password(user.getPassword()).authorities("ROLE_USER").build());

		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(uriComponentsBuilder.path("/api/user/{userName}").buildAndExpand(user.getUserName()).toUri());
		return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
	}

	/**
	 * listAllUsers
	 * 
	 * @param principal
	 * @return users
	 */
	@RequestMapping(value = "/user", method = RequestMethod.GET)
	public ResponseEntity<List<User>> listAllUsers(Principal principal) {
		Authentication authentication = (Authentication) principal;
		org.springframework.security.core.userdetails.User reqUser = (org.springframework.security.core.userdetails.User) authentication
				.getPrincipal();
		logger.debug("GET/listAllUsers requesting user: " + reqUser.toString());

		List<User> users = userService.findAllUsers();
		if (users.isEmpty())
			return new ResponseEntity<List<User>>(HttpStatus.NO_CONTENT);

		return new ResponseEntity<List<User>>(users, HttpStatus.OK);
	}

	/**
	 * getUser
	 * 
	 * @param userName
	 * @return user
	 */
	@RequestMapping(value = "/user/{userName}", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public ResponseEntity<?> getUser(Principal principal, @PathVariable("userName") String userName) {
		Authentication authentication = (Authentication) principal;
		org.springframework.security.core.userdetails.User reqUser = (org.springframework.security.core.userdetails.User) authentication
				.getPrincipal();
		logger.debug("GET/getUser requesting user: " + reqUser.toString());
		logger.debug("GET User with userName {}", userName);
		
		User user = userService.findByUserName(userName);
		if (user == null) {
			logger.debug("User with userName {} not found.", userName);
			return new ResponseEntity<Object>(String.format("User with userName %s is not found.", userName),
					HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<User>(user, HttpStatus.OK);
	}

	/**
	 * getUserAsync
	 * 
	 * @param userName
	 * @return user
	 */
	@RequestMapping(value = "/async/user/{userName}", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> getUserAsync(Principal principal, @PathVariable("userName") String userName) {
		Authentication authentication = (Authentication) principal;
		org.springframework.security.core.userdetails.User reqUser = (org.springframework.security.core.userdetails.User) authentication
				.getPrincipal();
		logger.debug("GET/getUserAsync requesting user: " + reqUser.toString());
		logger.debug("GET User with userName {}", userName);
		CompletableFuture<User> user = userService.findByUserNameAsync(userName);
		if (user == null) {
			logger.debug("User with userName {} not found.", userName);
			return new ResponseEntity<Object>(String.format("User with userName %s is not found.", userName),
					HttpStatus.NOT_FOUND);
		}

		String err;
		try {
			return new ResponseEntity<User>(user.get(30, TimeUnit.SECONDS), HttpStatus.OK);
		} catch (InterruptedException e) {
			logger.error(String.format("InterruptedException:%s", e));
			err = e.getMessage();
		} catch (ExecutionException e) {
			logger.error(String.format("ExecutionException:%s", e));
			err = e.getMessage();
		} catch (TimeoutException e) {
			logger.error(String.format("TimeoutException:%s", e));
			err = e.getMessage();
		}
		return new ResponseEntity<String>(String.format("{'ERR': '%s'}", err), HttpStatus.REQUEST_TIMEOUT);
	}

	/**
	 * updateUser
	 * 
	 * @param userName
	 * @param user
	 * @return updatedUser
	 */
	@RequestMapping(value = "/admin/user/{userName}", method = RequestMethod.PUT)
	public ResponseEntity<?> updateUser(Principal principal, @PathVariable("userName") String userName, @RequestBody User user) {
		Authentication authentication = (Authentication) principal;
		org.springframework.security.core.userdetails.User reqUser = (org.springframework.security.core.userdetails.User) authentication
				.getPrincipal();
		logger.debug("PUT/updateUser requesting user: " + reqUser.toString());
		logger.debug("PUT User with userName {}", userName);

		User currentUser = userService.findByUserName(userName);

		if (currentUser == null) {
			logger.debug("Unable to update. User with userName {} not found.", userName);
			return new ResponseEntity<Object>(
					String.format("Unable to upate. User with userName %s is not found.", userName),
					HttpStatus.NOT_FOUND);
		}

		currentUser.setId(user.getId());
		currentUser.setFirstName(user.getFirstName());
		currentUser.setLastName(user.getLastName());
		currentUser.setUserName(user.getUserName());
		currentUser.setEmail(user.getEmail());
		currentUser.setPassword(user.getPassword());

		userService.updateUser(currentUser);
		return new ResponseEntity<User>(currentUser, HttpStatus.OK);
	}

	/**
	 * deleteUser
	 * 
	 * 	TBD: instead deleting user, set status to terminated
	 * 
	 * @param userName
	 * @return
	 */
	@RequestMapping(value = "/admin/user/{userName}", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteUser(Principal principal, @PathVariable("userName") String userName) {
		Authentication authentication = (Authentication) principal;
		org.springframework.security.core.userdetails.User reqUser = (org.springframework.security.core.userdetails.User) authentication
				.getPrincipal();
		logger.debug("DELETE/deleteUser requesting user: " + reqUser.toString());
		logger.debug("Fetching & Deleting User with userName {}", userName);

		User user = userService.findByUserName(userName);
		if (user == null) {
			logger.error("Unable to delete. User with userName %s not found.", userName);
			return new ResponseEntity<Object>(
					String.format("Unable to delete. User with id %d is not found.", userName), HttpStatus.NOT_FOUND);
		}
		userService.deleteUserById(user.getId());
		return new ResponseEntity<User>(HttpStatus.NO_CONTENT);
	}

	/**
	 * deleteAllUsers
	 * 	- mainly used for testing.
	 *
	 * @return
	 */
	@RequestMapping(value = "/admin/user/", method = RequestMethod.DELETE)
	public ResponseEntity<User> deleteAllUsers(Principal principal) {
		Authentication authentication = (Authentication) principal;
		org.springframework.security.core.userdetails.User reqUser = (org.springframework.security.core.userdetails.User) authentication
				.getPrincipal();
		logger.debug("DELETE/deleteAllUsers requesting user: " + reqUser.toString());
		logger.debug("Deleting All Users");

		userService.deleteAllUsers();
		return new ResponseEntity<User>(HttpStatus.NO_CONTENT);
	}

}
