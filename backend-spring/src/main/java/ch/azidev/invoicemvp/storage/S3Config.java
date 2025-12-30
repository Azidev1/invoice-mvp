package ch.azidev.invoicemvp.storage;

import com.example.invoicemvp.config.AppProps;
import org.springframework.context.annotation.*;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.s3.presigner.*;
import java.net.URI;

@Configuration
public class S3Config {

    @Bean
    S3Client s3Client(AppProps props) {
        var s = props.storage();
        return S3Client.builder()
                .endpointOverride(URI.create(s.s3Endpoint()))
                .region(Region.of(s.s3Region()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s.s3AccessKey(), s.s3SecretKey())))
                .forcePathStyle(true)
                .build();
    }

    @Bean
    S3Presigner s3Presigner(AppProps props) {
        var s = props.storage();
        return S3Presigner.builder()
                .endpointOverride(URI.create(s.s3Endpoint()))
                .region(Region.of(s.s3Region()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s.s3AccessKey(), s.s3SecretKey())))
                .build();
    }
}
