package stu.thetinkerj.kg.queryRewriting.conceptExtract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.clarkparsia.modularity.IncrementalClassifier;
import com.clarkparsia.modularity.ModularityUtils;
import com.clarkparsia.owlapiv3.OWL;
import com.google.common.base.Optional;

import org.apache.jena.graph.Node;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.mindswap.pellet.jena.PelletReasonerFactory;
import org.mindswap.pellet.taxonomy.TaxonomyNode;

import org.semanticweb.owlapi.model.OWLDataFactory;
import org.apache.jena.ontology.OntModelSpec;



import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

/**
 * Created by jianglili on 2016/3/24.
 */
public class ConceptUtil {

    // Create an OWLAPI manager that allows to load an ontology
    private static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

    private static OWLOntology ontology;
    private static Model model;

    // OntModelSpec o ;
    
    private static ConceptUtil singleton = new ConceptUtil(); // singleton pattern
    private OWLOntology moduleOnt;
    private IncrementalClassifier classifier;

    // the fisrt method to initial variable in class
    public boolean generateReasoning(/* OpProject opProject */ Concept concept, Boolean extractFlag)
            throws OWLOntologyCreationException, OWLOntologyStorageException, FileNotFoundException {
        // reasoningModule(concept.getEntities());
        // reasoning mode

        reasoningOntology(ontology, extractFlag);
        if (concept.getModel() == null)
            concept.setModel(model);
        return true;

    }

    public static ConceptUtil getSingleton(Concept concept) {

        ontology = concept.getOntology();
        return singleton;
    }

    public static OWLOntology getSchema(String schemafile) throws OWLOntologyCreationException {

        // Load the schema file
        ontology = manager.loadOntology(IRI.create(new File(schemafile)));

        // manager.createOntology(ontologyIRI, ontologies)

        Set<OWLAxiom> owlTAxioms = ontology.getTBoxAxioms(Imports.fromBoolean(true));
        Set<OWLAxiom> owlRAxioms = ontology.getRBoxAxioms(Imports.fromBoolean(true));

        owlRAxioms.addAll(owlTAxioms);

        IRI ontoIRI = ontology.getOntologyID().getOntologyIRI().get();

        manager.removeOntology(ontology.getOntologyID());
        ontology=manager.createOntology(owlRAxioms,ontoIRI);

        return   ontology;
    }



    public  void reasoningModule( Set<OWLEntity> owlEntities) throws OWLOntologyCreationException {


        ModuleType moduleType = ModuleType.BOT;
        // Extract the module axioms for the specifieontod signature
        Set<OWLAxiom> moduleAxioms=
                ModularityUtils.extractModule(ontology, owlEntities, moduleType);
        // Create an ontology for the module axioms
        moduleOnt = manager.createOntology(moduleAxioms);
        //            PelletReasoner reasoner = PelletReasonerFactory.getInstance().createReasoner( moduleOnt );
        //            reasoner.getKB().classify();
        classifier = new IncrementalClassifier(  moduleOnt );
        classifier.classify();

    }
    public  void reasoningModuleIncremental( Set<OWLEntity> owlEntities) throws OWLOntologyCreationException {

        ModuleType moduleType = ModuleType.BOT;
        // Extract the module axioms for the specifieontod signature
        Set<OWLAxiom> moduleAxioms=
                ModularityUtils.extractModule(ontology, owlEntities, moduleType);
        // Create an ontology for the module axioms
//******************************************************************************************************************** */
        // manager.applyChanges(manager.addAxioms(moduleOnt, moduleAxioms));
        
        manager.addAxiom(moduleOnt, (OWLAxiom) moduleAxioms);
        

//********************************************************************************************************************** */
        // OWLOntologyChange

        //            PelletReasoner reasoner = PelletReasonerFactory.getInstance().createReasoner( moduleOnt );
        //            reasoner.getKB().classify();
        classifier = new IncrementalClassifier(  moduleOnt );
        classifier.classify();

    }

    /**
     *
     * @param ontology
     * @param type  which means  get the all reasoning graph or only the small and short graph
     * @throws OWLOntologyCreationException
     * @throws OWLOntologyStorageException
     * 实体推理
     *      
     * 
     *      
     */
    public  void reasoningOntology(OWLOntology ontology,Boolean type) 
            throws OWLOntologyCreationException, OWLOntologyStorageException, FileNotFoundException {

        if(classifier==null) {
            classifier = new IncrementalClassifier(ontology);
            classifier.classify();
            if(type) {
                
                Collection<TaxonomyNode<OWLClass>> classes =classifier.getTaxonomy().getTop().getSubs();
                for(TaxonomyNode<OWLClass> owlClass: classes){
                    
                    OWLAxiom axiom = new OWLSubClassOfAxiomImpl(owlClass.getName(), OWL.Thing, new TreeSet<OWLAnnotation>());
                    manager.addAxiom(ontology,axiom); //一种为本体添加单个公理的便捷方法
                }
                manager.saveOntology(ontology, IRI.create(new File("temp.owl").toURI()));
                
                //adjust the the type

                //创建一个本体实例
                OntModel reasoningModel = ModelFactory.createOntologyModel(/*PelletReasonerFactory.THE_SPEC*/OntModelSpec.OWL_MEM );
                
                //加载本体文件
                reasoningModel.read( "temp.owl" );

                reasoningModel.writeAll(new PrintStream(new File("temp2.owl")), "RDF/XML", null);

                // reasoningModel.write(new PrintStream(new File("temp2.owl")), "RDF/XML");

                model = RDFDataMgr.loadModel("temp2.owl");
                System.out.println();

            }


        }
    }
    //judge if the node object in a triple is class
    public  boolean IsOWLClass(Node object)
    {
        return ontology.containsClassInSignature(IRI.create(object.toString()));
    }

    //judge if the node predict  in a triple is DataProperties
    public  boolean IsObjectProperty(Node predict)
    {
        return ontology.containsObjectPropertyInSignature(IRI.create(predict.toString()));
    }
    //judge if the node predict in a tripleis ObjectProperties
    public   boolean IsDataProperty(Node predict)
    {
        return ontology.containsClassInSignature(IRI.create(predict.toString()));
    }

    /**
     * get the equivelent classes from classifier by class
     * @param owlClass
     * @return
     */

    public  Set<OWLClass> getEquivalentClasses(OWLClass owlClass)
    {
        Set<OWLClass>  owlClasses=classifier.getEquivalentClasses((OWLClassExpression)owlClass).getEntities();
        owlClasses.remove(owlClass);
        return  owlClasses;


    }

    /**
     * get the object subclasses from classifier by class
     * @param owlClass
     * @return
     */

    public  Set<OWLClass> getSubClasses(OWLClass owlClass)
    {
        // change parameter
           Set<OWLClass>  subClasses= classifier.getSubClasses((OWLClassExpression) owlClass, /*change*/true).getFlattened();
          Set<OWLClass>  equivalentNothing=classifier.getEquivalentClasses((OWLClassExpression)OWL.Nothing).getEntities();
          subClasses.removeAll(equivalentNothing);
          return   subClasses;

    }
    /**
     * get the object Superclasses from classifier by class
     * @param owlClass
     * @return
     */

    public  Set<OWLClass> getSuperClasses(OWLClass owlClass)
    {
        Set<OWLClass>  superClasses= classifier.getSuperClasses((OWLClassExpression) owlClass, true).getFlattened();
        Set<OWLClass>  equivalentNothing=classifier.getEquivalentClasses((OWLClassExpression)OWL.Nothing).getEntities();
        superClasses.removeAll(equivalentNothing);
        return   superClasses;

    }

    /**
     * get the object equivalent properties from classifier by object properties
     * @param owlObjectProperty
     * @return equivalentObjectProperties
     */
    public  Set<OWLObjectProperty> getEquivalentObjectProperties(OWLObjectProperty owlObjectProperty)
    {
        Set<OWLObjectProperty> equivalentObjectProperties=new HashSet<OWLObjectProperty>();
        for(OWLObjectPropertyExpression propertyExpression :
                classifier.getEquivalentObjectProperties((OWLObjectPropertyExpression) owlObjectProperty).getEntities() )
        {
            if(!propertyExpression.equals(owlObjectProperty))
                equivalentObjectProperties.add((OWLObjectProperty)propertyExpression);
        }
        return equivalentObjectProperties;
    }

    /**
     * get the object sub properties from classifier by object properties
     * @param owlObjectProperty
     * @return subObjectProperties
     */
    public  Set<OWLObjectProperty> getSubObjectProperties(OWLObjectProperty owlObjectProperty)
    {
        Set<OWLObjectProperty> subObjectProperties=new HashSet<OWLObjectProperty>();
        for(OWLObjectPropertyExpression propertyExpression :
                classifier.getSubObjectProperties((OWLObjectPropertyExpression) owlObjectProperty, true)
                        .getFlattened())
        {

              subObjectProperties.add((OWLObjectProperty)propertyExpression);
        }
        subObjectProperties.remove(OWL.bottomObjectProperty);
        return subObjectProperties;
    }
    /**
     * get the data equivalent properties from classifier by object properties
     * @param owlDataProperty
     * @return equivalentDataProperties
     */
    public  Set<OWLDataProperty> getEquivalentDataProperties(OWLDataProperty owlDataProperty)
    {
        Set<OWLDataProperty>  owlDataProperties=classifier.getEquivalentDataProperties((OWLDataProperty)owlDataProperty).getEntities();
        owlDataProperties.remove(owlDataProperty);

        return owlDataProperties;
    }

    /**
     * get the object sub properties from classifier by object properties
     * @param owldataProperty
     * @return subObjectProperties
     */
    public  Set<OWLDataProperty> getSubDataProperties(OWLDataProperty owldataProperty)    {
        Set<OWLDataProperty>  owlDataProperties=classifier.getSubDataProperties((OWLDataProperty) owldataProperty, true).getFlattened();
        owlDataProperties.remove(OWL.bottomObjectProperty);
        return owlDataProperties;
    }


    /**
     * get the role chain
     * @param owlObjectProperty
     * @return
     */
    public  Set<List<OWLObjectPropertyExpression>> getRoleObjectProperties(OWLObjectProperty owlObjectProperty)    {
        Set<List<OWLObjectPropertyExpression>>  roleChainSet=new HashSet<List<OWLObjectPropertyExpression>>();
        List<OWLObjectPropertyExpression> owlObjectPropertyExpressions;
        Set<OWLSubPropertyChainOfAxiom> axioms = ontology
                .getAxioms(AxiomType.SUB_PROPERTY_CHAIN_OF);
        for (OWLSubPropertyChainOfAxiom axiom : axioms) {
             if(axiom.getSuperProperty().equals(owlObjectProperty)) {
                 roleChainSet.add(axiom.getPropertyChain());
             }
        }
        return roleChainSet;

    }

    /**
     * get the equivelent classes from ontology by joins   A = B & C & D
     * @param owlClass
     * @return
     */

    public   Set<Set<OWLClass>> getEquivalentJoinClasses(OWLClass owlClass)
    {

       Set<Set<OWLClass>>   equivalentJojns=new HashSet<Set<OWLClass>>();
        for(OWLEquivalentClassesAxiom owlEquivalentClassesAxiom: ontology.getEquivalentClassesAxioms(owlClass)){
            for(OWLClassExpression owlClassExpression:owlEquivalentClassesAxiom.getClassExpressions()){
                //judge if is a join equivalent classes
               if(owlClassExpression instanceof OWLObjectIntersectionOfImpl){
                   Set<OWLClassExpression>  equivalentJoin=((OWLObjectIntersectionOfImpl) owlClassExpression)
                           .getOperands();
                   Set<OWLClass>  equivalentJoin1=new HashSet<>();
                   //interator the join  equivalent classes
                   for(OWLClassExpression owlClassExpression1: equivalentJoin) {
                       if (!(owlClassExpression1 instanceof OWLClassImpl))
                              break;
                       equivalentJoin1.add((OWLClassImpl)owlClassExpression1);
                   }
                   equivalentJojns.add(equivalentJoin1);
                   //just return only onr join equivalent classes

               }
            }

        }
        return equivalentJojns;
    }

    /**
     * get the inverse properties
     * @param owlObjectProperty
     * @return
     */

    public   Set<OWLObjectProperty> getInverseProperties(OWLObjectProperty owlObjectProperty){
        Set<OWLObjectProperty> objectProperties=new HashSet<OWLObjectProperty>();
        for(OWLObjectPropertyExpression inverseproperties: classifier.getInverseObjectProperties(owlObjectProperty)) {

                objectProperties.add((OWLObjectProperty) inverseproperties);
                for(OWLObjectPropertyExpression subObjectProperty:classifier.getSubObjectProperties(inverseproperties,true).getFlattened())
                       objectProperties.add((OWLObjectProperty)subObjectProperty);
        }
        return objectProperties;


    }



}
