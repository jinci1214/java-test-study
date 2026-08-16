package config;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.InputStream;
import java.util.Map;

public final  class Config {

    private static final String CONFIG_FILE = "config.yaml";
    private static final Map<String,Object> CONFIG;

    static{
        CONFIG = loadConfig();
    }
    private Config(){}

    private static Map<String,Object> loadConfig(){
        InputStream inputStream = Config.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE);

        if(inputStream == null){
            throw new IllegalStateException(
                    "找不到配置文件："+CONFIG_FILE
            );
        }

        LoaderOptions loaderOptions = new LoaderOptions();
        Yaml yaml = new Yaml(
                new SafeConstructor(loaderOptions)
        );
        return yaml.load(inputStream);
    }

    public static String getEnvironment(){
        String defaultEnvironment =
                (String) CONFIG.get("defaultEnvironment");

        return System.getProperty(
                "env",
                defaultEnvironment
        );
    }

    @SuppressWarnings("unchecked")
    public static String getBaseUrl() {
        String environment = getEnvironment();

        Map<String, Object> environments =
                (Map<String, Object>) CONFIG.get("environments");

        if (environments == null) {
            throw new IllegalStateException(
                    "配置文件中缺少 environments"
            );
        }

        Map<String, Object> environmentConfig =
                (Map<String, Object>)
                        environments.get(environment);

        if (environmentConfig == null) {
            throw new IllegalArgumentException(
                    "不存在配置环境：" + environment
            );
        }

        String baseUrl =
                (String) environmentConfig.get("baseUrl");

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(
                    environment + " 环境缺少 baseUrl"
            );
        }

        return baseUrl;
    }
}



