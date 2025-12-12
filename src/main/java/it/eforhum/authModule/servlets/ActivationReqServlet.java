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
import it.eforhum.authModule.dtos.ActivationDataDTO;
import it.eforhum.authModule.dtos.EmailReqDTO;
import it.eforhum.authModule.entities.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name="ActivationReqServlet", urlPatterns="/activate")
public class ActivationReqServlet extends HttpServlet{

    private ObjectMapper mapper = new ObjectMapper();

    private static final Dotenv dotenv = Dotenv.load();
    private UserDAOImp userDAO = new UserDAOImp();
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
    throws  ServletException, IOException{
        
        String body = new String(request.getInputStream().readAllBytes());
        ActivationDataDTO activationData = mapper.readValue(body, ActivationDataDTO.class);
        
        User u = userDAO.getByEmail(activationData.email());
        
        if(u == null){
            return;
        }

        int status = sendActivationCode(activationData.OTP(), activationData.email());

        if(status == 200){
            response.setStatus(200);
            return;
        }

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write("An error occoured while sending the email with the activation code");
    }

    protected int sendActivationCode(String token ,String email){
        HttpClient httpClient = HttpClient.newHttpClient();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URL(format("%s/send/%s", dotenv.get("MESSAGE_SERVICE_URL"), "email")).toURI())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                    mapper.writeValueAsString(new EmailReqDTO(
                            email,
                            "Account activation code",
                            getEmailBody(token)
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

    private String getEmailBody(String token){
        StringBuilder sb = new StringBuilder("<html>\r\n" + //
                        "    <body>\r\n" + //
                        "        <p>This is your account activation code</p>\r\n" + //
                        "            <h1>");
        sb.append(token);
        sb.append("</h1>\r\n" + //
                        "        <p>Insert this code at: /activate/authenticate</p>\r\n" + //
                        "    </body>\r\n" + //
                        "</html>");

        return sb.toString();
    }

}
