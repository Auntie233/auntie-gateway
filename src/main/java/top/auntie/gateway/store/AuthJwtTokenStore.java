package top.auntie.gateway.store;

import com.google.common.collect.Maps;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import top.auntie.gateway.store.converter.AuthJwtTokenConverter;

import java.util.Map;

@Import(AuthJwtTokenConverter.class)
public class AuthJwtTokenStore {

    /**
     * 将额外信息添加进token中
     * @return
     */
    @Bean
    public TokenEnhancer tokenEnhancer() {
        return (accessToken, authentication) -> {
            final Map<String, Object> additionalInfo = Maps.newHashMapWithExpectedSize(1);
            ((DefaultOAuth2AccessToken)accessToken).setAdditionalInformation(additionalInfo);
            return accessToken;
        };
    }

}
