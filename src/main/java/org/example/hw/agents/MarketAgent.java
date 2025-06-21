package org.example.hw.agents;
// MarketAgent.java
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.example.hw.behaviours.MarketResponderBehaviour;

import java.util.HashMap;
import java.util.Map;

public class MarketAgent extends Agent {
    private String deliveryServiceId;
    private Map<String, Double> inventory;

    protected void setup() {
        // Get arguments: multiplier and deliveryServiceId
        Object[] args = getArguments();
        Double multiplier = 10.0;
        deliveryServiceId = "General";

        if (args != null) {
            if (args.length > 0 && args[0] instanceof Double) {
                multiplier = (Double) args[0];
            }
            if (args.length > 1 && args[1] instanceof String) {
                deliveryServiceId = (String) args[1];
            }
        }

        // Initialize inventory based on delivery service and multiplier
        initializeInventory(deliveryServiceId, multiplier);

        // Registration with the DF as Market-Service
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Market-Service");
        sd.setName(getLocalName() + "-market");
        
        // Add the delivery service as a property (fixed)
        Property prop = new Property("deliveryService", deliveryServiceId);
        sd.addProperties(prop);
        
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
            System.out.println(getLocalName() + " registered as Market-Service for delivery service: " + 
                deliveryServiceId + " with inventory: " + inventory);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        MessageTemplate mt = MessageTemplate.or(
            MessageTemplate.MatchPerformative(ACLMessage.CFP),
            MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchConversationId("market-query")
            )
        );
        addBehaviour(new MarketResponderBehaviour(this, mt, multiplier));
    }

    private void initializeInventory(String deliveryServiceId, Double multiplier) {
        inventory = new HashMap<>();
        
        // Create different inventory configurations based on delivery service and market
        // This simulates different markets having different products available
        String agentName = getLocalName();
        
        if (deliveryServiceId.equals("Bolt")) {
            if (agentName.contains("Market1")) {
                inventory.put("milk", 5.0 + multiplier);
                inventory.put("coffee", 30.0 + multiplier);
            } else if (agentName.contains("Market2")) {
                inventory.put("coffee", 25.0 + multiplier);
                inventory.put("rice", 3.0 + multiplier);
            }
        } else if (deliveryServiceId.equals("Uber")) {
            if (agentName.contains("Market1")) {
                inventory.put("milk", 4.0 + multiplier);
                inventory.put("rice", 4.0 + multiplier);
            } else if (agentName.contains("Market2")) {
                inventory.put("coffee", 28.0 + multiplier);
                inventory.put("rice", 2.0 + multiplier);
            }
        } else if (deliveryServiceId.equals("Glovo")) {
            if (agentName.contains("Market1")) {
                inventory.put("milk", 6.0 + multiplier);
                inventory.put("coffee", 32.0 + multiplier);
            } else if (agentName.contains("Market2")) {
                inventory.put("rice", 5.0 + multiplier);
                inventory.put("milk", 3.0 + multiplier);
            }
        } else {
            // Default inventory for unknown delivery services
            inventory.put("milk", 5.0 + multiplier);
            inventory.put("coffee", 30.0 + multiplier);
            inventory.put("rice", 4.0 + multiplier);
        }
    }

    public String getDeliveryServiceId() {
        return deliveryServiceId;
    }
    
    public Map<String, Double> getInventory() {
        return inventory;
    }
}
