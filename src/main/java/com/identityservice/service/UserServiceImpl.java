package com.identityservice.service;

import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import com.identityservice.controller.UserController;
import com.identityservice.dto.User;

/*
 * User Identity Service Implementation
 * 
 * Methods are secured by Roles.
 * 
 */
@Service("userService")
public class UserServiceImpl implements UserService {

	public static final Logger logger = LoggerFactory.getLogger(UserController.class);
	private static Map<String, User> usersCache;

	static {
		usersCache = populateDemoDatabase();
	}

	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	@Override
	public User findById(long id) {
		return usersCache.values().stream().filter(u -> u.getId().equals(id)).findFirst().orElse(null);
	}

	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	@Override
	public User findByUserName(String userName) {
		return usersCache.get(userName);
	}

	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	@Async
	@Override
	public CompletableFuture<User> findByUserNameAsync(String userName) {
		if (usersCache.containsKey(userName))
			return CompletableFuture.completedFuture(usersCache.get(userName));
		else
			return null;
	}

	@Secured("ROLE_ADMIN")
	@Override
	public void saveUser(User user) {
		user.completeDefaultsIfMissing();
		usersCache.put(user.getUserName(), user);
	}

	@Secured("ROLE_ADMIN")
	@Override
	public void updateUser(User user) {
		user.setUpdateDate(Calendar.getInstance());
		usersCache.put(user.getUserName(), user);
	}

	@Secured("ROLE_ADMIN")
	@Override
	public void deleteUserById(long id) {
		User user = findById(id);
		usersCache.remove(user.getUserName());
	}

	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	@Override
	public List<User> findAllUsers() {
		return usersCache.entrySet().stream().sorted((e1, e2) -> e1.getValue().getId().compareTo(e2.getValue().getId()))
				.map(Map.Entry::getValue).collect(Collectors.toList());
	}

	@Secured("ROLE_ADMIN")
	@Override
	public void deleteAllUsers() {
		usersCache.clear();
	}

	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	@Override
	public boolean isUserExist(User user) {
		return findByUserName(user.getUserName()) != null;
	}

	/*
	 * New users are added to this demo database, in other words users cache.
	 */
	private static Map<String, User> populateDemoDatabase() {
		Map<String, User> users = new HashMap<>();
		User u1 = new User("admin", "admin", "admin", "admin");
		users.put(u1.getUserName(), u1);
		User u2 = new User("guest", "guest", "guest", "guest");
		users.put(u2.getUserName(), u2);
		return users;
	}

}
