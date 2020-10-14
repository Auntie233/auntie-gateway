package top.auntie.gateway.store.converter;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

public class ResJWTTokenConverter {

    @Autowired
    private ResourceServerProperties resource;

    @Bean
    public TokenStore tokenStore(JwtAccessTokenConverter jwtAccessTokenConverter) {
        return new JwtTokenStore(jwtAccessTokenConverter);
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setVerifierKey(getPubKey());
        return converter;
    }

    private String getPubKey() {
        Resource resource = new ClassPathResource("");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            return br.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            return getKeyFromAuthorizationServer();
        }
    }

    private String getKeyFromAuthorizationServer() {
        if (StringUtils.isNotEmpty(this.resource.getJwt().getKeyUri())) {
            final HttpHeaders headers = new HttpHeaders();
            final String username = this.resource.getClientId();
            final String password = this.resource.getClientSecret();
            if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
                final byte[] token = Base64.getEncoder().encode(username.concat(":").concat(password).getBytes(StandardCharsets.UTF_8));
                headers.add("Authorization", "Basic "+new String(token, StandardCharsets.UTF_8));
            }
            final HttpEntity<Void> request = new HttpEntity<>(headers);
            final String url = this.resource.getJwt().getKeyUri();
            return (String) new RestTemplate()
                    .exchange(url, HttpMethod.GET, request, Map.class)
                    .getBody()
                    .get("value");
        }
        return null;
    }

}
