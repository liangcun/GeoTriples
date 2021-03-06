package eu.linkedeodata.geotriples;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.d2rq.algebra.TripleRelation;
import org.d2rq.db.op.DatabaseOp;
import org.d2rq.db.op.LimitOp;
import org.d2rq.db.op.util.OpUtil;
import org.d2rq.find.TripleQueryIter;
import org.d2rq.nodes.FixedNodeMaker;
import org.d2rq.nodes.NodeMaker;
import org.d2rq.vocab.SKOS;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * A collection of IRI-identified resources that are generated by a shared
 * rule (like a d2rq:ClassMap or rr:SubjectMap). The class provides access
 * to descriptions of the collection.
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class GeneralResourceCollection {
	private static final Logger log = LoggerFactory.getLogger(GeneralResourceCollection.class);
	private static final Var RESOURCE = Var.alloc("resource");
	
	private final GeneralCompiledMapping mapping;
	private final GeneralConnection connection;
	private final NodeMaker entityMaker;
	private final DatabaseOp entityTable;
	private final GeneralNodeRelation entityNodeRelation;
	private final Collection<GeneralTripleRelation> inventory;

	public GeneralResourceCollection(GeneralCompiledMapping mapping, GeneralConnection connection,
			NodeMaker entityMaker, 
			DatabaseOp entityTable, Collection<GeneralTripleRelation> entityDescription) {
		this.mapping = mapping;
		this.connection = connection;
		this.entityMaker = entityMaker;
		this.entityTable = entityTable;
		this.entityNodeRelation = new GeneralNodeRelation(connection,
				entityTable, Collections.singletonMap(RESOURCE, entityMaker));
		this.inventory = filterTripleRelations(entityDescription);
	}
	
	public Model getInventoryModel() {
		return getInventoryModel(LimitOp.NO_LIMIT);
	}

	public Model getInventoryModel(int limit) {
		log.info("Listing entity set: " + entityMaker);
		Model result = ModelFactory.createDefaultModel();
		result.setNsPrefixes(mapping.getPrefixes());
		GeneralFindQuery query = new GeneralFindQuery(Triple.ANY, inventory, limit, 
				new ExecutionContext(mapping.getContext(), null, null, null));
		Iterator<Triple> it = TripleQueryIter.create(query.iterator());
		while (it.hasNext()) {
			result.getGraph().add(it.next());
		}
		return result;
	}

	public boolean mayContain(Node node) {
		return !OpUtil.isEmpty(GeneralNodeRelationUtil.select(entityNodeRelation, RESOURCE, node).getBaseTabular());
	}

	private Collection<GeneralTripleRelation> filterTripleRelations(Collection<GeneralTripleRelation> entityDescription) {
		List<GeneralTripleRelation> result = new ArrayList<GeneralTripleRelation>();
		for (GeneralTripleRelation triples: entityDescription) {
			triples = triples.orderBy(TripleRelation.SUBJECT, true);
			if (triples.selectTriple(new Triple(Node.ANY, RDF.Nodes.type, Node.ANY)) != null) {
				result.add(triples);
			}
			// TODO: The list of label properties is redundantly specified in PageServlet
			if (triples.selectTriple(new Triple(Node.ANY, RDFS.label.asNode(), Node.ANY)) != null) {
				result.add(triples);
			} else if (triples.selectTriple(new Triple(Node.ANY, SKOS.prefLabel.asNode(), Node.ANY)) != null) {
				result.add(triples);
			} else if (triples.selectTriple(new Triple(Node.ANY, DC.title.asNode(), Node.ANY)) != null) {
				result.add(triples);					
			} else if (triples.selectTriple(new Triple(Node.ANY, DCTerms.title.asNode(), Node.ANY)) != null) {
				result.add(triples);					
			} else if (triples.selectTriple(new Triple(Node.ANY, FOAF.name.asNode(), Node.ANY)) != null) {
				result.add(triples);					
			}
		}
		if (result.isEmpty()) {
			result.add(new GeneralTripleRelation(connection, entityTable, 
					entityMaker, 
					new FixedNodeMaker(RDF.type.asNode()), 
					new FixedNodeMaker(RDFS.Resource.asNode())));
		}
		return result;
	}
}
