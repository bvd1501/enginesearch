package searchengine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "jsoup-setting")
public class JsoupCfg {
        private String userAgent;
        private String referrer;
        private Integer timeout;
        private  boolean ignoreContentType;
        private boolean ignoreHttpErrors;
        private boolean followRedirects;
        //private Integer timeoutMin;
        //private Integer timeoutMax;
}
