package practica;

import java.io.Serializable;
import java.util.Random;

public class WaterMass implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Each WaterMass has a volume and a capacity. 
	// It represents the water that flows through a section of a river.
	// We assume that every WaterMass has a capacity of 1000
	// The volume depends on a fixed amount of 500l plus a variable amount that
	// ranges from 0l to 500l. 
	
	private double volume;
	private double capacity;
	
	private double suspendedSolids;
	private double chemicalOxygenDemand;
	private double biologicalOxygenDemand;
	private double totalSulfites;
	private double totalNitrates;
	
	private Random r;
	
	public WaterMass () {
		
		r = new Random();		
		setVolume(500 + 500*r.nextDouble());
		setCapacity(1000);
		
		setSuspendedSolids(0);
		setChemicalOxygenDemand(0);
		setBiologicalOxygenDemand(0);
		setTotalNitrates(0);
		setTotalSulfites(0);
	}


	public WaterMass(WaterMass m) {
		this.capacity=m.capacity;
		this.volume=m.volume;
		this.suspendedSolids=m.suspendedSolids;
		this.chemicalOxygenDemand=m.chemicalOxygenDemand;
		this.biologicalOxygenDemand=m.biologicalOxygenDemand;
		this.totalNitrates=m.totalNitrates;
		this.totalSulfites=m.totalSulfites;
	}
	
	public WaterMass(double wE, double sS, double cOD, double bOD, double tN, double tS) {

		setCapacity(1000);
		setVolume(wE);
		setSuspendedSolids(sS);
		setChemicalOxygenDemand(cOD);
		setBiologicalOxygenDemand(bOD);
		setTotalNitrates(tN);
		setTotalSulfites(tS);
	}
	
	//This method merges two WaterMasses,returning the addition of all the attributes of s+d, except the maximum capacity, which remains constant. 
	public static WaterMass mergeWater(WaterMass s, WaterMass d) {
		d.setVolume(d.getVolume()+s.getVolume());
		d.setSuspendedSolids(d.getSuspendedSolids()+s.getSuspendedSolids());
		d.setChemicalOxygenDemand(d.getChemicalOxygenDemand()+s.getChemicalOxygenDemand());
		d.setBiologicalOxygenDemand(d.getBiologicalOxygenDemand()+s.getBiologicalOxygenDemand());
		d.setTotalNitrates(d.getTotalNitrates()+s.getTotalNitrates());
		d.setTotalSulfites(d.getTotalSulfites()+s.getTotalSulfites());
		return d;
	}
	
	public double getVolume() {
		return volume;
	}
	
	public void setVolume(double volume) {
		this.volume = volume;
	}
	
	public double getCapacity() {
		return capacity;
	}
	
	public void setCapacity(double capacity) {
		this.capacity = capacity;
	}

	public double getSuspendedSolids() {
		return suspendedSolids;
	}

	public void setSuspendedSolids(double suspendedSolids) {
		this.suspendedSolids = suspendedSolids;
	}

	public double getChemicalOxygenDemand() {
		return chemicalOxygenDemand;
	}

	public void setChemicalOxygenDemand(double chemicalOxygenDemand) {
		this.chemicalOxygenDemand = chemicalOxygenDemand;
	}

	public double getBiologicalOxygenDemand() {
		return biologicalOxygenDemand;
	}

	public void setBiologicalOxygenDemand(double biologicalOxygenDemand) {
		this.biologicalOxygenDemand = biologicalOxygenDemand;
	}

	public double getTotalSulfites() {
		return totalSulfites;
	}

	public void setTotalSulfites(double totalSulfites) {
		this.totalSulfites = totalSulfites;
	}

	public double getTotalNitrates() {
		return totalNitrates;
	}

	public void setTotalNitrates(double totalNitrates) {
		this.totalNitrates = totalNitrates;
	}
	
	public String toString() {
		
		String s = "Total Capacity: " + capacity + "\n";
		s += "Volume: " + volume + "\n";
		s += "Suspended Solids: " + suspendedSolids + "\n";
		s += "Chemical Oxygen Demand: " + chemicalOxygenDemand + "\n";
		s += "Biological Oxygen Demand: " + biologicalOxygenDemand + "\n";
		s += "Total Sulfites: " + totalSulfites + "\n";
		s += "Total Nitrates: " + totalNitrates;
		return s;
	}


	public double getAvailableVolume() {
		
		return capacity - volume;
	}


	public void addWaterMass(WaterMass wM) {
		this.volume += wM.getVolume();
		this.suspendedSolids += wM.getSuspendedSolids();
		this.biologicalOxygenDemand += wM.getBiologicalOxygenDemand();
		this.chemicalOxygenDemand += wM.getChemicalOxygenDemand();
		this.totalNitrates += wM.getTotalNitrates();
		this.totalSulfites += wM.getTotalSulfites(); 
	}

	public void substractWaterMass(WaterMass wM) {

		this.volume -= wM.getVolume();
		this.suspendedSolids -= wM.getSuspendedSolids();
		this.biologicalOxygenDemand -= wM.getBiologicalOxygenDemand();
		this.chemicalOxygenDemand -= wM.getChemicalOxygenDemand();
		this.totalNitrates -= wM.getTotalNitrates();
		this.totalSulfites -= wM.getTotalSulfites();
	}

	public double getWaterRate() {
		
		return volume/capacity;
	}


	public WaterMass getPortion(double v) {
		
		double rate = v/volume;
		WaterMass wM = new WaterMass(v, rate*suspendedSolids, rate*chemicalOxygenDemand, rate*biologicalOxygenDemand, rate*totalNitrates, rate*totalSulfites);
		wM.setCapacity(wM.getVolume());
		return wM;
	}


	
}
