package top.auntie.gateway.properties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthProperties {

    private boolean urlEnabled = false;

    private String[] httpUrls = {};

}
