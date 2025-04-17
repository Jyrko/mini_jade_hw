package org.example.hw.behaviours;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import java.util.Date;

import java.util.Vector;

public class ClientNegotiatorBehaviour extends ContractNetInitiator {
    public ClientNegotiatorBehaviour(Agent a, ACLMessage cfp) {
        super(a, cfp);
    }

    protected void handlePropose(ACLMessage propose, Vector responses) {
        System.out.println("Received proposal from "
                + propose.getSender().getLocalName()
                + ": Price = " + propose.getContent());
    }

    protected void handleRefuse(ACLMessage refuse) {
        System.out.println("Agent " + refuse.getSender().getLocalName()
                + " refused the request.");
    }

    protected void handleFailure(ACLMessage failure) {
        System.out.println("Agent " + failure.getSender().getLocalName()
                + " failed to respond.");
    }

    // Once all responses are in, choose the best proposal
    protected void handleAllResponses(Vector responses, Vector acceptances) {
        ACLMessage bestProposal = null;
        double bestPrice = Double.MAX_VALUE;
        for (Object obj : responses) {
            ACLMessage response = (ACLMessage) obj;
            if (response.getPerformative() == ACLMessage.PROPOSE) {
                double price = Double.parseDouble(response.getContent());
                if (price < bestPrice) {
                    bestPrice = price;
                    bestProposal = response;
                }
            }
        }

        for (Object obj : responses) {
            ACLMessage response = (ACLMessage) obj;
            ACLMessage reply = response.createReply();
            if (response == bestProposal) {
                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);

                reply.setContent("Payment: " + bestPrice);
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
