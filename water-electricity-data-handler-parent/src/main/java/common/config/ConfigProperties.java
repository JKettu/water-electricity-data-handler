package common.config;

import lombok.SneakyThrows;
import lombok.val;

import java.io.InputStreamReader;
import java.util.Properties;

public class ConfigProperties {
    private static final String CONFIG_GUI_PROPERTY_FILE_NAME = "/gui.properties";
    private static ConfigProperties instance;
    private InputStreamReader configFileReader;

    private ConfigProperties(ConfigPropertiesSections configPropertiesSections) {

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
