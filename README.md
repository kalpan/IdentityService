# IdentityService

Clone:
	git clone https://github.com/kalpan/IdentityService.git

Test & Build:
	mvn clean install
	
Test:
	mvn test
	
Run:
	mvn spring-boot:run
	

After running:
	Example use cases with cURL:
  
  Create user:
  curl -v -u 'admin:admin' http://localhost:8080/api/user -H "Content-Type: application/json" -X POST -d '{"firstName":"john", "lastName":"doe", "userName":"jdoe", "password": "blabla"}'
  
  Get user:
  curl -v http://localhost:8080/api/user/zoe -H "Content-Type: application/json" -u 'john:blabla'
  
  Asynchronous get user:
  curl -v http://localhost:8080/api/async/user/zoe -H "Content-Type: application/json" -u 'john:blabla'
  
  
Integration Tests:  
  Please refer to IdentityServiceApplicationTests.java for Java REST examples for each API call.