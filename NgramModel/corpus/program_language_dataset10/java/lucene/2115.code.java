package org.apache.solr.handler.dataimport;
import org.junit.Assert;
import org.junit.Test;
import java.text.SimpleDateFormat;
import java.util.*;
public class TestDateFormatTransformer {
  @Test
  @SuppressWarnings("unchecked")
  public void testTransformRow_SingleRow() throws Exception {
    List fields = new ArrayList();
    fields.add(AbstractDataImportHandlerTest.createMap(DataImporter.COLUMN,
            "lastModified"));
    fields.add(AbstractDataImportHandlerTest.createMap(DataImporter.COLUMN,
            "dateAdded", RegexTransformer.SRC_COL_NAME, "lastModified",
            DateFormatTransformer.DATE_TIME_FMT, "MM/dd/yyyy"));
    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
    Date now = format.parse(format.format(new Date()));
    Map row = AbstractDataImportHandlerTest.createMap("lastModified", format
            .format(now));
    VariableResolverImpl resolver = new VariableResolverImpl();
    resolver.addNamespace("e", row);
    Context context = AbstractDataImportHandlerTest.getContext(null, resolver,
            null, Context.FULL_DUMP, fields, null);
    new DateFormatTransformer().transformRow(row, context);
    Assert.assertEquals(now, row.get("dateAdded"));
  }
  @Test
  @SuppressWarnings("unchecked")
  public void testTransformRow_MultipleRows() throws Exception {
    List fields = new ArrayList();
    fields.add(AbstractDataImportHandlerTest.createMap(DataImporter.COLUMN,
            "lastModified"));
    fields.add(AbstractDataImportHandlerTest.createMap(DataImporter.COLUMN,
            "dateAdded", RegexTransformer.SRC_COL_NAME, "lastModified",
            DateFormatTransformer.DATE_TIME_FMT, "MM/dd/yyyy hh:mm:ss.SSS"));
    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss.SSS");
    Date now1 = format.parse(format.format(new Date()));
    Date now2 = format.parse(format.format(new Date()));
    Map row = new HashMap();
    List list = new ArrayList();
    list.add(format.format(now1));
    list.add(format.format(now2));
    row.put("lastModified", list);
    VariableResolverImpl resolver = new VariableResolverImpl();
    resolver.addNamespace("e", row);
    Context context = AbstractDataImportHandlerTest.getContext(null, resolver,
            null, Context.FULL_DUMP, fields, null);
    new DateFormatTransformer().transformRow(row, context);
    List output = new ArrayList();
    output.add(now1);
    output.add(now2);
    Assert.assertEquals(output, row.get("dateAdded"));
  }
}
