// package JADE;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;


public class StartJade {

    ContainerController cc;
    private int nCNPs = 3;
    private int nClients = 5;
    private int nWorkers = 50;
    
    public static void main(String[] args) throws Exception {
        StartJade s = new StartJade();

        if(args.length > 0)
        {
            s.nWorkers = Integer.parseInt(args[0]);
            if(args.length > 1)
            {
                s.nClients = Integer.parseInt(args[1]);
                if(args.length > 2)
                {
                    s.nCNPs = Integer.parseInt(args[2]);
                }
            }
        }

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
        for (int i=1; i<=nWorkers; i++) {
            AgentController ac = cc.createNewAgent("worker"+i, "agents.Worker", new Object[] { i });
            ac.start();
        }
        //creating Clients
        for (int i=1; i<=nClients; i++) {
            AgentController ac = cc.createNewAgent("client"+i, "agents.Client", new Object[] { i, nCNPs });
            ac.start();
        }

        AgentController ac = cc.createNewAgent("severino", "agents.Severino", new Object[] { cc });
        ac.start();
    }
}
