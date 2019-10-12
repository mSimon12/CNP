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

public class Worker extends Agent {
    private int myNumber = -1;
    Occupation myFunction; 
    private int price = 0;
    private Boolean working = false;
    private int jobs = 0;
    private int money = 0;
    
    @Override
    protected void setup() {
        // gets the argument
        Object[] args = getArguments();
        myNumber = Integer.valueOf(args[0].toString());
        myFunction = new Occupation();

        System.out.println("Hello from " + getAID().getName() + "\tI am worker number " + myNumber + ": " + myFunction.getOccup());                

        // Register the worker service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(myFunction.getOccup());
        sd.setName("worker" + myNumber);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Add the behaviour for evaluating clients agents
        addBehaviour(new OfferRequestsServer());

        // Add the behaviour for serving clients agents
        addBehaviour(new PurchaseOrdersServer());
    }

    protected void takeDown() {
        // Printout a dismissal message
        System.out.println("Worker-agent "+getAID().getName()+" terminating.");
    }

    /**
       Inner class OfferRequestsServer.
       This is the behaviour used by Worker agents to serve incoming requests 
       for offer from Client agents.
       If the requested function is the offered by this worker, agent replies 
       with a PROPOSE message specifying the price. Otherwise a REFUSE message is
       sent back.
  */
    private class OfferRequestsServer extends CyclicBehaviour {
        public void action(){          
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);

            if(msg != null){
                // CFP Message received. Process it
                String req = msg.getContent();
                ACLMessage reply = msg.createReply();               
                
                //Verify if the required service is the Function of this Worker
                if(req.equals("Need " + myFunction.getOccup())){
                    
                    //Verify if this worker is available
                    if(!working){
                        reply.setPerformative(ACLMessage.PROPOSE);
                        price = myFunction.getPrice();
                        reply.setContent(String.valueOf(price));
                    } else {
                        // The requested worker is NOT available.
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("not-available");
                    }
                }
                else{
                    // The requested function not available for this worker.
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-myFunction");
                }
                myAgent.send(reply);
            } 
            else {
                block();
            }
        }
    }  // End of inner class OfferRequestsServer
    
  /**
       Inner class PurchaseOrdersServer.
       This is the behaviour used by Worker agents to serve incoming 
       offer acceptances (i.e. purchase orders) from Client agents.
       The Worker agent executes its actions.
  */
    private class PurchaseOrdersServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            
            if (msg != null) {
                // ACCEPT_PROPOSAL Message received. Process it
                String req = msg.getContent();
                ACLMessage reply = msg.createReply();

                //Verify if the required service is the Function of this Worker
                if(req.equals("Need " + myFunction.getOccup())){
                    if(!working){
                        reply.setPerformative(ACLMessage.INFORM);
                        
                        // Add the behaviour for agent working
                        working = true;
                        jobs++;
                        money += price;
                        myAgent.addBehaviour(new agentWorking(myFunction.getTime()));
                        System.out.println("-> Worker" + myNumber + ": \tWorking for: " + msg.getSender().getName());
                    } 
                    else{
                        // The requested action is not available
                        reply.setPerformative(ACLMessage.FAILURE);
                        reply.setContent("not-available");
                    }
                }
                else{
                    // The requested action is not the Workers function
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not-myFunction");
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }   // End of inner class OfferRequestsServer

    /**
       Inner class agentWorking.
       This is the behaviour used by Worker agents to simulate an 
       service being done.
  */

    private class agentWorking extends CyclicBehaviour {
        private int cont=0;
        private int time;

        agentWorking(int t){
            this.time = t;
        }

        public void action(){
            try{
                //Thread.sleep(200);
                Thread.sleep(1);
                cont++;
            } catch(Exception e){
                System.out.println("-> Worker" + myNumber + ": \n\tErro in 'sleep'.");
            }   
            if (cont>=this.time){
                working = false;
                System.out.println("-> Worker" + myNumber + ": \tFinished working!!!");
                myAgent.removeBehaviour(this);
            }   
        } 

    }// End of inner class agentWorking 

}
