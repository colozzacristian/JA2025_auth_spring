package it.eforhum.auth_module;


import java.util.List;
import java.util.Set;

import javax.crypto.SecretKey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import it.eforhum.auth_module.entity.Token;
import it.eforhum.auth_module.entity.User;
import it.eforhum.auth_module.service.JWTUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JWTUtilsTest {


	
	private static final User testUser = new User(1L, "a@a.a", "hash", "a", "pino", true);
	static{
		testUser.setGroups(Set.of("USER","ADMIN"));
	}

	@Value("${token.secret}")
	private String secretKey;

	@Autowired
	private JWTUtils jwtUtils;

	@Test
	public void generateJWT_activeUser_shouldContainClaims() throws Exception {
		String[] groups = {"ADMIN","USER"};
	    Token token = jwtUtils.generateJWT(testUser);
		List<String> groupsJWT;
		
		
		assertNotNull("Token should not be null", token);
		
		Claims claims = Jwts.parser().verifyWith(hmacShaKeyFor(secretKey.getBytes())).build()
                .parseSignedClaims(token.getTokenValue())
                .getPayload();

		assertEquals(testUser.getEmail(), claims.get("email"));
		assertEquals(testUser.getFirstName(), claims.get("firstName"));
		assertEquals(testUser.getLastName(), claims.get("lastName"));
		assertEquals(testUser.getUserId(), (Long) ((Number)claims.get("userId")).longValue());
		groupsJWT = (List<String>) claims.get("groups");
		assertEquals(groups.length, groupsJWT.size());
		for( String s : groupsJWT ){
			assert(List.of(groups).contains(s));
		}	
		
	}

	@Test(expected = IllegalArgumentException.class)
	public void generateJWT_inactiveUser_shouldThrow() {
		User user = new User(2L, "a", "hash", "Bob", "Jones", false);
		jwtUtils.generateJWT(user);
	}

}
