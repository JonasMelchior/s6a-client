package com.github.jonasmelchior;

import com.github.jonasmelchior.service.S6aClient;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jdiameter.api.*;

@ApplicationScoped
@Slf4j
public class S6aClientBootstrap {

    @Inject
    S6aClient client;

    void onStart(@Observes StartupEvent event) throws Exception {
        Log.info("Starting S6a test client");

        Log.info("Sending test AIR");
        client.sendTestAuthenticationInformationRequest();

//        Log.info("Sending test ULR");
//        client.sendTestUpdateLocationRequest("mme1.localhost");
//
//        // Delay for 2 seconds (2000 milliseconds)
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt(); // restore interrupt flag
//            e.printStackTrace();
//        }
//
//        client.sendTestUpdateLocationRequest("mme2.localhost");
    }
}
