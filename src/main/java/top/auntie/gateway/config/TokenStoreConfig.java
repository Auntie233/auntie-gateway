package top.auntie.gateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import top.auntie.gateway.store.AuthDbTokenStore;
import top.auntie.gateway.store.AuthRedisTokenStore;
import top.auntie.gateway.store.ResJwtTokenStore;
import top.auntie.gateway.store.ResRedisTokenStore;

public class TokenStoreConfig {

    @Configuration
    @ConditionalOnProperty(prefix = "auntie.security.token.store", name = "type", havingValue = "authRedis")
    @Import(AuthRedisTokenStore.class)
    public class AuthRedisTokenConfig {
    }

    @Configuration
    @ConditionalOnProperty(prefix = "auntie.security.token.store", name = "type", havingValue = "authDb")
    @Import(AuthDbTokenStore.class)
    public class AuthDbTokenConfig {
    }

    @Configuration
    @ConditionalOnProperty(prefix = "auntie.security.token.store", name = "type", havingValue = "authJwt")
    @Import(AuthRedisTokenStore.class)
    public class AuthJwtTokenConfig {
    }

    @Configuration
    @ConditionalOnProperty(prefix = "auntie.security.token.store", name = "type", havingValue = "resJwt")
    @Import(ResJwtTokenStore.class)
    public class ResJwtTokenConfig {
    }

    @Configuration
    @ConditionalOnProperty(prefix = "auntie.security.token.store", name = "type", havingValue = "resRedis", matchIfMissing = true)
    @Import(ResRedisTokenStore.class)
    public class ResRedisTokenConfig {
    }
}
