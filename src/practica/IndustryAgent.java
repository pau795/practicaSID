package practica;

import java.io.IOException;
import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;


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
			MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.INFORM_REF);
			ACLMessage msg2 = myAgent.receive(template);
			if(msg2 != null) {
			// Water mass sent from the river to the industry in order to be extracted
				if(msg2 != null && msg2.getPerformative() == ACLMessage.INFORM_REF && msg2.getInReplyTo() != null && msg2.getInReplyTo().equals("volume")) {
					try {
						waterExtracted = (WaterMass) msg2.getContentObject();
						polluteWater();
						System.out.println("Industry " + getLocalName() + " has received and polluted the water.");
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				}
			} else block(2000);
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
				MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.INFORM_REF);
				ACLMessage msg2 = myAgent.receive(template);
				if(msg2 != null) {
					if(msg2.getPerformative() == ACLMessage.INFORM_REF && msg2.getConversationId() != null && msg2.getConversationId().equals("volumeEDAR")) {
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
						MessageTemplate template2 = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
						ACLMessage msg4 = myAgent.receive(template2);
						if(msg4 != null) {
							if(msg4.getPerformative() == ACLMessage.CONFIRM && msg4.getConversationId() != null && msg4.getConversationId().equals("dump")) {
								System.out.println("Industry " + getLocalName() + " dumps " + waterToDump.getVolume() + " liters of water to the EDAR");
								tankOfWater.substractWaterMass(waterToDump);
								System.out.println("The tank of the industry " + getLocalName() + " stores " + tankOfWater.getVolume() + " liters of water after the dump");
								waterToDump = null;
							}
						} else block(2000);
					}
				} else block(2000);
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
	
	
	private class ContractNetResponderBehaviour extends ContractNetResponder
    { 
        public ContractNetResponderBehaviour(Agent a, MessageTemplate mt)
        {
            super(a,mt);
        }
        
        //Method to prive a proposal to the CPF
        protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
                System.out.println("Industry "+getLocalName()+" receives a CFP from "+cfp.getSender().getName()+" to perform action: "+cfp.getContent() + "");
                if (tankOfWater.getVolume() > 0) {
                    // We provide a proposal   
                	 ACLMessage propose = cfp.createReply();
                	 propose.setPerformative(ACLMessage.PROPOSE);
                    try {
						propose.setContentObject(tankOfWater);
					} catch (IOException e) {
						e.printStackTrace();
					}
                    System.out.println("Industry "+getLocalName()+" proposes "+tankOfWater.getVolume() +" liters of water");
                    return propose;
                }
                else {
                    // We refuse to provide a proposal
                    System.out.println("Industry '"+getLocalName()+" has no water to offer");
                    throw new RefuseException("noWater");
                }
            }
        	
        
        	//Method to perform an action when the agent accepts our proposal
            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
                System.out.println("Industry "+getLocalName()+" accepts proposal and is about to dump water");
                double v = Double.valueOf(accept.getUserDefinedParameter("volume"));
                if (tankOfWater.getVolume() >= v) {
                    
                	ACLMessage inform = accept.createReply();
                    inform.setPerformative(ACLMessage.INFORM);
                    WaterMass m = tankOfWater.getPortion(v);
                    tankOfWater.substractWaterMass(m);
                    try {
						inform.setContentObject(m);
					} catch (IOException e) {
						e.printStackTrace();
					}
                    System.out.println("Industry "+getLocalName()+" succesfully dumps " + v + " liters of water to EDAR");
                    return inform;
                }
                else {
                    System.out.println("Industry "+getLocalName()+" unexpectedly is not able to provide the proposed volume of water");
                    throw new FailureException("noWater");
                }
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
	
	private void initializeCNI() {
		
		MessageTemplate template = MessageTemplate.and(
		MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
		MessageTemplate.MatchPerformative(ACLMessage.CFP) );
		addBehaviour(new ContractNetResponderBehaviour(this, template));
	}
	
	protected void setup() {
		
		setIndustryParameters();
		searchRiver();
		searchEDAR();
		registerSection();
		initializeCNI();		 
		
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
	            System.out.println("Industry " + getLocalName() + " has found the AID of the EDAR and it is: " + EDARAID.getLocalName());
	            
	            ACLMessage msg = new ACLMessage(ACLMessage.INFORM_IF);
				msg.setSender(getAID());
				msg.addReceiver(EDARAID);
				msg.setConversationId("Industry");
				send(msg);
				System.out.println("Industry " + getLocalName() + " has sended its local name to the EDAR");
				
				ACLMessage msg2 = blockingReceive(3000);
				if(msg2 != null && msg2.getPerformative() == ACLMessage.CONFIRM && msg2.getConversationId().equals("Industry"))
					EDARHasIndustryName = true;
	        }
	        else {
	        	System.out.println("Industry " + getLocalName() + " hasn't found the EDAR.");
	        }
        }
	}
}
