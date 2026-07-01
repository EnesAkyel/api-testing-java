package com.apitest.config;

import org.aeonbits.owner.ConfigFactory;

public class ConfigManager {

    private static AppConfig config;

    private ConfigManager() {}

    public static AppConfig getConfig() {
        if (config == null) {
            config = ConfigFactory.create(AppConfig.class);
        }
        return config;
    }
}
