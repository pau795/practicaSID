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
			managePollutedWater();
			
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
			
			// Try to store the polluted water into the tank 
			if(waterExtracted != null && tankOfWater.getAvailableVolume() >= waterExtracted.getVolume()) {
				tankOfWater.addWaterMass(waterExtracted);
				System.out.println("Industry " + getLocalName() + " has stored " + waterExtracted.getVolume() + " liters of water into the tank");
				System.out.println("The tank of the industry " + getLocalName() + " stores " + tankOfWater.getVolume() + " liters of water");
				waterExtracted = null;
			}
				
			// If the tank is more than 70% full try to dump water to the EDAR
			if(tankOfWater.getWaterRate() > tankThreshold) {
				
				// Ask for the available volume of the EDAR
				ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
				msg.setConversationId("volumeEDAR");
				msg.setSender(getAID());
				msg.addReceiver(EDARAID);
				send(msg);			
				System.out.println("Industry " + getLocalName() + " asking for the avaiable capacity of water to the EDAR");
				
				// Receive the available water of the EDAR and send a water mass
				ACLMessage msg2 = blockingReceive(1000);
				if(msg2 != null && msg2.getPerformative() == ACLMessage.INFORM_REF && msg2.getConversationId().equals("volumeEDAR")) {
					double EDARWater = Double.min(Double.valueOf(msg2.getContent()), tankOfWater.getVolume()*0.5);					
					waterToDump = tankOfWater.getPortion(EDARWater);
					
					// The industry try to dump polluted water to the EDAR
					ACLMessage msg3 = new ACLMessage(ACLMessage.QUERY_IF);
					msg3.setContent("dump");
					msg3.setSender(getAID());
					msg3.addReceiver(EDARAID);
					msg3.setConversationId("dump");
					try {
						msg3.setContentObject(waterToDump);
					} catch (IOException e) {
						e.printStackTrace();
					}
					send(msg3);			
					System.out.println("Industry " + getLocalName() + " requesting to dump " + waterToDump.getVolume() + " liters of water to the EDAR");

					// The industry waits for the confirmation that the water to dump has been dumped
					ACLMessage msg4 = blockingReceive(1000);
					if(msg4 != null && msg4.getPerformative() == ACLMessage.CONFIRM && msg4.getConversationId().equals("dump")) {
						System.out.println("Industry " + getLocalName() + " dumps " + waterToDump.getVolume() + " liters of water to the EDAR");
						tankOfWater.substractWaterMass(waterToDump);
						System.out.println("The tank of the industry " + getLocalName() + " stores " + tankOfWater.getVolume() + " liters of water after the dump");
						waterToDump = null;
					}
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
	
	private AID riverAID;
	private AID EDARAID;
	private int riverSection;
	
    private double waterVolume;
	private double suspendedSolids;
	private double chemicalOxygenDemand;
	private double biologicalOxygenDemand;
	private double totalSulfites;
	private double totalNitrates;
	private double tankThreshold;
	
	private Random r = new Random(); 
	
	private WaterMass waterExtracted;
	private WaterMass waterToDump;
	private WaterMass tankOfWater;
	
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

		waterVolume = 700*r.nextDouble();
		suspendedSolids = r.nextDouble()*200;
		chemicalOxygenDemand = r.nextDouble()*200;
		biologicalOxygenDemand = r.nextDouble()*200;
		totalNitrates = r.nextDouble()*200;
		totalSulfites = r.nextDouble()*200;
		waterExtracted = null;
		tankOfWater = new WaterMass(0,0,0,0,0,0);
		tankOfWater.setCapacity(5000);
		tankThreshold = 0.7;
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
