package br.com.fai.Vox.implementation.service.authentication.jwt;

import br.com.fai.Vox.domain.UserModel;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Profile("jwt")
@Component
public class JwtService {

    private final String secret = "XUFAE3FQG1RLBlgQ93fDSUlj4HfbKi4a1kFl1gDloOg=";

    public String getEmailFromToken(String token){
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token){
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver){
        Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public Claims getAllClaimsFromToken(String token){
        return Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(token).getBody();
    }

    public boolean tokenExpired(String token){
        final Date expirationDate = getExpirationDateFromToken(token);
        return expirationDate.before(new Date());
    }

    public boolean validateToken(String token, UserDetails userDetails){
        final String email = getEmailFromToken(token);
        return (
                email.equals(userDetails.getUsername()) && !tokenExpired(token)
        );
    }

    public String generateToken(UserDetails userDetails, String fullname, UserModel.UserRole role, String email){
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("fullname", fullname);
        claims.put("role", role);
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subjct){
        return Jwts.builder().setClaims(claims).setSubject(subjct).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(
                        new Date(System.currentTimeMillis() * 1000 * 60 * 60 * 10)
                ).signWith(SignatureAlgorithm.HS256, secret).compact();
    }
}
