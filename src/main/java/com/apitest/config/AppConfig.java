package com.apitest.config;

import org.aeonbits.owner.Config;

@Config.Sources("classpath:config.properties")
public interface AppConfig extends Config {

    @Key("base.url")
    String baseUrl();

    @Key("timeout")
    int timeout();

    @Key("response.time.threshold.ms")
    @DefaultValue("3000")
    int responseTimeThresholdMs();
}