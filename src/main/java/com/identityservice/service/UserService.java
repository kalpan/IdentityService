package com.identityservice.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.identityservice.dto.User;

/*
 * User Identity Service Contract
 * 
 */
public interface UserService {

	/**
	 * @param id
	 * @return user
	 */
	User findById(long id);

	/**
	 * @param name
	 * @return user
	 */
	User findByUserName(String name);
	
	/**
	 * @param name
	 * @return user
	 */
	CompletableFuture<User> findByUserNameAsync(String name);
	
	/**
	 * This is to demonstrate a delayed request as well as a timeout situation
	 * 
	 * @param name
	 * @return user
	 */
	CompletableFuture<User> findByUserNameAsyncDelayed(String userName, long delayInMillis);

	/**
	 * @param user
	 */
	void saveUser(User user);

	/**
	 * @param user
	 */
	void updateUser(User user);

	/**
	 * @param id
	 */
	void deleteUserById(long id);

	/**
	 * @return users
	 */
	List<User> findAllUsers();

	void deleteAllUsers();

	/**
	 * @param user
	 * @return boolean
	 */
	public boolean isUserExist(User user);

}
