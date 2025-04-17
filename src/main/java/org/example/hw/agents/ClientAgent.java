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
//        FIXME: Make it as arguments
        String order = "milk,coffee,rice";

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
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
}
