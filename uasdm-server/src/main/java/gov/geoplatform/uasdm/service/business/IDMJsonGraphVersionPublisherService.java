package gov.geoplatform.uasdm.service.business;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.system.metadata.MdVertex;

import gov.geoplatform.uasdm.service.IndexService;
import net.geoprism.registry.service.request.JsonGraphVersionPublisherService;
import net.geoprism.registry.service.request.JsonGraphVersionPublisherServiceIF;

@Service
@Primary
public class IDMJsonGraphVersionPublisherService extends JsonGraphVersionPublisherService implements JsonGraphVersionPublisherServiceIF
{
  
  @Override
  protected VertexObject publish(State state, MdVertex mdVertex, GeoObject geoObject)
  {
    VertexObject object = super.publish(state, mdVertex, geoObject);
    
    IndexService.createDocument(state.synchronization, object);

    return object;
  }
}
