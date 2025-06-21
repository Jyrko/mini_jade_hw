package org.example.hw.behaviours;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.Property;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Iterator;

public class ClientNegotiatorBehaviour extends ContractNetInitiator {
    private Map<String, String> deliveryServiceDetails = new HashMap<>();
    
    public ClientNegotiatorBehaviour(Agent a, ACLMessage cfp) {
        super(a, cfp);
    }

    protected void handlePropose(ACLMessage propose, Vector responses) {
        System.out.println("Received proposal from " + propose.getSender().getLocalName()
                + ": Price = " + propose.getContent());

        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(propose.getSender());
            DFAgentDescription[] result = DFService.search(myAgent, dfd);
            if (result.length > 0) {
                ServiceDescription sd = (ServiceDescription) result[0].getAllServices().next();

                String serviceId = null;
                
                Iterator it = sd.getAllProperties();
                while (it.hasNext()) {
                    Property prop = (Property) it.next();
                    if (prop.getName().equals("serviceId")) {
                        serviceId = (String) prop.getValue();
                    }
                }
                
                if (serviceId != null) {
                    deliveryServiceDetails.put(propose.getSender().getLocalName(), 
                        "Service: " + serviceId);
                }
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    protected void handleRefuse(ACLMessage refuse) {
        System.out.println("Agent " + refuse.getSender().getLocalName()
                + " refused the request: " + refuse.getContent());
    }

    protected void handleFailure(ACLMessage failure) {
        System.out.println("Agent " + failure.getSender().getLocalName()
                + " failed to respond: " + failure.getContent());
    }

    protected void handleAllResponses(Vector responses, Vector acceptances) {
        ACLMessage bestProposal = null;
        double bestPrice = Double.MAX_VALUE;
        
        System.out.println("\n--- EVALUATING ALL DELIVERY SERVICE OFFERS ---");
        
        for (Object obj : responses) {
            ACLMessage response = (ACLMessage) obj;
            if (response.getPerformative() == ACLMessage.PROPOSE) {
                double price = Double.parseDouble(response.getContent());
                String details = deliveryServiceDetails.getOrDefault(
                    response.getSender().getLocalName(), "Unknown delivery service");
                
                System.out.println("Offer from " + response.getSender().getLocalName() + 
                    " (" + details + "): " + price);
                    
                if (price < bestPrice) {
                    bestPrice = price;
                    bestProposal = response;
                }
            }
        }
        
        if (bestProposal != null) {
            System.out.println("\n--- BEST OFFER SELECTED ---");
            System.out.println("Best offer from: " + bestProposal.getSender().getLocalName() + 
                " (" + deliveryServiceDetails.getOrDefault(bestProposal.getSender().getLocalName(), 
                "Unknown delivery service") + ")");
            System.out.println("Final price: " + bestPrice);
        }

        for (Object obj : responses) {
            ACLMessage response = (ACLMessage) obj;
            ACLMessage reply = response.createReply();
            if (response == bestProposal) {
                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                reply.setContent("Payment: " + bestPrice);
                System.out.println("ClientAgent paid to " + response.getSender().getLocalName() + " Amount: " + bestPrice);
            } else {
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
            }
            acceptances.add(reply);
        }
    }

    protected void handleInform(ACLMessage inform) {
        System.out.println("Final confirmation received from "
                + inform.getSender().getLocalName() + ": "
                + inform.getContent());
    }
}
