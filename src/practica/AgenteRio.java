package practica;

import java.util.ArrayList;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import paqueteSid.MassaAgua;
import paqueteSid.impl.DefaultMassaAgua;

public class AgenteRio extends Agent{
	
	private class MyTicker extends TickerBehaviour{

		public MyTicker(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
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
			MassaAgua m= new DefaultMassaAgua();
			m.addVolumen(pvolumen);
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
		for (int i=0; i<l; ++i) {
			MassaAgua m = massaAguaPredeterminada();
			rio.add(m);		
		}
		
		
	}
}
