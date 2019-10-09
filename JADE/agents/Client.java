package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class Client extends Agent {
    private int myNumber = -1;
    private int nCNPs = 3;              //Define how many CNP will be started
    private int nContracts = 0;

    @Override
    protected void setup() {
        // gets the argument
        Object[] args = getArguments();
        myNumber = Integer.valueOf(args[0].toString());
        
        System.out.println("Hello from " + getAID().getName() + "\tI am Client number " + myNumber);

        // create a simple behavior 
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                while(nContracts < nCNPs){
                    //System.out.println("-> Client" + myNumber + ": \tNumber of contracts: " + nContracts);
                    
                    //Define which service will be required
                    Occupation task = new Occupation();
                    String myNeed = task.getOccup();                    
                    System.out.println("-> Client" + myNumber + ": \tTrying to contract a " + myNeed);
                    
                    // Update the list of worker agents that do the required service
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType(myNeed);
                    template.addServices(sd);
                    
                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, template); 
                        if(result.length > 0){
                            // System.out.println("-> Client" + myNumber + ": \tFound the following worker agents:");
                            AID[] workerAgents = new AID[result.length];
                            for (int i = 0; i < result.length; ++i) {
                                workerAgents[i] = result[i].getName();
                                // System.out.println("\t-> " + workerAgents[i].getName());
                            }

                            // Perform the request
                            nContracts++;
                            System.out.println("\n------------------- Client" + myNumber + " starting Contract Net Protocol " + nContracts + "! -------------------");
                            myAgent.addBehaviour(new RequestPerformer(workerAgents, myNeed));                            
                        }
                        else{
                            System.out.println("-> Client" + myNumber + ": \tNone worker agents found.");
                        }
                        
                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    }                    
                }                
            }
        });
        //doDelete();
    }

    protected void takeDown() {
        // Printout a dismissal message
        System.out.println("Client-agent "+getAID().getName()+" terminating.");
    }

    /**
       Inner class RequestPerformer.
       This is the behaviour used by Client agents to request Worker 
       agents to do some action.
     */
    private class RequestPerformer extends Behaviour {  
        private MessageTemplate mt;     // The template to receive replies
        private AID bestWorker;         // The agent who provides the best offer
        private AID[] workerAgents;     // Possible workers to be contracted
        private String myNeed;          //Required service
        private int bestPrice;          // The best offered price
        private int repliesCnt = 0;     // The counter of replies from seller agents
        private int step = 0;
        
        //Constructor to define Workers that are capable to do the desired need
        RequestPerformer(AID[] id, String need){
            workerAgents = id;
            myNeed = need;
        }

        public void action() {   
            switch (step) {
            case 0:     
                // Send the cfp to all sellers
                ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                
                //Insert all intended workers to receive the message
                for (int i = 0; i < workerAgents.length; ++i) {
                    cfp.addReceiver(workerAgents[i]);
                } 
                cfp.setContent("Need " + myNeed);
                cfp.setConversationId("contract");
                cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
                myAgent.send(cfp);
                
                // Prepare the template to get proposals
                mt = MessageTemplate.and(MessageTemplate.MatchConversationId("contract"),
                                        MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                step = 1;
                break;
            case 1:
                // Receive all proposals/refusals from seller agents
                ACLMessage reply = myAgent.receive(mt);

                if (reply != null) {
                    // Reply received
                    if (reply.getPerformative() == ACLMessage.PROPOSE) {
                        
                        // This is an offer 
                        int price = Integer.parseInt(reply.getContent());
                        if (bestWorker == null || price < bestPrice) {
                            // This is the best offer at present
                            bestPrice = price;
                            bestWorker = reply.getSender();
                        }
                    }
                    repliesCnt++;
                    if (repliesCnt >= workerAgents.length) {
                        // We received all replies
                        step = 2; 
                    }
                } else {
                    block();
                }
                break;
            case 2:                
                // Send the purchase order to the seller that provided the best offer
                ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                order.addReceiver(bestWorker);
                order.setContent("Need " + myNeed);
                order.setConversationId("contract");
                order.setReplyWith("order" + System.currentTimeMillis());
                myAgent.send(order);
                
                // Prepare the template to get the purchase order reply
                mt = MessageTemplate.and(MessageTemplate.MatchConversationId("contract"),
                                        MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                step = 3;
                break;
            case 3:      
                // Receive the purchase order reply
                reply = myAgent.receive(mt);
                if (reply != null) {
                    // Purchase order reply received
                    if (reply.getPerformative() == ACLMessage.INFORM) {
                        // Contract successful. We can terminate
                        System.out.println("-> Client" + myNumber + ": \n\tSuccessfully contract a " + myNeed + ": " + reply.getSender().getName());
                        System.out.println("\tPrice = " + bestPrice + "\n");
                        //nContracts--;    
                    } else {
                        // System.out.println("-> Client" + myNumber + ": \tAttempt failed: required worker already busy.");
                        myAgent.addBehaviour(new RequestPerformer(workerAgents,myNeed));
                    }
                    step=4;
                } else {
                    block();
                }
                break;  
            }              
        }

        public boolean done() {
            if (step == 2 && bestWorker == null) {
                // System.out.println("-> Client" + myNumber + ": \tAttempt failed: " + myNeed + " not available!");
                
                //restart intention
                try{
                    myAgent.addBehaviour(new RequestPerformer(workerAgents,myNeed));
                    myAgent.removeBehaviour(this);  
                }catch(NullPointerException ex){
                    System.out.println("!!!!! Error restarting intention !!!!!");
                }
                           
            }
            else if(step==4){
                try{
                    myAgent.removeBehaviour(this);          //Deleting this behaviour (Contract done)  
                }catch(NullPointerException ex){
                    System.out.println("!!!!! Error removing intention !!!!!");
                }
                
            }   
            return ((step == 2 && bestWorker == null) || step == 4); 
        }
    }  // End of inner class RequestPerformer
}
