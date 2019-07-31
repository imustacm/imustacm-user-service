package cn.imustacm.user.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wangjianli
 * @date 2019-07-31 13:52
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret-key}")
    private String secret;

    public String createToken(int id, String username, String time, String ip){
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("alg", "HS256");
        map.put("typ", "JWT");
        Algorithm algorithm = Algorithm.HMAC256(secret);
        String token = JWT.create()
                .withHeader(map)
                .withClaim("id", id)
                .withClaim("username", username)
                .withClaim("time", time)
                .withClaim("ip", ip)
                .withIssuer("imustacm")
                .withIssuedAt(new Date())
                .sign(algorithm);
        return token;
    }

    public String verifyToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm)
            .withIssuer("imustacm")
            .build();
        DecodedJWT jwt = verifier.verify(token);

        Map<String, Claim> claims = jwt.getClaims();
        Claim claimId = claims.get("id");
        return claimId.asString();
    }


}
