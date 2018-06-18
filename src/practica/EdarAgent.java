package practica;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;

@SuppressWarnings("serial")
public class EdarAgent extends Agent{
		
	private class WaterPurifier extends TickerBehaviour {
		
		public WaterPurifier(Agent a, long period) {
			super(a,period);
		}
		
		public void onTick() {
			
			//Purify water
			waterToPurify = getWater();
			if(waterToPurify.getVolume() > 0) {
				System.out.println("\n"+"Water to purify:"+"\n");
				System.out.println(waterToPurify+"\n");
				System.out.println("Purifying...");
				waterToPurify.setSuspendedSolids(waterToPurify.getSuspendedSolids()*sspr);
				waterToPurify.setChemicalOxygenDemand(waterToPurify.getChemicalOxygenDemand()*codpr);
				waterToPurify.setBiologicalOxygenDemand(waterToPurify.getBiologicalOxygenDemand()*bodpr);
				waterToPurify.setTotalNitrates(waterToPurify.getTotalNitrates()*tnpr);
				waterToPurify.setTotalSulfites(waterToPurify.getTotalSulfites()*tspr);
				System.out.println("Result:\n\n"+waterToPurify+"\n");
				if (isPurified(waterToPurify)) {
					System.out.println("Water successfully purified");
					purifiedWater = WaterMass.mergeWater(waterToPurify, purifiedWater);
					waterToPurify = new WaterMass(0,0,0,0,0,0);
					System.out.println("EDAR purified water tank has " + purifiedWater.getVolume() + " liters of water");
				}
			}
			else System.out.println("The EDAR has no water to purify");
			
			//If there is not enough polluted water to work with, make a call for proposals asking for water
			if (pollutedWater.getVolume() < minPollutedWater && !cfpInProgress) {
				System.out.println("Doing a call for proposals asking for water to all the industries");
				ACLMessage cnim = generateCNIMessage();
				ContractNetInitiatorBehaviour cni = new ContractNetInitiatorBehaviour(myAgent, cnim);
				addBehaviour(cni);	
			}	
			
			//Pour the water into the river
			WaterMass m = new WaterMass();
			if (purifiedWater.getVolume() <= sectionCapacity*pourRatio) m = new WaterMass(purifiedWater);
			else {
				m.setCapacity(sectionCapacity);
				m.setVolume(sectionCapacity*pourRatio);	
				m.setSuspendedSolids(purifiedWater.getSuspendedSolids());
				m.setChemicalOxygenDemand(purifiedWater.getChemicalOxygenDemand()*pourRatio);
				m.setBiologicalOxygenDemand(purifiedWater.getBiologicalOxygenDemand()*pourRatio);
				m.setTotalNitrates(purifiedWater.getTotalNitrates()*pourRatio);
				m.setTotalSulfites(purifiedWater.getTotalSulfites()*pourRatio);
			}
			if(m.getVolume() > 0) {
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
				System.out.println("The EDAR is trying to pour " + m.getVolume() +" liters of water to the river");
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
						System.out.println("EDAR has poured " + m.getVolume() + " liters to the river successfully");
					}
					else System.out.println("Is not possible to pour water to the river");
				}
			}
			else System.out.println("The EDAR has no water ready to be poured");
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
	
	@SuppressWarnings("rawtypes")
	private class ContractNetInitiatorBehaviour extends ContractNetInitiator {

		public ContractNetInitiatorBehaviour(Agent a, ACLMessage cfp) {
			super(a, cfp);
		}
		
		public void onStart(){
			super.onStart();
			cfpInProgress=true;
		}
		
		public int onEnd() {
			cfpInProgress=false;
			System.out.println("Call for proposals ended");
			return super.onEnd();
		}
		
	    protected void handlePropose(ACLMessage propose,  Vector v) {
	    	try {
				WaterMass m = (WaterMass) propose.getContentObject();
				System.out.println("EDAR has resgistered industry "+propose.getSender().getLocalName()+" proposal of "+m.getVolume() + " liters of water");
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
	    	
        }

        protected void handleRefuse(ACLMessage refuse)
        {
                System.out.println("Industry "+refuse.getSender().getLocalName()+" refused to propose");
        }

        protected void handleFailure(ACLMessage failure) {
                if (failure.getSender().equals(myAgent.getAMS())) {
                        // FAILURE notification from the JADE runtime: the receiver
                        // does not exist
                        System.out.println("Responder does not exist");
                }
                else {
                        System.out.println("Industry "+failure.getSender().getLocalName()+" failed");
                }
                // Immediate failure --> we will not receive a response from this agent
                nResponders--;
        }
        
        @SuppressWarnings("unchecked")
		protected void handleAllResponses(Vector responses, Vector acceptances) {
                if (responses.size() < nResponders) {
                        // Some responder didn't reply within the specified timeout
                        System.out.println("Timeout expired: missing "+(nResponders - responses.size())+" responses");
                }              
                                
                //The EDAR wants to fill an 80% of the available volume of the polluted tank.
                //It uses the following strategy: it estimates the amount of water that wants from each proposal dividing the
                //total desired volume by the number of proposals, and tries to get the same amount of volume from each one.
                //However, if any proposal has less volume than the estimated one, it will accept that amount,
                //and try to get the rest from other proposals, if possible, trying to get as close as possible to the total desired water.
                double totalDesiredWater = pollutedWater.getAvailableVolume()*0.8;
                double estimatedWater = totalDesiredWater/responses.size();
                double deficit = 0;
                Enumeration e = responses.elements();
                while (e.hasMoreElements()) {
                	ACLMessage msg = (ACLMessage) e.nextElement();
                    if (msg.getPerformative() == ACLMessage.PROPOSE) {
                    	try {
                    		ACLMessage reply = msg.createReply();
                    		reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
							WaterMass m =(WaterMass) msg.getContentObject();
							//we check if the volume proposed can satisfy our estimated volume
							if (m.getVolume() > estimatedWater) {
								//and even more, if we can take all the volume from the deficit of other proposals
								if (m.getVolume() > estimatedWater+deficit) {
									reply.addUserDefinedParameter("volume",String.valueOf(estimatedWater+deficit));
									deficit = 0;
								}
								//if not, take as much volume as possible to minimice the deficit
								else {
									reply.addUserDefinedParameter("volume", String.valueOf(m.getVolume()));
									deficit -= m.getVolume()-estimatedWater;
								}
							}
							//if the proposal has less volume than the estimated, accpet and add the deficit
							else {
								reply.addUserDefinedParameter("volume", String.valueOf(m.getVolume()));
								deficit += estimatedWater - m.getVolume();								
							}
							acceptances.addElement(reply);
						} catch (UnreadableException e1) {
							e1.printStackTrace();
						}
                    }
                }
        }
        
        protected void handleInform(ACLMessage inform) {
        	try {
				WaterMass m =(WaterMass) inform.getContentObject();
				WaterMass.mergeWater(m, pollutedWater);
	        	System.out.println("Industry "+inform.getSender().getLocalName()+" has successfully dumped "+ m.getVolume() + " liters of water");
	        	System.out.println("EDAR polluted water tank has " + pollutedWater.getVolume()+" liters of water");
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
        }
	}
	
	private class MessageReciver extends CyclicBehaviour {

		public MessageReciver(Agent a) {
			super(a);
		}
		
		@Override
		public void action() {
			MessageTemplate qif = MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF);
			MessageTemplate qref = MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF);
			MessageTemplate iif = MessageTemplate.MatchPerformative(ACLMessage.INFORM_IF);
			MessageTemplate mt = MessageTemplate.or(MessageTemplate.or(qif, qref), iif);
			ACLMessage  msg = myAgent.receive(mt);
			if (msg!=null) {
				
				ACLMessage reply = msg.createReply();
				String content = msg.getContent();
				
				// If a QUERY_IF is received
				if (msg.getPerformative()==ACLMessage.QUERY_IF) {
					
					// An agent ask to dump a waterMass and the EDAR
					if (content!=null && msg.getConversationId().equals("dump")) {
						try {
							WaterMass m = (WaterMass) msg.getContentObject();
							
							// EDAR confirms that can receive such a Mass
							if (pollutedWater.getVolume() + m.getVolume() <= pollutedWater.getCapacity()) {
								pollutedWater=WaterMass.mergeWater(m, pollutedWater);
								reply.setPerformative(ACLMessage.CONFIRM);
								reply.setContent("The EDAR has received a water mass succesfully");
								reply.setConversationId("dump");
								send(reply);
								System.out.println("The EDAR has received a water mass from " + msg.getSender().getLocalName() + " succesfully");
								System.out.println("EDAR polluted water tank has " + pollutedWater.getVolume()+" liters of water");
							}
							
							// EDAR refuses receiving such a Mass
							else {
								reply.setPerformative(ACLMessage.REFUSE);
								reply.setContent("EDAR polluted water tank cannot receive that volume of water");
								reply.setConversationId("dump");
								send(reply);
								System.out.println("EDAR polluted water tank cannot receive that volume of water");
							}		
						} catch (UnreadableException e) {
							e.printStackTrace();
						}
					}
				}
				
				// If a QUERY_REF is received
				else if (msg.getPerformative() == ACLMessage.QUERY_REF) {

					// An agent ask for the available space in the pollutedTank
					if (msg.getConversationId().equals("volumeEDAR")) {
						reply.setPerformative(ACLMessage.INFORM_REF);
						reply.setContent(String.valueOf(pollutedWater.getAvailableVolume()));
						send(reply);
						System.out.println("EDAR informs to " + msg.getSender().getLocalName() + " the available volume.");
					}
				}
				
				// If a INFORM_IF is received
				else if (msg.getPerformative() == ACLMessage.INFORM_IF) {
					
					// An industry sends its AID to the EDAR and the EDAR confirms
					if(msg.getConversationId().equals("Industry")){
						industries.add(msg.getSender());
						reply.setPerformative(ACLMessage.CONFIRM);
						send(reply);
						System.out.println("EDAR has registered industry " + msg.getSender().getLocalName());
					}
				}
				
			}
			else block();
		}
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
	            System.out.println("EDAR searching the river");
	        } catch (FIPAException e) {
	            e.printStackTrace();
	        }
	        if (results.length > 0) {
	            DFAgentDescription dfd2 = results[0];
	            riverAID = dfd2.getName();
		        System.out.println("EDAR has found the AID of the river and it is: " + riverAID.getLocalName());
	        }
	        else {
	        	System.out.println("EDAR hasn't found the river.");
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
			System.out.println("EDAR request the number of sections");
			
			ACLMessage msg2 = blockingReceive(3000);
			if(msg2 != null && msg2.getPerformative() == ACLMessage.INFORM_REF && msg2.getContent().equals("section"))
				riverSection = r.nextInt(Integer.valueOf(msg2.getUserDefinedParameter("section")));
				System.out.println("EDAR has been assigned to the section " + riverSection);
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
	
	private void registerAgent() {
		System.out.println("Agent " + getLocalName() + " registering EDAR Sevice");
	  	DFAgentDescription dfd = new DFAgentDescription();
	  	dfd.setName(getAID());
  		ServiceDescription sd = new ServiceDescription();
  		sd.setName("Edar");
  		sd.setType("Edar");
  		dfd.addServices(sd);
  		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	private double sspr = 1 - 0.8;		//suspended solids purifiable ratio
	private double codpr = 1 - 0.7;		//chemical oxygen demand purifiable ratio	
	private double bodpr = 1 - 0.8;		//biological oxygen demand purifiable ratio
	private double tnpr = 1 - 0.9;		//total nitrates purifiable ratio
	private double tspr = 1 - 0.9;		//total sulfites purifiable ratio
	
	//purifiable thresholds
	private double sspt = 5;		//suspended solids purifiable threshold
	private double codpt = 2;		//chemical oxygen demand purifiable threshold	
	private double bodpt = 3;		//biological oxygen demand purifiable threshold
	private double tnpt = 0.8;		//total nitrates purifiable threshold
	private double tspt = 0.5;		//total sulfites purifiable threshold
	
	private double pourRatio = 0.3;	//pour ratio
	private Set<AID> industries = new HashSet<>();
	
	private double minPollutedWater =1000;
	
	ACLMessage cniMessage;
	int nResponders;
	boolean cfpInProgress=false;
	
	
	private void initializeTanks() {
		pollutedWater= new WaterMass(0,0,0,0,0,0);
		waterToPurify= new WaterMass(0,0,0,0,0,0);
		purifiedWater= new WaterMass(0,0,0,0,0,0);
		pollutedWater.setCapacity(MaxTankCapacity);
		waterToPurify.setCapacity(MaxPurifiableCapacity);
		purifiedWater.setCapacity(MaxTankCapacity);
	}
	
	private ACLMessage generateCNIMessage() {
		nResponders=industries.size();
		ACLMessage cniMessage = new ACLMessage(ACLMessage.CFP);
		cniMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
		cniMessage.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
		for (AID a:industries) cniMessage.addReceiver(a);
		cniMessage.setContent("waterProposal");
		return cniMessage;
	}
	
	public void setup() {
		
		searchRiver();
		initializeTanks();		
		registerSection();
		getSectionCapacity();
		registerAgent();
		
		WaterPurifier w = new WaterPurifier(this, 5000);
		addBehaviour(w);
		
		MessageReciver mr = new MessageReciver(this);
		addBehaviour(mr);
	}
}