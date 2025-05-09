package org.example.hw.behaviours;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

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
        double totalPrice = calculatePrice(order);
        ACLMessage reply = cfp.createReply();
        reply.setPerformative(ACLMessage.PROPOSE);
        reply.setContent(String.valueOf(totalPrice));
        return reply;
    }

    // A simple price calculation method.
    // In a full implementation, this would consider available items and their individual pricing.
    private double calculatePrice(String order) {
        // For demonstration, suppose each item costs 10.0zl.
        String[] items = order.split(",");
        return items.length * multiplier;
    }
}
