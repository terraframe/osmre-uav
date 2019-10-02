package gov.geoplatform.uasdm.bus;

import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.uasdm.view.Option;

public class Bureau extends BureauBase
{
  private static final long  serialVersionUID = 806603996;

  public static final String OTHER            = "OTHER";

  public Bureau()
  {
    super();
  }

  public static List<Option> getOptions()
  {
    List<Option> options = new LinkedList<Option>();

    BureauQuery query = new BureauQuery(new QueryFactory());
    query.ORDER_BY_ASC(query.getDisplayLabel());

    try (OIterator<? extends Bureau> it = query.getIterator())
    {
      List<? extends Bureau> bureaus = it.getAll();

      for (Bureau bureau : bureaus)
      {
        options.add(new Option(bureau.getOid(), bureau.getDisplayLabel()));
      }

      return options;
    }
  }

}
