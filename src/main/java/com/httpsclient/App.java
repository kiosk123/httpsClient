package com.httpsclient;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {

        String baseUrl = "https://dev-mtls-server.com";
        String requestUrl = baseUrl + "/oauth/2.0/token";

        SSLContext sslContext = getSSLContext();

        CloseableHttpClient closeableHttpClient = HttpClientBuilder
            .create()
            .setSSLContext(sslContext) // SSLContext 설정
            .setProxy(new HttpHost("proxy.server.com", 7777, "http")) // 프록시 서버 설정
            .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(closeableHttpClient);

        RestTemplate restTemplate = new RestTemplate(requestFactory); //RestTemplate 생성

        //============================================================
        // 전송 헤더 설정
        //============================================================

        HttpHeaders headers = new HttpHeaders();
        // headers.add(key, value) 형태로도 설정 가능함
        headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setAcceptCharset(Arrays.asList(Charset.forName("UTF-8")));
        // headers.add("User-Agent", "Mozilla/5.0"); //User-Agent는 필요하면 설정

        //===========================================================================
        // application/x-www-form-urlencoded;charset=UTF-8 로 전송하기 위한 바디부 설정
        //=========================================================================== 
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", "aaaaa");
        formData.add("client_secret", "bbbbb");
        formData.add("scope", "Bearer");

        // 전송 요청 데이터 
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

        // restTemplate Post 요청 - RuntimeException인 RestClientException을 던지기 때문에 필요시 예외처리 필요
        ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, request, String.class);

        logger.info("response status code : {}", response.getStatusCode());
        logger.info("reponse body : {}", response.getBody());

    }

    /**
     * SSLContext를 생성한다.
     * @return SSLContext
     */
    public static SSLContext getSSLContext() {
        SSLContext sslContext = null;

        String certType = "jks"; //인증서 유형
        String certPath = "keyStore.jks"; //인증서 경로
        String certPass = "!q@w#e"; //인증서 패스워드
        String tlsVer = "TLSv1.3"; //TLS 버전

        try {
            FileInputStream fis = new FileInputStream(new File(certPath));

            KeyStore keyStore = KeyStore.getInstance(certType);
            keyStore.load(fis, certPass.toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, certPass.toCharArray());

            TrustManager[] trustManager = new TrustManager [] {
                new X509TrustManager() {

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException { }
                    
                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException { }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }
            };

            sslContext = SSLContext.getInstance(tlsVer);
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManager, new SecureRandom());

        } catch (Exception e) {
            logger.error("While init SSLContext, exception occured", e);
        }

        return sslContext;
    }
    
}
