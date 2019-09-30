package gov.geoplatform.uasdm.bus;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

public class AbstractUploadTask extends AbstractUploadTaskBase
{
  private static final long serialVersionUID = 1447500757;

  public AbstractUploadTask()
  {
    super();
  }

  public static AbstractUploadTask getTaskByUploadId(String uploadId)
  {
    AbstractUploadTaskQuery query = new AbstractUploadTaskQuery(new QueryFactory());
    query.WHERE(query.getUploadId().EQ(uploadId));

    OIterator<? extends AbstractUploadTask> it = query.getIterator();

    try
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }
    finally
    {
      it.close();
    }

    return null;
  }

}
