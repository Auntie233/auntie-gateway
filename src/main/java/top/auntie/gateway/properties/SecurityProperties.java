package top.auntie.gateway.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Setter
@Getter
@RefreshScope
@ConfigurationProperties(prefix = "auntie.security")
public class SecurityProperties {

    private AuthProperties auth = new AuthProperties();

    private PermitProperties ignore = new PermitProperties();

    private String tokenSecurity;

}
