package gov.geoplatform.uasdm.model;

import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.dataaccess.transaction.Transaction;

public class CompositeComponent<T extends UasComponentIF>
{

  private T                             component;

  private List<ComponentWithAttributes> metadatas;

  public CompositeComponent(T component)
  {
    this.component = component;
    this.metadatas = new LinkedList<>();
  }

  public T getComponent()
  {
    return component;
  }

  public void setComponent(T component)
  {
    this.component = component;
  }

  public void addMetadata(ComponentWithAttributes metadata)
  {
    this.metadatas.add(metadata);
  }

  @Transaction
  public void apply()
  {
    this.component.apply();

    this.metadatas.forEach(metadata -> metadata.apply());
  }

  @Transaction
  public void applyWithParent(UasComponentIF parent)
  {
    this.component.applyWithParent(parent);

    this.metadatas.forEach(metadata -> metadata.apply());
  }
}
