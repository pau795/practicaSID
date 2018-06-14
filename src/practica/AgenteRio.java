package practica;

import java.util.ArrayList;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

public class AgenteRio extends Agent{
	
	private class MyTicker extends TickerBehaviour{
		
		private ArrayList<WaterMass> river;
		
		public MyTicker(Agent a, long period, ArrayList<WaterMass> r) {
			
			super(a, period);
		}

		@Override
		protected void onTick() {										//Fluir del rio
			int l =  river.size();
			for (int i = l - 1; i > 0; --i) 
				river.set(i, river.get(i - 1));
			
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
		
		river = new ArrayList<WaterMass>();
		
		Object[] args = getArguments();
		if (args.length == 1 ) sections = Integer.valueOf((String) args[0]); //la longitud del rio viene determinada por el primer parametro
		else sections = 100; //longitud predeterminada si no hay parametros	
		
		for (int i = 0; i < sections; ++i)						//Test de prueba, para que el rio empiece con masas de volumnes diversos
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
