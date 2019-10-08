
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class clientTest extends Agent {
    int myNumber = -1;
    private String myNeed = "plumper";

    // private AID[] workerAgents;
    
    @Override
    protected void setup() {
        // gets the argument
        Object[] args = getArguments();
        myNumber = Integer.valueOf(args[0].toString());
        
        System.out.println("Hello from "+getAID().getName() + "\n\tI am Client number " + myNumber);

        // create a simple behavior 
        addBehaviour(new CyclicBehaviour() {
            
            @Override
            public void action() {
                
                System.out.println("Trying to contract a " + myNeed);

                //creating message
                ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                cfp.addReceiver(new AID("worker1", AID.ISLOCALNAME));
                cfp.setContent(myNeed);
                cfp.setConversationId("contract");
                cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
                myAgent.send(cfp);
                
                ACLMessage reply = myAgent.receive();
                if (reply != null) {
                    // Message received. Process it
                    String info = reply.getContent();

                    System.out.println("I am " + getAID().getName() + " and received a message from" + reply.getSender().getName());    
                    System.out.println("\t" + info);
                    doDelete();

                } 
                else{
                    block();
                }    
            }
        });
    }

    protected void takeDown(){
        System.out.println("Ending client!");
    }
}
