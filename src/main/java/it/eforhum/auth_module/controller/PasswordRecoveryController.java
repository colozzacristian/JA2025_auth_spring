package it.eforhum.auth_module.controller;

import org.springframework.web.bind.annotation.RestController;

import it.eforhum.auth_module.dto.PasswordChangeReqDTO;
import it.eforhum.auth_module.dto.RecoveryAuthReqDTO;
import it.eforhum.auth_module.dto.RecoveryRequestDTO;
import it.eforhum.auth_module.service.SendUtils;
import it.eforhum.auth_module.entity.Token;
import it.eforhum.auth_module.entity.User;
import it.eforhum.auth_module.repository.UserDAO;
import it.eforhum.auth_module.repository.TokenStore;

import it.eforhum.auth_module.service.JWTUtils;
import it.eforhum.auth_module.service.RateLimitingUtils;
import it.eforhum.auth_module.dto.TempTokenRespDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@Slf4j
@RequestMapping("/recovery")
public class PasswordRecoveryController {

    private final UserDAO userDAO;
    private final TokenStore tokenStore;
    private final RateLimitingUtils rateLimitingUtils;
    private final JWTUtils jwtUtils;
    private final SendUtils sendUtils;

    public PasswordRecoveryController(UserDAO userDAO, TokenStore tokenStore, RateLimitingUtils rateLimitingUtils, JWTUtils jwtUtils, SendUtils sendUtils){
        this.userDAO = userDAO;
        this.tokenStore = tokenStore;
        this.rateLimitingUtils = rateLimitingUtils;
        this.jwtUtils = jwtUtils;
        this.sendUtils = sendUtils;
    }

    @PostMapping("")
    public ResponseEntity<Void> passwordRecovery(@RequestBody RecoveryRequestDTO recoveryDTO) {

        User u = userDAO.getByEmail(recoveryDTO.recipient());    
       
        int status = sendUtils.sendRecoveryEmail(u, u.getEmail());

        if(status == 200) {
          
            log.info("Sent recovery message to user: %s", u.getEmail());
            return ResponseEntity.ok().build();
        }

       
        log.error("Failed to send recovery message, status code: %s", status);
        return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
    }

    @PostMapping("auth")
    public ResponseEntity<TempTokenRespDTO> passwordRecoveryAuth(@RequestBody RecoveryAuthReqDTO entity,HttpServletRequest req){
        
        if(!tokenStore.getOtpTokens().isTokenValid(entity.email(), entity.otp())){
            
            log.warn("Invalid or expired OTP used from IP: %s", req.getRemoteAddr());
            rateLimitingUtils.recordFailedAttempt(req.getRemoteAddr());
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).build();
        }

        User u = userDAO.getByEmail(entity.email());
        if(u == null ){
            log.error("MESSED UP BIG TIME. User not found for email during recovery auth: %s", entity.email());
            return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
        }

        Token t = jwtUtils.generateJWT(u);
        tokenStore.getRecoveryTokens().saveToken(t);
       
        log.info("Issued temporary JWT for password recovery to user: %s", u.getEmail());
        return ResponseEntity.ok(new TempTokenRespDTO(t.getTokenValue()));
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody PasswordChangeReqDTO passwordChangeDTO,HttpServletRequest req){ 

        if(!tokenStore.getRecoveryTokens().isTokenValid(passwordChangeDTO.token())){
            
            log.warn("Invalid or expired recovery token used from IP: %s", req.getRemoteAddr());
            rateLimitingUtils.recordFailedAttempt(req.getRemoteAddr());
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).build();
        }


        tokenStore.getRecoveryTokens().invalidateToken(passwordChangeDTO.token());
        String email = jwtUtils.getEmailFromToken(passwordChangeDTO.token());
        User u = userDAO.getByEmail(email);
        if(u == null){
            log.error("MESSED UP BIG TIME. User not found for email extracted from token during password change: %s", email);
            return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
        }        

        if(! userDAO.changePassword(u, passwordChangeDTO.newPassword())){
            
            log.error("Failed to change password for user: %s", u.getEmail());
            return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
        }

        log.info("Password changed successfully for user: %s", u.getEmail());
        
        return ResponseEntity.ok().build();
    }
    
    
}
