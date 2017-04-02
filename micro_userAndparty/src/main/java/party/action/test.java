package party.action;

import org.neo4j.graphdb.RelationshipType;

public class test {
	
	public static enum RelTypes implements RelationshipType {  
        NEO_NODE,  
        KNOWS,  
        CODED_BY,
        CONTAIN
    }

}
