package com.fz.entity.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.time.Duration;


/**
 * @Author: fz
 * @Date: 2025/1/13 20:28
 * @Description: Elasticsearch配置类
 */
@Configuration
@Slf4j
public class EsConfiguration extends ElasticsearchConfiguration{

    @Resource
    private AppConfig appConfig;


    @Override
    public ClientConfiguration clientConfiguration() {

        return ClientConfiguration.builder()
                .connectedTo(appConfig.getEsHostPort())
                .usingSsl(getSslContext(), (hostname, session) -> true)
                .withBasicAuth(appConfig.getEsUsername(), appConfig.getEsPassword())
                .build();
    }

    /**
     * getSslContext
     */
    private SSLContext getSslContext() {
        try {
            Certificate ca = CertificateFactory.getInstance("X.509")
                    .generateCertificate(appConfig.getElasticCert().getInputStream());

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);
            return context;
        }catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException |
                KeyManagementException e) {
            log.error("创建SSLContext失败", e);
            throw new RuntimeException(e);
        }

    }
}
