package org.example.hw.agents;
// MarketAgent.java
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.example.hw.behaviours.MarketResponderBehaviour;

public class MarketAgent extends Agent {

    protected void setup() {
        // Registration with the DF as Market-Service (Task 1)
        Object[] args = getArguments();
        Double multiplier = 10.0;

        if (args != null && args.length > 0 && args[0] instanceof Double) {
            multiplier = (Double) args[0];
        }

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Market-Service");
        sd.setName(getLocalName() + "-market");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
            System.out.println(getLocalName() + " registered as Market-Service.");
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
        addBehaviour(new MarketResponderBehaviour(this, mt, multiplier));
    }
}
