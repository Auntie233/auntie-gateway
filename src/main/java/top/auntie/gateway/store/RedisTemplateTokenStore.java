package top.auntie.gateway.store;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.common.ExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class RedisTemplateTokenStore implements TokenStore {

    private static final String ACCESS = "access:";
    private static final String AUTH_TO_ACCESS = "auth_to_access:";
    private static final String AUTH = "auth:";
    private static final String REFRESH_AUTH = "refresh_auth:";
    private static final String ACCESS_TO_REFRESH = "access_to_refresh:";
    private static final String REFRESH = "refresh:";
    private static final String REFRESH_TO_ACCESS = "refresh_to_access:";
    private static final String CLIENT_ID_TO_ACCESS = "client_id_to_access:";
    private static final String UNAME_TO_ACCESS = "uname_to_access:";
    private static final String TOKEN = "token:";

    private RedisTemplate<String, Object> redisTemplate;

    private AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();

    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setAuthenticationKeyGenerator(AuthenticationKeyGenerator authenticationKeyGenerator) {
        this.authenticationKeyGenerator = authenticationKeyGenerator;
    }

    public RedisTemplateTokenStore() {
    }

    public RedisTemplateTokenStore(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public OAuth2Authentication readAuthentication(OAuth2AccessToken oAuth2AccessToken) {
        return readAuthentication(oAuth2AccessToken.getValue());
    }

    @Override
    public OAuth2Authentication readAuthentication(String token) {
        return (OAuth2Authentication) this.redisTemplate.opsForValue().get(AUTH + token);
    }

    @Override
    public void storeAccessToken(OAuth2AccessToken oAuth2AccessToken, OAuth2Authentication oAuth2Authentication) {
        OAuth2AccessToken tokenWhichExist = this.getAccessToken(oAuth2Authentication);
        this.redisTemplate.opsForValue().set(ACCESS + oAuth2AccessToken.getValue(), oAuth2AccessToken);
        this.redisTemplate.opsForValue().set(AUTH + oAuth2AccessToken.getValue(), oAuth2Authentication);
        this.redisTemplate.opsForValue().set(AUTH_TO_ACCESS + authenticationKeyGenerator.extractKey(oAuth2Authentication), oAuth2AccessToken);

        Map<String, Object> params = Maps.newHashMap();
        params.put("clientId", oAuth2Authentication.getOAuth2Request().getClientId());

        if (oAuth2Authentication.getUserAuthentication() instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) oAuth2Authentication.getUserAuthentication();

        }

        if (!params.isEmpty()) {
            this.redisTemplate.opsForValue().set(TOKEN + oAuth2AccessToken.getValue(), params);
        }
        if (!oAuth2Authentication.isClientOnly()) {
            if (tokenWhichExist != null) {
                if (!tokenWhichExist.isExpired()) {
                    int second = oAuth2AccessToken.getExpiresIn();
                    redisTemplate.expire(UNAME_TO_ACCESS + oAuth2Authentication.getOAuth2Request().getClientId(), second, TimeUnit.SECONDS);
                } else {
                    redisTemplate.opsForList().rightPush(UNAME_TO_ACCESS + getApprovalKey(oAuth2Authentication), oAuth2AccessToken);
                }
            } else {
                redisTemplate.opsForList().rightPush(UNAME_TO_ACCESS + getApprovalKey(oAuth2Authentication), oAuth2AccessToken);
            }
        }

        if (oAuth2AccessToken.getExpiration() != null) {
            int seconds = oAuth2AccessToken.getExpiresIn();
            redisTemplate.expire(ACCESS + oAuth2AccessToken.getValue(), seconds, TimeUnit.SECONDS);
            redisTemplate.expire(AUTH + oAuth2AccessToken.getValue(), seconds, TimeUnit.SECONDS);
            redisTemplate.expire(TOKEN + oAuth2AccessToken.getValue(), seconds, TimeUnit.SECONDS);
            redisTemplate.expire(AUTH_TO_ACCESS + authenticationKeyGenerator.extractKey(oAuth2Authentication), seconds, TimeUnit.SECONDS);
            redisTemplate.expire(CLIENT_ID_TO_ACCESS + oAuth2Authentication.getOAuth2Request().getClientId(), seconds, TimeUnit.SECONDS);
            redisTemplate.expire(UNAME_TO_ACCESS + getApprovalKey(oAuth2Authentication), seconds, TimeUnit.SECONDS);
        }

        OAuth2RefreshToken refreshToken = oAuth2AccessToken.getRefreshToken();
        if (refreshToken != null && refreshToken.getValue() != null) {
            this.redisTemplate.opsForValue().set(REFRESH_TO_ACCESS + oAuth2AccessToken.getRefreshToken().getValue(), oAuth2AccessToken.getValue());
            this.redisTemplate.opsForValue().set(ACCESS_TO_REFRESH + oAuth2AccessToken.getValue(), oAuth2AccessToken.getRefreshToken().getValue());
            if (refreshToken instanceof ExpiringOAuth2RefreshToken) {
                ExpiringOAuth2RefreshToken expiringRefreshToken = (ExpiringOAuth2RefreshToken) refreshToken;
                Date expiration = expiringRefreshToken.getExpiration();
                if (expiration != null) {
                    int seconds = (int) ((expiration.getTime() - System.currentTimeMillis()) / 1000L);

                    redisTemplate.expire(REFRESH_TO_ACCESS + oAuth2AccessToken.getRefreshToken().getValue(), seconds, TimeUnit.SECONDS);
                    redisTemplate.expire(ACCESS_TO_REFRESH + oAuth2AccessToken.getValue(), seconds, TimeUnit.SECONDS);
                }
            }
        }

    }

    private String getApprovalKey(OAuth2Authentication authentication) {
        String userName = authentication.getUserAuthentication() == null ? "" : authentication.getUserAuthentication().getName();
        return getApprovalKey(authentication.getOAuth2Request().getClientId(), userName);
    }

    private String getApprovalKey(String clientId, String userName) {
        return clientId + (userName == null ? "" : ":" + userName);
    }

    @Override
    public OAuth2AccessToken readAccessToken(String tokenValue) {
        return (OAuth2AccessToken) this.redisTemplate.opsForValue().get(ACCESS + tokenValue);
    }

    @Override
    public void removeAccessToken(OAuth2AccessToken oAuth2AccessToken) {
        removeAccessToken(oAuth2AccessToken.getValue());
    }

    private void removeAccessToken(String tokenValue) {
        OAuth2Authentication authentication = (OAuth2Authentication) this.redisTemplate.opsForValue().get(AUTH + tokenValue);
        redisTemplate.delete(AUTH + tokenValue);
        redisTemplate.delete(ACCESS + tokenValue);
        redisTemplate.delete(TOKEN + tokenValue);
        redisTemplate.delete(ACCESS_TO_REFRESH + tokenValue);
        if (authentication != null) {
            String clientId = authentication.getOAuth2Request().getClientId();
            redisTemplate.opsForList().leftPop(UNAME_TO_ACCESS + getApprovalKey(clientId, authentication.getName()));
            redisTemplate.opsForList().leftPop(CLIENT_ID_TO_ACCESS + clientId);
            redisTemplate.delete(AUTH_TO_ACCESS + tokenValue);
        }
    }

    @Override
    public void storeRefreshToken(OAuth2RefreshToken oAuth2RefreshToken, OAuth2Authentication oAuth2Authentication) {
        redisTemplate.opsForValue().set(REFRESH + oAuth2RefreshToken.getValue(), oAuth2RefreshToken);
        redisTemplate.opsForValue().set(REFRESH_AUTH + oAuth2RefreshToken.getValue(), oAuth2Authentication);
    }

    @Override
    public OAuth2RefreshToken readRefreshToken(String tokenValue) {
        return (OAuth2RefreshToken) redisTemplate.opsForValue().get(REFRESH + tokenValue);
    }

    @Override
    public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken oAuth2RefreshToken) {
        return readAuthenticationForRefreshToken(oAuth2RefreshToken.getValue());
    }

    private OAuth2Authentication readAuthenticationForRefreshToken(String tokenValue) {
        return (OAuth2Authentication) redisTemplate.opsForValue().get(REFRESH_AUTH + tokenValue);
    }

    @Override
    public void removeRefreshToken(OAuth2RefreshToken oAuth2RefreshToken) {
        removeRefreshToken(oAuth2RefreshToken.getValue());
    }

    private void removeRefreshToken(String tokenValue) {
        redisTemplate.delete(REFRESH+ tokenValue);
        redisTemplate.delete(REFRESH_AUTH + tokenValue);
        redisTemplate.delete(REFRESH_TO_ACCESS + tokenValue);
    }

    @Override
    public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken oAuth2RefreshToken) {
        removeAccessTokenUsingRefreshToken(oAuth2RefreshToken.getValue());
    }

    private void removeAccessTokenUsingRefreshToken(String tokenValue) {
        String token = (String) redisTemplate.opsForValue().get(REFRESH_TO_ACCESS + tokenValue);
        if (token != null) {
            redisTemplate.delete(REFRESH_TO_ACCESS + tokenValue);
        }
    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication oAuth2Authentication) {
        String key = authenticationKeyGenerator.extractKey(oAuth2Authentication);
        OAuth2AccessToken accessToken = (OAuth2AccessToken) redisTemplate.opsForValue().get(AUTH_TO_ACCESS + key);
        if (accessToken != null && !key.equals(authenticationKeyGenerator.extractKey(readAuthentication(accessToken.getValue())))) {
            storeAccessToken(accessToken, oAuth2Authentication);
        }
        return accessToken;
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String userName) {
        List<Object> result = redisTemplate.opsForList().range(UNAME_TO_ACCESS + getApprovalKey(clientId, userName), 0, -1);
        return getTokenCollection(result);
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
        List<Object> result = redisTemplate.opsForList().range((CLIENT_ID_TO_ACCESS + clientId), 0, -1);
        return getTokenCollection(result);
    }

    private Collection<OAuth2AccessToken> getTokenCollection(List<Object> result) {
        if (result == null || result.isEmpty()) {
            return Collections.emptySet();
        }
        List<OAuth2AccessToken> accessTokens = Lists.newArrayListWithCapacity(result.size());
        for (Object obj :result) {
            OAuth2AccessToken accessToken = (OAuth2AccessToken) obj;
            accessTokens.add(accessToken);
        }
        return Collections.unmodifiableCollection(accessTokens);
    }

}
