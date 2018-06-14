package practica;

import java.util.Random;

public class WaterMass {
	
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
		
		r= new Random();		
		setVolume(500 + 500*r.nextDouble());
		setCapacity(1000);
		
		setSuspendedSolids(0);
		setChemicalOxygenDemand(0);
		setBiologicalOxygenDemand(0);
		setTotalNitrates(0);
		setTotalSulfites(0);
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
	
}
