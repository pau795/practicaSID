package practica;

import java.text.DecimalFormat;
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
public class RiverAgent extends Agent{
	
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
				System.out.print(format.format(river.get(i).getVolume()) + " ");		//Print del rio a cada tick
			
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
					
					// If river receives a QUERY_REF
					if (msg.getPerformative() == ACLMessage.QUERY_REF){
						String content = msg.getContent();
						
						// An agent ask for the number of sections
						if (content != null && content.equals("section")) {
							reply.setPerformative(ACLMessage.INFORM_REF);
							reply.setContent("section");
							reply.addUserDefinedParameter("section",String.valueOf(sections));
							reply.setInReplyTo("section");
							System.out.println("Number of sections sended from the river to " + msg.getSender().getLocalName());
						}
						
						// An agent asks for the capacity of a section of the river
						else if (content != null && content.equals("capacity")) {
							reply.setPerformative(ACLMessage.INFORM_REF);
							reply.setContent("capacity");
							reply.addUserDefinedParameter("capacity",String.valueOf(river.get(0).getCapacity()));
							reply.setInReplyTo("capacity");
							System.out.println("Capacity sent from the river to " + msg.getSender().getLocalName());
						}
						
						// An agent asks for the current volume of the section received as parameter
						else if (content != null && content.equals("volume")) {
							
							int s = Integer.valueOf(msg.getUserDefinedParameter("section"));
							double v = Double.valueOf(msg.getUserDefinedParameter("volume"));
							WaterMass w = river.get(s);
							
							// The river has been requested for a available volume of water
							if (w.getVolume() >= v) {
								WaterMass wr = new WaterMass(v, w.getSuspendedSolids(), w.getChemicalOxygenDemand(), w.getBiologicalOxygenDemand(), w.getTotalNitrates(), w.getTotalSulfites());
								w.setVolume(w.getVolume()-v);
								reply.setPerformative(ACLMessage.INFORM_REF);
								reply.setContentObject(wr);
								reply.setInReplyTo("volume");
								System.out.println("The river section has the volume of water that " + msg.getSender().getLocalName() + " has requested.");
							}
							
							
							// The river has been requested for a non available volume of water
							else {
								reply.setPerformative(ACLMessage.REFUSE);
								reply.setContent("The river section does not contain the volume of water that " + msg.getSender().getLocalName() + " has requested.");
								reply.setInReplyTo("volume");
								System.out.println("The river section has NOT the volume of water that " + msg.getSender().getLocalName() + " has requested.");
							}
						}
					}
					
					else if (msg.getPerformative()== ACLMessage.QUERY_IF){
						String content = msg.getContent();
						if (content != null  && msg.getConversationId().equals("purified")) {
							int s = Integer.valueOf(msg.getUserDefinedParameter("section"));
							WaterMass m = (WaterMass) msg.getContentObject();
							if (river.get(s).getVolume() + m.getVolume() <= river.get(s).getCapacity()) {
								river.set(s,WaterMass.mergeWater(m, river.get(s)));
								reply.setPerformative(ACLMessage.CONFIRM);
								reply.setContent("Water mass recived succesfully");
								reply.setConversationId("purified");
							}
							else {
								reply.setPerformative(ACLMessage.REFUSE);
								reply.setContent("Is not possible to pour that volume of water");
								reply.setConversationId("purified");
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
	DecimalFormat format = new DecimalFormat("0.00");
		
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
			System.out.print(format.format(river.get(i).getVolume()) + " ");  //Test del rio inicial
		
		System.out.println("");
	}
	
	protected void takeDown() {
		for (int i = 0; i < sections; ++i) 
			System.out.print(format.format(river.get(i).getVolume()) + " ");
		
		System.out.println();
	}
}
