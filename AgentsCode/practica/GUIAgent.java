package practica;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
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
		private static XYChart.Series<String, Double> riverSeries;
		static FXMLController controller;
		
		private class FXMLController implements Initializable {

			@FXML public AnchorPane anchor;
			@FXML public GridPane mainGrid;
			@FXML public GridPane pollutionGrid;
			@SuppressWarnings("rawtypes")
			@FXML public BarChart EDARBarChart;
			@SuppressWarnings("rawtypes")
			@FXML public AreaChart riverAreaChart;
			@SuppressWarnings("rawtypes")
			@FXML public BarChart indBarChart;
			private String activeBar = null;
			
			@FXML public Label sSLab;
			@FXML public Label bODLab;
			@FXML public Label cODLab;
			@FXML public Label tSLab;
			@FXML public Label tNLab;
			@FXML public Label waterNameLab;
			@FXML public Label volumeLab;
			@FXML public Label volumeRateLab;

			@Override @FXML
			public void initialize(URL location, ResourceBundle resources) {
				
			}
			
			@SuppressWarnings("unchecked")
			public void updateCharts() {
				EDARBarChart.getData().clear();
				EDARBarChart.getData().add(EDARSeries);
				setupHover(EDARSeries);
				setupHover(EDARSeries);
				setupHover(EDARSeries);
				
				indBarChart.getData().clear();
				indBarChart.getData().add(IndustriesSeries);
				setupHover(IndustriesSeries);
				setupHover(IndustriesSeries);
				setupHover(IndustriesSeries);
				
				riverAreaChart.getData().clear();
				riverAreaChart.getData().add(riverSeries);
			}
			
			public void updateInfoWaterMass() {
				
				if(activeBar != null) {
					AID aid = new AID(activeBar, false);
					WaterMass wM = industries.get(aid);
					if(wM == null) {
						if(activeBar.equals("Polluted Water")) wM = pollutedTank;
						else if(activeBar.equals("Purified Water")) wM = purifiedWater;
						else if(activeBar.equals("Water To Purify")) wM = waterToPurify;
						else if(activeBar.equals("Metereological Tank")) wM = metereologicalTank;
					}
					if(wM != null) {
						sSLab.setText(String.valueOf(dF.format(wM.getSuspendedSolids())) + " mg/l");
						bODLab.setText(String.valueOf(dF.format(wM.getBiologicalOxygenDemand())) + " mg/l");
						cODLab.setText(String.valueOf(dF.format(wM.getChemicalOxygenDemand())) + " mg/l");
						tSLab.setText(String.valueOf(dF.format(wM.getTotalSulfites())) + " mg/l");
						tNLab.setText(String.valueOf(dF.format(wM.getTotalNitrates())) + " mg/l");
						waterNameLab.setText(activeBar);
						volumeLab.setText(String.valueOf(dF.format(wM.getVolume())) + " m3");
						volumeRateLab.setText(String.valueOf(dF.format(wM.getWaterRate()*100)) + " %");
					}
				}
			}
			
//			private final Glow glowHover = new Glow(.6);
			private final Glow glowClick = new Glow(.9);

			private void setupHover(XYChart.Series<String, Double> series) {
			    for (final XYChart.Data<String, Double> dt : series.getData()) {
			        final Node n = dt.getNode();

			        n.setEffect(null);
			        n.setOnMouseExited(new EventHandler<MouseEvent>() {
			            @Override
			            public void handle(MouseEvent e) {
			                n.setEffect(null);
			            }
			        });
			        n.setOnMouseClicked(new EventHandler<MouseEvent>() {
			            @Override
			            public void handle(MouseEvent e) {
			            	n.setEffect(glowClick);
			            	activeBar = dt.getXValue();
		            		updateInfoWaterMass();
			            }
			        });
			        
			    }
			}
			
		}
		
		@Override
        public void start(Stage s) {

        	stage = s;
    	    Platform.runLater(new Runnable() {
                @Override
                public void run() {

                	FXMLLoader loader = new FXMLLoader();
                	URL resource = getClass().getClassLoader().getResource("practica/GUIDist.fxml");
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
                    
            	    Scene scene  = new Scene(root, 1100, 700);
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
            EDARSeries.getData().add(new XYChart.Data("Metereological Tank", metereologicalTank.getWaterRate()*100));
            
            IndustriesSeries = new XYChart.Series();
            Iterator<AID> it = industries.keySet().iterator();
			while(it.hasNext()) {
				AID ind = it.next();
                String name = ind.getLocalName();
                IndustriesSeries.getData().add(new XYChart.Data(name, industries.get(ind).getWaterRate()*100));
			}
			
			riverSeries = new XYChart.Series();
			for(int i = 0; i < river.size(); ++i) {
				riverSeries.getData().add(new XYChart.Data(String.valueOf(i), river.get(i)));
			}
			
			controller.updateCharts();
		}

		public static void updateGUI() {
        	
        	Platform.runLater(new Runnable() {
                @Override
                public void run() {
                	
                	updateBarChart();
                	controller.updateInfoWaterMass();
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
		
		@SuppressWarnings("unchecked")
		@Override
		public void action() {
			
			MessageTemplate tmp2 = MessageTemplate.MatchPerformative(ACLMessage.INFORM_REF);
			ACLMessage msg = receive(tmp2);
			if(msg != null) {
				ACLMessage reply = msg.createReply();
				
				if(msg.getPerformative() == ACLMessage.INFORM_REF) {
					
					if(msg.getConversationId() != null && msg.getConversationId().equals("IndustryID")) {
						try {
							industries.put(msg.getSender(), (WaterMass) msg.getContentObject());
						} catch (UnreadableException e) {
							e.printStackTrace();
						}
						reply.setPerformative(ACLMessage.CONFIRM);
						send(reply);
						//System.out.println("GUI Agent has receive the volume of the industry " + msg.getSender() + " and it is " + Double.valueOf(msg.getContent()));
					}
								
					
					if(msg.getConversationId() != null && msg.getConversationId().equals("tankVolume")) {
						try {
							industries.put(msg.getSender(), (WaterMass) msg.getContentObject());
						} catch (UnreadableException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
//						System.out.println("GUI receives the tank volume of the industry " + msg.getSender().getLocalName());
					}
					
					else if(msg.getConversationId() != null && msg.getConversationId().equals("pollutedTank")) {
						try {
							pollutedTank = (WaterMass) msg.getContentObject();
//							System.out.println("GUI receives the pollutedTank of the EDAR");
						} catch (UnreadableException e) {
							e.printStackTrace();
						}
					}
					
					else if(msg.getConversationId() != null && msg.getConversationId().equals("purifiedWater")) {
						try {
							purifiedWater = (WaterMass) msg.getContentObject();
//							System.out.println("GUI receives the purified water of the EDAR");
						} catch (UnreadableException e) {
							e.printStackTrace();
						}
					}
					
					else if(msg.getConversationId() != null && msg.getConversationId().equals("waterToPurify")) {
						try {
							waterToPurify = (WaterMass) msg.getContentObject();
//							System.out.println("GUI receives the water to purify of the EDAR");
						} catch (UnreadableException e) {
							e.printStackTrace();
						}
					}
					
					else if(msg.getConversationId() != null & msg.getConversationId().equals("fullRiver")) {
						
						try {
							river = (ArrayList<Double>) msg.getContentObject();
//							System.out.println("GUI receives the volume of the river");
						} catch (UnreadableException e) {
							e.printStackTrace();
						}
					}

					else if(msg.getConversationId() != null & msg.getConversationId().equals("meteoTank")) {
						
						try {
							metereologicalTank = (WaterMass) msg.getContentObject();
//							System.out.println("GUI receives the volume of the metereologic tank");
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
			updateMeteorologicalTank();
			updateRiver();
			Window.updateGUI();
			
		}

		private void updateMeteorologicalTank() {

			ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
			msg.setSender(getAID());
			msg.addReceiver(rainAID);
			msg.setConversationId("meteoTank");
			send(msg);
//			System.out.println("GUI requesting the volume of the metereological tank");
			
		}

		private void updateRiver() {

			ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
			msg.setSender(getAID());
			msg.addReceiver(riverAID);
			msg.setContent("fullRiver");
			send(msg);
//			System.out.println("GUI requesting the volume of the river");
			
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
//				System.out.println("GUI requesting the volume to industry " + ind.getName());
			}
		}

		private void updatePollutedTank() {
			
			ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
			msg.setSender(getAID());
			msg.addReceiver(EDARAID);
			msg.setConversationId("pollutedTank");
			send(msg);
//			System.out.println("GUI requesting the volume of the polluted tank");
			
		}

		private void updatePurifiedWater() {
			
			ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
			msg.setSender(getAID());
			msg.addReceiver(EDARAID);
			msg.setConversationId("purifiedWater");
			send(msg);
//			System.out.println("GUI requesting the volume of the purified water");
			
		}

		private void updateWaterToPurify() {
			
			ACLMessage msg = new ACLMessage(ACLMessage.QUERY_REF);
			msg.setSender(getAID());
			msg.addReceiver(EDARAID);
			msg.setConversationId("waterToPurify");
			send(msg);
//			System.out.println("GUI requesting the volume of the water to purify");
			
		}
	}
	
	private static WaterMass pollutedTank;
	private static WaterMass purifiedWater;
	private static WaterMass waterToPurify;
	private static WaterMass metereologicalTank;
	private static DecimalFormat dF = new DecimalFormat("0.00");
	private static ArrayList<Double> river;
	
	
	private AID riverAID;
	private AID EDARAID;
	private AID rainAID;
	
	private static LinkedHashMap<AID, WaterMass> industries;
	
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
	
	private void searchRain() {
		// The behaviour register the type of the service that will search, in this case a River
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Rain");
		dfd.addServices(sd);
		
		// Set the number of results desired to 1
		SearchConstraints sc = new SearchConstraints();
		sc.setMaxResults(new Long(1));
        DFAgentDescription[] results = null;
        
        rainAID = null;
        while(rainAID == null) {
        
	        // Search the river
	        try {
	            results = DFService.search(this, dfd, sc );
	            System.out.println("GUI Agent is searching the Rain");
	        } catch (FIPAException e) {
	            e.printStackTrace();
	        }
	        if (results.length > 0) {
	            DFAgentDescription dfd2 = results[0];
	            rainAID = dfd2.getName();
	            System.out.println("GUI Agent has found the AID of the rain and it is: " + rainAID.getLocalName());
	        }
	        else {
	        	System.out.println("GUI Agent hasn't found the rain.");
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
		searchRain();
		
		pollutedTank = new WaterMass(0,0,0,0,0,0);
		purifiedWater = new WaterMass(0,0,0,0,0,0);
		waterToPurify = new WaterMass(0,0,0,0,0,0);
		metereologicalTank = new WaterMass(0,0,0,0,0,0);
		industries = new LinkedHashMap<>();
		river = new ArrayList<>();
		
		MessageReceiver mr = new MessageReceiver(this);
		addBehaviour(mr);
		
		UpdateGUI uGUI = new UpdateGUI(this, 1000);
		addBehaviour(uGUI);
		
		String[] args = null;
		main(args);
		
		
	}
}