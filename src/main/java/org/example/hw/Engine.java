package org.example.hw;

import static org.example.JADEEngine.runAgent;
import static org.example.JADEEngine.runGUI;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.HashMap;
import java.util.Map;

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
            
            Map<String, Double> boltMarket1Inventory = new HashMap<>();
            boltMarket1Inventory.put("milk", 5.0);
            boltMarket1Inventory.put("coffee", 30.0);
            
            Map<String, Double> boltMarket2Inventory = new HashMap<>();
            boltMarket2Inventory.put("coffee", 25.0);

            runAgent(container, "BoltDelivery", "DeliveryAgent", "hw", new Object[] { "Bolt" });
            runAgent(container, "BoltMarket1", "MarketAgent", "hw", new Object[] { boltMarket1Inventory, "Bolt" });
            runAgent(container, "BoltMarket2", "MarketAgent", "hw", new Object[] { boltMarket2Inventory, "Bolt" });

            Map<String, Double> uberMarket1Inventory = new HashMap<>();
            uberMarket1Inventory.put("milk", 40.0);
            uberMarket1Inventory.put("coffee", 40.0);
            Map<String, Double> uberMarket2Inventory = new HashMap<>();
            uberMarket2Inventory.put("coffee", 28.0);
            uberMarket2Inventory.put("rice", 2.0);

            runAgent(container, "UberDelivery", "DeliveryAgent", "hw", new Object[] { "Uber" });
            runAgent(container, "UberMarket1", "MarketAgent", "hw", new Object[] { uberMarket1Inventory, "Uber" });
            runAgent(container, "UberMarket2", "MarketAgent", "hw", new Object[] { uberMarket2Inventory, "Uber" });

            Map<String, Double> glovoMarket1Inventory = new HashMap<>();
            glovoMarket1Inventory.put("milk", 6.0);
            glovoMarket1Inventory.put("coffee", 3.0);
            
            Map<String, Double> glovoMarket2Inventory = new HashMap<>();
            glovoMarket2Inventory.put("milk", 3.0);
            glovoMarket2Inventory.put("rice", 2.0);

            runAgent(container, "GlovoDelivery", "DeliveryAgent", "hw", new Object[] { "Glovo" });
            runAgent(container, "GlovoMarket1", "MarketAgent", "hw", new Object[] { glovoMarket1Inventory, "Glovo" });
            runAgent(container, "GlovoMarket2", "MarketAgent", "hw", new Object[] { glovoMarket2Inventory, "Glovo" });

            String[] clientOrder = { "milk", "coffee", "rice" };
            runAgent(container, "ClientAgent", "ClientAgent", "hw", new Object[] { clientOrder });
        } catch (final InterruptedException | ExecutionException e) {
            throw new JadePlatformInitializationException(e);
        }
    }
}
