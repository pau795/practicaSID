package practica;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class GUIAgent extends Agent {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static class Window extends Application {
		
		private static Stage stage;
		private static XYChart.Series<String, Double> EDARSeries;
		private static XYChart.Series<String, Double> IndustriesSeries;
		static FXMLController controller;
		
		private class FXMLController implements Initializable {

			@FXML public AnchorPane anchor;
			@FXML public GridPane mainGrid;
			@FXML public GridPane pollutionGrid;
			@FXML public BarChart EDARBarChart;
			@SuppressWarnings("rawtypes")
			@FXML public BarChart riverBarChart;
			@SuppressWarnings("rawtypes")
			@FXML public BarChart indBarChart;
			
//			public Label sS;
//			public Label sSV;
//			public Label bOD;
//			public Label bODV;
//			public Label cOD;
//			public Label cODV;
//			public Label tS;
//			public Label tSV;
//			public Label tN;
//			public Label tNV;

			@Override @FXML
			public void initialize(URL location, ResourceBundle resources) {
				
			}
			
			@SuppressWarnings("unchecked")
			public void updateCharts() {
				EDARBarChart.getData().clear();
				EDARBarChart.getData().add(EDARSeries);
				
				indBarChart.getData().clear();
				indBarChart.getData().add(IndustriesSeries);
			}
			
		}
		
		@Override
        public void start(Stage s) {

        	stage = s;
    	    Platform.runLater(new Runnable() {
                @Override
                public void run() {

                	FXMLLoader loader = new FXMLLoader();
                    URL resource = getClass().getResource("/AgentsCode/practica/GUIDist.fxml");
                    System.out.print("resource: " + resource + "\n");
                    loader.setLocation(resource);
                    controller = new FXMLController();
                    loader.setController(controller);
                    Parent root = null;
					
                    try {
						root = loader.load();
					} catch (IOException e) {
						e.printStackTrace();
					}
                    
            	    Scene scene  = new Scene(root, 1100, 800);
                    stage.setScene(scene);
                    stage.show();
                }
            });
        }

		@SuppressWarnings({ "rawtypes", "unchecked" })
		private static void updateBarChart() {
			
			EDARSeries = new XYChart.Series();
            EDARSeries.getData().add(new XYChart.Data("Polluted Water", pollutedTank.getWaterRate()*100));
            EDARSeries.getData().add(new XYChart.Data("Purified Water", purifiedWater.getWaterRate()*100));
            EDARSeries.getData().add(new XYChart.Data("Water To Purify", waterToPurify.getWaterRate()*100));
            
            IndustriesSeries = new XYChart.Series();
            Iterator<AID> it = industries.keySet().iterator();
			while(it.hasNext()) {
				AID ind = it.next();
                String name = ind.getLocalName();
                IndustriesSeries.getData().add(new XYChart.Data(name, industries.get(ind)*100));
			}
			
			controller.updateCharts();
		}

		public static void updateGUI() {
        	
        	Platform.runLater(new Runnable() {
                @Override
                public void run() {
                	
                	updateBarChart();
//                	grid.add(bc, 0, 0);
//                	grid.add(bc, 1, 0);
//                	Scene scene = stage.getScene();
//                	scene.setRoot(grid);
//                    stage.setScene(scene);
//                    stage.show();
                    
                }
            });
        	
        }
	}

	public class MessageReceiver extends CyclicBehaviour {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public MessageReceiver(Agent a) {
			super(a);
		}
		
		@Override
		public void action() {
			
			MessageTemplate tmp2 = MessageTemplate.MatchPerformative(ACLMessage.INFORM_REF);
//			MessageTemplate tmp3 = MessageTemplate.or(tmp1, tmp2);
			ACLMessage msg = receive(tmp2);
			if(msg != null) {
				ACLMessage reply = msg.createReply();
				
				if(msg.getPerformative() == ACLMessage.INFORM_REF && msg.getConversationId() != null && msg.getConversationId() == "IndustryID") {
					industries.put(msg.getSender(), Double.valueOf(msg.getContent()));
					reply.setPerformative(ACLMessage.CONFIRM);
					send(reply);
					System.out.println("GUI Agent has receive the volume of the industry " + msg.getSender() + " and it is " + Double.valueOf(msg.getContent()));
				}
				
				else if(msg.getPerformative() == ACLMessage.INFORM_REF) {
					
					if(msg.getConversationId() != null && msg.getConversationId().equals("tankVolume")) {
						industries.put(msg.getSender(), Double.valueOf(msg.getContent()));
						System.out.println("GUI receives the tank volume of the industry " + msg.getSender().getLocalName());
					}
					
					else if(msg.getConversationId() != null && msg.getConversationId().equals("pollutedTank")) {
						try {
							pollutedTank = (WaterMass) msg.getContentObject();
							System.out.println("GUI receives the pollutedTank of the EDAR");
						} catch (UnreadableException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					else if(msg.getConversationId() != null && msg.getConversationId().equals("purifiedWater")) {
						try {
							purifiedWater = (WaterMass) msg.getContentObject();
							System.out.println("GUI receives the purified water of the EDAR");
						} catch (UnreadableException e) {
							e.printStackTrace();
						}
					}
					
					else if(msg.getConversationId() != null && msg.getConversationId().equals("waterToPurify")) {
						try {
							waterToPurify = (WaterMass) msg.getContentObject();
							System.out.println("GUI receives the water to purify of the EDAR");
						} catch (UnreadableException e) {
							e.printStackTrace();
						}
					}
				}
				
			} else block();
			
		}
		
	}
	
	public class UpdateGUI extends TickerBehaviour {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public UpdateGUI(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			
			updateIndustries();
			updatePollutedTank();
			updatePurifiedWater();
			updateWaterToPurify();
			Window.updateGUI();
			
		}

		private void updateIndustries() {
		
			Iterator<AID> it = industries.keySet().iterator();
			while(it.hasNext()) {
				AID ind = it.next();
				ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
				msg.setSender(myAgent.getAID());
				msg.setConversationId("tankVolume");
				msg.addReceiver(ind);
				send(msg);
				System.out.println("GUI requesting the volume to industry " + ind.getName());
			}
		}

		private void updatePollutedTank() {
			
			ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
			msg.setSender(getAID());
			msg.addReceiver(EDARAID);
			msg.setConversationId("pollutedTank");
			send(msg);
			System.out.println("GUI requesting the volume of the polluted tank");
			
		}

		private void updatePurifiedWater() {
			
			ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
			msg.setSender(getAID());
			msg.addReceiver(EDARAID);
			msg.setConversationId("purifiedWater");
			send(msg);
			System.out.println("GUI requesting the volume of the purified water");
			
		}

		private void updateWaterToPurify() {
			
			ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
			msg.setSender(getAID());
			msg.addReceiver(EDARAID);
			msg.setConversationId("waterToPurify");
			send(msg);
			System.out.println("GUI requesting the volume of the water to purify");
			
		}
	}
	
	private static WaterMass pollutedTank;
	private static WaterMass purifiedWater;
	private static WaterMass waterToPurify;
	
	private AID riverAID;
	private AID EDARAID;
	
	private static LinkedHashMap<AID, Double> industries;
	
	private void registerAgent() {
		System.out.println("Agent " + getLocalName() + " registering GUI Sevice");
	  	DFAgentDescription dfd = new DFAgentDescription();
	  	dfd.setName(getAID());
  		ServiceDescription sd = new ServiceDescription();
  		sd.setName("GUI");
  		sd.setType("GUI");
  		dfd.addServices(sd);
  		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  		System.out.println("GUI Registered");
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
	            System.out.println("GUI Agent is searching the river");
	        } catch (FIPAException e) {
	            e.printStackTrace();
	        }
	        if (results.length > 0) {
	            DFAgentDescription dfd2 = results[0];
	            riverAID = dfd2.getName();
	            System.out.println("GUI Agent has found the AID of the river and it is: " + riverAID.getLocalName());
	        }
	        else {
	        	System.out.println("GUI Agent hasn't found the river.");
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
        
	        // Search the EDAR
	        try {
	            results = DFService.search(this, dfd, sc );
	            System.out.println("GUI Agent is searching the EDAR");
	        } catch (FIPAException e) {
	            e.printStackTrace();
	        }
	        if (results.length > 0) {
	            DFAgentDescription dfd2 = results[0];
	            EDARAID = dfd2.getName();
	            System.out.println("GUI Agent has found the AID of the EDAR and it is: " + EDARAID.getLocalName());
	        }
	        else {
	        	System.out.println("GUI Agent hasn't found the EDAR.");
	        }
        }
	}
	
    public static void main(String args[]) {
        
    	Thread t1 = new Thread(new Runnable() {
    	    public void run()
    	    {
    	         Application.launch(Window.class, args);
    	    }}); 
    	
    	    t1.start();
    }
    
	protected void setup() {
		
		registerAgent();
		searchRiver();
		searchEDAR();
		
		pollutedTank = new WaterMass(0,0,0,0,0,0);
		purifiedWater = new WaterMass(0,0,0,0,0,0);
		waterToPurify = new WaterMass(0,0,0,0,0,0);
		industries = new LinkedHashMap<>();
		
		MessageReceiver mr = new MessageReceiver(this);
		addBehaviour(mr);
		
		UpdateGUI uGUI = new UpdateGUI(this, 2000);
		addBehaviour(uGUI);
		
		String[] args = null;
		main(args);
		
		
	}
}