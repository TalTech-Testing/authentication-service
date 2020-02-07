package ee.taltech.arete_admin_panel.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import ee.taltech.arete_admin_panel.domain.User;
import ee.taltech.arete_admin_panel.pojo.abi.users.user.UserResponseIdToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    @Autowired
    private final UserService userService;

    private Algorithm algorithm = Algorithm.HMAC256("secret");

    public TokenService(UserService userService) {
        this.userService = userService;
    }

    public String createAndSignToken(long id) {

        try {
            return JWT.create()
                    .withIssuer("auth0")
                    .withSubject(String.valueOf(id))
                    .withClaim("isAdmin", false)
                    .sign(algorithm);

        } catch (JWTVerificationException exception) {
            return "Bamboozled";
        }
    }

    public String createAndSignAdminToken(long id) {

        try {
            return JWT.create()
                    .withIssuer("auth0")
                    .withSubject(String.valueOf(id))
                    .withClaim("isAdmin", true)
                    .sign(algorithm);

        } catch (JWTVerificationException exception) {
            return "Bamboozled";
        }
    }

    public boolean verifyTokenIsAdmin(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).withIssuer("auth0").build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("isAdmin").asBoolean();
        } catch (JWTVerificationException exception) {
            return false;
        }
    }

    public boolean verifyTokenIsCertainId(String token, long id) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).withIssuer("auth0").build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getSubject().equals(String.valueOf(id));
        } catch (JWTVerificationException exception) {
            return false;
        }
    }

    public UserResponseIdToken createResponse(long id) {

        String token;
        User user = userService.getUser(id);
        token = user.getRole().equals(User.Role.ADMIN) ? createAndSignAdminToken(id) : createAndSignToken(id);
        return new UserResponseIdToken(id, user.getRole(), token, user.getUsername(), user.getColor());
    }
}

