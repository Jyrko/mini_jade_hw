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

            runGUI(container);

            runAgent(container, "BoltDelivery", "DeliveryAgent", "hw", new Object[] { "Bolt", 11.2 });
            runAgent(container, "BoltMarket1", "MarketAgent", "hw", new Object[] { 11.0, "Bolt" });
            runAgent(container, "BoltMarket2", "MarketAgent", "hw", new Object[] { 15.0, "Bolt" });

            runAgent(container, "UberDelivery", "DeliveryAgent", "hw", new Object[] { "Uber", 1.5 });
            runAgent(container, "UberMarket1", "MarketAgent", "hw", new Object[] { 18.0, "Uber" });
            runAgent(container, "UberMarket2", "MarketAgent", "hw", new Object[] { 22.0, "Uber" });

            runAgent(container, "GlovoDelivery", "DeliveryAgent", "hw", new Object[] { "Glovo", 1.3 });
            runAgent(container, "GlovoMarket1", "MarketAgent", "hw", new Object[] { 14.0, "Glovo" });
            runAgent(container, "GlovoMarket2", "MarketAgent", "hw", new Object[] { 19.0, "Glovo" });

            runAgent(container, "ClientAgent", "ClientAgent", "hw");
        } catch (final InterruptedException | ExecutionException e) {
            throw new JadePlatformInitializationException(e);
        }
    }
}
