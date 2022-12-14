package com.example.demo;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.springframework.web.bind.annotation.*;

@Service
@RestController
@RequestMapping("push")
public class MessageService {

    @Value("${vapid.public.key}")
    private String publicKey;
    @Value("${vapid.private.key}")
    private String privateKey;

    private PushService pushService;
    private List<Subscription> subscriptions = new ArrayList<>();

    @PostConstruct
    private void init() throws GeneralSecurityException {
        Security.addProvider(new BouncyCastleProvider());
        pushService = new PushService(publicKey, privateKey);
    }

    public String getPublicKey() {
        return publicKey;
    }

    @CrossOrigin(origins = { "http://localhost:8081", "http://127.0.0.1:8081" })
    @PostMapping(
        consumes = { MediaType.APPLICATION_JSON_VALUE },
        path = "/subscribe"
    )
    public String subscribe(@RequestBody Subscription subscription/*Subscription subscription*/) {
        // Save subscription for future use
        this.subscriptions.add(subscription);

        String output = "Subscribed to " + subscription.endpoint;
        System.out.println(output);

        //Log subscription JSON
        Gson gson = new Gson();
        String jsonSub = gson.toJson(subscription);
        System.out.println("<subscription>" + jsonSub);

        return output;
    }

//    public void unsubscribe(String endpoint) {
//        System.out.println("Unsubscribed from " + endpoint);
//        subscriptions = subscriptions.stream().filter(s -> !endpoint.equals(s.endpoint))
//                .collect(Collectors.toList());
//    }
//
    public void sendNotification(Subscription subscription, String messageJson) {
        try {
            pushService.send(new Notification(subscription, messageJson));
        } catch (GeneralSecurityException | IOException | JoseException | ExecutionException
                 | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedRate = 15000)
    private void sendNotifications() {

        System.out.println("Sending notifications to all subscribers");

        // Create a push message to send to subscribers
        PushMessage message = new PushMessage();
        message.setTitle("Server says hello!");
        message.setBody("It is now: %s");

        // Convert to json before sending
        Gson gson = new Gson();
        String json = gson.toJson(message);

        // Do the actual push notification
        subscriptions.forEach(subscription -> {
            sendNotification(subscription, String.format(json, LocalTime.now()));
        });
    }
}