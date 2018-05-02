package com.identityservice.service;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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
	private final ReadWriteLock stateLock = new ReentrantReadWriteLock();
	private static Map<String, User> usersCache;

	static {
		usersCache = populateDemoDatabase();
	}

	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	@Override
	public User findById(long id) {
		this.stateLock.readLock().lock();
		try {
			return usersCache.values().stream().filter(u -> u.getId().equals(id)).findFirst().orElse(null);
		} finally {
			stateLock.readLock().unlock();
		}
	}

	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	@Override
	public User findByUserName(String userName) {
		this.stateLock.readLock().lock();
		try {
			return usersCache.get(userName);
		} finally {
			stateLock.readLock().unlock();
		}
	}

	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	@Async
	@Override
	public CompletableFuture<User> findByUserNameAsync(String userName) {
		this.stateLock.readLock().lock();
		try {
			if (usersCache.containsKey(userName))
				return CompletableFuture.supplyAsync(() -> usersCache.get(userName));
			else
				return null;
		} finally {
			stateLock.readLock().unlock();
		}
	}
	
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	@Async
	@Override
	public CompletableFuture<User> findByUserNameAsyncDelayed(String userName, long delayInMillis) {
		this.stateLock.readLock().lock();
		try {
			if (usersCache.containsKey(userName))
				return CompletableFuture.supplyAsync(() -> getUserFromCacheDelayed(userName, delayInMillis));
			else
				return null;
		} finally {
			stateLock.readLock().unlock();
		}
	}
	
	private User getUserFromCacheDelayed(String userName, long delayInMillis) {
		try {
			Thread.sleep(delayInMillis);
		} catch (InterruptedException e) {
			//ignore
		}
		return usersCache.get(userName);
	}

	@Secured("ROLE_ADMIN")
	@Override
	public void saveUser(User user) {
		user.completeDefaultsIfMissing();
		stateLock.writeLock().lock();
		try {
			usersCache.put(user.getUserName(), user);
		} finally {
			stateLock.writeLock().unlock();
		}
	}

	@Secured("ROLE_ADMIN")
	@Override
	public void updateUser(User user) {
		user.setUpdateDate(Calendar.getInstance());
		stateLock.writeLock().lock();
		try {
			usersCache.put(user.getUserName(), user);
		} finally {
			stateLock.writeLock().unlock();
		}
	}

	@Secured("ROLE_ADMIN")
	@Override
	public void deleteUserById(long id) {
		User user = findById(id);
		stateLock.writeLock().lock();
		try {
			usersCache.remove(user.getUserName());
		} finally {
			stateLock.writeLock().unlock();
		}
	}

	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	@Override
	public List<User> findAllUsers() {
		this.stateLock.readLock().lock();
		try {
			return usersCache.entrySet().stream().sorted((e1, e2) -> e1.getValue().getId().compareTo(e2.getValue().getId()))
					.map(Map.Entry::getValue).collect(Collectors.toList());
		} finally {
			stateLock.readLock().unlock();
		}
	}

	@Secured("ROLE_ADMIN")
	@Override
	public void deleteAllUsers() {
		stateLock.writeLock().lock();
		try {
			usersCache.clear();
		} finally {
			stateLock.writeLock().unlock();
		}
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
		Map<String, User> users = new ConcurrentHashMap<>();
		User u1 = new User("admin", "admin", "admin", "admin");
		users.put(u1.getUserName(), u1);
		User u2 = new User("guest", "guest", "guest", "guest");
		users.put(u2.getUserName(), u2);
		return users;
	}

}
