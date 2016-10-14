package common.config;

import lombok.SneakyThrows;
import lombok.val;

import java.io.InputStreamReader;
import java.util.Properties;

public class ConfigProperties {
    private static final String CONFIG_FTP_PROPERTY_FILE_NAME = "/ftp.properties";
    private static ConfigProperties instance;
    private Properties properties;


    @SneakyThrows
    private ConfigProperties(ConfigPropertiesSections configPropertiesSections) {
        switch (configPropertiesSections) {
            case FTP:
                val configFileReader =
                        new InputStreamReader(ConfigProperties.class.getResourceAsStream(CONFIG_FTP_PROPERTY_FILE_NAME));
                properties = new Properties();
                properties.load(configFileReader);
                break;
        }
    }

    public static ConfigProperties getConfigProperties(ConfigPropertiesSections configPropertiesSections) {
        if (instance == null) {
            instance = new ConfigProperties(configPropertiesSections);
        }
        return instance;
    }

    public String getPropertyValue(String propertyName) {
        return properties.getProperty(propertyName);
    }

}
