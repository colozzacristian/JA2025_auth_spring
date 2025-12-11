package it.eforhum.authModule.servlets;

import java.io.IOException;
import static java.lang.String.format;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;
import it.eforhum.authModule.daos.UserDAOImp;
import it.eforhum.authModule.dtos.EmailDTO;
import it.eforhum.authModule.dtos.EmailReqDTO;
import it.eforhum.authModule.entities.Token;
import it.eforhum.authModule.entities.User;
import it.eforhum.authModule.utils.OTPUtils;
import it.eforhum.authModule.utils.TokenStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name="ActivationReqServlet", urlPatterns="/activate")
public class ActivationReqServlet extends HttpServlet{

    private ObjectMapper mapper = new ObjectMapper();

    private static final Dotenv dotenv = Dotenv.load();
    private TokenStore tokenStore = TokenStore.getInstance();
    private UserDAOImp userDAO = new UserDAOImp();
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
    throws  ServletException, IOException{
        
        String body = new String(request.getInputStream().readAllBytes());
        EmailDTO email = mapper.readValue(body, EmailDTO.class);

        User u = userDAO.getByEmail(email.email());

        if(u == null){
            return;
        }

        Token otpToken = OTPUtils.generateOTP(u);
        tokenStore.getOtpToken().saveToken(otpToken);

        int status = sendActivationCode(otpToken, email.email());

        if(status == 200){
            response.setStatus(200);
            return;
        }

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write("An error occoured while sending the email with the activation code");
    }

    private int sendActivationCode(Token t,String email){
        HttpClient httpClient = HttpClient.newHttpClient();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URL(format("%s/send/%s", dotenv.get("MESSAGE_SERVICE_URL"), "email")).toURI())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                    mapper.writeValueAsString(new EmailReqDTO(
                            email,
                            "Account activation code",
                            getEmailBody(t)
                        )
                    )))
                    .timeout(Duration.ofSeconds(200))
                    .build();

            HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
            return response.statusCode();
            /* Log failure from mail module */

        } catch (Exception e) {

            /*Log the exception */
            return 500;
        }
    }

    private String getEmailBody(Token t){
        StringBuilder sb = new StringBuilder("<html>\r\n" + //
                        "    <body>\r\n" + //
                        "        <p>This is your account activation code</p>\r\n" + //
                        "            <h1>");
        sb.append(t.getToken());
        sb.append("</h1>\r\n" + //
                        "        <p>Insert this code at: /activate/authenticate</p>\r\n" + //
                        "    </body>\r\n" + //
                        "</html>");

        return sb.toString();
    }

}
