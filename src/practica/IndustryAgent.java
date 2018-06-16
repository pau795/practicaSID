package practica;

import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;


@SuppressWarnings("serial")
public class IndustryAgent extends Agent {
	
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
			msg.addUserDefinedParameter("volume", String.valueOf(waterVolume));
			send(msg);
			System.out.println("Message Sended");
			// If is possible to extract, do it, and inform
			waterExtracted = null;
			ACLMessage msg2 = blockingReceive();
			if(msg2 != null && msg2.getPerformative() == ACLMessage.INFORM_REF && msg2.getInReplyTo().equals("volume")) {
				try {
					System.out.println("Intenta extraer Agua");
					waterExtracted = (WaterMass) msg2.getContentObject();
					polluteWater();
					System.out.println("Water Received and polluted");
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
			}
		}

		private void polluteWater() {
			
			double sS = waterExtracted.getSuspendedSolids();
			waterExtracted.setSuspendedSolids(sS + suspendedSolids);
			
			double cOD = waterExtracted.getChemicalOxygenDemand();
			waterExtracted.setChemicalOxygenDemand(cOD + chemicalOxygenDemand);
			
			double bOD = waterExtracted.getBiologicalOxygenDemand();
			waterExtracted.setBiologicalOxygenDemand(bOD + biologicalOxygenDemand);
			
			double tS = waterExtracted.getTotalSulfites();
			waterExtracted.setTotalSulfites(tS + totalSulfites);
			
			double tN = waterExtracted.getTotalNitrates();
			waterExtracted.setTotalNitrates(tN + totalNitrates);
		}
		
	}
	
	static private AID riverAID;
	static private int riverSection;
	
	static private double waterVolume;
	static private double suspendedSolids;
	static private double chemicalOxygenDemand;
	static private double biologicalOxygenDemand;
	static private double totalSulfites;
	static private double totalNitrates;
	
	private Random r = new Random();
	private WaterMass waterExtracted;
	
	protected void setup() {
		
		// The behaviour register the type of the service that will search, in this case a River
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("River");
		dfd.addServices(sd);
		
		// Set the number of results desired to 1
		SearchConstraints sc = new SearchConstraints();
		sc.setMaxResults(new Long(1));
        DFAgentDescription[] results = null;
        
        riverAID = null;
        while(riverAID == null) {
        
	        // Search the river
	        try {
	            results = DFService.search(this, dfd, sc );
	            System.out.println("Searching The river");
	        } catch (FIPAException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	        if (results.length > 0) {
	            DFAgentDescription dfd2 = results[0];
	            riverAID = dfd2.getName();
		        System.out.println("The AID of the river has been found and it is: " + riverAID);
	        }
	        else {
	        	System.out.println("River not found");
	        }
        }
		
		// Register the number of section in which the industry will extract water
		riverSection = -1;
		while(riverSection < 0) {
			
			ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
			msg.setSender(getAID());
			msg.addReceiver(riverAID);
			msg.setContent("section");
			send(msg);
			System.out.println("Request de section");
			
			ACLMessage msg2 = blockingReceive(3000);
			if(msg2 != null && msg2.getPerformative() == ACLMessage.INFORM_REF && msg2.getContent().equals("section"))
				riverSection = r.nextInt(Integer.valueOf(msg2.getUserDefinedParameter("section")));
				System.out.println("seccio assignada " + riverSection);
		}
		
		// The volume of water that this industry extracts from the river and how much it 
		// pollutes the water remains constant during all the execution

		waterVolume = 1000*r.nextDouble();
		suspendedSolids = r.nextDouble()*200;
		chemicalOxygenDemand = r.nextDouble()*200;
		biologicalOxygenDemand = r.nextDouble()*200;
		totalNitrates = r.nextDouble()*200;
		totalSulfites = r.nextDouble()*200;
		
		
		// Register the behavior of the Agent -> Extract water
		ExtractWater eW = new ExtractWater(this, 5000);
		addBehaviour(eW);
		
		System.out.println("Industry extracts water from section: " + riverSection);
		System.out.println("Industry extracts " + waterVolume + " liters of water." );
		
		System.out.println();
	}
	
}
