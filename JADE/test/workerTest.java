

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.util.Random;

public class workerTest extends Agent {
    int myNumber = -1;
    String myFunction = null; 
    
    @Override
    protected void setup() {
        // gets the argument
        Object[] args = getArguments();
        myNumber = Integer.valueOf(args[0].toString());
        
        Random rand = new Random();
        int n = rand.nextInt(3);

        if(n==0){
            myFunction = "plumper";
        }
        else if(n==1){
            myFunction = "electrician";
        }else{
            myFunction = "builder";
        }

        System.out.println("Hello from " + getAID().getName() + "\n\tI am worker number " + myNumber + ": " + myFunction );    
                
        // create a simple behavior         
        addBehaviour(new CyclicBehaviour() {
            
            @Override
            public void action() {
                System.out.println("Worker looking for messages...");
                ACLMessage msg = myAgent.receive();
                if (msg != null) {
                    // Message received. Process it
                    String info = msg.getContent();

                    System.out.println("Message received from " + msg.getSender().getName());
                    System.out.println(info);
                    
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.PROPOSE);
                    // reply.addReceiver(msg.getSender());
                    reply.setConversationId(msg.getConversationId());
                    
                    reply.setContent("I am worker " + myFunction );
                    myAgent.send(reply);
                    doDelete();

                } 
                else{
                    block();
                }  
            }
        });
    }

    protected void takeDown(){
        System.out.println("Ending worker!");
    }
}
