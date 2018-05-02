# IdentityService

Clone:
```
	git clone https://github.com/kalpan/IdentityService.git
```

Test & Build:
```
	mvn clean install
```
	
Test:
```
	mvn test
```
	
Run:
```
	mvn spring-boot:run
```

After running:
	Example use cases with cURL:
  
  Create user:
  ```
  curl -v http://localhost:8080/api/admin/user -H "Content-Type: application/json" -X POST -d '{"firstName":"john", "lastName":"doe", "userName":"jdoe", "password": "blabla"}' -u 'admin:admin'
  
  curl -v http://localhost:8080/api/admin/user -H "Content-Type: application/json" -X POST -d '{"firstName":"zoe", "lastName":"zoe", "userName":"zoe", "password":"password"}' -u 'admin:admin'
  ```
  
  Get user:
  ```
  curl -v http://localhost:8080/api/user/zoe -H "Content-Type: application/json" -u 'jdoe:blabla'
  ```
  
  Asynchronous get user:
  ```
  curl -v http://localhost:8080/api/async/user/zoe -H "Content-Type: application/json" -u 'jdoe:blabla'
  ```
  
  List all users:
  ```
  curl -v http://localhost:8080/api/user -H "Content-Type: application/json" -u 'jdoe:blabla'
  ```
  
  Update user:
  ```
  curl -v http://localhost:8080/api/admin/user/zoe -H "Content-Type: application/json" -X PUT -d '{"userName":"zoe", "status":"INACTIVE"}' -u 'admin:admin'
  ```
  
  Delete user:
  ```
  curl -v http://localhost:8080/api/admin/user/zoe -H "Content-Type: application/json" -X DELETE -u 'admin:admin'
  ```
  
  Delete all users:
  ```
  curl -v http://localhost:8080/api/admin/user -H "Content-Type: application/json" -X DELETE -u 'admin:admin'
  ```
  
  
Integration Tests:  
  Please refer to IdentityServiceApplicationTests.java for Java REST examples for each API call.