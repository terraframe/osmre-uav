package com.runwaysdk.build.domain;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import gov.geoplatform.uasdm.graph.Sensor;

public class SensorWidthMigration implements Runnable
{
  private static final Logger logger = LoggerFactory.getLogger(SensorWidthMigration.class);

  public static void main(String[] args)
  {
    new SensorWidthMigration().run();
  }

  public static void start()
  {
    Thread t = new Thread(new SensorWidthMigration(), "SensorWidthMigration");
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
      sensor.setRealSensorHeight(new BigDecimal(sensor.getSensorHeight()));
      sensor.setRealSensorWidth(new BigDecimal(sensor.getSensorWidth()));
      sensor.apply();
    }
  }
}
