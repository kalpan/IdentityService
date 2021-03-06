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
import com.identityservice.dto.Status;
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
	private static final long ASYNC_TIMEOUT_SECS = 10L;

	@Autowired
	UserService userService;

	@Autowired
	private InMemoryUserDetailsManager inMemoryUserDetailsManager;


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
	@RequestMapping(value = "/user", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
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
	@RequestMapping(value = "/user/{userName}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
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

		if (user.getFirstName() != null)
			currentUser.setFirstName(user.getFirstName());
		
		if (user.getLastName() != null)
			currentUser.setLastName(user.getLastName());

		if (user.getEmail() != null)
			currentUser.setEmail(user.getEmail());
		
		if (user.getPassword() != null)
			currentUser.setPassword(user.getPassword());
		
		if (user.getStatus() != null) {
			currentUser.setStatus(user.getStatus());
			
			// Based on Status: Remove user's access if deactivated OR Activate user's access if it doesn't exist.
			if (currentUser.getStatus().equals(Status.INACTIVE))
				inMemoryUserDetailsManager.deleteUser(currentUser.getUserName());
			else if (user.getStatus().equals(Status.ACTIVE)) {
				if (!inMemoryUserDetailsManager.userExists(currentUser.getUserName())) {
						inMemoryUserDetailsManager.createUser(org.springframework.security.core.userdetails.User
								.withUsername(currentUser.getUserName()).password(currentUser.getPassword()).authorities("ROLE_USER").build());
				}
			}
		}

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
					String.format("Unable to delete. User with id %s is not found.", userName), HttpStatus.NOT_FOUND);
		}
		userService.deleteUserById(user.getId());
		inMemoryUserDetailsManager.deleteUser(user.getUserName());
		return new ResponseEntity<User>(HttpStatus.NO_CONTENT);
	}

	/**
	 * deleteAllUsers
	 * 	- mainly used for testing.
	 *
	 * @return
	 */
	@RequestMapping(value = "/admin/user", method = RequestMethod.DELETE)
	public ResponseEntity<User> deleteAllUsers(Principal principal) {
		Authentication authentication = (Authentication) principal;
		org.springframework.security.core.userdetails.User reqUser = (org.springframework.security.core.userdetails.User) authentication
				.getPrincipal();
		logger.debug("DELETE/deleteAllUsers requesting user: " + reqUser.toString());
		logger.debug("Deleting All Users");

		List<User> users = userService.findAllUsers();
		userService.deleteAllUsers();
		users.forEach(u -> inMemoryUserDetailsManager.deleteUser(u.getUserName()));
		return new ResponseEntity<User>(HttpStatus.NO_CONTENT);
	}
	
	/** Async APIs */
	
	/**
	 * getUserAsync
	 * 
	 * @param userName
	 * @return user
	 */
	@RequestMapping(value = "/async/user/{userName}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> getUserAsync(Principal principal, @PathVariable("userName") String userName) {
		Authentication authentication = (Authentication) principal;
		org.springframework.security.core.userdetails.User reqUser = (org.springframework.security.core.userdetails.User) authentication
				.getPrincipal();
		logger.debug("GET/getUserAsync requesting user: " + reqUser.toString());
		logger.debug("GET User with userName {}", userName);
		
		String err;
		try {
			CompletableFuture<User> userFuture = userService.findByUserNameAsync(userName);
			if (userFuture == null) {
				logger.debug("User with userName {} not found.", userName);
				return new ResponseEntity<Object>(String.format("User with userName %s is not found.", userName),
						HttpStatus.NOT_FOUND);
			}
			
			User user = userFuture.get(ASYNC_TIMEOUT_SECS, TimeUnit.SECONDS);
			return new ResponseEntity<User>(user, HttpStatus.OK);
		} catch (InterruptedException e) {
			logger.error(String.format("InterruptedException:%s", e));
			err = "Request was interrupted.";
		} catch (ExecutionException e) {
			logger.error(String.format("ExecutionException:%s", e));
			err = "There was an execution error.";
		} catch (TimeoutException e) {
			logger.error(String.format("TimeoutException:%s", e));
			err = "Request timed out.";
		}
		return new ResponseEntity<String>(String.format("{'ERR': '%s'}", err), HttpStatus.REQUEST_TIMEOUT);
	}
	
	/**
	 * getUserAsync
	 * 
	 * @param userName
	 * @return user
	 */
	@RequestMapping(value = "/async/user/{userName}/{delay}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> getUserAsyncDelayed(Principal principal, @PathVariable("userName") String userName, @PathVariable("delay") Long delay) {
		Authentication authentication = (Authentication) principal;
		org.springframework.security.core.userdetails.User reqUser = (org.springframework.security.core.userdetails.User) authentication
				.getPrincipal();
		logger.debug("GET/getUserAsync requesting user: " + reqUser.toString());
		logger.debug("GET User with userName {}", userName);
		
		String err;
		try {
			CompletableFuture<User> userFuture = userService.findByUserNameAsyncDelayed(userName, delay.longValue());
			if (userFuture == null) {
				logger.debug("User with userName {} not found.", userName);
				return new ResponseEntity<Object>(String.format("User with userName %s is not found.", userName),
						HttpStatus.NOT_FOUND);
			}
			
			User user = userFuture.get(ASYNC_TIMEOUT_SECS, TimeUnit.SECONDS);
			return new ResponseEntity<User>(user, HttpStatus.OK);
		} catch (InterruptedException e) {
			logger.error(String.format("InterruptedException:%s", e));
			err = "Request was interrupted.";
		} catch (ExecutionException e) {
			logger.error(String.format("ExecutionException:%s", e));
			err = "There was an execution error.";
		} catch (TimeoutException e) {
			logger.error(String.format("TimeoutException:%s", e));
			err = "Request timed out.";
		}
		return new ResponseEntity<String>(String.format("{'ERR': '%s'}", err), HttpStatus.REQUEST_TIMEOUT);
	}


}
