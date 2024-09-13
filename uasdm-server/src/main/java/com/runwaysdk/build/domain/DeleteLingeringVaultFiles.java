package com.runwaysdk.build.domain;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.VaultFile;
import com.runwaysdk.system.VaultFileQuery;

public class DeleteLingeringVaultFiles
{
  private Logger logger = LoggerFactory.getLogger(DeleteLingeringVaultFiles.class);
  
  public static void main(String[] args) throws Throwable
  {
    new DeleteLingeringVaultFiles().doIt();
  }
  
  public DeleteLingeringVaultFiles()
  {
    
  }
  
  @Request
  public void doIt() throws Throwable
  {
    VaultFileQuery vfq = new VaultFileQuery(new QueryFactory());
    
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MONTH, -4);
    vfq.WHERE(vfq.getLastUpdateDate().LT(cal.getTime()));
    
    logger.error("deleting " + vfq.getCount() + " old vault files");
    
    OIterator<? extends VaultFile> it = vfq.getIterator();
    
    while (it.hasNext()) {
      it.next().delete();
    }
  }
}
