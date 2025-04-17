package org.example.hw;

import static org.example.JADEEngine.runAgent;
import static org.example.JADEEngine.runGUI;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.example.exceptions.JadePlatformInitializationException;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.ContainerController;

public class Engine {

    private static final ExecutorService jadeExecutor = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        final Runtime runtime = Runtime.instance();
        final Profile profile = new ProfileImpl();

        try {
            final ContainerController container = jadeExecutor.submit(() -> runtime.createMainContainer(profile)).get();
            final Object[] agentArgs = {"arg"};

            runGUI(container);
            runAgent(container, "ClientAgent", "ClientAgent", "hw");
            runAgent(container, "DeliveryAgent", "DeliveryAgent", "hw");
            runAgent(container, "MarketAgent", "MarketAgent", "hw");
        } catch (final InterruptedException | ExecutionException e) {
            throw new JadePlatformInitializationException(e);
        }
    }
}
