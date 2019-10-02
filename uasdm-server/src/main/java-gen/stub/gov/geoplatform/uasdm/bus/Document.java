package gov.geoplatform.uasdm.bus;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

public class Document extends DocumentBase
{
  private static final long serialVersionUID = -877956259;

  public Document()
  {
    super();
  }

  @Override
  protected String buildKey()
  {
    return this.getS3location();
  }

  public static Document createIfNotExist(UasComponent uasComponent, String key, String name)
  {
    Document document = Document.find(key);

    if (document == null)
    {
      document = new Document();
      document.setComponent(uasComponent);
      document.setS3location(key);
      document.setName(name);
      document.apply();
    }

    return document;
  }

  private static Document find(String key)
  {
    DocumentQuery query = new DocumentQuery(new QueryFactory());
    query.WHERE(query.getS3location().EQ(key));

    try (OIterator<? extends Document> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    return null;
  }

}
