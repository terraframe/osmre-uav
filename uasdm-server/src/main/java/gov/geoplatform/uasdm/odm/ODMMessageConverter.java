/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.geoplatform.uasdm.odm;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;

import com.runwaysdk.RunwayException;
import com.runwaysdk.session.Session;

public class ODMMessageConverter
{
  public static String OUT_OF_MEMORY = "ODM ran out of memory during processing. Please contact your technical support. You can also try turning down the quality settings and re-processing.";
  
  private Set<String> messages = new HashSet<String>();
  
  public void process(ODMProcessingTaskIF task, String msg)
  {
    task.setMessage(convertMessage(msg));
  }
  
  public void process(ODMProcessingTaskIF task, String msg, JSONArray odmOutput)
  {
    processODMOutput(odmOutput, task);
    
    if (!isMessageImportant(msg))
    {
      if (messages.size() == 1)
      {
        task.setMessage(messages.stream().findFirst().get());
      }
      else if (messages.size() > 1)
      {
        task.setMessage("Errors were discovered in ODM output. See messages for more information.");
      }
      else
      {
        task.setMessage(convertMessage(msg));
      }
    }
    else
    {
      task.setMessage(convertMessage(msg));
    }
    
    if (isMessageImportant(msg) || messages.size() > 1)
    {
      for (String action : messages)
      {
        task.createAction(action, "error");
      }
    }
  }
  
  private boolean isMessageImportant(String msg)
  {
    return msg != null && msg.length() > 0 && !msg.toLowerCase().contains("cannot process dataset");
  }
  
  private String convertMessage(String msg)
  {
    // We're doing a little bit of guessing with these error messages. Typically when a host becomes unreachable it's because the server crashed due to OOM
    if (msg.toLowerCase().contains("invalid route for")
        || msg.toLowerCase().contains("not enough memory")
        || msg.toLowerCase().matches(".*error.* connecting .* localhost.*")
        || msg.toLowerCase().contains("proxy redirect error"))
    {
      return OUT_OF_MEMORY;
    }
    
    return msg;
  }
  
  /**
   * Read through the ODM output and attempt to identify issues that may have
   * occurred.
   */
  private void processODMOutput(JSONArray output, ODMProcessingTaskIF task)
  {
    String sOutput = output.toString();

    for (int i = 0; i < output.length(); ++i)
    {
      String line = output.getString(i);

      if (line.contains("MICA-CODE:1"))
      {
        messages.add("Unable to find image panel. This may effect image color quality. Did you include a panel with naming convention IMG_0000_*.tif? Refer to the multispectral documentation for more information.");
      }
      else if (line.contains("MICA-CODE:2"))
      {
        messages.add("Alignment image not found. Did you include an alignment image with naming convention IMG_0001_*.tif? Refer to the multispectral documentation for more information.");
      }
      else if (line.contains("MICA-CODE:3"))
      {
        Pattern pattern = Pattern.compile(".*Image \\[(.*)\\] does not match the naming convention\\. Are you.*\\(MICA-CODE:3\\).*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(line);
        boolean matchFound = matcher.find();
        if (matchFound)
        {
          messages.add("Image [" + matcher.group(1) + "] does not match the naming convention. Are you following the proper naming convention for your images? Refer to the multispectral documentation for more information.");
        }
        else
        {
          messages.add("Image does not match the naming convention. Are you following the proper naming convention for your images? Refer to the multispectral documentation for more information.");
        }
      }
      else if (line.contains("MICA-CODE:4"))
      {
        messages.add("Image alignment has failed. Take the time to look through your image collection and weed out any bad images, i.e. pictures of the camera inside the case, or pictures which are entirely black, etc. Also make sure that if your collection includes panel images, that they are named as IMG_0000_*.tif. You can also try using a different alignment image (by renaming a better image set to IMG_0001_*.tif). Finally, make sure this collection is of a single flight and that there were no 'shock' events. Refer to the multispectral documentation for more information.");
      }
      else if (line.contains("OSError: Panels not detected in all images")) // Micasense
      {
        messages.add("Your upload includes images with naming convention IMG_0000_*.tif, however those images do not appear to be of a panel. Make sure that IMG_0000_*.tif, if included, is of a panel, and that there are no other panels anywhere else in your images. Refer to the multispectral documentation for more information.");
      }
      else if (line.contains("[Errno 2] No such file or directory: '/var/www/data/0182028a-c14f-40fe-bd6f-e98362ec48c7/opensfm/reconstruction.json'") // 0.9.1
                                                                                                                                                      // output
          || line.contains("The program could not process this dataset using the current settings. Check that the images have enough overlap") // 0.9.8
                                                                                                                                               // output
          || ( line.contains("IndexError: list index out of range") && output.getString(i - 1).contains("reconstruction = data[0]") && sOutput.contains("Traceback (most recent call last):") ) // 0.9.1
      )
      {
        messages.add("ODM failed to produce a reconstruction from the image matches. Check that the images have enough overlap, that there are enough recognizable features and that the images are in focus. (more info: https://github.com/OpenDroneMap/ODM/issues/524)");
      }
      else if (line.contains("Not enough supported images in") // 0.9.1
          || ( line.contains("numpy.AxisError: axis 1 is out of bounds for array of dimension 0") // 2.4.7
          ))
      {
        messages.add("Couldn't find enough usable images. The orthomosaic image data must contain at least two images with extensions '.jpg','.jpeg','.png'");
      }
      else if (line.contains("bad_alloc") || line.contains("MemoryError") || line.contains("Unable to allocate"))
      {
        messages.add(OUT_OF_MEMORY);
      }
    }
  }
}
