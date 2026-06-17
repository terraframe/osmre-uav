package gov.geoplatform.uasdm.processing.geolocation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.runwaysdk.resource.ApplicationFileResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.FileResource;
import com.runwaysdk.resource.ResourceException;

import gov.geoplatform.uasdm.bus.AbstractWorkflowTask;
import gov.geoplatform.uasdm.bus.AbstractWorkflowTask.TaskActionType;

/**
 * Used to modify the "ODM geo location file" right before upload to ODM. This is needed because ODM
 * doesn't handle case sensitivity and the file might have references to files which don't exist in
 * the collection, but we don't want ODM to freak out.
 *
 * This class will loop through all items in the geolog file and validate them against what's in the
 * collection. If one is referenced in the geolog that doesn't exist in collection, log a WARNING. If
 * an image exists in the collection but not the geolog file, throw an error. Convert all filename
 * case to match the image filename.
 */
public class ODMGeoLocationProcessConverter
{
  public static ApplicationFileResource convert(Set<String> colImageNames, ApplicationFileResource res, AbstractWorkflowTask task)
  {
    Map<String, String> collectionNamesByLowercase = colImageNames.stream()
      .collect(Collectors.toMap(n -> n.toLowerCase(), n -> n));

    Set<String> geologImageNamesLowercase = new HashSet<>();
    List<String> lines = new ArrayList<>();

    boolean hasError = false;

    try (BufferedReader br = new BufferedReader(new InputStreamReader(res.openNewStream(), StandardCharsets.UTF_8)))
    {
      int num = 1;
      String line;

      while ((line = br.readLine()) != null)
      {
        if (line.strip().toUpperCase().contains("EPSG:"))
        {
          lines.add(line);
          num++;
          continue;
        }

        String[] vals = line.strip().split("\\s+");

        for (int i = 0; i < vals.length; ++i)
        {
          switch (i)
          {
            case 0:
              String lowercaseName = vals[i].toLowerCase();

              if (!collectionNamesByLowercase.containsKey(lowercaseName))
              {
                task.createAction("Geo-location file line [" + num + "] references image [" + vals[i] + "] but it does not exist in the collection.", TaskActionType.WARNING);
              }
              else
              {
                vals[i] = collectionNamesByLowercase.get(lowercaseName);
                geologImageNamesLowercase.add(lowercaseName);
              }

              break;

            case 1:
              try
              {
                new BigDecimal(vals[i]);
              }
              catch (Throwable t)
              {
                task.createAction("Geo-location file line [" + num + "] contains a non-numeric longitude.", TaskActionType.ERROR);
                hasError = true;
              }

              break;

            case 2:
              try
              {
                new BigDecimal(vals[i]);
              }
              catch (Throwable t)
              {
                task.createAction("Geo-location file line [" + num + "] contains a non-numeric latitude.", TaskActionType.ERROR);
                hasError = true;
              }

              break;

            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
              try
              {
                new BigDecimal(vals[i]);
              }
              catch (Throwable t)
              {
                task.createAction("Geo-location file line [" + num + "] contains a non-numeric value.", TaskActionType.ERROR);
                hasError = true;
              }

              break;

            default:
              break;
          }
        }

        lines.add(String.join(" ", vals));
        num++;
      }

      for (String lowercaseName : collectionNamesByLowercase.keySet())
      {
        if (!geologImageNamesLowercase.contains(lowercaseName))
        {
          task.createAction("Image [" + collectionNamesByLowercase.get(lowercaseName) + "] exists in the collection but is not referenced in the geo-location file.", TaskActionType.ERROR);
          hasError = true;
        }
      }

      if (hasError)
      {
        throw new RuntimeException("Geo-location file is invalid and cannot be sent to ODM. Upload a new geolocation file and try processsing again. View messages for more information.");
      }

      File parent = Files.createTempDirectory(res.getName()).toFile();

      // Write the corrected contents into a new temp-backed resource.
      File newFile = new File(parent, res.getName());

      Files.write(newFile.toPath(), lines, StandardCharsets.UTF_8);

      return new FileResource(new CloseableFile(newFile));
    }
    catch (GeoLocationFileInvalidFormatException e)
    {
      throw e;
    }
    catch (IOException e)
    {
      throw new ResourceException(e);
    }
    catch (RuntimeException e)
    {
      throw e;
    }
    catch (Throwable t)
    {
      throw new GeoLocationFileInvalidFormatException(t.getMessage(), t);
    }
  }
}