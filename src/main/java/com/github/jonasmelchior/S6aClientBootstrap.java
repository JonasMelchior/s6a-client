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

    void onStart(@Observes StartupEvent event) throws IllegalDiameterStateException, RouteException, OverloadException, InternalException, AvpDataException {
        Log.info("Starting S6a test client");
        Log.info("Sending test AIR");
        client.sendTestAuthenticationInformationRequest();
    }
}
