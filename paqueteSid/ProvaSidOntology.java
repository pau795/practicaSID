// file: ProvaSidOntology.java generated by ontology bean generator.  DO NOT EDIT, UNLESS YOU ARE REALLY SURE WHAT YOU ARE DOING!
package paqueteSid;

import jade.content.onto.*;
import jade.content.schema.*;

/** file: ProvaSidOntology.java
 * @author OntologyBeanGenerator v4.1
 * @version 2018/06/6, 11:55:40
 */
public class ProvaSidOntology extends jade.content.onto.Ontology  {

  private static final long serialVersionUID = 262976333997734927L;

  //NAME
  public static final String ONTOLOGY_NAME = "provaSid";
  // The singleton instance of this ontology
  private static Ontology theInstance = new ProvaSidOntology();
  public static Ontology getInstance() {
     return theInstance;
  }


   // VOCABULARY
    public static final String EDAR="EDAR";
    public static final String INDUSTRIA="Industria";
    public static final String RIO="Rio";
    public static final String TANQUEAGUA="TanqueAgua";
    public static final String TRASPASARMASAAGUA="TraspasarMasaAgua";
    public static final String EXTRAERAGUA="ExtraerAgua";
    public static final String MASSAAGUA_DEMANDAQUIMICAOXIGENO="DemandaQuimicaOxigeno";
    public static final String MASSAAGUA_DEMANDABIOLOGICAOXIGENO="DemandaBiologicaOxigeno";
    public static final String MASSAAGUA_SOLIDOSSUSPENSION="SolidosSuspension";
    public static final String MASSAAGUA_VOLUMEN="Volumen";
    public static final String MASSAAGUA_TOTALSULFATOS="TotalSulfatos";
    public static final String MASSAAGUA_TOTALNITRATOS="TotalNitratos";
    public static final String MASSAAGUA="MassaAgua";

  /**
   * Constructor
  */
  private ProvaSidOntology(){ 
    super(ONTOLOGY_NAME, BasicOntology.getInstance());
    try { 

    // adding Concept(s)
    ConceptSchema massaAguaSchema = new ConceptSchema(MASSAAGUA);
    add(massaAguaSchema, paqueteSid.MassaAgua.class);

    // adding AgentAction(s)
    AgentActionSchema extraerAguaSchema = new AgentActionSchema(EXTRAERAGUA);
    add(extraerAguaSchema, paqueteSid.ExtraerAgua.class);
    AgentActionSchema traspasarMasaAguaSchema = new AgentActionSchema(TRASPASARMASAAGUA);
    add(traspasarMasaAguaSchema, paqueteSid.TraspasarMasaAgua.class);

    // adding AID(s)
    ConceptSchema tanqueAguaSchema = new ConceptSchema(TANQUEAGUA);
    add(tanqueAguaSchema, paqueteSid.TanqueAgua.class);
    ConceptSchema rioSchema = new ConceptSchema(RIO);
    add(rioSchema, paqueteSid.Rio.class);
    ConceptSchema industriaSchema = new ConceptSchema(INDUSTRIA);
    add(industriaSchema, paqueteSid.Industria.class);
    ConceptSchema edarSchema = new ConceptSchema(EDAR);
    add(edarSchema, paqueteSid.EDAR.class);

    // adding Predicate(s)


    // adding fields
    massaAguaSchema.add(MASSAAGUA_TOTALNITRATOS, (TermSchema)getSchema(BasicOntology.FLOAT), 0, ObjectSchema.UNLIMITED);
    massaAguaSchema.add(MASSAAGUA_TOTALSULFATOS, (TermSchema)getSchema(BasicOntology.FLOAT), 0, ObjectSchema.UNLIMITED);
    massaAguaSchema.add(MASSAAGUA_VOLUMEN, (TermSchema)getSchema(BasicOntology.FLOAT), 0, ObjectSchema.UNLIMITED);
    massaAguaSchema.add(MASSAAGUA_SOLIDOSSUSPENSION, (TermSchema)getSchema(BasicOntology.FLOAT), 0, ObjectSchema.UNLIMITED);
    massaAguaSchema.add(MASSAAGUA_DEMANDABIOLOGICAOXIGENO, (TermSchema)getSchema(BasicOntology.FLOAT), 0, ObjectSchema.UNLIMITED);
    massaAguaSchema.add(MASSAAGUA_DEMANDAQUIMICAOXIGENO, (TermSchema)getSchema(BasicOntology.FLOAT), 0, ObjectSchema.UNLIMITED);

    // adding name mappings

    // adding inheritance

   }catch (java.lang.Exception e) {e.printStackTrace();}
  }
}
