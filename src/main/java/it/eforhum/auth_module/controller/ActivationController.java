package it.eforhum.auth_module.controller;



import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.eforhum.auth_module.dto.ActivationDataDTO;
import it.eforhum.auth_module.dto.JWTRespDTO;
import it.eforhum.auth_module.dto.RecoveryRequestDTO;
import it.eforhum.auth_module.service.JWTUtils;
import it.eforhum.auth_module.service.RateLimitingUtils;
import it.eforhum.auth_module.service.SendUtils;
import it.eforhum.auth_module.repository.TokenStore;
import it.eforhum.auth_module.entity.Token;
import it.eforhum.auth_module.repository.UserDAO;
import it.eforhum.auth_module.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/activation")
public class ActivationController {

    private final UserDAO userDAO;
    private final SendUtils sendUtils;
    private final JWTUtils jwtUtils;
    private final TokenStore tokenStore;
    private final RateLimitingUtils rateLimitingUtils;

    public ActivationController(UserDAO userDAO, SendUtils sendUtils, JWTUtils jwtUtils, TokenStore tokenStore, RateLimitingUtils rateLimitingUtils){
        this.userDAO = userDAO;
        this.sendUtils = sendUtils;
        this.jwtUtils = jwtUtils;
        this.tokenStore = tokenStore;
        this.rateLimitingUtils = rateLimitingUtils;
    }

    @PostMapping("")
    public ResponseEntity<Void> activationRequest(@RequestBody RecoveryRequestDTO activationData) {
    
        
        User u = userDAO.getByEmail(activationData.recipient());
        
        if(u == null){
            log.warn("Activation request for non-existing user: {}", activationData.recipient());
            return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
        }

        

        int status = sendUtils.sendActivationCode(u, activationData.recipient());

        if(status == 200){
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
    
    }

    @PostMapping("auth")
    public ResponseEntity<JWTRespDTO> postMethodName(@RequestBody ActivationDataDTO activationDataDTO, HttpServletRequest request) {
        User u;
        String email;

         u = userDAO.getByEmail(activationDataDTO.email());

        if(u == null){
            log.warn("Activation attempt for non-existing user: {}", activationDataDTO.email());
        }

        if(!tokenStore.getOtpTokens().isTokenValid(activationDataDTO.email(), activationDataDTO.otp())){
            u = null;
            log.warn("Invalid OTP during activation for user: {}", activationDataDTO.email());    
        }

        if(u == null){
            rateLimitingUtils.recordFailedAttempt(request.getRemoteAddr());
            return ResponseEntity.status(400).build();
        }

        email = u.getEmail();

        if(!userDAO.activateUser(u)){
            log.error("Failed to activate user: {}", email);
            return ResponseEntity.status(500).build();
        }

        u = userDAO.getByEmail(u.getEmail());

        if(u == null){
            log.error("Failed to retrieve activated user: {}", email);
            return ResponseEntity.status(500).build();
        }

        Token jwtToken = jwtUtils.generateJWT(u);
        tokenStore.getJwtTokens().saveToken(jwtToken);
        
        return ResponseEntity.ok(new JWTRespDTO(jwtToken.getTokenValue()));
    }
    
}
