package gov.geoplatform.uasdm.bus;

@com.runwaysdk.business.ClassSignature(hash = -2011790463)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to WorkflowAction.java
 *
 * @author Autogenerated by RunwaySDK
 */
public class WorkflowActionQueryDTO extends com.runwaysdk.business.BusinessQueryDTO
{
private static final long serialVersionUID = -2011790463;

  protected WorkflowActionQueryDTO(String type)
  {
    super(type);
  }

@SuppressWarnings("unchecked")
public java.util.List<? extends gov.geoplatform.uasdm.bus.WorkflowActionDTO> getResultSet()
{
  return (java.util.List<? extends gov.geoplatform.uasdm.bus.WorkflowActionDTO>)super.getResultSet();
}
}