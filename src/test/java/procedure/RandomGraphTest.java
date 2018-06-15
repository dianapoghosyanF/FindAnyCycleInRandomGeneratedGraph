package procedure;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.harness.junit.Neo4jRule;

/**
 * Created by Diana_Poghosyan on 6/13/2018.
 */
public class RandomGraphTest
{

  private static final String[] NODE_NAMES = {"'node1'", "'node2'", "'node3'", "'node4'", "'node5'", "'node6'"};
  private static final int NODES_COUNT = NODE_NAMES.length;
  private static final int EDGES_COUNT = 4;

  private static final String QUERY_PROCEDURE = "CALL " + Constants.PROCEDURE_NAME + "([ %s ], %d )";

  private static final int INVALID_EDGES_COUNT = -1;
  private static final String[] INVALID_NODE_NAMES = {};

  @Rule
  public Neo4jRule neo4j = new Neo4jRule().withProcedure( RandomGraph.class );

  @Test
  public void procedureTestForCorrectGraphCreation()
  {
    try (Driver driver = GraphDatabase.driver( neo4j.boltURI(), Config.build()
      .withEncryptionLevel( Config.EncryptionLevel.NONE ).toConfig() );
         Session session = driver.session()) {

      session.run( getQueryString( NODE_NAMES, EDGES_COUNT ));

      StatementResult result1 = session.run( Constants.QUERY_COUNT_EDGES );
      StatementResult result2 = session.run( Constants.QUERY_COUNT_NODES );

      int edgeCount = result1.single().get( 0 ).asInt();
      int nodeCount = result2.single().get( 0 ).asInt();

      assertThat( nodeCount, equalTo( NODES_COUNT ) );
      assertThat( edgeCount, equalTo( EDGES_COUNT ) );
    }
  }

  @Test(expected = Exception.class)
  public void procedureTestForException_1()
  {

    try (Driver driver = GraphDatabase.driver( neo4j.boltURI(), Config.build()
      .withEncryptionLevel( Config.EncryptionLevel.NONE ).toConfig() );
         Session session = driver.session()) {

      session.run( getQueryString( NODE_NAMES, INVALID_EDGES_COUNT ) );

    }
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void procedureTestForException_2()
  {

    try (Driver driver = GraphDatabase.driver( neo4j.boltURI(), Config.build()
      .withEncryptionLevel( Config.EncryptionLevel.NONE ).toConfig() );
         Session session = driver.session()) {

      session.run( getQueryString( INVALID_NODE_NAMES, EDGES_COUNT ) );

      thrown.expect( RuntimeException.class );
      thrown.expectMessage( Constants.ERROR_MSG_FOR_INCORRECT_PARAMS );

    }
  }

  private String getQueryString( String[] nodeNames, int edgesCount ) {
    return String.format( QUERY_PROCEDURE, getFormattedNodeNamesFrom( nodeNames ), edgesCount );
  }

  private String getFormattedNodeNamesFrom( String[] names ) {
    return String.join(", ", Arrays.asList( names ));
  }
}
