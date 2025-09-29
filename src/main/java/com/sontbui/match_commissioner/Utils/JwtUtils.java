package com.sontbui.match_commissioner.Utils;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;


@Component
public class JwtUtils {
    

    private final Key key;
    private final long jwtExpiration;
    private final long refeshExpiration;

    public JwtUtils(
        @Value("${jwt.secretKey}") String secretKey,
        @Value("${jwt.expiration}") long jwtExpiration,
        @Value("${jwt.expiration-refresh-token}") long refeshExpiration
    ){
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.jwtExpiration = jwtExpiration * 1000;
        this.refeshExpiration = refeshExpiration * 1000;
    }

    // Generate Access Token
    public String generateToken(String username, Map<String, Object> claims){
        return Jwts.builder()
        .setClaims(claims)
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
    }

    // Generate Refresh Token 
    public String generateRefreshToken(String username){
        return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + refeshExpiration))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
    }

    // Extract User Name 
    public String extractUsername(String token){
        return extractClaims(token, Claims::getSubject);
    }

    public Date extractExpiration(String token){
        return extractClaims(token, Claims::getExpiration);
    }

    public <T> T extractClaims(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();
    }

    public boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    public boolean  validateToken(String token, String username){
        final String extractedUser = extractUsername(token);
        return (extractedUser.equals(username) && !isTokenExpired(token));
    }

}
