package procedure;

/**
 * Created by Diana_Poghosyan on 6/14/2018.
 */
final public class Constants
{
  public static final String PROCEDURE_NAME = "custom.hasCycleRandomGeneratedGraph";

  public static final String PROCEDURE_DESCRIPTION = PROCEDURE_NAME
    + "(['nodeName1','nodeName2',...], N) \n"
    + ": Procedure creates random Graph with given ['nodeName1','nodeName2',...] node names "
    + "and 'N' number of relationships. "
    + "Its return 'true' if there is at least one cycle path by 'cyclePath' object, otherwise return false.";

  public static final String ERROR_MSG_FOR_INCORRECT_PARAMS = "\nPlease correct procedure params.\n" + Constants.PROCEDURE_DESCRIPTION;


  public static final String NODE_LABEL = "NODE";
  public static final String NODE_NAME_PROPERTY = "name";
  public static final String RELATIONSHIP_TYPE = "CONNECTED";

  public static final String QUERY_COUNT_NODES = "MATCH (n:" + NODE_LABEL + ") RETURN count(n) as nodeCount";
  public static final String QUERY_COUNT_EDGES = "MATCH (n:" + NODE_LABEL + ")-[r:" + RELATIONSHIP_TYPE + "]->() RETURN COUNT(r) as edgeCount";
}
