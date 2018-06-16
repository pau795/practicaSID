package practica;

import java.util.ArrayList;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

@SuppressWarnings("serial")
public class AgenteRio extends Agent{
	
	private class MyTicker extends TickerBehaviour{
		
		public MyTicker(Agent a, long period) {
			
			super(a, period);
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
			
			System.out.println("");
		}
	}	
	
	
	private class MsgRiver extends CyclicBehaviour {
		public MsgRiver(Agent a) {
			super(a);
		}
		
		@Override
		public void action() {
			try {
				ACLMessage  msg = myAgent.receive();
				if (msg != null) {
					ACLMessage reply = msg.createReply();
					if (msg.getPerformative()== ACLMessage.QUERY_REF){
						String content = msg.getContent();
						if (content != null && content.equals("section")) {
							reply.addReceiver(msg.getSender());
							reply.setPerformative(ACLMessage.INFORM_REF);
							reply.setContent("section");
							reply.addUserDefinedParameter("section",String.valueOf(sections));
							reply.setInReplyTo("section");
							System.out.println("Secciones Enviadas");
						}
						else if (content != null && content.equals("volume")) {
							
							int s =Integer.valueOf(msg.getUserDefinedParameter("section"));
							double v = Double.valueOf(msg.getUserDefinedParameter("volume"));
							WaterMass w = river.get(s);
							
							if (w.getVolume() >= v) {
								WaterMass wr = new WaterMass(v, w.getSuspendedSolids(), w.getChemicalOxygenDemand(), w.getBiologicalOxygenDemand(), w.getTotalNitrates(), w.getTotalSulfites());
								w.setVolume(w.getVolume()-v);
								reply.addReceiver(msg.getSender());
								reply.setPerformative(ACLMessage.INFORM_REF);
								reply.setContentObject(wr);
								reply.setInReplyTo("volume");
							}
							else {
								reply.addReceiver(msg.getSender());
								reply.setPerformative(ACLMessage.REFUSE);
								reply.setContent("The river section does not contain that volume of water");
								reply.setInReplyTo("volume");
							}
						}
					}
					else {
						reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
						reply.setContent("( (Unexpected-act "+ACLMessage.getPerformative(msg.getPerformative())+") )");
						reply.setInReplyTo("Unknown");
					}
					send(reply);
				}
				else block();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	
	private ArrayList<WaterMass> river;
	private int sections; //longitud rio;
		
	protected void setup() {
		
	  	// Register the service
	  	System.out.println("Agent " + getLocalName() + " registering River Sevice");
	  	try {
	  		DFAgentDescription dfd = new DFAgentDescription();
	  		dfd.setName(getAID());
	  		
	  		ServiceDescription sd = new ServiceDescription();
	  		sd.setName("River");
	  		sd.setType("River");
	  		dfd.addServices(sd);
	  		DFService.register(this, dfd);
	  		System.out.println("River Registered");
	  	}
	  	catch (FIPAException fe) {
	  		fe.printStackTrace();
	  	}
		
		river = new ArrayList<WaterMass>();
		
		Object[] args = getArguments();
		if (args.length == 1 ) sections = Integer.valueOf((String) args[0]); //la longitud del rio viene determinada por el primer parametro
		else sections = 20; //longitud predeterminada si no hay parametros	
		
		for (int i = 0; i < sections; ++i)					
			river.add(new WaterMass());		
		
		MyTicker ticker = new MyTicker(this, 3000);
		addBehaviour(ticker);
		
		MsgRiver mr = new MsgRiver(this);
		addBehaviour(mr);
		
		
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
