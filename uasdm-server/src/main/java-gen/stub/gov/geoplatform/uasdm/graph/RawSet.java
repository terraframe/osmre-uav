package gov.geoplatform.uasdm.graph;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;

import gov.geoplatform.uasdm.AppProperties;
import gov.geoplatform.uasdm.GenericException;
import gov.geoplatform.uasdm.bus.InvalidUasComponentNameException;
import gov.geoplatform.uasdm.model.DocumentIF;
import gov.geoplatform.uasdm.model.EdgeType;
import gov.geoplatform.uasdm.model.UasComponentIF;
import gov.geoplatform.uasdm.processing.report.CollectionReportFacade;
import gov.geoplatform.uasdm.remote.RemoteFileFacade;

public class RawSet extends RawSetBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1976921284;

  public RawSet()
  {
    super();
  }

  public boolean isPublished()
  {
    return this.getPublished() != null && this.getPublished();
  }

  public boolean isLocked()
  {
    return this.getLocked() != null && this.getLocked();
  }

  public UasComponent getComponent()
  {
    final List<UasComponent> parents = this.getParents(EdgeType.COMPONENT_HAS_RAW_SET, UasComponent.class);

    return parents.get(0);
  }

  public List<DocumentIF> getDocuments()
  {
    return this.getChildren(EdgeType.RAW_SET_HAS_DOCUMENT, DocumentIF.class);
  }
  
  @Transaction
  public void apply(UasComponentIF component)
  {
    final boolean isNew = this.isNew();

    if (!UasComponentIF.isValidName(this.getName()))
    {
      MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(RawSet.CLASS);
      MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(RawSet.NAME);

      InvalidUasComponentNameException ex = new InvalidUasComponentNameException("The product name [" + this.getName() + "] has an invalid character. Disallowed characters are " + UasComponentIF.DISALLOWED_FILENAME_REGEX);
      ex.setAttributeName(mdAttribute.getDisplayLabel(Session.getCurrentLocale()));
      throw ex;
    }

    this.apply();

    if (isNew)
    {
      this.addParent((UasComponent) component, EdgeType.COMPONENT_HAS_RAW_SET).apply();
    }
  }


  @Transaction
  public void toggleLock()
  {
    SessionIF session = Session.getCurrentSession();

    if (session != null)
    {
      Map<String, String> roles = session.getUserRoles();

      SingleActorDAOIF user = session.getUser();

      String ownerOid = this.getComponent().getOwnerOid();

      if (user.getOid().equals(ownerOid) || roles.containsKey("geoprism.admin.Administrator"))
      {
        this.setLocked(!this.isLocked());
        this.setLockedById(user.getOid());
        this.apply();

        return;
      }
    }

    GenericException ex = new GenericException();
    ex.setUserMessage("Only the owner can lock a product");
    throw ex;
  }

  @Transaction
  public void togglePublished()
  {
    if (this.isLocked())
    {
      GenericException ex = new GenericException();
      ex.setUserMessage("The product has been locked and can not be changed.");
      throw ex;
    }

    UasComponent component = this.getComponent();

    if (component.isPrivate() && !this.isPublished())
    {
      GenericException ex = new GenericException();
      ex.setUserMessage("Private collections can not be published");
      throw ex;
    }

    try
    {
      this.setPublished(!this.isPublished());
      this.apply();

      List<DocumentIF> documents = this.getDocuments();

      for (DocumentIF document : documents)
      {
        if (this.isPublished())
        {
          // Add to public S3 bucket
          RemoteFileFacade.copyObject(document.getS3location(), AppProperties.getBucketName(), document.getS3location(), AppProperties.getPublicBucketName());

          // Copy the thumbnail over to the public bucket
          String ext = FilenameUtils.getExtension(document.getName());

          if (ext.toUpperCase().equals("PNG"))
          {
            String rootPath = FilenameUtils.getPath(document.getS3location());
            String baseName = FilenameUtils.getBaseName(document.getName());

            RemoteFileFacade.copyObject(rootPath + "thumbnails/" + baseName + ".png", AppProperties.getBucketName(), rootPath + "thumbnails/" + baseName + ".png", AppProperties.getPublicBucketName());
          }

        }
        else
        {
          // Remove from public S3 bucket
          RemoteFileFacade.deleteObject(document.getS3location(), AppProperties.getPublicBucketName());

          String ext = FilenameUtils.getExtension(document.getName());

          if (ext.toUpperCase().equals("PNG"))
          {
            String rootPath = FilenameUtils.getPath(document.getS3location());
            String baseName = FilenameUtils.getBaseName(document.getName());

            RemoteFileFacade.deleteObject(rootPath + "thumbnails/" + baseName + ".png", AppProperties.getPublicBucketName());
          }
        }
      }
    }
    finally
    {
    }
  }

  /**
   * WARNING This method does not create an associated metadata with the created
   * product. All products must have metadata associated with them. You probably
   * want to use component.createProductIfNotExist instead.
   * 
   * @param uasComponent
   * @param productName
   * @return
   */
  public static RawSet createIfNotExist(UasComponentIF uasComponent, String setName)
  {
    RawSet set = find(uasComponent, setName).orElseGet(() -> {
      RawSet newInstance = new RawSet();
      newInstance.setName(setName);
      newInstance.setPublished(false);      
      
      return newInstance;
    });

    set.setLastUpdateDate(new Date());
    set.apply(uasComponent);

    return set;
  }

  public static Optional<RawSet> find(UasComponentIF component, String name)
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(EdgeType.COMPONENT_HAS_RAW_SET);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND( OUT('" + mdEdge.getDBClassName() + "')[name = :name])\n");
    statement.append("FROM :rid \n");

    final GraphQuery<RawSet> query = new GraphQuery<RawSet>(statement.toString());
    query.setParameter("rid", ( (UasComponent) component ).getRID());
    query.setParameter("name", name);

    return Optional.ofNullable(query.getSingleResult());
  }

}
