package practica;

import java.util.ArrayList;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import paqueteSid.MassaAgua;
import paqueteSid.impl.DefaultMassaAgua;

public class AgenteRio extends Agent{
	
	private class MyTicker extends TickerBehaviour{
		
		ArrayList<MassaAgua> rio;
		
		public MyTicker(Agent a, long period, ArrayList<MassaAgua> r) {
			
			super(a, period);
			rio=r;
		}

		@Override
		protected void onTick() {										//Fluir del rio
			int l =  rio.size();
			for (int i = l-2; i>=0; --i) {
				if(i==0) {
					rio.set(i+1, rio.get(i));
					MassaAgua m =AgenteRio.massaAguaPredeterminada();
					m.addVolumen((float) 100.0);
					rio.set(i, m);
				}
				else rio.set(i+1, rio.get(i));
			}
			for (int i=0; i<l; ++i) {
				System.out.print(rio.get(i).getVolumen().get(0) + " ");		//Print del rio a cada tick
			}
			System.out.println();
		}
	}	
	
	ArrayList<MassaAgua> rio;
	int l; //longitud rio;
	
	 //---------MASA DE AGUA PREDETERMINADA-----------
	
		static private float pvolumen = 100;
		static private float pnitratos = 1;
		static private float psulfatos = 1;
		static private float psolidos = 1;
		static private float poxigenoq = 1;
		static private float poxigenob = 1;
		
		static public MassaAgua massaAguaPredeterminada() {
			MassaAgua m= new DefaultMassaAgua();		//Falta añadir el volumen predeterminado, de momento lo dejo asi para que los tests funcionen
			m.addTotalNitratos(pnitratos);
			m.addTotalSulfatos(psulfatos);
			m.addSolidosSuspension(psolidos);
			m.addDemandaQuimicaOxigeno(poxigenoq);
			m.addDemandaBiologicaOxigeno(poxigenob);
			return m;
		}
		
	//----------------------------------------------
	protected void setup() {
		rio = new ArrayList<MassaAgua>();
		Object[] args = getArguments();
		if (args.length == 1 ) l = Integer.valueOf((String) args[0]); //la longitud del rio viene determinada por el primer parametro
		else l = 100; //longitud predeterminada si no hay parametros	
		for (int i=0; i<l; ++i) {						//Test de prueba, para que el rio empiece on masas de volumnes diversos
			MassaAgua m = massaAguaPredeterminada();
			m.addVolumen((float)i);
			rio.add(m);		
		}
		MyTicker ticker = new MyTicker(this, 3000, rio);
		addBehaviour(ticker);
		for (int i=0; i<l; ++i) {
			System.out.print(rio.get(i).getVolumen().get(0) + " ");  //Test del rio inicial
		}
		System.out.println();
	}
	
	protected void takeDown() {
		for (int i=0; i<l; ++i) {
			System.out.print(rio.get(i).getVolumen().get(0) + " ");
		}
		System.out.println();
	}
}
