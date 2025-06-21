package org.example.hw.behaviours;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import org.example.hw.agents.MarketAgent;

import java.util.Map;

public class MarketResponderBehaviour extends ContractNetResponder {
    Agent agent;
    Double multiplier;

    public MarketResponderBehaviour(Agent a, MessageTemplate mt) {
        this(a, mt, 10.0);
    }

    public MarketResponderBehaviour(Agent a, MessageTemplate mt, Double multiplier) {
        super(a, mt);
        this.multiplier = multiplier;
        this.agent = a;
    }

    protected ACLMessage handleCfp(ACLMessage cfp) {
        String order = cfp.getContent();
        System.out.println(agent.getLocalName()
                + " received market quote request for order: " + order);

        // Handle both old CFP format and new REQUEST format
        if (cfp.getConversationId() != null && cfp.getConversationId().equals("market-quote")) {
            // Original CFP format - return total price
            double totalPrice = calculatePrice(order);
            ACLMessage reply = cfp.createReply();
            reply.setPerformative(ACLMessage.PROPOSE);
            reply.setContent(String.valueOf(totalPrice));
            return reply;
        } else if (cfp.getPerformative() == ACLMessage.REQUEST &&
                cfp.getConversationId() != null && cfp.getConversationId().equals("market-query")) {
            // New REQUEST format - return item:price inventory
            return handleMarketQuery(cfp);
        }

        // Default behavior
        double totalPrice = calculatePrice(order);
        ACLMessage reply = cfp.createReply();
        reply.setPerformative(ACLMessage.PROPOSE);
        reply.setContent(String.valueOf(totalPrice));
        return reply;
    }

    private ACLMessage handleMarketQuery(ACLMessage request) {
        String content = request.getContent();
        String[] requestedItems = content.split(",");
        StringBuilder responseContent = new StringBuilder();

        // Get inventory from MarketAgent if available
        Map<String, Double> inventory = null;
        if (agent instanceof MarketAgent) {
            inventory = ((MarketAgent) agent).getInventory();
        }

        if (inventory != null) {
            for (String item : requestedItems) {
                item = item.trim().toLowerCase();
                if (inventory.containsKey(item)) {
                    responseContent.append(item)
                            .append(":")
                            .append(inventory.get(item))
                            .append(",");
                }
            }
            if (responseContent.length() > 0) {
                responseContent.setLength(responseContent.length() - 1);
            }
        } else {
            // Fallback to old calculation method
            for (String item : requestedItems) {
                item = item.trim().toLowerCase();
                double price = multiplier; // Simple price calculation
                responseContent.append(item)
                        .append(":")
                        .append(price)
                        .append(",");
            }
            if (responseContent.length() > 0) {
                responseContent.setLength(responseContent.length() - 1);
            }
        }

        ACLMessage reply = request.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        reply.setContent(responseContent.toString());
        System.out.println(agent.getLocalName() + ": Replied with inventory info: " + reply.getContent());
        return reply;
    }

    // A simple price calculation method for backward compatibility
    private double calculatePrice(String order) {
        String[] items = order.split(",");
        final double result = items.length * multiplier;
        System.out.println(this.agent.getLocalName() + " calculated order. Result: " + result);
        return result;
    }
}
