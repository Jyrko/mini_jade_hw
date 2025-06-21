package org.example.hw.agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.example.hw.behaviours.DeliveryResponderBehaviour;

public class DeliveryAgent extends Agent {
    private String deliveryServiceId;
    
    protected void setup() {
        Object[] args = getArguments();
        if (args != null) {
            if (args.length > 0 && args[0] instanceof String) {
                deliveryServiceId = (String) args[0];
            }
        } else {
            deliveryServiceId = getLocalName();
        }
        
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Delivery-Service");
        sd.setName(getLocalName() + "-delivery");

        Property serviceProp = new Property("serviceId", deliveryServiceId);
        sd.addProperties(serviceProp);
        
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
            System.out.println(getLocalName() + " registered as Delivery-Service with ID: " + deliveryServiceId);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new DeliveryResponderBehaviour(this, MessageTemplate.MatchPerformative(ACLMessage.CFP)));
    }
    
    public String getDeliveryServiceId() {
        return deliveryServiceId;
    }
}
