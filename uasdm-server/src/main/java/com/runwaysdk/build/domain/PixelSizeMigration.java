package com.runwaysdk.build.domain;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.graph.Sensor;

public class PixelSizeMigration implements Runnable
{
  private static final Logger logger = LoggerFactory.getLogger(PixelSizeMigration.class);

  public static void main(String[] args)
  {
    new PixelSizeMigration().run();
  }

  public static void start()
  {
    Thread t = new Thread(new PixelSizeMigration(), "PixelSizeMigration");
    t.setDaemon(true);
    t.start();
  }

  @Request
  public void run()
  {
    transaction();
  }

  @Transaction
  protected void transaction()
  {
    List<Sensor> sensors = Sensor.getAll();

    logger.info("Patcher will update [" + sensors.size() + "] sensors");

    for (Sensor sensor : sensors)
    {
      sensor.setRealPixelSizeHeight(new BigDecimal(sensor.getPixelSizeHeight()));
      sensor.setRealPixelSizeWidth(new BigDecimal(sensor.getPixelSizeWidth()));
      sensor.apply();
    }
  }
}
