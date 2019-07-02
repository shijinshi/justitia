package cn.shijinshi.fabricmanager.service;

import cn.shijinshi.fabricmanager.dao.UserService;
import cn.shijinshi.fabricmanager.dao.entity.User;
import cn.shijinshi.fabricmanager.exception.IdentityVerifyException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class TokenManager {
    private static final Logger log = Logger.getLogger(TokenManager.class);

    @Autowired
    private UserService userService;

    private static final String secret = "sPMja62eSJa5eg4612";      //token密钥
    private static final int tokenExpires = 7;                      //token的有效期设置为7天
    private static final String HEADER_TOKEN = "Authorization";     //token在header中的key

    private static final String claim_userid = "userId";
    private static final String claim_username = "userName";
    private static final String claim_identity = "identity";
    private static final String claim_affiliation = "affiliation";

    public String getToken(HttpServletRequest request){
        String token = request.getHeader(HEADER_TOKEN);     //放在cookie中存在跨域问题
        if (token == null || token.isEmpty()) {
            throw new IdentityVerifyException("Token is empty.");
        }
        return token;
    }

    public User getRequester(HttpServletRequest request){
        String token = getToken(request);
        Map<String, Claim> claims = decodedToken(token).getClaims();
        User user = new User();
        user.setUserId(claims.get(claim_userid).asString());

        Claim username = claims.get(claim_username);
        if (username == null) {
            user.setUserName(null);
        } else {
            user.setIdentity(username.asString());
        }

        user.setIdentity(claims.get(claim_identity).asString());
        user.setAffiliation(claims.get(claim_affiliation).asString());
        return user;
    }


    public String createToken(User user) {
        return createToken(user, secret);
    }

    /**
     * 创建一个token，并将这个token和用户关联
     * @param user
     * @param secret
     * @return
     */
    public String createToken(User user, String secret) {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("alg", "HS256");
        headerMap.put("typ", "JWT");

        String token = JWT.create()
                .withHeader(headerMap)
                .withClaim(claim_userid, user.getUserId())
                .withClaim(claim_username, user.getUserName())
                .withClaim(claim_identity, user.getIdentity())
                .withClaim(claim_affiliation, user.getAffiliation())
                .withIssuedAt(new Date())   //签发时间
                .withExpiresAt(getFutureDate(tokenExpires))  //到期时间
                .sign(Algorithm.HMAC256(secret));  //加密算法

        return token;
    }


    public boolean verifyToken(HttpServletRequest request) {
        String token = getToken(request);
        DecodedJWT jwt = decodedToken(token, secret);//没有抛出异常说明则验证通过

        //检查用户是否存在
        String userId = jwt.getClaim(claim_userid).asString();
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new IdentityVerifyException("Not found user:" + userId);
        } else if (!token.equals(user.getToken())) {
            throw new IdentityVerifyException("Invalid token.");
        }
        return true;
    }

    public DecodedJWT decodedToken(String token) {

        return decodedToken(token, secret);
    }

    /**
     * 使用密钥解码token，签名不正确时抛出异常
     * @param token token
     * @param secret 密钥
     * @return 解码后的token数据
     * @throws JWTVerificationException Invalid token.
     */
    public DecodedJWT decodedToken(String token, String secret) throws JWTVerificationException{
        DecodedJWT verify = null;
        try {
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(secret)).build();
            verify = jwtVerifier.verify(token);
        }catch (JWTDecodeException e) {
            log.info(e);
            throw new IdentityVerifyException("Invalid token.");
        }

        return verify;
    }

    /**
     * 获取未来 第 past 天的日期,凌晨两点的时间
     *
     * @param past
     * @return
     */
    private Date getFutureDate(int past) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + past);
        calendar.set(Calendar.HOUR_OF_DAY, 2);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

}
