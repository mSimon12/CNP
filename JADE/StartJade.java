// package JADE;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;


public class StartJade {

    ContainerController cc;
    
    public static void main(String[] args) throws Exception {
        StartJade s = new StartJade();
        s.startContainer();
        s.createAgents();         
    }

    void startContainer() {
        //Runtime rt= Runtime.instance();
        ProfileImpl p = new ProfileImpl();
        p.setParameter(Profile.MAIN_HOST, "localhost");
        p.setParameter(Profile.GUI, "true");
        
        cc = Runtime.instance().createMainContainer(p);
    }

    void createAgents() throws Exception {
        //creating Workers
        for (int i=1; i<2; i++) {
            AgentController ac = cc.createNewAgent("worker"+i, "agents.Worker", new Object[] { i });
            ac.start();
        }
        //creating Clients
        for (int i=1; i<2; i++) {
            AgentController ac = cc.createNewAgent("client"+i, "agents.Client", new Object[] { i });
            ac.start();
        }
    }
}
