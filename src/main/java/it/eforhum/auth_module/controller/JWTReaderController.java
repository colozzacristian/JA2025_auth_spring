package it.eforhum.auth_module.controller;

import org.springframework.web.bind.annotation.RestController;

import it.eforhum.auth_module.dto.EmailRespDTO;
import it.eforhum.auth_module.dto.GroupsListRespDTO;
import it.eforhum.auth_module.dto.InGroupsRespDTO;
import it.eforhum.auth_module.service.JWTUtils;
import it.eforhum.auth_module.repository.TokenStore;
import it.eforhum.auth_module.entity.User;
import it.eforhum.auth_module.service.RateLimitingUtils;
import it.eforhum.auth_module.repository.UserDAO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;





@RestController
@Slf4j
@RequestMapping("/token/")
public class JWTReaderController {

    private final TokenStore tokenStore;
    private final JWTUtils jwtUtils;
    private final RateLimitingUtils rateLimitingUtils;
    private final UserDAO userDAO;

    public JWTReaderController(TokenStore tokenStore, JWTUtils jwtUtils, RateLimitingUtils rateLimitingUtils, UserDAO userDAO){
        this.tokenStore = tokenStore;
        this.jwtUtils = jwtUtils;
        this.rateLimitingUtils = rateLimitingUtils;
        this.userDAO = userDAO;
    }

    @GetMapping("email")
    public ResponseEntity<EmailRespDTO> getEmailFromToken(@RequestHeader("Authorization") String authHeader,HttpServletRequest request){
        String email;

        String jwtToken = checkTokenValidity(authHeader, request);
        if(jwtToken != null) {
            email = jwtUtils.getEmailFromToken(jwtToken);
            return ResponseEntity.ok(new EmailRespDTO(email));
        }
        
        return ResponseEntity.status(401).build();
    }

    @GetMapping("validate")
    public ResponseEntity<Void> validateToken(@RequestHeader("Authorization") String authHeader, HttpServletRequest request) {
       

        String jwtToken = checkTokenValidity(authHeader, request);
        if(jwtToken != null) {
            return ResponseEntity.ok().build();
        }
        
        return ResponseEntity.status(401).build();
    }
    
    @GetMapping("groups")
    public ResponseEntity<Object> groupsChecks(@RequestParam(required = false) String g,@RequestHeader("Authorization") String authHeader, HttpServletRequest request) {
         String[] groupsToCheck;
        String[] groups;
        boolean isInGroups = true;
        User user;
        String jwtToken = checkTokenValidity(authHeader, request);
        if(jwtToken == null) {
            return ResponseEntity.status(401).build();
        }
        
        // User should never be null if token is valid
        user = userDAO.getByEmail(jwtUtils.getEmailFromToken(jwtToken));
       
        groups = user.getGroupsForJWT();
        if(request.getParameter("g") == null) {
            return ResponseEntity.ok(new GroupsListRespDTO(groups));
        }

        groupsToCheck = request.getParameter("g").split(",");

        for(String group : groupsToCheck) {
            if(group.isBlank() || !List.of(groups).contains(group.trim())) {
                isInGroups = false;
                break;
            }
        }
        
        return ResponseEntity.ok(new InGroupsRespDTO(isInGroups));
    }


    private String checkTokenValidity(String authHeader, HttpServletRequest request) {
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
    
            log.warn("Missing or invalid Authorization header");
            
            if( !rateLimitingUtils.isWhitelisted(request.getRemoteAddr()) )
                rateLimitingUtils.recordFailedAttempt(request.getRemoteAddr());
            
            return null;
        }

        String jwtToken = authHeader.substring(7);


            if(! tokenStore.getJwtTokens().isTokenValid(jwtToken)){
           
                log.warn("Invalid or expired JWT token");
            
                if( !rateLimitingUtils.isWhitelisted(request.getRemoteAddr()) )
                    rateLimitingUtils.recordFailedAttempt(request.getRemoteAddr());

            }

        return jwtToken;
        
        

    }
    
    
    
}
