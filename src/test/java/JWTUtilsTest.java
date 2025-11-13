

import java.time.LocalDateTime;

import javax.crypto.SecretKey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;
import it.eforhum.authModule.entities.User;
import it.eforhum.authModule.utils.JWTUtils;


public class JWTUtilsTest {

	private static final SecretKey SECRET_KEY = hmacShaKeyFor(Dotenv.load().get("JWT_SECRET").getBytes());
	private static final User testUser = new User(1, "a@a.a", "hash", "a", "pino", true, LocalDateTime.now(), LocalDateTime.now());

	@Test
	public void generateJWT_activeUser_shouldContainClaims() throws Exception {
		String[] roles = {"USER","ADMIN"};
		String token = JWTUtils.generateJWT(testUser);
		assertNotNull("Token should not be null", token);
		
		Claims claims = Jwts.parser().verifyWith(SECRET_KEY).build()
                .parseSignedClaims(token)
                .getPayload();

		assertEquals(testUser.getEmail(), claims.get("email"));
		assertEquals(testUser.getFirstName(), claims.get("firstName"));
		assertEquals(testUser.getLastName(), claims.get("lastName"));
		assertEquals(testUser.getUserId(), ((Number)claims.get("userId")).intValue());
		assertEquals(roles, claims.get("roles"));// temp
	}

	@Test(expected = IllegalArgumentException.class)
	public void generateJWT_inactiveUser_shouldThrow() {
		User user = new User(2, "", "hash", "Bob", "Jones", false, LocalDateTime.now(), LocalDateTime.now());
		JWTUtils.generateJWT(user);
	}

}
