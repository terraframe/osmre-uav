package gov.geoplatform.uasdm.bus;

import java.util.LinkedList;
import java.util.List;

import net.geoprism.GeoprismUser;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.SingleActor;

public class Mission extends MissionBase
{
  private static final long serialVersionUID = -112103870;
  
  public static final String ACCESSIBLE_SUPPORT = "accessible_support";
  
  public Mission()
  {
    super();
  }
  
  public Collection createChild()
  {
    return new Collection();
  }
  
  public ComponentHasComponent addComponent(UasComponent uasComponent)
  {
    return this.addProject((Project)uasComponent);
  }
  
  /**
   * Creates the object and builds the relationship with the parent.
   * 
   * Creates directory in S3.
   * 
   * @param parent
   */
  @Transaction
  @Override 
  public void applyWithParent(UasComponent parent)
  { 
    super.applyWithParent(parent);
    
    if (this.isNew())
    {      
      this.createS3Folder(this.buildAccessibleSupportKey());
    }
  }
  
  public void delete()
  { 
    super.delete();
    
    if (!this.getS3location().trim().equals(""))
    {
      this.deleteS3Folder(this.buildAccessibleSupportKey());
    }
  }
  
  private String buildAccessibleSupportKey()
  {    
    return this.getS3location()+ACCESSIBLE_SUPPORT+"/";
  }
  
  public static List<Mission> getMissingMetadata()
  {
    List<Mission> missionList = new LinkedList<Mission>();
    
    SingleActor singleActor = GeoprismUser.getCurrentUser();
    
    if (singleActor != null)
    {
      QueryFactory qf = new QueryFactory();
    
      MissionQuery mQ = new MissionQuery(qf);
      CollectionQuery cQ = new CollectionQuery(qf);
      MissionHasCollectionQuery mHasCQ = new MissionHasCollectionQuery(qf);
      WorkflowTaskQuery wQ = new WorkflowTaskQuery(qf);
      
      // Get Workflow Tasks created by the current user
      wQ.WHERE(wQ.getGeoprismUser().EQ(singleActor));

      // Get Collections associated with those tasks
      cQ.WHERE(cQ.getOid().EQ(wQ.getOid()));
      
      // Get the Missions of those Collections;    
      mQ.WHERE(mQ.collections(cQ));

      
      OIterator<? extends Mission> i = mQ.getIterator();
      
      for (Mission mission : i)
      {
        missionList.add(mission);
      }
    }
    
    return missionList;
  }
}
