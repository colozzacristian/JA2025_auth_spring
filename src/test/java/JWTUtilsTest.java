

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import javax.crypto.SecretKey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;
import it.eforhum.authModule.entities.Group;
import it.eforhum.authModule.entities.Token;
import it.eforhum.authModule.entities.User;
import it.eforhum.authModule.utils.JWTUtils;


public class JWTUtilsTest {

	private static final SecretKey SECRET_KEY = hmacShaKeyFor(Dotenv.load().get("JWT_SECRET").getBytes());
	private static final User testUser = new User(1L, "a@a.a", "hash", "a", "pino", true, LocalDateTime.now(), LocalDateTime.now());
	static{
		testUser.setGroups(Set.of(new Group(1L, "USER"), new Group(2L, "ADMIN")));
	}
	@Test
	public void generateJWT_activeUser_shouldContainClaims() throws Exception {
		List<String> groups = List.of("ADMIN","USER");
	    Token token = JWTUtils.generateJWT(testUser);
		List<String> groupsJWT = null;
		
		
		assertNotNull("Token should not be null", token);
		
		Claims claims = Jwts.parser().verifyWith(SECRET_KEY).build()
                .parseSignedClaims(token.getToken())
                .getPayload();

		assertEquals(testUser.getEmail(), claims.get("email"));
		assertEquals(testUser.getFirstName(), claims.get("firstName"));
		assertEquals(testUser.getLastName(), claims.get("lastName"));
		assertEquals((Long) testUser.getUserId(), (Long) ((Number)claims.get("userId")).longValue());
		groupsJWT = (List<String>) claims.get("groups");
		assertEquals(groups.size(), groupsJWT.size());
		for( String s : groupsJWT ){
			assert(groups.contains(s));
		}	
		
	}

	@Test(expected = IllegalArgumentException.class)
	public void generateJWT_inactiveUser_shouldThrow() {
		User user = new User(2L, "", "hash", "Bob", "Jones", false, LocalDateTime.now(), LocalDateTime.now());
		JWTUtils.generateJWT(user);
	}

}
