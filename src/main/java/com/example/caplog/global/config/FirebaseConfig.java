package com.example.caplog.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        try {
            // resources 폴더의 firebase-service-account.json을 읽어온다.
            InputStream serviceAccount = new ClassPathResource("firebase-service-account.json").getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if(FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("[FCM] FirebaseApp 초기화 성공");
            }
        } catch (IOException e) {
            log.error("[FCM] FirebaseApp 초기화 실패: ", e);
        }
    }
}
