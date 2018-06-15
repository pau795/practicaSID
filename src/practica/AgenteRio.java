package practica;

import java.util.ArrayList;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

@SuppressWarnings("serial")
public class AgenteRio extends Agent{
	
	private class MyTicker extends TickerBehaviour{
		
		private ArrayList<WaterMass> river;
		
		public MyTicker(Agent a, long period, ArrayList<WaterMass> r) {
			
			super(a, period);
			this.river=r;
		}

		@Override
		protected void onTick() {										//Fluir del rio
			int l =  river.size();
			for (int i = l - 1; i > 0; --i) 
				river.set(i, river.get(i - 1));
			
			// First section is always a new waterMass
			WaterMass m = new WaterMass();
			river.set(0, m);
			
			for (int i = 0; i < l; ++i) 
				System.out.print(river.get(i).getVolume() + " ");		//Print del rio a cada tick
			
			System.out.println();
		}
	}	
	
	private ArrayList<WaterMass> river;
	private int sections; //longitud rio;
		
	
	protected void setup() {
		
	  	// Register the service
	  	System.out.println("Agent " + getLocalName() + " registering service \"" + "River Sevice");
	  	try {
	  		DFAgentDescription dfd = new DFAgentDescription();
	  		dfd.setName(getAID());
	  		
	  		ServiceDescription sd = new ServiceDescription();
	  		sd.setType("River");
	  		dfd.addServices(sd);
	  		DFService.register(this, dfd);
	  	}
	  	catch (FIPAException fe) {
	  		fe.printStackTrace();
	  	}
		
		river = new ArrayList<WaterMass>();
		
		Object[] args = getArguments();
		if (args.length == 1 ) sections = Integer.valueOf((String) args[0]); //la longitud del rio viene determinada por el primer parametro
		else sections = 100; //longitud predeterminada si no hay parametros	
		
		for (int i = 0; i < sections; ++i)					
			river.add(new WaterMass());		
		
		MyTicker ticker = new MyTicker(this, 3000, river);
		addBehaviour(ticker);
		
		for (int i=0; i<sections; ++i) 
			System.out.print(river.get(i).getVolume() + " ");  //Test del rio inicial
		
		System.out.println();
	}
	
	protected void takeDown() {
		for (int i = 0; i < sections; ++i) 
			System.out.print(river.get(i).getVolume() + " ");
		
		System.out.println();
	}
}
