package practica;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class InitJade extends Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	protected void setup() {
		
		this.blockingReceive(1000);
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println("How many sections should the river has?");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in) );
		String sections = "";
		try {
			sections = br.readLine();
			System.out.println();
			System.out.println("Ok, the river will have " + sections + " sections.");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println();
		System.out.println();
		System.out.println("How many industries do you want?");
		String industries = "";
		try {
			industries = br.readLine();
			System.out.println("Ok, there will be " + industries + " industries.");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println();
		System.out.println();
		System.out.println("Let's run JADE!!!");
		System.out.println();
		System.out.println();
		
		
		ContainerController container = getContainerController();
		try {
			AgentController acEDAR = container.createNewAgent("EDAR", "practica.EdarAgent", null);
			Object args[] = new String[1];
			args [0] = sections;
			AgentController acRiver = container.createNewAgent("River", "practica.RiverAgent", args);
			AgentController acGUI = container.createNewAgent("GUI", "practica.GUIAgent", null);
			
			acEDAR.start();
			acRiver.start();
			acGUI.start();
			
			for(int i = 1; i <= Integer.valueOf(industries); ++i) {
				AgentController acInd = container.createNewAgent("Industry" + String.valueOf(i), "practica.IndustryAgent", null);
				acInd.start();
			}
				
			
		} catch (StaleProxyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
