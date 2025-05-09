package org.example.hw.agents;
// MarketAgent.java
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.example.hw.behaviours.MarketResponderBehaviour;

public class MarketAgent extends Agent {
    private String deliveryServiceId;

    protected void setup() {
        Object[] args = getArguments();
        Double multiplier = 10.0;
        deliveryServiceId = "General";

        if (args != null) {
            if (args.length > 0 && args[0] instanceof Double) {
                multiplier = (Double) args[0];
            }
            if (args.length > 1 && args[1] instanceof String) {
                deliveryServiceId = (String) args[1];
            }
        }

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Market-Service");
        sd.setName(getLocalName() + "-market");

        Property prop = new Property("deliveryService", deliveryServiceId);
        sd.addProperties(prop);
        
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
            System.out.println(getLocalName() + " registered as Market-Service for delivery service: " + deliveryServiceId);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
        addBehaviour(new MarketResponderBehaviour(this, mt, multiplier));
    }

    public String getDeliveryServiceId() {
        return deliveryServiceId;
    }
}
