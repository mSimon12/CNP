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

enum Steps{
    START,
    CFP,
    OFFERS,
    ANSWER,
    CONFIRMATION,
    END
  }


public class Client extends Agent {
    private int myNumber = -1;
    private int nCNPs = 3;              //Define how many CNP will be started
    private int CNPended = 0;
    private int nContracts = 0;
    private int[] tries; 

    @Override
    protected void setup() {
        // gets the argument
        Object[] args = getArguments();
        myNumber = Integer.valueOf(args[0].toString());
        nCNPs = Integer.valueOf(args[1].toString());
        tries = new int[nCNPs];
        
        System.out.println("Hello from " + getAID().getName() + "\tI am Client number " + myNumber);

        // Register the client service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("client");
        sd.setName("client" + myNumber);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // create a simple behavior 
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                while(nContracts < nCNPs){
                    //System.out.println("-> Client" + myNumber + ": \tNumber of contracts: " + nContracts);
      
                    //Define which service will be required
                    Occupation task = new Occupation();
                    String myNeed = task.getOccup();    
                                        
                    System.out.println("\n------------------- Client" + myNumber + " starting Contract Net Protocol " + (nContracts+1) + "! -------------------");
                    System.out.println("-> Client" + myNumber + ": \tTrying to contract a " + myNeed);
                    
                    tries[nContracts]=0;
                    myAgent.addBehaviour(new RequestPerformer(nContracts, myNeed));
                    nContracts++;                                              
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
        private Steps step = Steps.START;
        private int CNPId;
        private AID severino;
        
        //Constructor to define Workers that are capable to do the desired need
        RequestPerformer(int CNP, String need){
            this.CNPId = CNP;
            myNeed = need;
        }

        public void action() {   
            switch (step) {
            case START:
                // Update the list of worker agents that do the required service
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType(myNeed);
                template.addServices(sd);

                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template); 
                    if(result.length > 0){
                        // System.out.println("-> Client" + myNumber + ": \tFound the following worker agents:");
                        workerAgents = new AID[result.length];
                        for (int i = 0; i < result.length; ++i) {
                            workerAgents[i] = result[i].getName();
                            //System.out.println("\t-> " + workerAgents[i].getName());
                        }   
                        step = Steps.CFP;                         
                    }
                    else{
                        tries[CNPId]++;
                        System.out.println("-> Client" + myNumber + "-" + CNPId + ": \tNone worker agents found.");
                        try{
                            //Thread.sleep(2000);
                            Thread.sleep(100);
                        } catch(Exception e){
                        } 
                    }
                    
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                } 
                break;
            case CFP:     
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
                // System.out.println("Sending CFP...");
                
                // Prepare the template to get proposals
                mt = MessageTemplate.and(MessageTemplate.MatchConversationId("contract"),
                                        MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                step = Steps.OFFERS;
                break;
            case OFFERS:
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
                        step = Steps.ANSWER; 
                    }
                } else {
                    block();
                }
                break;
            case ANSWER:                
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
                step = Steps.CONFIRMATION;
                break;
            case CONFIRMATION:      
                // Receive the purchase order reply
                reply = myAgent.receive(mt);
                if (reply != null) {
                    // Purchase order reply received
                    if (reply.getPerformative() == ACLMessage.INFORM) {
                        // Contract successful. We can terminate
                        System.out.println("-> Client" + myNumber + "-" + CNPId + ": \n\tSuccessfully contract a " + myNeed + ": " + reply.getSender().getName());
                        System.out.println("\tPrice = " + bestPrice + "\n");
                        //nContracts--;    
                    } else {
                        // System.out.println("-> Client" + myNumber + ": \tAttempt failed: required worker already busy.");
                        tries[CNPId]++;
                        myAgent.addBehaviour(new RequestPerformer(CNPId, myNeed));
                    }
                    step=Steps.END;
                } else {
                    block();
                }
                break;  
            }              
        }

        public boolean done() {
            if(step == Steps.START && tries[CNPId]>=5){
                //End intention
                try{
                    CNPended++;
                    if(CNPended == nCNPs){
                        //System.out.println("SENDING END");
    
                        ACLMessage msg = new ACLMessage(ACLMessage.CFP);

                        DFAgentDescription template = new DFAgentDescription();
                        ServiceDescription sd = new ServiceDescription();
                        sd.setType("severino");
                        template.addServices(sd);
                
                        try{
                            DFAgentDescription[] result = DFService.search(myAgent, template);
                            severino = result[0].getName();
                        } catch (FIPAException fe) {
                            fe.printStackTrace();
                        } 

                        msg.addReceiver(severino);
                        msg.setContent("Ending");
                        msg.setConversationId("END");
                        myAgent.send(msg);
                    }
                    System.out.println("-> Client" + myNumber + "-" + CNPId + ": \tCould not found a worker for " + myNeed + ". Giving up!");
                    myAgent.removeBehaviour(this);  
                }catch(NullPointerException ex){
                    System.out.println("!!!!! Error removing intention !!!!!");
                }
            }
            
            if (step == Steps.ANSWER && bestWorker == null) {
                //restart intention
                try{
                    tries[CNPId]++;
                    if(tries[CNPId]<5){
                        myAgent.addBehaviour(new RequestPerformer(CNPId, myNeed));
                    }
                    else{
                        System.out.println("-> Client" + myNumber + "-" + CNPId + ": \tFound a suitable worker for " + myNeed + " but he is buzy. Waiting for a while.");
                        try{
                            //Thread.sleep(20000);
                            Thread.sleep(500);
                        } catch(Exception e){
                            System.out.println("-> Client" + myNumber + "-" + CNPId + ": \n\tErro in 'sleep'.");
                        }   
                        myAgent.addBehaviour(new RequestPerformer(CNPId, myNeed));

                    }
                    myAgent.removeBehaviour(this);  
                }catch(NullPointerException ex){
                    System.out.println("!!!!! Error restarting intention !!!!!");
                }
                           
            }
            else if(step == Steps.END){
                try{
                    CNPended++;
                    if(CNPended == nCNPs){
                        //System.out.println("SENDING END");
    
                        ACLMessage msg = new ACLMessage(ACLMessage.CFP);

                        DFAgentDescription template = new DFAgentDescription();
                        ServiceDescription sd = new ServiceDescription();
                        sd.setType("severino");
                        template.addServices(sd);
                
                        try{
                            DFAgentDescription[] result = DFService.search(myAgent, template);
                            severino = result[0].getName();
                        } catch (FIPAException fe) {
                            fe.printStackTrace();
                        } 

                        msg.addReceiver(severino);
                        msg.setContent("Ending");
                        msg.setConversationId("END");
                        myAgent.send(msg);
                    }
                    myAgent.removeBehaviour(this);          //Deleting this behaviour (Contract done)  
                }catch(NullPointerException ex){
                    System.out.println("!!!!! Error removing intention !!!!!");
                }
                
            }   
            return ((step == Steps.ANSWER && bestWorker == null) || step == Steps.END); 
        }
    }  // End of inner class RequestPerformer
}
