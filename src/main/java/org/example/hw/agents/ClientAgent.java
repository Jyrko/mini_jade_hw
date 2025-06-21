package org.example.hw.agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import org.example.hw.behaviours.ClientNegotiatorBehaviour;

import java.util.Date;
import java.util.Vector;

public class ClientAgent extends Agent {
    protected void setup() {
        // Get order from arguments - can be either String[] or String
        String[] orderItems = {"milk", "coffee", "rice"}; // Default fallback
        Object[] args = getArguments();
        
        if (args != null && args.length > 0) {
            if (args[0] instanceof String[]) {
                // Array of products
                orderItems = (String[]) args[0];
            } else if (args[0] instanceof String) {
                // Backward compatibility: single string with comma separation
                String orderString = (String) args[0];
                orderItems = orderString.split(",");
                // Trim whitespace from each item
                for (int i = 0; i < orderItems.length; i++) {
                    orderItems[i] = orderItems[i].trim();
                }
            }
        }
        
        // Convert array back to string for message content (maintaining protocol compatibility)
        String order = String.join(",", orderItems);
        
        System.out.println(getLocalName() + " ordering products: " + java.util.Arrays.toString(orderItems));

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Delivery-Service");
        template.addServices(sd);
        try {
            DFAgentDescription[] results = DFService.search(this, template);
            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
            for (DFAgentDescription dfd : results) {
                cfp.addReceiver(dfd.getName());
            }
            cfp.setContent(order);
            cfp.setConversationId("delivery-order");
            cfp.setReplyByDate(new Date(System.currentTimeMillis() + 10000));

            addBehaviour(new ClientNegotiatorBehaviour(this, cfp));
            System.out.println(getLocalName() + " registered as Client-Service");
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
}
