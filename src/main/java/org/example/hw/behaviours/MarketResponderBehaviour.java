package org.example.hw.behaviours;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import org.example.hw.agents.MarketAgent;

import java.util.Map;

public class MarketResponderBehaviour extends ContractNetResponder {
    Agent agent;

    public MarketResponderBehaviour(Agent a, MessageTemplate mt) {
        super(a, mt);
        this.agent = a;
    }

    protected ACLMessage handleCfp(ACLMessage cfp) {
        String order = cfp.getContent();
        System.out.println(agent.getLocalName()
                + " received market quote request for order: " + order);

        if (cfp.getPerformative() == ACLMessage.REQUEST &&
                cfp.getConversationId() != null && cfp.getConversationId().equals("market-query")) {
            return handleMarketQuery(cfp);
        }

        System.out.println(agent.getLocalName() + ": Unexpected message format, ignoring");
        return null;
    }

    private ACLMessage handleMarketQuery(ACLMessage request) {
        String content = request.getContent();
        String[] requestedItems = content.split(",");
        StringBuilder responseContent = new StringBuilder();

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
        }

        ACLMessage reply = request.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        reply.setContent(responseContent.toString());
        System.out.println(agent.getLocalName() + ": Replied with inventory info: " + reply.getContent());
        return reply;
    }
}
