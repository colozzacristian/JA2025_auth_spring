import java.util.List;
import java.util.Set;

import javax.crypto.SecretKey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;
import it.eforhum.auth_module.entities.Token;
import it.eforhum.auth_module.entities.User;
import it.eforhum.auth_module.utils.JWTUtils;


public class JWTUtilsTest {

	private static final SecretKey SECRET_KEY = hmacShaKeyFor(System.getenv("JWT_SECRET").getBytes());
	private static final User testUser = new User(1L, "a@a.a", "hash", "a", "pino", true);
	static{
		testUser.setGroups(Set.of("USER","ADMIN"));
	}
	@Test
	public void generateJWT_activeUser_shouldContainClaims() throws Exception {
		List<String> groups = List.of("ADMIN","USER");
	    Token token = JWTUtils.generateJWT(testUser);
		List<String> groupsJWT;
		
		
		assertNotNull("Token should not be null", token);
		
		Claims claims = Jwts.parser().verifyWith(SECRET_KEY).build()
                .parseSignedClaims(token.getTokenValue())
                .getPayload();

		assertEquals(testUser.getEmail(), claims.get("email"));
		assertEquals(testUser.getFirstName(), claims.get("firstName"));
		assertEquals(testUser.getLastName(), claims.get("lastName"));
		assertEquals(testUser.getUserId(), (Long) ((Number)claims.get("userId")).longValue());
		groupsJWT = (List<String>) claims.get("groups");
		assertEquals(groups.size(), groupsJWT.size());
		for( String s : groupsJWT ){
			assert(groups.contains(s));
		}	
		
	}

	@Test(expected = IllegalArgumentException.class)
	public void generateJWT_inactiveUser_shouldThrow() {
		User user = new User(2L, "a", "hash", "Bob", "Jones", false);
		JWTUtils.generateJWT(user);
	}

}
