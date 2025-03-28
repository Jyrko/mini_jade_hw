package org.example.hw.agents;

import jade.core.Agent;
import jade.core.AID;

public class BookBuyerAgent extends Agent {
    private String targetBookTitle;

    private AID[] sellerAgents = {
      new AID("seller1", AID.ISLOCALNAME)
    };
    protected void setup() {
        System.out.println("Hello! Buyer-agent " + getAID().getName() + " is ready!");
    }
}
