package org.example.hw.behaviours;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

import java.util.Date;

public class DeliveryResponderBehaviour extends ContractNetResponder {
    Agent agent;
    public DeliveryResponderBehaviour(Agent agent, MessageTemplate mt) {
        super(agent, mt);
        this.agent = agent;
    }
    protected ACLMessage handleCfp(ACLMessage cfp) {
        String order = cfp.getContent();
        System.out.println(agent.getLocalName()
                + " received order: " + order);
        double finalPrice = getPriceFromMarkets(order);
        ACLMessage reply = cfp.createReply();
        if (finalPrice >= 0) {
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
        double bestPrice = Double.MAX_VALUE;
        try {

            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("Market-Service");
            template.addServices(sd);
            DFAgentDescription[] results = DFService.search(agent, template);

            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
            for (DFAgentDescription dfd : results) {
                cfp.addReceiver(dfd.getName());
            }
            cfp.setContent(order);
            cfp.setConversationId("market-quote");
            cfp.setReplyByDate(new Date(System.currentTimeMillis() + 5000));
            agent.send(cfp);


            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchConversationId("market-quote"),
                    MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 5000) {
                ACLMessage reply = agent.blockingReceive(mt, 1000);
                if (reply != null) {
                    double price = Double.parseDouble(reply.getContent());
                    if (price < bestPrice) {
                        bestPrice = price;
                    }
                }
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        return bestPrice == Double.MAX_VALUE ? -1 : bestPrice;
    }
}
