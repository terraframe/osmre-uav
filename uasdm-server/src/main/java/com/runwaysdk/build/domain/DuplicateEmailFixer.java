package com.runwaysdk.build.domain;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.runwaysdk.ComponentIF;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.MdAttributeUUIDInfo;
import com.runwaysdk.dataaccess.BusinessDAO;
import com.runwaysdk.dataaccess.BusinessDAOIF;
import com.runwaysdk.dataaccess.EntityDAO;
import com.runwaysdk.dataaccess.EntityDAOIF;
import com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.MdClassDAOIF;
import com.runwaysdk.dataaccess.MdEntityDAOIF;
import com.runwaysdk.dataaccess.MdGraphClassDAOIF;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.graph.GraphObjectDAO;
import com.runwaysdk.dataaccess.graph.GraphRequest;
import com.runwaysdk.dataaccess.metadata.MdAttributeDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.EntityQuery;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import net.geoprism.account.ExternalProfile;
import net.geoprism.account.ExternalProfileQuery;

public class DuplicateEmailFixer
{
  public static void main(String[] args)
  {
    mainInReq();
  }
  
  @Request
  public static void mainInReq()
  {
//    migrateUser("Joseph.Filkins@usda.gov", "joseph.filkins@usda.gov");
    
    setEmailLabel();
    
    migrateAllUsers();
    
    checkForDuplicates();
  }
  
  public static void setEmailLabel()
  {
    MdAttributeDAO email = (MdAttributeDAO) MdAttributeDAO.getByKey(ExternalProfile.CLASS + "." + ExternalProfile.EMAIL);
    
    email.setStructValue(MdAttributeLocalInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, "email");
    
    email.apply();
  }
  
  public static void checkForDuplicates()
  {
    ExternalProfileQuery uq = new ExternalProfileQuery(new QueryFactory());
    OIterator<? extends ExternalProfile> it = uq.getIterator();
    
    Set<String> set = new HashSet<String>();
    List<String> duplicates = new ArrayList<String>();
    
    while (it.hasNext())
    {
      ExternalProfile ep = it.next();
      
      if (!set.contains(ep.getEmail().toLowerCase()))
      {
        set.add(ep.getEmail().toLowerCase());
      }
      else
      {
        duplicates.add(ep.getEmail());
      }
    }
    
    if (duplicates.size() > 0)
    {
      throw new RuntimeException("Found duplicates [" + StringUtils.join(duplicates, ",") + "]");
    }
  }
  
  @Request
  private static void migrateAllUsers()
  {
    ExternalProfileQuery uq = new ExternalProfileQuery(new QueryFactory());
    OIterator<? extends ExternalProfile> it = uq.getIterator();
    
    HashMap<String, List<ExternalProfile>> userEmailMap = new HashMap<String, List<ExternalProfile>>();
    
    while (it.hasNext())
    {
      ExternalProfile ep = it.next();
      
      List<ExternalProfile> list = userEmailMap.getOrDefault(ep.getEmail().toLowerCase(), new ArrayList<ExternalProfile>());
      list.add(ep);
      userEmailMap.put(ep.getEmail().toLowerCase(), list);
    }
    
    int migrated = 0;
    
    for (String email : userEmailMap.keySet())
    {
      List<ExternalProfile> list = userEmailMap.get(email);
      
      if (list.size() > 1)
      {
        ExternalProfile primary = list.get(0);
        
        for (int i = 1; i < list.size(); ++i)
        {
          migrated++;
          migrateUser(list.get(i).getEmail(), primary.getEmail());
        }
      }
    }
    
    System.out.println("Migrated " + migrated + " users");
  }
  
  @Transaction
  private static void migrateUser(String duplicateEmail, String authoritativeEmail)
  {
    ExternalProfile u1 = getUserByEmail(authoritativeEmail);
    ExternalProfile u2 = getUserByEmail(duplicateEmail);
    
    migrateAllReferences(BusinessDAO.get(u2.getOid()), u1.getOid());
    
    u2.delete();
  }
  
  private static void migrateAllReferences(BusinessDAOIF dao, String newId)
  {
    final String oldId = dao.getOid();
    
    MdBusinessDAOIF mdBusinessDAOIF = dao.getMdBusinessDAO();

    List<MdAttributeReferenceDAOIF> mdAttrRefList = mdBusinessDAOIF.getAllReferenceAttributes();

    QueryFactory queryFactory = new QueryFactory();

    for (MdAttributeReferenceDAOIF mdAttrRefDAOIF : mdAttrRefList)
    {
      MdAttributeReferenceDAOIF mdAttrRefDAO = (MdAttributeReferenceDAOIF) mdAttrRefDAOIF;
      MdClassDAOIF mdClassDAOIF = mdAttrRefDAO.definedByClass();

      // Update the attribute references
      if (mdClassDAOIF instanceof MdEntityDAOIF)
      {
        MdEntityDAOIF mdEntityDAOIF = ( (MdEntityDAOIF) mdClassDAOIF );

        EntityQuery entityQ = queryFactory.entityQueryDAO(mdEntityDAOIF);
        entityQ.WHERE(entityQ.aReference(mdAttrRefDAO.definesAttribute()).EQ(oldId));

        OIterator<? extends ComponentIF> i = null;

        try
        {
          i = entityQ.getIterator();

          while (i.hasNext())
          {
            EntityDAO entityDAO = ( (EntityDAOIF) i.next() ).getEntityDAO();
            entityDAO.getAttribute(mdAttrRefDAO.definesAttribute()).setValueNoValidation(newId);

            // Write the field to the database
            List<PreparedStatement> preparedStatementList = new LinkedList<PreparedStatement>();
            PreparedStatement preparedStmt = null;
            preparedStmt = Database.buildPreparedUpdateFieldStatement(mdEntityDAOIF.getTableName(), entityDAO.getOid(), mdAttrRefDAO.getDefinedColumnName(), "?", oldId, newId, MdAttributeUUIDInfo.CLASS);
            preparedStatementList.add(preparedStmt);
            Database.executeStatementBatch(preparedStatementList);
          }
        }
        finally
        {
          if (i != null)
          {
            i.close();
          }
        }
      }
      else if (mdClassDAOIF instanceof MdGraphClassDAOIF)
      {
        MdGraphClassDAOIF mdGraphDAOIF = ( (MdGraphClassDAOIF) mdClassDAOIF );
        
        String osql = "SELECT FROM " + mdGraphDAOIF.getDBClassName() + " WHERE " + mdAttrRefDAO.getColumnName() + " = '" + oldId + "'";
        
        GraphDBService service = GraphDBService.getInstance();
        GraphRequest request = service.getGraphDBRequest();

        List<Object> results = service.query(request, osql, new HashMap<String,Object>());

        for (Object result : results)
        {
          if (result instanceof GraphObjectDAO)
          {
            GraphObjectDAO vobj = (GraphObjectDAO) result;
            
            vobj.setValue(mdAttrRefDAO.getColumnName(), newId);
            vobj.apply();
          }
        }
      }
    }
  }
  
  public static ExternalProfile getUserByEmail(String email)
  {
    ExternalProfileQuery uq = new ExternalProfileQuery(new QueryFactory());
    uq.WHERE(uq.getEmail().EQ(email));
    return uq.getIterator().getAll().get(0);
  }
}
