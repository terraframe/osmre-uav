package gov.geoplatform.uasdm.bus;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

public class AbstractUploadTask extends AbstractUploadTaskBase
{
  public static final long serialVersionUID = 1447500757;

  public AbstractUploadTask()
  {
    super();
  }

  public static AbstractUploadTask getTaskByUploadId(String uploadId)
  {
    AbstractUploadTaskQuery query = new AbstractUploadTaskQuery(new QueryFactory());
    query.WHERE(query.getUploadId().EQ(uploadId));

    try (OIterator<? extends AbstractUploadTask> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    return null;
  }

}
