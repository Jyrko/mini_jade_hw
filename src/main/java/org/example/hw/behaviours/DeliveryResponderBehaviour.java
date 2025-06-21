package org.example.hw.behaviours;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import org.example.hw.agents.DeliveryAgent;

import java.util.*;

public class DeliveryResponderBehaviour extends ContractNetResponder {
    private Agent agent;
    
    public DeliveryResponderBehaviour(Agent agent, MessageTemplate mt) {
        super(agent, mt);
        this.agent = agent;
    }
    
    protected ACLMessage handleCfp(ACLMessage cfp) {
        String order = cfp.getContent();
        System.out.println(agent.getLocalName()
                + " received order: " + order + " from " + cfp.getSender().getLocalName());
        double finalPrice = getPriceFromMarkets(order);
        ACLMessage reply = cfp.createReply();
        if (finalPrice >= 0) {
            if (agent instanceof DeliveryAgent) {
                DeliveryAgent deliveryAgent = (DeliveryAgent) agent;
                finalPrice *= deliveryAgent.getDeliveryFeeMultiplier();
            }
            
            reply.setPerformative(ACLMessage.PROPOSE);
            reply.setContent(String.valueOf(finalPrice));
        } else {
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent("Unable to complete order");
        }
        return reply;
    }

    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        System.out.println("Client rejected proposal from " + agent.getLocalName());
    }

    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
        ACLMessage inform = accept.createReply();
        inform.setPerformative(ACLMessage.INFORM);
        inform.setContent("Order completed successfully");
        return inform;
    }
    
    private double getPriceFromMarkets(String order) {
        try {
            String deliveryServiceId = null;
            if (agent instanceof DeliveryAgent) {
                deliveryServiceId = ((DeliveryAgent) agent).getDeliveryServiceId();
            }

            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("Market-Service");
            
            if (deliveryServiceId != null) {
                Property prop = new Property("deliveryService", deliveryServiceId);
                sd.addProperties(prop);
            }
            
            template.addServices(sd);
            DFAgentDescription[] results = DFService.search(agent, template);
            
            if (results.length == 0) {
                System.out.println(agent.getLocalName() + ": No markets found for delivery service: " + 
                    (deliveryServiceId != null ? deliveryServiceId : "unknown"));
                return -1;
            }

            ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
            for (DFAgentDescription dfd : results) {
                req.addReceiver(dfd.getName());
                System.out.println(agent.getLocalName() + " sending request to market: " + dfd.getName().getLocalName());
            }
            req.setContent(order);
            req.setConversationId("market-query");
            req.setReplyByDate(new Date(System.currentTimeMillis() + 5000));
            agent.send(req);


            List<ACLMessage> responses = new ArrayList<>();
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchConversationId("market-query"),
                    MessageTemplate.or(
                            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                            MessageTemplate.MatchPerformative(ACLMessage.REFUSE)
                    )
            );
            
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 5000 && responses.size() < results.length) {
                ACLMessage reply = agent.blockingReceive(mt, 1000);
                if (reply != null) {
                    responses.add(reply);
                    System.out.println(agent.getLocalName() + " received market response from " + 
                        reply.getSender().getLocalName() + ": " + reply.getContent());
                }
            }

            return computeIterativeAggregatedCost(order, responses);
            
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        return -1;
    }
    
    private double computeIterativeAggregatedCost(String order, List<ACLMessage> responses) {
        List<Map<String, Double>> marketOffers = new ArrayList<>();
        for (ACLMessage msg : responses) {
            if (msg.getPerformative() == ACLMessage.INFORM) {
                Map<String, Double> offer = new HashMap<>();
                String content = msg.getContent();
                if (!content.isEmpty()) {
                    String[] pairs = content.split(",");
                    for (String pair : pairs) {
                        String[] keyValue = pair.split(":");
                        if (keyValue.length == 2) {
                            String item = keyValue[0].trim().toLowerCase();
                            double price = Double.parseDouble(keyValue[1].trim());
                            offer.put(item, price);
                        }
                    }
                }
                marketOffers.add(offer);
            }
        }

        List<String> remainingItems = new ArrayList<>();
        String[] requestedItems = order.split(",");
        for (String item : requestedItems) {
            remainingItems.add(item.trim().toLowerCase());
        }
        double total = 0.0;

        // Updated product selection algo
        while (!remainingItems.isEmpty()) {
            int bestCount = 0;
            double bestPrice = Double.MAX_VALUE;
            Map<String, Double> bestOffer = null;

            for (Map<String, Double> offer : marketOffers) {
                List<String> available = new ArrayList<>();
                double priceSum = 0.0;
                for (String item : remainingItems) {
                    if (offer.containsKey(item)) {
                        available.add(item);
                        priceSum += offer.get(item);
                    }
                }
                int count = available.size();
                if (count > bestCount || (count == bestCount && count > 0 && priceSum < bestPrice)) {
                    bestCount = count;
                    bestPrice = priceSum;
                    bestOffer = offer;
                }
            }

            if (bestCount == 0) {
                System.out.println(agent.getLocalName() + ": Unable to supply items " + remainingItems);
                return -1;
            }

            total += bestPrice;
            System.out.println(agent.getLocalName() + ": Selected market offer covering " + bestCount
                    + " items at cost " + bestPrice);

            Iterator<String> iter = remainingItems.iterator();
            while (iter.hasNext()) {
                String item = iter.next();
                if (bestOffer.containsKey(item)) {
                    iter.remove();
                }
            }
        }
        return total;
    }
}
