package it.eforhum.auth_module;


import static java.lang.String.format;

import java.net.http.HttpClient;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.eforhum.auth_module.entity.Token;
import it.eforhum.auth_module.entity.User;
import it.eforhum.auth_module.repository.TokenStore;
import it.eforhum.auth_module.repository.UserDAO;
import it.eforhum.auth_module.service.JWTUtils;



/* 
@SpringBootTest
@AutoConfigureMockMvc
public class GroupsCheckTest {

    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static boolean initialized = false;

    private static String[] expectedGroups = {"USER"};


    private MockMvc mockMvc;
    private Token jwtToken;
    private User testUser;

    @Before
    public void setUp(@Autowired JWTUtils jwtUtils,
                      @Autowired TokenStore tokenStore,
                      @Autowired MockMvc mockMvc,
                      @Autowired UserDAO userDao) {
        if (!initialized) {
            this.mockMvc = mockMvc;
        }

        testUser = userDao.getByEmail("a@a.a");
        jwtToken = jwtUtils.generateJWT(testUser);
        tokenStore.getJwtTokens().saveToken(jwtToken);
        
    }


    @Test
    public void testGroupsNoParam() throws Exception {
        mockMvc.perform(get("/token/groups")
                .header("Authorization", format("Bearer %s", jwtToken.getTokenValue())))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.groups").isArray());
    }



    @Test
    public void testGroupsParam() throws Exception {
        mockMvc.perform(get("/token/groups")
                .param("g", "USER")
                .header("Authorization", format("Bearer %s", jwtToken.getTokenValue())))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isInGroups").value(true));
    }

    @Test
    public void testGroupsWrongParam() throws Exception {
        mockMvc.perform(get("/token/groups")
                .param("g", "USER,,ADMIN")
                .header("Authorization", format("Bearer %s", jwtToken.getTokenValue())))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isInGroups").value(false));
    }

    @Test
    public void testGroupsWrongToken() throws Exception {
        mockMvc.perform(get("/token/groups")
                .param("g", "USER,")
                .header("Authorization", format("Bearer a%s", jwtToken.getTokenValue())))
                .andExpect(status().isUnauthorized());
    }

}
*/