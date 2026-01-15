package it.eforhum.auth_module.controller;

import org.springframework.web.bind.annotation.RestController;

import it.eforhum.auth_module.dto.JWTRespDTO;
import it.eforhum.auth_module.dto.LoginReqDTO;
import it.eforhum.auth_module.dto.RegistrationReqDTO;
import it.eforhum.auth_module.service.JWTUtils;
import it.eforhum.auth_module.repository.UserDAO;
import it.eforhum.auth_module.repository.TokenStore;
import it.eforhum.auth_module.entity.Token;
import it.eforhum.auth_module.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import it.eforhum.auth_module.service.RateLimitingUtils;
import it.eforhum.auth_module.service.SendUtils;
import lombok.extern.slf4j.Slf4j;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@Slf4j
@RequestMapping("/token/")
public class UserAccessibleController {

    private final JWTUtils jwtUtils;
    private final RateLimitingUtils rateLimitingUtils;
    private final UserDAO userDAO;
    private final TokenStore tokenStore;
    private final SendUtils sendUtils;

    public UserAccessibleController(JWTUtils jwtUtils, RateLimitingUtils rateLimitingUtils, UserDAO userDAO, TokenStore tokenStore, SendUtils sendUtils){
        this.jwtUtils = jwtUtils;
        this.rateLimitingUtils = rateLimitingUtils;
        this.userDAO = userDAO;
        this.tokenStore = tokenStore;
        this.sendUtils = sendUtils;
    }

    @PostMapping("auth")
    public ResponseEntity<JWTRespDTO> postMethodName(@RequestBody LoginReqDTO loginDTO, HttpServletRequest request) {
        String email = loginDTO.email();

        User u = userDAO.login(email, loginDTO.password());

        if(u == null){
            log.warn("Failed login attempt for email: %s from IP: %s", email, request.getRemoteAddr());
            rateLimitingUtils.recordFailedAttempt(request.getRemoteAddr());
            return ResponseEntity.status(401).build();
        }
        
        if(!u.isActive()){
            log.warn("Login attempt for inactive user: %s from IP: %s", email, request.getRemoteAddr());
            return ResponseEntity.status(403).build();
        }
        
        Token t = jwtUtils.generateJWT(u);
        if (tokenStore.getJwtTokens().isTokenValid(email)) {
            log.info("Invalidating previous token for email: %s from IP: %s", email, request.getRemoteAddr());
            tokenStore.getJwtTokens().invalidateToken(email);
        }

        tokenStore.getJwtTokens().saveToken(t);
       
        log.info("generated token for email: %s from IP: %s", email, request.getRemoteAddr());

        return ResponseEntity.ok(new JWTRespDTO(t.getTokenValue()));
    }

    @DeleteMapping("logout")
    public ResponseEntity<Void> logoutUser(@RequestHeader("Authorization") String authHeader, HttpServletRequest request) {



        String jwtToken = authHeader.substring(7);
        if(tokenStore.getJwtTokens().isTokenValid(jwtToken)){

            String email = jwtUtils.getEmailFromToken(jwtToken);
            tokenStore.getJwtTokens().invalidateToken(email);

        }

        return ResponseEntity.ok().build();
        
    }

    @PostMapping("register")
    public ResponseEntity<Void> registerUser(@RequestBody RegistrationReqDTO registrationDTO, HttpServletRequest request) {

        String email = registrationDTO.email();
        String name = registrationDTO.firstName();
        String surname = registrationDTO.lastName();
        String password = registrationDTO.password();

        User u = userDAO.login(email, password);

        if( u!= null){
            if( u.isActive()) {
                log.warn("Registration attempt with existing email: %s", email);
                return ResponseEntity.status(413).build();
            }else{
               
                log.warn("Registration attempt with inactive email: %s", email);
                return ResponseEntity.status(403).build();
            }
        }

        if( userDAO.getByEmail(email) != null){
            
            log.warn("Registration attempt with existing email: %s", email);
            rateLimitingUtils.recordFailedAttempt(request.getRemoteAddr());
            return ResponseEntity.status(400).build();
        }


        u = userDAO.create(email, password, name, surname);

        if(u == null){
            log.warn("Failed to create user during registration for email: %s", email);
            return ResponseEntity.status(400).build();
        }
        
        
        if(sendUtils.sendActivationCode(u, email) == 200){
            return ResponseEntity.ok().build();
        }            
    
        log.error("Failed to send OTP token after registration for email: %s", email);
        return ResponseEntity.status(400).build();
        
    }
}
