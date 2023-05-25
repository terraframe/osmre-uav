package gov.geoplatform.uasdm.cog;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CogPreviewParams
{
  private Integer max_size;
  
  private Integer width;
  
  private Integer height;
  
  public CogPreviewParams()
  {
    
  }
  
  public CogPreviewParams(int width, int height)
  {
    this.width = width;
    this.height = height;
  }

  public int getMax_size()
  {
    return max_size;
  }

  public void setMax_size(int max_size)
  {
    this.max_size = max_size;
  }

  public int getWidth()
  {
    return width;
  }

  public void setWidth(int width)
  {
    this.width = width;
  }

  public int getHeight()
  {
    return height;
  }

  public void setHeight(int height)
  {
    this.height = height;
  }
  
  public void addParameters(Map<String, List<String>> parameters)
  {
    if (this.height != null)
    {
      parameters.put("height", Arrays.asList(String.valueOf(height)));
    }
    
    if (this.width != null)
    {
      parameters.put("width", Arrays.asList(String.valueOf(width)));
    }
    
    if (this.max_size!= null)
    {
      parameters.put("max_size", Arrays.asList(String.valueOf(max_size)));
    }
  }
}
