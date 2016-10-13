package common.config;

import lombok.SneakyThrows;
import lombok.val;

import java.io.InputStreamReader;
import java.util.Properties;

public class ConfigProperties {
    private static final String CONFIG_FTP_PROPERTY_FILE_NAME = "/ftp.properties";
    private static ConfigProperties instance;
    private InputStreamReader configFileReader;


    private ConfigProperties(ConfigPropertiesSections configPropertiesSections) {
        switch (configPropertiesSections) {
            case FTP:
                configFileReader =
                        new InputStreamReader(
                                ConfigProperties.class.getResourceAsStream(CONFIG_FTP_PROPERTY_FILE_NAME));
                break;
        }
    }

    public static ConfigProperties getConfigProperties(ConfigPropertiesSections configPropertiesSections) {
        if (instance == null) {
            instance = new ConfigProperties(configPropertiesSections);
        }
        return instance;
    }

    @SneakyThrows
    public String getPropertyValue(String propertyName) {
        val properties = new Properties();
        properties.load(configFileReader);
        return properties.getProperty(propertyName);
    }

}
