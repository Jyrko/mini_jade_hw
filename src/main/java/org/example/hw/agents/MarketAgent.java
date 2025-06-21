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
        // Get arguments: inventory and deliveryServiceId
        Object[] args = getArguments();
        deliveryServiceId = "General";

        if (args != null) {
            if (args.length > 0 && args[0] instanceof Map) {
                inventory = (Map<String, Double>) args[0];
            }
            if (args.length > 1 && args[1] instanceof String) {
                deliveryServiceId = (String) args[1];
            }
        }

        // Fallback inventory if none provided
        if (inventory == null) {
            inventory = new HashMap<>();
            inventory.put("milk", 5.0);
            inventory.put("coffee", 30.0);
            inventory.put("rice", 4.0);
        }

        // Registration with the DF as Market-Service
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Market-Service");
        sd.setName(getLocalName() + "-market");
        
        // Add the delivery service as a property
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
        // No more multiplier - behavior relies solely on inventory prices
        addBehaviour(new MarketResponderBehaviour(this, mt));
    }

    public String getDeliveryServiceId() {
        return deliveryServiceId;
    }
    
    public Map<String, Double> getInventory() {
        return inventory;
    }
}
