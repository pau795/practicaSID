package practica;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Queue;
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
import jade.lang.acl.UnreadableException;

@SuppressWarnings("serial")
public class EdarAgent extends Agent{
		
	@SuppressWarnings("serial")
	private class WaterPurifier extends TickerBehaviour {
		
		public WaterPurifier(Agent a, long period) {
			super(a,period);
		}
		
		public void onTick() {
			
			//Purify water
			WaterMass waterToPurify = getWater();
			waterToPurify.setSuspendedSolids(waterToPurify.getSuspendedSolids()*sspr);
			waterToPurify.setChemicalOxygenDemand(waterToPurify.getChemicalOxygenDemand()*codpr);
			waterToPurify.setBiologicalOxygenDemand(waterToPurify.getBiologicalOxygenDemand()*bodpr);
			waterToPurify.setTotalNitrates(waterToPurify.getTotalNitrates()*tnpr);
			waterToPurify.setTotalSulfites(waterToPurify.getTotalSulfites()*tspr);
			if (isPurified(waterToPurify)) {
				purifiedWater = WaterMass.mergeWater(waterToPurify, purifiedWater);
				waterToPurify = new WaterMass(0,0,0,0,0,0);
			}
			
			//Pour the water into the river
			WaterMass m = new WaterMass();
			if (purifiedWater.getVolume() <= sectionCapacity*pourRatio) m = purifiedWater;
			else {
				m.setVolume(sectionCapacity*pourRatio);	
				m.setSuspendedSolids(purifiedWater.getSuspendedSolids());
				m.setChemicalOxygenDemand(purifiedWater.getChemicalOxygenDemand()*pourRatio);
				m.setBiologicalOxygenDemand(purifiedWater.getBiologicalOxygenDemand()*pourRatio);
				m.setTotalNitrates(purifiedWater.getTotalNitrates()*pourRatio);
				m.setTotalSulfites(purifiedWater.getTotalSulfites()*pourRatio);
			}
			if (m.getVolume() > 0) {
				ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
				msg.addReceiver(riverAID);
				msg.setSender(getAID());
				msg.setConversationId("purified");
				msg.addUserDefinedParameter("section", String.valueOf(riverSection));
				try {
					msg.setContentObject(m);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				send(msg);
				
				boolean answer = false;
				while (!answer) {
					ACLMessage msg2 = blockingReceive(3000);
					answer=true;
					if(msg2.getPerformative() == ACLMessage.CONFIRM && msg2.getConversationId() == "purified") {
						purifiedWater.setVolume(purifiedWater.getVolume()-m.getVolume());
						purifiedWater.setSuspendedSolids(purifiedWater.getSuspendedSolids()-m.getSuspendedSolids());
						purifiedWater.setChemicalOxygenDemand(purifiedWater.getChemicalOxygenDemand()-m.getChemicalOxygenDemand());
						purifiedWater.setBiologicalOxygenDemand(purifiedWater.getBiologicalOxygenDemand()-m.getBiologicalOxygenDemand());
						purifiedWater.setTotalNitrates(purifiedWater.getTotalNitrates()-m.getTotalNitrates());
						purifiedWater.setTotalSulfites(purifiedWater.getTotalSulfites()-m.getTotalSulfites());
						System.out.println(m.getVolume()+" liters of were poured to the river successfully");
					}
					else System.out.println("Is not possible to pour water to the river");
				}
			}
		}
		
	}
	
	//This method returns true when all the pollutants of m are below their threshold
	private boolean isPurified(WaterMass m) {
		if (m.getSuspendedSolids() <= sspt &&
			m.getChemicalOxygenDemand() <= codpt &&
			m.getBiologicalOxygenDemand() <= bodpt &&
			m.getTotalNitrates() <= tnpt &&
			m.getTotalSulfites() <= tspt) return true;
		else return false;
			
	}
	
	@SuppressWarnings("serial")
	private class MessageReciver extends CyclicBehaviour {

		public MessageReciver(Agent a) {
			super(a);
		}
		
		@Override
		public void action() {
			ACLMessage  msg = myAgent.receive();
			if (msg!=null) {
				ACLMessage reply =msg.createReply();
				String content =msg.getContent();
				if (msg.getPerformative()==ACLMessage.QUERY_IF) {
					if (content!=null && msg.getConversationId().equals("dump")) {
						try {
							WaterMass m = (WaterMass) msg.getContentObject();
							if (pollutedWater.getVolume() + m.getVolume() <= pollutedWater.getCapacity()) {
								pollutedWater=WaterMass.mergeWater(m, pollutedWater);
								reply.addReceiver(msg.getSender());
								reply.setPerformative(ACLMessage.CONFIRM);
								reply.setContent("Water mass recived succesfully");
								reply.setConversationId("dump");
								send(reply);
							}
							else {
								reply.addReceiver(msg.getSender());
								reply.setPerformative(ACLMessage.REFUSE);
								reply.setContent("The polluted water tank cannot recive that volume of water");
								reply.setConversationId("dump");
								send(reply);
							}		
						} catch (UnreadableException e) {
							e.printStackTrace();
						}
					}
				}
			}
			
		}
		
	}
	
	
	//This method decides which WaterMass is going to be purified.
	//If there is a WaterMass on the procces of being purified, it chooses that one.
	//Otherwise, it gets a portion of the polluted water tank, trying to maximize the maximum purifiable volume
	private WaterMass getWater() {
		WaterMass m;
		if (waterToPurify.getVolume()>0) m=waterToPurify;
		else if (pollutedWater.getVolume()>waterToPurify.getCapacity()) {
			double ratio = waterToPurify.getCapacity()/pollutedWater.getVolume();
			double v = waterToPurify.getCapacity();
			double ss = pollutedWater.getSuspendedSolids()*ratio;
			double cod = pollutedWater.getChemicalOxygenDemand()*ratio;
			double bod = pollutedWater.getBiologicalOxygenDemand()*ratio;
			double tn = pollutedWater.getTotalNitrates()*ratio;
			double ts = pollutedWater.getTotalSulfites()*ratio;
			m=new WaterMass(v, ss, cod, bod, tn, ts);
			m.setCapacity(v);
			pollutedWater.setVolume(pollutedWater.getVolume()-v);
			pollutedWater.setSuspendedSolids(pollutedWater.getSuspendedSolids()-ss);
			pollutedWater.setChemicalOxygenDemand(pollutedWater.getChemicalOxygenDemand()-cod);
			pollutedWater.setBiologicalOxygenDemand(pollutedWater.getBiologicalOxygenDemand()-bod);
			pollutedWater.setTotalNitrates(pollutedWater.getTotalNitrates()-tn);
			pollutedWater.setTotalSulfites(pollutedWater.getTotalSulfites()-ts);
		}
		else {
			m = pollutedWater;
			m.setCapacity(MaxPurifiableCapacity);
			pollutedWater= new WaterMass(0,0,0,0,0,0);
			pollutedWater.setCapacity(MaxTankCapacity);				
		}
		return m;
	}
	
	
	private void searchRiver() {
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
			System.out.println("Request de section");
			
			ACLMessage msg2 = blockingReceive(3000);
			if(msg2 != null && msg2.getPerformative() == ACLMessage.INFORM_REF && msg2.getContent().equals("section"))
				riverSection = r.nextInt(Integer.valueOf(msg2.getUserDefinedParameter("section")));
				System.out.println("seccion assignada " + riverSection);
		}
	}
	
	private void getSectionCapacity() {
		ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
		msg.setSender(getAID());
		msg.addReceiver(riverAID);
		msg.setContent("capacity");
		msg.setInReplyTo("capacity");
		send(msg);
		
		sectionCapacity = -1;
		while(sectionCapacity == -1) {
			ACLMessage msg2 = blockingReceive(3000);
			if(msg2.getPerformative() == ACLMessage.INFORM_REF && msg2.getContent().equals("capacity"))
				sectionCapacity = Double.valueOf(msg2.getUserDefinedParameter("capacity"));
		}
	}
	
	private void registerAgent() throws FIPAException {
		System.out.println("Agent " + getLocalName() + " registering River Sevice");
	  	DFAgentDescription dfd = new DFAgentDescription();
	  	dfd.setName(getAID());
  		ServiceDescription sd = new ServiceDescription();
  		sd.setName("Edar");
  		sd.setType("Edar");
  		dfd.addServices(sd);
  		DFService.register(this, dfd);
  		System.out.println("Edar Registered");
	}
	
	private Random r = new Random();
	
	static private AID riverAID;
	static private int riverSection;
	static private double sectionCapacity;
	
	private WaterMass pollutedWater;
	private WaterMass waterToPurify;
	private WaterMass purifiedWater;
	
	private double MaxTankCapacity = 20000;
	private double MaxPurifiableCapacity = 1000;
	
	//purifiable ratios
	private double sspr = 0.8;		//suspended solids purifiable ratio
	private double codpr = 0.7;		//chemical oxygen demand purifiable ratio	
	private double bodpr = 0.8;		//biological oxygen demand purifiable ratio
	private double tnpr = 0.9;		//total nitrates purifiable ratio
	private double tspr = 0.9;		//total sulfites purifiable ratio
	
	//purifiable thresholds
	private double sspt = 5;		//suspended solids purifiable threshold
	private double codpt = 2;		//chemical oxygen demand purifiable threshold	
	private double bodpt = 3;		//biological oxygen demand purifiable threshold
	private double tnpt = 0.8;		//total nitrates purifiable threshold
	private double tspt = 0.5;		//total sulfites purifiable threshold
	
	private double pourRatio = 0.3;	//pour ratio

	
	private void initialiteTanks() {
		pollutedWater= new WaterMass(0,0,0,0,0,0);
		waterToPurify= new WaterMass(0,0,0,0,0,0);
		purifiedWater= new WaterMass(0,0,0,0,0,0);
		pollutedWater.setCapacity(MaxTankCapacity);
		waterToPurify.setCapacity(MaxPurifiableCapacity);
		purifiedWater.setCapacity(MaxTankCapacity);
	}
	
	public void setup() {
		
		searchRiver();
		initialiteTanks();		
		registerSection();
		getSectionCapacity();
		try {
			registerAgent();
		}
		catch (FIPAException e){
			e.printStackTrace();
		}		
		WaterPurifier w = new WaterPurifier(this, 5000);
		addBehaviour(w);
		
		MessageReciver mr = new MessageReciver(this);
		addBehaviour(mr);
	}
}
