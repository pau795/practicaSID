package practica;

import java.io.IOException;
import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;


@SuppressWarnings("serial")
public class IndustryAgent extends Agent {
	
	private class OneShotFindRiver extends OneShotBehaviour {
		 
		@Override
		public void action() {
			
			// The behaviour register the type of the service that will search, in this case a River
			DFAgentDescription dfd = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("River");
			dfd.addServices(sd);
			
			// Set the number of results desired to 1
			SearchConstraints sc = new SearchConstraints();
			sc.setMaxResults(new Long(1));
	        DFAgentDescription[] results = null;
	        
	        // Search the river
	        try {
	            results = DFService.search(myAgent, dfd, sc );
	        } catch (FIPAException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	        if (results.length > 0) {
	            DFAgentDescription dfd2 = results[0];
	            riverAID = dfd2.getName();
		        System.out.println("The AID of the river has been found and it is: " + riverAID);
	        }
	    }
	}
	private class ReceiveMessages extends CyclicBehaviour {
	

		@Override
		public void action() {
			
			ACLMessage msg = blockingReceive();
			
			if(msg.getPerformative() == ACLMessage.INFORM_REF && msg.getContent() == "section")
				riverSection = Integer.valueOf(msg.getUserDefinedParameter("section"));
			
			if(msg.getPerformative() == ACLMessage.INFORM_REF && msg.getContent() == "volume")
				volumeOfSection = Double.valueOf(msg.getUserDefinedParameter("volume"));
		}
		
	}
	private class ExtractWater extends TickerBehaviour {
		
		public ExtractWater(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			// Ask for the volume of water available
			ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
			msg.setSender(getAID());
			msg.addReceiver(riverAID);
			msg.setContent("volume");
			msg.addUserDefinedParameter("section", String.valueOf(riverSection));
			send(msg);
			
			// If is possible to extract, do it, and inform
			while(volumeOfSection < 0) {
				ReceiveMessages rM = new ReceiveMessages();
				addBehaviour(rM);
			}
			
			if(volumeOfSection <= waterExtracted) {
				
				WaterMass pollutedWater = new WaterMass(waterExtracted, suspendedSolids, chemicalOxygenDemand, biologicalOxygenDemand, totalNitrates, totalSulfites);
				ACLMessage msgInf = new ACLMessage(ACLMessage.INFORM);
				msgInf.setSender(getAID());
				msgInf.addReceiver(riverAID);
				try {
					// Sends a WaterMass serialized
					msg.setContentObject(pollutedWater);
				} catch (IOException e) {
					e.printStackTrace();
				}
				send(msgInf);
			}
			
			volumeOfSection = -1;
			
		}
		
	}
	
	static private AID riverAID;
	static private int riverSection;
	
	static private double waterExtracted;
	static private double suspendedSolids;
	static private double chemicalOxygenDemand;
	static private double biologicalOxygenDemand;
	static private double totalSulfites;
	static private double totalNitrates;
	
	private Random r;
	private double volumeOfSection;
	
	protected void setup() {
		
		OneShotFindRiver fR = new OneShotFindRiver();
		addBehaviour(fR);
		
		ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
		msg.setSender(getAID());
		msg.addReceiver(riverAID);
		msg.setContent("sections");
		send(msg);
		
		// Register the number of section in which the industry will extract water
		ReceiveMessages rM = new ReceiveMessages();
		addBehaviour(rM);
		
		// The volume of water that this industry extracts from the river and how much it 
		// pollutes the water remains constant during all the execution

		r = new Random();
		waterExtracted = 1000*r.nextDouble();
		
		suspendedSolids = r.nextDouble()*200;
		chemicalOxygenDemand = r.nextDouble()*200;
		biologicalOxygenDemand = r.nextDouble()*200;
		totalNitrates = r.nextDouble()*200;
		totalSulfites = r.nextDouble()*200;
		
		
		// Register the behavior of the Agent -> Extract water
		ExtractWater eW = new ExtractWater(this, 5000);
		addBehaviour(eW);
		
		System.out.println("Industry extracts water from section: " + riverSection);
		System.out.println("Industry extracts " + waterExtracted + " liters of water." );
		
		System.out.println();
	}
	
}
