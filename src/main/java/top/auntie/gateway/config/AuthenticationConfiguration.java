package top.auntie.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.expression.OAuth2WebSecurityExpressionHandler;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import top.auntie.gateway.properties.SecurityProperties;

import javax.annotation.Resource;

@Configuration
@EnableResourceServer
@EnableConfigurationProperties(value = SecurityProperties.class)
@Import({DefaultSecurityHandlerConfig.class, TokenStoreConfig.class})
public class AuthenticationConfiguration extends ResourceServerConfigurerAdapter {

//    @Autowired
//    private TokenStore tokenStore;

    /**
     * 认证入口
     */
    @Resource
    private AuthenticationEntryPoint authenticationEntryPoint;

    /**
     * oauth2 web加密表达句柄
     */
    @Resource
    private OAuth2WebSecurityExpressionHandler expressionHandler;

    /**
     * oauth2 认证拒绝句柄
     */
    @Resource
    private OAuth2AccessDeniedHandler deniedHandler;

    @Resource
    private SecurityProperties securityProperties;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.accessDeniedHandler(deniedHandler)
                .expressionHandler(expressionHandler)
//                .tokenStore(tokenStore)
                .authenticationEntryPoint(authenticationEntryPoint)
                .stateless(true);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.AuthorizedUrl authorizedUrls = http.authorizeRequests()
                .antMatchers(securityProperties.getIgnore().getUrls()).permitAll()
                .antMatchers(securityProperties.getAuth().getHttpUrls()).authenticated()
                .antMatchers(HttpMethod.OPTIONS).permitAll().anyRequest();
        setAuthenicate(authorizedUrls);

    }

    private HttpSecurity setAuthenicate(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.AuthorizedUrl authorizedUrls) {
        return authorizedUrls.authenticated().and();
    }


}
