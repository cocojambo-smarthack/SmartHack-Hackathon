package coco.cocoapplication.service;

import coco.cocoapplication.model.Session;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ApiServiceTest {

    @Autowired
    private ApiService apiService;

    @Test
    public void testStartSession() {
        Session session = apiService.startSession();
        System.out.println(session);
    }
}