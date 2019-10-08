import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;


public class jadeTest {

    ContainerController cc;
    
    public static void main(String[] args) throws Exception {
        jadeTest s = new jadeTest();
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
            AgentController ac = cc.createNewAgent("worker"+i, "workerTest", new Object[] { i });
            ac.start();
        }
        //creating Clients
        for (int i=1; i<2; i++) {
            AgentController ac = cc.createNewAgent("client"+i, "clientTest", new Object[] { i });
            ac.start();
        }
    }
}
