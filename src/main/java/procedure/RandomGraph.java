package procedure;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Pair;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;
import org.neo4j.graphdb.RelationshipType;

/**
 * Created by Diana_Poghosyan on 6/13/2018.
 */
public class RandomGraph {

  @Context
  public GraphDatabaseService graphDb;

  @Context
  public Log log;

  private static final String QUERY_FIND_CYCLE_PATH = "MATCH (e:" + Constants.NODE_LABEL + ") " +
    "WHERE SIZE((e:" + Constants.NODE_LABEL + ")<-[:" + Constants.RELATIONSHIP_TYPE + "]-()) <> 0 " +
    "AND SIZE(()<-[:" + Constants.RELATIONSHIP_TYPE + "]-(e:" + Constants.NODE_LABEL + ")) <> 0 " +
    "MATCH path = (e:" + Constants.NODE_LABEL + ")<-[:" + Constants.RELATIONSHIP_TYPE + "*]-(e:" + Constants.NODE_LABEL + ") " +
    "RETURN path";

  /**
   * This procedure creates random Graph with given 'nodeNames' node names and
   * 'numberOfEdges' number of relationships.
   * Its return 'true' if there is at least one cycle path by 'cyclePath' object,
   * otherwise returns false.
   *
   * @param nodeNames     the node names
   * @param numberOfEdges the edges number
   */
  @Procedure(value = Constants.PROCEDURE_NAME, mode = Mode.WRITE)
  @Description(Constants.PROCEDURE_DESCRIPTION)
  public Stream<ProcOutput> hasCycleRandomGeneratedGraph(
    @Name("nodeNames") List<String> nodeNames,
    @Name("numberOfEdges") Long numberOfEdges )
  {
    int numberOfNodes = ( nodeNames == null || nodeNames.isEmpty() ? 0 : nodeNames.size() );

    if ( numberOfNodes == 0 || numberOfEdges <= 0 )
      throw new RuntimeException( Constants.ERROR_MSG_FOR_INCORRECT_PARAMS );

    boolean hasCycle = false;
    Path cyclePath = null;

    try (Transaction tx = graphDb.beginTx()) {

      /// generate random relationship edges' indexes
      final Set<Pair<Integer, Integer>> edges = new HashSet<>();
      final Random random = new Random();
      while ( edges.size() < numberOfEdges ) {
        int start = random.nextInt( numberOfNodes );
        int end = random.nextInt( numberOfNodes );

        if ( start == end )
          continue;

        edges.add( Pair.of( start, end ) );
      }

      /// create nodes
      List<Node> nodes = nodeNames.stream().map( nodeName -> {
        Node node = graphDb.createNode( Label.label( Constants.NODE_LABEL ) );
        node.setProperty( Constants.NODE_NAME_PROPERTY, nodeName );
        return node;
      } ).collect( Collectors.toList() );

      /// create relationships
      edges.forEach( e -> {
        Relationship relationship = nodes.get( e.first() ).createRelationshipTo(
          nodes.get( e.other() ), Relationships.CONNECTED );
      } );


      /// find cycle
      Result result = graphDb.execute( QUERY_FIND_CYCLE_PATH );
      if ( result.hasNext() ) {
        cyclePath = (Path)result.next().get( "path" );
        if ( cyclePath != null ) {
          hasCycle = true;
          log.info( cyclePath.toString() );
        }
      }

      tx.success();
    } catch( Exception e ) {
      log.error( e.getMessage() );
      throw new RuntimeException( e );
    }

    return Stream.of( new ProcOutput( hasCycle, cyclePath ) );
  }

  private static enum Relationships implements RelationshipType
  {
    CONNECTED( Constants.RELATIONSHIP_TYPE );

    private String value;

    Relationships( String connected )
    {
      this.value = connected;
    }

    public String getValue()
    {
      return value;
    }

    public void setValue( String value )
    {
      this.value = value;
    }
  }

  public static class ProcOutput
  {
    public boolean hasCycle;
    public Path cyclePath;

    public ProcOutput() { }

    public ProcOutput( Boolean hasCycle, Path cyclePath )
    {
      this.hasCycle = hasCycle;
      this.cyclePath = cyclePath;
    }
  }

}
