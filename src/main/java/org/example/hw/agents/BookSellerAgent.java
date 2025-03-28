package org.example.hw.agents;

import jade.core.Agent;
import jade.core.behaviours.*;

import java.util.*;

public class BookSellerAgent extends Agent {
    private Hashtable catalogue;
//    private BookSellerGui muHui;

    protected void setup() {
        catalogue = new Hashtable();

//        myGui = new BookSellerGiu(this);
//        myGui.show();

//        addBehaviour(new OfferRequestsServer());
//        addBehaviour(new PurchaseOrdersServer());
    }

    protected void takeDown() {
//        myGui.dispose();
        System.out.println("Seller-Aget "+getAID().getName()+" terminating");
    }

    public void updateCatalogue(final String title, final int price) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                catalogue.put(title, price);
            }
        });
    }
}
