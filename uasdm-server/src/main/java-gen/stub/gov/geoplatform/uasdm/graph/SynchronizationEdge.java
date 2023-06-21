package gov.geoplatform.uasdm.graph;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdEdge;

import net.geoprism.graph.LabeledPropertyGraphTypeVersion;

public class SynchronizationEdge extends SynchronizationEdgeBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -1169089664;

  public SynchronizationEdge()
  {
    super();
  }

  @Override
  public void delete()
  {
    MdEdge graphEdge = this.getGraphEdge();

    super.delete();

    if (graphEdge != null)
    {
      graphEdge.delete();
    }
  }

  public static SynchronizationEdge get(LabeledPropertyGraphTypeVersion version)
  {
    SynchronizationEdgeQuery query = new SynchronizationEdgeQuery(new QueryFactory());
    query.WHERE(query.getVersion().EQ(version));

    try (OIterator<? extends SynchronizationEdge> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    return null;
  }

}
