package top.auntie.gateway.store;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.provider.token.TokenStore;
import top.auntie.gateway.store.converter.AuthJwtTokenConverter;
import top.auntie.gateway.store.converter.ResJWTTokenConverter;

@Import(ResJWTTokenConverter.class)
public class ResRedisTokenStore {

    @Autowired
    private RedisTemplate redisTemplate;

    @Bean
    public TokenStore tokenStore() {
        return new RedisTemplateTokenStore(redisTemplate);
    }

}
