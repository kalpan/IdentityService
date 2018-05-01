package com.identityservice;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.identityservice.controller.UserController;
import com.identityservice.dto.User;

/*
 * Integration tests that test each REST API call.
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IdentityServiceApplicationTests {

	public static final Logger logger = LoggerFactory.getLogger(UserController.class);
	public static final String TEST_URI = "/api";

	@Autowired
	private TestRestTemplate restTemplate;

	/*
	 * Headers needed for Basic Authentication
	 */
	private HttpHeaders getHeaders() {
		String plainCredentials = "admin:admin";
		String base64Credentials = new String(Base64.encodeBase64(plainCredentials.getBytes()));

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + base64Credentials);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		return headers;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void getAllUsers() {
		HttpEntity<String> request = new HttpEntity<String>(getHeaders());
		ResponseEntity<List> response = restTemplate.exchange(TEST_URI + "/user/", HttpMethod.GET, request, List.class);
		List<LinkedHashMap<String, Object>> usersMap = response.getBody();

		if (usersMap != null) {
			for (LinkedHashMap<String, Object> map : usersMap) {
				logger.info(map.toString());
			}
		} else {
			logger.info("There are no users.");
		}
	}

	private void createUser() {
		User user = new User("John", "Tester", "jtester", "password");
		HttpEntity<Object> request = new HttpEntity<Object>(user, getHeaders());
		ResponseEntity<User> response = restTemplate.exchange(TEST_URI + "/admin/user/", HttpMethod.POST, request,
				User.class);
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
	}

	private void getUser() {
		HttpEntity<String> request = new HttpEntity<String>(getHeaders());
		ResponseEntity<User> response = restTemplate.exchange(TEST_URI + "/user/jtester", HttpMethod.GET, request,
				User.class);
		User user = response.getBody();
		assertEquals("jtester", user.getUserName());
	}

	private void updateUser() {
		User user = new User("John", "TesterChanged", "jtester");
		HttpEntity<Object> request = new HttpEntity<Object>(user, getHeaders());
		ResponseEntity<User> response = restTemplate.exchange(TEST_URI + "/admin/user/jtester", HttpMethod.PUT, request,
				User.class);
		User updatedUser = response.getBody();
		assertEquals("TesterChanged", updatedUser.getLastName());
	}

	private void deleteUser() {
		HttpEntity<String> request = new HttpEntity<String>(getHeaders());
		ResponseEntity<User> response = restTemplate.exchange(TEST_URI + "/admin/user/jtester", HttpMethod.DELETE,
				request, User.class);
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	private void deleteAllUsers() {
		HttpEntity<String> request = new HttpEntity<String>(getHeaders());
		ResponseEntity<User> response = restTemplate.exchange(TEST_URI + "/admin/user/", HttpMethod.DELETE, request,
				User.class);
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	@Test
	public void testInOrder() {
		deleteAllUsers();
		getAllUsers();
		createUser();
		getUser();
		updateUser();
		getAllUsers();
		deleteUser();
		deleteAllUsers();
	}
}
