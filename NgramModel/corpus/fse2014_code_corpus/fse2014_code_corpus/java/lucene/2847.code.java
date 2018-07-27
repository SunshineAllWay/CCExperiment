package org.apache.solr.search;
import org.apache.solr.util.AbstractSolrTestCase;
public class TestExtendedDismaxParser extends AbstractSolrTestCase {
  public String getSchemaFile() { return "schema12.xml"; }
  public String getSolrConfigFile() { return "solrconfig.xml"; }
  public void setUp() throws Exception {
    super.setUp();
  }
  public void tearDown() throws Exception {
    super.tearDown();
  }
  public void testFocusQueryParser() {
    assertU(adoc("id", "42", "trait_ss", "Tool", "trait_ss", "Obnoxious",
            "name", "Zapp Brannigan"));
    assertU(adoc("id", "43" ,
            "title", "Democratic Order op Planets"));
    assertU(adoc("id", "44", "trait_ss", "Tool",
            "name", "The Zapper"));
    assertU(adoc("id", "45", "trait_ss", "Chauvinist",
            "title", "25 star General"));
    assertU(adoc("id", "46", "trait_ss", "Obnoxious",
            "subject", "Defeated the pacifists op the Gandhi nebula"));
    assertU(adoc("id", "47", "trait_ss", "Pig",
            "text", "line up and fly directly at the enemy death cannons, clogging them with wreckage!"));
    assertU(adoc("id", "48", "text_sw", "this has gigabyte potential", "foo_i","100"));
    assertU(adoc("id", "49", "text_sw", "start the big apple end", "foo_i","-100"));
    assertU(adoc("id", "50", "text_sw", "start new big city end"));
    assertU(commit());
    String allq = "id:[42 TO 50]";
    String allr = "*[count(//doc)=9]";
    String oner = "*[count(//doc)=1]";
    String twor = "*[count(//doc)=2]";
    String nor = "*[count(//doc)=0]";
    assertQ("standard request handler returns all matches",
            req(allq),
            allr
    );
   assertQ("edismax query parser returns all matches",
            req("q", allq,
                "defType", "edismax"
            ),
            allr
    );
   assertQ(req("defType", "edismax", "qf", "trait_ss",
               "q","Tool"), twor
    );
   assertQ(req("defType", "edismax", "qf", "trait_ss foo_i foo_f foo_dt foo_l foo_d foo_b",
               "q","Tool"), twor
    );
   assertQ(req("defType", "edismax", "qf", "text_sw",
               "q","foo_i:100"), oner
    );
   assertQ(req("defType", "edismax", "qf", "text_sw",
               "q","foo_i:-100"), oner
    );
   assertQ(req("defType", "edismax", "qf", "text_sw foo_i",
               "q","100"), oner
    );
   assertQ("qf defaults to defaultSearchField"
           , req( "defType", "edismax"
                 ,"q","op")
           , twor
           );
   assertQ(req("defType", "edismax", "qf", "name title subject text",
               "q","op"), twor
    );
   assertQ(req("defType", "edismax", "qf", "name title subject text",
               "q","Order op"), oner
    );
   assertQ(req("defType", "edismax", "qf", "name title subject text",
               "q","Order AND op"), oner
    );
   assertQ(req("defType", "edismax", "qf", "name title subject text",
               "q","Order and op"), oner
    );
    assertQ(req("defType", "edismax", "qf", "name title subject text",
               "q","+Order op"), oner
    );
    assertQ(req("defType", "edismax", "qf", "name title subject text",
               "q","Order OR op"), twor
    );
    assertQ(req("defType", "edismax", "qf", "name title subject text",
               "q","Order or op"), twor
    );
    assertQ(req("defType", "edismax", "qf", "name title subject text",
               "q","*:*"), allr
    );
    assertQ(req("defType", "edismax", "qf", "name title subject text",
           "q","star OR (-star)"), allr
    );
    assertQ(req("defType", "edismax", "qf", "name title subject text",
           "q","id:42 OR (-id:42)"), allr
    );
    assertQ(req("defType", "edismax", "qf", "text_sw",
           "q","GB"), oner
    );
    assertQ(req("defType", "edismax", "qf", "text_sw",
           "q","the big"), twor
    );
    assertQ(req("defType", "edismax", "qf", "text_sw", "stopwords","false",
           "q","the big"), oner
    );
  }
}
