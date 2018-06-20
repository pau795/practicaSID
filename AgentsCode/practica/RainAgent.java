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
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")
public class RainAgent extends Agent{

	private class RainTicker extends TickerBehaviour {
		 
		public RainTicker(Agent a, long period) {
			super(a, period);
		}
		
		protected void onTick() {
			if (rain) {
				raining();
			}
			else if (r.nextDouble() < 0.5) { 		//random chance to start raining
				rainTick=0;
				rain=true;
				System.out.println("It has started to rain");
				raining();
			}
			if (meteorologicWaterTank.getWaterRate() > tankThreshold) {
				// Ask for the available volume of the EDAR
				ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
				msg.setConversationId("volumeEDAR");
				msg.setSender(getAID());
				msg.addReceiver(EDARAID);
				send(msg);			
				System.out.println("Meteorologic tank asking for the avaiable capacity of water to the EDAR");
				
				MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.INFORM_REF);
				ACLMessage msg2 = myAgent.receive(template);
				if(msg2 != null) {
					if(msg2.getPerformative() == ACLMessage.INFORM_REF && msg2.getConversationId() != null && msg2.getConversationId().equals("volumeEDAR")) {
						double EDARWater = Double.min(Double.valueOf(msg2.getContent()), meteorologicWaterTank.getVolume()*0.5);					
						WaterMass waterToDump = meteorologicWaterTank.getPortion(EDARWater);
						// The tank tries to dump water to the EDAR
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
						System.out.println("Meteorologic water tank requesting to dump " + waterToDump.getVolume() + " liters of water to the EDAR");
	
						// The tank waits for the confirmation that the water to dump has been dumped
						MessageTemplate template2 = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
						ACLMessage msg4 = myAgent.receive(template2);
						if(msg4 != null) {
							if(msg4.getPerformative() == ACLMessage.CONFIRM && msg4.getConversationId() != null && msg4.getConversationId().equals("dump")) {
								System.out.println("Industry " + getLocalName() + " dumps " + waterToDump.getVolume() + " liters of water to the EDAR");
								meteorologicWaterTank.substractWaterMass(waterToDump);
								System.out.println("The meteorologic water tank stores " + meteorologicWaterTank.getVolume() + " liters of water after the dump");
							}
						} else block(2000);
					}
				} else block(2000);
			}
		}
		
	}
	
	private void raining(){
		double waterVolume = minRainVolume + (r.nextDouble()*(maxRainVolume-minRainVolume));
		
		if (meteorologicWaterTank.getAvailableVolume()>= waterVolume) {
			WaterMass.mergeWater(new WaterMass(waterVolume,0,0,0,0,0), meteorologicWaterTank);
			System.out.println("The meteorologic water tank contains " +waterVolume+ " liters of water");
		}
		else System.out.println("The meteorologic water tank is full and cannot recive more water");
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(riverAID);
		msg.setConversationId("rain");
		msg.addUserDefinedParameter("rain", String.valueOf(waterVolume));
		send(msg);
				
		System.out.println("Its raining " + waterVolume +" liters of water per section");
		if (rainTick >= minRainTicks && rainTick < maxRainTicks) {
			int n = r.nextInt(maxRainTicks-rainTick);
			if (n==0) {
				rain=false;
				System.out.println("It has stopped to rain");
			}
		}
		else if (rainTick >= maxRainTicks) {
			rain=false;
			System.out.println("It has stopped to rain");
		}
		++rainTick;
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
	            System.out.println("RainAgent " + getLocalName() + " is searching the river");
	        } catch (FIPAException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	        if (results.length > 0) {
	            DFAgentDescription dfd2 = results[0];
	            riverAID = dfd2.getName();
	            System.out.println("RainAgent " + getLocalName() + " has found the AID of the river and it is: " + riverAID.getLocalName());
	        }
	        else {
	        	System.out.println("RainAgent " + getLocalName() + " hasn't found the river.");
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
        boolean EDARHasIndustryName = false;
        while(EDARAID == null && !EDARHasIndustryName) {
        
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
	            System.out.println("RainAgent has found the AID of the EDAR and it is: " + EDARAID.getLocalName());
	        }
	        else {
	        	System.out.println("Rain Agent has not found the EDAR.");
	        }
        }        
	}
	
	AID EDARAID;
	AID riverAID;
	int minRainTicks = 5, maxRainTicks = 10;
	double minRainVolume = 10, maxRainVolume = 100;
	boolean rain = false;
	int rainTick;
	
	WaterMass meteorologicWaterTank;
	double tankMaxCapacity = 10000;
	double tankThreshold = 0.7;
	
	Random r=new Random();
	
	private void initializeTank() {
		meteorologicWaterTank = new WaterMass(0,0,0,0,0,0);
		meteorologicWaterTank.setCapacity(tankMaxCapacity);
	}
	
	protected void setup() {
		searchRiver();
		searchEDAR();
		initializeTank();
		RainTicker t = new RainTicker(this, 5000);
		addBehaviour(t);
	}
}
