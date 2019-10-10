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
import jade.wrapper.ContainerController;

public class Severino extends Agent {
    ContainerController cc;
    private int nClients = 0;
    private int finishedClients = 0;
    private int n;

    @Override
    protected void setup() {
        // gets the argument
        Object[] args = getArguments();
        cc = (ContainerController)args[0];
        
        // Register the client service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sdv = new ServiceDescription();
        sdv.setType("severino");
        sdv.setName("severino");
        dfd.addServices(sdv);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }


        System.out.println("Hello from " + getAID().getName());
        try{
            Thread.sleep(10000);
        } catch(Exception e){
            System.out.println("-> Severino" + ": \n\tErro in 'sleep'.");
        }
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("client");
        template.addServices(sd);

        try{
            DFAgentDescription[] result = DFService.search(this, template);
            nClients = result.length;
        } catch (FIPAException fe) {
            fe.printStackTrace();
        } 
    
        MessageTemplate mt = MessageTemplate.MatchConversationId("END");

        // create a simple behavior 
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                while(finishedClients < nClients){
                    //System.out.println("-> Client" + myNumber + ": \tNumber of contracts: " + nContracts);

                    try{
                        Thread.sleep(2000);
                    } catch(Exception e){
                        System.out.println("-> Severino: \n\tErro in 'sleep'.");
                    }

                    //Verify quantity of clients
                    try{
                        DFAgentDescription[] result = DFService.search(myAgent, template);  
                        nClients = result.length;                
                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    } 
                    
                    ACLMessage reply = myAgent.receive(mt);

                    if (reply != null) {
                        finishedClients++;
                    } else {
                        block();
                    }                                    
                }

                try{
                    Thread.sleep(10000);
                } catch(Exception e){
                    System.out.println("-> Severino: \n\tErro in 'sleep'.");
                }
                
                //All clients ended
                System.out.println("\nAll Clients ended.\n");
                
                for(int i = 0; i < Occupation.nOpccupations(); i++){
                    sd.setType(Occupation.num2func(i));
                    template.addServices(sd);
                    try{
                        DFAgentDescription[] result = DFService.search(myAgent, template);  
                        n = result.length;                  
                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    } 
                    System.out.println("Quantity of " + Occupation.num2func(i) + ": " + n);
                }
                try{
                    cc.getPlatformController().kill();
                }catch(Exception e){
                    System.out.println("Erro in kill.");
                }
                
                
            }
        });
        //doDelete();
    }

    protected void takeDown() {
        // Printout a dismissal message
        System.out.println("Severino terminating.");
    }
}
