package ch.azidev.invoicemvp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProps(Storage storage, Rabbit rabbit) {
    public record Storage(String s3Endpoint, String s3Region, String s3AccessKey, String s3SecretKey, String s3Bucket) {}
    public record Rabbit(String host, int port) {}
}
