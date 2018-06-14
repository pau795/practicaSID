package paqueteSid.impl;


import jade.util.leap.*;
import paqueteSid.*;

/**
* Protege name: http://www.owl-ontologies.com/Ontology1528273816.owl#MassaAgua
* @author OntologyBeanGenerator v4.1
* @version 2018/06/6, 11:55:40
*/
public class DefaultMassaAgua implements MassaAgua {

  private static final long serialVersionUID = 262976333997734927L;

  private String _internalInstanceName = null;

  public DefaultMassaAgua() {
    this._internalInstanceName = "";
  }

  public DefaultMassaAgua(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * Protege name: http://www.owl-ontologies.com/Ontology1528273816.owl#TotalNitratos
   */
   private List totalNitratos = new ArrayList();
   public void addTotalNitratos(Float elem) { 
     totalNitratos.add(elem);
   }
   public boolean removeTotalNitratos(Float elem) {
     boolean result = totalNitratos.remove(elem);
     return result;
   }
   public void clearAllTotalNitratos() {
     totalNitratos.clear();
   }
   public Iterator getAllTotalNitratos() {return totalNitratos.iterator(); }
   public List getTotalNitratos() {return totalNitratos; }
   public void setTotalNitratos(List l) {totalNitratos = l; }

   /**
   * Protege name: http://www.owl-ontologies.com/Ontology1528273816.owl#TotalSulfatos
   */
   private List totalSulfatos = new ArrayList();
   public void addTotalSulfatos(Float elem) { 
     totalSulfatos.add(elem);
   }
   public boolean removeTotalSulfatos(Float elem) {
     boolean result = totalSulfatos.remove(elem);
     return result;
   }
   public void clearAllTotalSulfatos() {
     totalSulfatos.clear();
   }
   public Iterator getAllTotalSulfatos() {return totalSulfatos.iterator(); }
   public List getTotalSulfatos() {return totalSulfatos; }
   public void setTotalSulfatos(List l) {totalSulfatos = l; }

   /**
   * Protege name: http://www.owl-ontologies.com/Ontology1528273816.owl#Volumen
   */
   private List volumen = new ArrayList();
   public void addVolumen(Float elem) { 
     volumen.add(elem);
   }
   public boolean removeVolumen(Float elem) {
     boolean result = volumen.remove(elem);
     return result;
   }
   public void clearAllVolumen() {
     volumen.clear();
   }
   public Iterator getAllVolumen() {return volumen.iterator(); }
   public List getVolumen() {return volumen; }
   public void setVolumen(List l) {volumen = l; }

   /**
   * Protege name: http://www.owl-ontologies.com/Ontology1528273816.owl#SolidosSuspension
   */
   private List solidosSuspension = new ArrayList();
   public void addSolidosSuspension(Float elem) { 
     solidosSuspension.add(elem);
   }
   public boolean removeSolidosSuspension(Float elem) {
     boolean result = solidosSuspension.remove(elem);
     return result;
   }
   public void clearAllSolidosSuspension() {
     solidosSuspension.clear();
   }
   public Iterator getAllSolidosSuspension() {return solidosSuspension.iterator(); }
   public List getSolidosSuspension() {return solidosSuspension; }
   public void setSolidosSuspension(List l) {solidosSuspension = l; }

   /**
   * Protege name: http://www.owl-ontologies.com/Ontology1528273816.owl#DemandaBiologicaOxigeno
   */
   private List demandaBiologicaOxigeno = new ArrayList();
   public void addDemandaBiologicaOxigeno(Float elem) { 
     demandaBiologicaOxigeno.add(elem);
   }
   public boolean removeDemandaBiologicaOxigeno(Float elem) {
     boolean result = demandaBiologicaOxigeno.remove(elem);
     return result;
   }
   public void clearAllDemandaBiologicaOxigeno() {
     demandaBiologicaOxigeno.clear();
   }
   public Iterator getAllDemandaBiologicaOxigeno() {return demandaBiologicaOxigeno.iterator(); }
   public List getDemandaBiologicaOxigeno() {return demandaBiologicaOxigeno; }
   public void setDemandaBiologicaOxigeno(List l) {demandaBiologicaOxigeno = l; }

   /**
   * Protege name: http://www.owl-ontologies.com/Ontology1528273816.owl#DemandaQuimicaOxigeno
   */
   private List demandaQuimicaOxigeno = new ArrayList();
   public void addDemandaQuimicaOxigeno(Float elem) { 
     demandaQuimicaOxigeno.add(elem);
   }
   public boolean removeDemandaQuimicaOxigeno(Float elem) {
     boolean result = demandaQuimicaOxigeno.remove(elem);
     return result;
   }
   public void clearAllDemandaQuimicaOxigeno() {
     demandaQuimicaOxigeno.clear();
   }
   public Iterator getAllDemandaQuimicaOxigeno() {return demandaQuimicaOxigeno.iterator(); }
   public List getDemandaQuimicaOxigeno() {return demandaQuimicaOxigeno; }
   public void setDemandaQuimicaOxigeno(List l) {demandaQuimicaOxigeno = l; }

}
