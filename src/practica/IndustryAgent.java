package practica;

import java.io.IOException;
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
			
			if(waterExtracted == null) extractWater();
			if(waterExtracted != null) managePollutedWater();
			
		}

		private void extractWater() {
			ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
			msg.setSender(getAID());
			msg.addReceiver(riverAID);
			msg.setContent("volume");
			msg.addUserDefinedParameter("section", String.valueOf(riverSection));
			msg.addUserDefinedParameter("volume", String.valueOf(waterVolume));
			send(msg);
			System.out.println("Industry " + getLocalName() + " is trying to extract "+ waterVolume +" liters of water");

			// If is possible to extract, do it, and inform
			waterExtracted = null;
			ACLMessage msg2 = blockingReceive(3000);
			
			// Water mass sent from the river to the industry in order to be extracted
			if(msg2 != null && msg2.getPerformative() == ACLMessage.INFORM_REF && msg2.getInReplyTo().equals("volume")) {
				try {
					waterExtracted = (WaterMass) msg2.getContentObject();
					polluteWater();
					System.out.println("Industry " + getLocalName() + " has received and polluted the water.");
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
			}
		}

		private void managePollutedWater() {
			
			ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
			msg.setContent("dump");
			msg.setSender(getAID());
			msg.addReceiver(EDARAID);
			try {
				msg.setConversationId("dump");
				msg.setContentObject(waterExtracted);
			} catch (IOException e) {
				e.printStackTrace();
			}
			send(msg);			
			System.out.println("Industry " + getLocalName() + " requesting to dump " + waterExtracted.getVolume()+" liters of water to the EDAR");

			ACLMessage msg2 = blockingReceive(3000);
			if(msg2 != null && msg2.getPerformative() == ACLMessage.CONFIRM && msg2.getConversationId().equals("dump")) {
				System.out.println("Industry " + getLocalName() + " dumps " + waterExtracted.getVolume()+" liters of water to the EDAR");
				waterExtracted = null;
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
	
	private AID riverAID;
	private AID EDARAID;
	private int riverSection;
	
	static private double waterVolume;
	static private double suspendedSolids;
	static private double chemicalOxygenDemand;
	static private double biologicalOxygenDemand;
	static private double totalSulfites;
	static private double totalNitrates;
	
	private Random r = new Random();
	private WaterMass waterExtracted;
	
	
	protected void setup() {
		
		setIndustryParameters();
		searchRiver();
		searchEDAR();
		registerSection();
		
		// Register the behavior of the Agent -> Extract water
		ExtractWater eW = new ExtractWater(this, 5000);
		addBehaviour(eW);
	}



	private void setIndustryParameters() {
		// The volume of water that this industry extracts from the river and how much it 
		// pollutes the water remains constant during all the execution

		waterVolume = 1000*r.nextDouble();
		suspendedSolids = r.nextDouble()*200;
		chemicalOxygenDemand = r.nextDouble()*200;
		biologicalOxygenDemand = r.nextDouble()*200;
		totalNitrates = r.nextDouble()*200;
		totalSulfites = r.nextDouble()*200;
		waterExtracted = null;
	}

	private void registerSection() {
		// Register the number of section in which the industry will extract water
		riverSection = -1;
		while(riverSection < 0) {
			
			ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
			msg.setSender(getAID());
			msg.addReceiver(riverAID);
			msg.setContent("section");
			send(msg);
			System.out.println("Industry " + getLocalName() + " has requested the number of sections of the river");
			
			ACLMessage msg2 = blockingReceive(3000);
			if(msg2 != null && msg2.getPerformative() == ACLMessage.INFORM_REF && msg2.getContent().equals("section"))
				riverSection = r.nextInt(Integer.valueOf(msg2.getUserDefinedParameter("section")));
			System.out.println("Industry " + getLocalName() + " extracts water from section " + riverSection + " of the river");
			System.out.println("Industry " + getLocalName() + "  extracts " + waterVolume + " liters of water." );
		}
	}

	private void searchRiver() {
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
	            System.out.println("Industry " + getLocalName() + " is searching the river");
	        } catch (FIPAException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	        if (results.length > 0) {
	            DFAgentDescription dfd2 = results[0];
	            riverAID = dfd2.getName();
	            System.out.println("Industry " + getLocalName() + " has found the AID of the river and it is: " + riverAID.getLocalName());
	        }
	        else {
	        	System.out.println("Industry " + getLocalName() + " hasn't found the river.");
	        }
        }
		
	}
	
	private void searchEDAR() {
		
		// The behaviour register the type of the service that will search, in this case an EDAR
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("EDAR");
		dfd.addServices(sd);
		
		// Set the number of results desired to 1
		SearchConstraints sc = new SearchConstraints();
		sc.setMaxResults(new Long(1));
        DFAgentDescription[] results = null;
        
        EDARAID = null;
        while(EDARAID == null) {
        
	        // Search the river
	        try {
	            results = DFService.search(this, dfd, sc );
	            System.out.println("Industry " + getLocalName() + " is searching the EDAR");
	        } catch (FIPAException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	        if (results.length > 0) {
	            DFAgentDescription dfd2 = results[0];
	            EDARAID = dfd2.getName();
	            System.out.println("Industry " + getLocalName() + " has found the AID of the EDAR and it is: " + EDARAID.getLocalName());
	        }
	        else {
	        	System.out.println("Industry " + getLocalName() + " hasn't found the EDAR.");
	        }
        }
	}
}
