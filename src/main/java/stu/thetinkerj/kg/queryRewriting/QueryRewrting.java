package stu.thetinkerj.kg.queryRewriting;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.sparql.algebra.Op;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import stu.thetinkerj.kg.queryRewriting.assertionReplace.AlgebraTransformer;
import stu.thetinkerj.kg.queryRewriting.conceptExtract.*;

/**
 * Created by jianglili on 2016/3/23.
 */
public class QueryRewrting {
    

    public static Concept initSchema( String schemafile, int tag)
            throws OWLOntologyCreationException,
                   OWLOntologyStorageException,
                   FileNotFoundException {
                     
        OWLOntology ontology= ConceptUtil.getSchema(schemafile);
        //get all concrete and (class or properties)
        Concept concept=new Concept();
        // prepare all the reasoning and schema gaph work
        concept.setOntology(ontology);
        //Transform each Op (from old op to new op)
      return concept;   
    }

    public static Op transform( Op op, Concept concept,int limitation)
            throws OWLOntologyCreationException,
                   OWLOntologyStorageException,
                   FileNotFoundException {
        
        return new AlgebraTransformer(concept,true,limitation).BothTransform(op);
    }
    

    // test code 
    public static void main(String[] args)
      throws OWLOntologyCreationException, OWLOntologyStorageException, FileNotFoundException {
      QueryRewrting  qr = new QueryRewrting();
      // String schemafile = "/home/thetinkerj/Repo_nlsde_gitlab/Report_CodeSpace/Query/EM/figureoutOWL/dbpedia.ttl";
      String schemafile = "/home/thetinkerj/Repo_nlsde_gitlab/Report_CodeSpace/Query/EM/figureoutOWL/simple-galen.owl";
      
      Concept concept =  QueryRewrting.initSchema(schemafile, 1);
      ConceptUtil singleton = ConceptUtil.getSingleton(concept);
      singleton.generateReasoning(concept, true);

      //解析发现问题

      // RioParserImpl 

    }

    //只有跑实验的时候才用到了这个函数
    // public static List<Op> exe( List<Op> ops, String schemafile, int tag,int limitation)
    //         throws OWLOntologyCreationException, OWLOntologyStorageException, FileNotFoundException {
    //     OWLOntology ontology= ConceptUtil.getSchema(schemafile);
    //     //get all concrete and (class or properties)
    //     Concept concept=new Concept();
    //     // prepare all the reasoning and schema gaph work
    //     concept.setOntology(ontology);
    //     //Transform each Op (from old op to new op)
    //     List<Op> newOpList=new ArrayList<>();
    //     for(Op op:ops) {
    //        System.out.println(op);
    //         newOpList.add(transform(op, concept,limitation));
    //     }
    //     return newOpList;
    // }

    // public static Op transformDBpedia( Op op, Concept concept,Boolean extractFlag,int limitation)
    //         throws OWLOntologyCreationException, OWLOntologyStorageException, FileNotFoundException {
    //     return new AlgebraTransformer(concept,extractFlag,limitation).transform(op);
    // }

}
