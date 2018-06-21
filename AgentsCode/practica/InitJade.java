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
		System.out.println("How many sections should the river have? (recommended 30)");
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
		System.out.println("How many industries do you want? (recommended 10)");
		String industries = "";
		try {
			industries = br.readLine();
			System.out.println("Ok, there will be " + industries + " industries.");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		String riverPeriod="";
		System.out.println();
		System.out.println();
		System.out.println("Choose the period time for each river cicle (in miliseconds, minimum 1000 ms, recommended 1500ms)");
		try {
			riverPeriod=br.readLine();
			int p = Integer.valueOf(riverPeriod);
			System.out.println();
			if (p < 1000) {
				System.out.println("The proposed period is too short. It will be 1000ms");
				riverPeriod=String.valueOf(1000);
			}
			else {
				System.out.println("The river period will be " + p + "ms");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String industryPeriod="";
		System.out.println();
		System.out.println();
		System.out.println("Choose the period time for each industry cicle (in miliseconds, minimum 1000 ms, recommended 3000ms)");
		try {
			industryPeriod=br.readLine();
			int p = Integer.valueOf(industryPeriod);
			System.out.println();
			if (p < 1000) {
				System.out.println("The proposed period is too short. It will be 1000ms)");
				industryPeriod=String.valueOf(1000);
			}
			else {
				System.out.println("The industry period will be " + p + "ms");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String edarPeriod="";
		System.out.println();
		System.out.println();
		System.out.println("Choose the period time for each EDAR cicle (in miliseconds, minimum 1000 ms, recommended 3000ms)");
		try {
			edarPeriod=br.readLine();
			int p = Integer.valueOf(edarPeriod);
			System.out.println();
			if (p < 1000) {
				System.out.println("The proposed period is too short. It will be 1000ms");
				edarPeriod=String.valueOf(1000);
			}
			else {
				System.out.println("The EDAR period will be " + p + "ms");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		String rainPeriod="";
		System.out.println();
		System.out.println();
		System.out.println("Choose the period time for each rain cicle (in miliseconds, minimum 1000 ms, recommended 3000ms)");
		try {
			rainPeriod=br.readLine();
			int p = Integer.valueOf(rainPeriod);
			System.out.println();
			if (p < 1000) {
				System.out.println("The proposed period is too short. It will be 1000ms");
				rainPeriod=String.valueOf(1000);
			}
			else {
				System.out.println("The rain period will be " + p + "ms");
			}
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
			Object edarArgs[] = new String[1];
			edarArgs[0] = edarPeriod;
			AgentController acEDAR = container.createNewAgent("EDAR", "practica.EdarAgent", edarArgs);
			
			Object riverArgs[] = new String[2];
			riverArgs[0] = sections;
			riverArgs[1] = riverPeriod;
			AgentController acRiver = container.createNewAgent("River", "practica.RiverAgent", riverArgs);
			
			Object rainArgs[] = new String[1];
			rainArgs[0] = rainPeriod;
			AgentController acRain = container.createNewAgent("Rain", "practica.RainAgent", rainArgs);
			
			AgentController acGUI = container.createNewAgent("GUI", "practica.GUIAgent", null);			
			acEDAR.start();
			acRiver.start();
			acGUI.start();
			acRain.start();
			
			Object industryArgs[] = new String[1];
			industryArgs[0] = industryPeriod;
			
			for(int i = 1; i <= Integer.valueOf(industries); ++i) {
				AgentController acInd = container.createNewAgent("Industry" + String.valueOf(i), "practica.IndustryAgent", industryArgs);
				acInd.start();
			}
				
			
		} catch (StaleProxyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
