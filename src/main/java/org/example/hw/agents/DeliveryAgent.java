package org.example.hw.agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.example.hw.behaviours.DeliveryResponderBehaviour;


public class DeliveryAgent extends Agent {
    protected void setup() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Delivery-Service");
        sd.setName(getLocalName() + "-delivery");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
            System.out.println(getLocalName() + " registered as Delivery-Service.");
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new DeliveryResponderBehaviour(this, MessageTemplate.MatchPerformative(ACLMessage.CFP)));
    }
}
