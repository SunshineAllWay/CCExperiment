package org.apache.lucene.queryParser.core.nodes;
import java.util.Arrays;
import org.apache.lucene.util.LuceneTestCase;
public class TestQueryNode extends LuceneTestCase {
  public void testAddChildren() throws Exception {
    FieldQueryNode nodeA = new FieldQueryNode("foo", "A", 0, 1);
    FieldQueryNode nodeB = new FieldQueryNode("foo", "B", 1, 2);
    BooleanQueryNode bq = new BooleanQueryNode(
        Arrays.asList(new QueryNode[] { nodeA }));
    bq.add(Arrays.asList(new QueryNode[] { nodeB }));
    assertEquals(2, bq.getChildren().size());
  }
}
