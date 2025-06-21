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
        String[] orderItems = {"milk", "coffee", "rice"}; // Default fallback
        Object[] args = getArguments();
        
        if (args != null && args.length > 0) {
            orderItems = (String[]) args[0];
        }
        
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
