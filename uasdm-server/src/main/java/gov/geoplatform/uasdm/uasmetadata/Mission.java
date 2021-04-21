package gov.geoplatform.uasdm.uasmetadata;

public class Mission
{
  private String name;
  
  private String description;
  
  private String contractingOffice;
  
  private String vendor;

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getContractingOffice()
  {
    return contractingOffice;
  }

  public void setContractingOffice(String contractingOffice)
  {
    this.contractingOffice = contractingOffice;
  }

  public String getVendor()
  {
    return vendor;
  }

  public void setVendor(String vendor)
  {
    this.vendor = vendor;
  }
}
