import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SamHierarchyBuilder
{
  private static class FileEntry
  {
    File file;

    int  offset;

    public FileEntry(File file)
    {
      this.file = file;
      this.offset = 0;
    }

    public FileEntry(File file, int offset)
    {
      this.file = file;
      this.offset = offset;
    }
  }

  private String apiKey;

  private String folderName;

  private String resultPath;

  public SamHierarchyBuilder(String apiKey, String folderName, String resultPath)
  {
    super();
    this.apiKey = apiKey;
    this.folderName = folderName;
    this.resultPath = resultPath;
  }

  public void run() throws Exception
  {
    JsonObject government = this.createNode("Government", "Government");

    JsonArray roots = new JsonArray();
    roots.add(government);
    roots.add(this.createEducation());
    roots.add(this.createNode("Private Sector", "Private-Sector"));
    roots.add(this.createNode("Unspecified", "Unspecified"));

    // File folder = new File(this.folderName);
    // File[] files = folder.listFiles((file, fileName) ->
    // fileName.startsWith("root"));
    //
    // for (File file : files)
    // {
    // JsonObject object = JsonParser.parseReader(new
    // FileReader(file)).getAsJsonObject();
    // JsonArray orgs = object.get("orglist").getAsJsonArray();
    //
    // for (int i = 0; i < orgs.size(); i++)
    // {
    // JsonObject organization = orgs.get(i).getAsJsonObject();
    //
    // String status = organization.get("status").getAsString();
    //
    // String orgType = organization.get("fhorgtype").getAsString();
    //
    // if (!status.equalsIgnoreCase("INACTIVE") &&
    // !orgType.equalsIgnoreCase("office"))
    // {
    // Long orgId = organization.get("fhorgid").getAsLong();
    // String name = organization.get("fhorgname").getAsString();
    //
    // JsonArray links = organization.get("links").getAsJsonArray();
    //
    // boolean hasChildren = false;
    //
    // for (int j = 0; j < links.size(); j++)
    // {
    // JsonObject link = links.get(j).getAsJsonObject();
    // String rel = link.get("rel").getAsString();
    //
    // if (rel.equals("nextlevelchildren"))
    // {
    // hasChildren = true;
    // }
    // }
    //
    // if (hasChildren)
    // {
    // JsonObject root = processFile(orgId.toString());
    //
    // if (root != null)
    // {
    // government.get("children").getAsJsonArray().add(root);
    // }
    // }
    // else
    // {
    // JsonObject obj = createObject(name, orgId);
    //
    // government.get("children").getAsJsonArray().add(obj);
    // }
    // }
    // }
    // }

    // DOI and USDA organizations
    List<String> rootIds = Arrays.asList("100010393", "100006809", "100035122", "100004222");

    for (String rootId : rootIds)
    {
      JsonObject root = processFile(rootId);

      if (root != null)
      {
        government.get("children").getAsJsonArray().add(root);
      }
    }

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    try (FileWriter writer = new FileWriter(new File(this.resultPath)))
    {
      gson.toJson(roots, writer);
    }
  }

  private JsonObject processFile(String rootId) throws FileNotFoundException
  {
    long oasId = 100169579;
    String officeId = "100045573";

    JsonObject root = null;

    Stack<FileEntry> files = new Stack<>();
    files.push(new FileEntry(new File(this.folderName, "hierarchy_" + rootId + ".json")));

    int count = 0;

    while (!files.isEmpty())
    {
      FileEntry entry = files.pop();
      File file = entry.file;

      if (file.exists())
      {
        JsonObject object = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
        JsonArray orgs = object.get("orglist").getAsJsonArray();

        if (count == 0)
        {
          long totalRecords = object.get("totalrecords").getAsLong();

          int offset = 100;

          while (totalRecords >= 100)
          {
            files.push(new FileEntry(new File(this.folderName, "hierarchy_" + rootId + "_" + offset + ".json"), offset));

            totalRecords -= 100;
            offset += 100;
          }
        }

        for (int i = 0; i < orgs.size(); i++)
        {
          JsonObject organization = orgs.get(i).getAsJsonObject();
          String status = organization.get("status").getAsString();
          String orgType = organization.get("fhorgtype").getAsString();
          long orgId = organization.get("fhorgid").getAsLong();

          if (orgId == oasId || (!status.equalsIgnoreCase("INACTIVE") && !orgType.equalsIgnoreCase("office")) )
          {
            String name = organization.get("fhorgname").getAsString();

            JsonObject obj = createObject(name, orgId);

            JsonArray links = organization.get("links").getAsJsonArray();

            for (int j = 0; j < links.size(); j++)
            {
              JsonObject link = links.get(j).getAsJsonObject();
              String rel = link.get("rel").getAsString();
              String href = link.get("href").getAsString();

              if (rel.equals("nextlevelchildren"))
              {
                String[] split = href.split("fhorgid=");
                String childId = split[1];

//                if (!childId.equals(rootId) && childId.equals(Long.valueOf(oasId).toString()))
                if (!childId.equals(rootId) &&  (childId.equals(Long.valueOf(oasId).toString()) || childId.equals(officeId.toString())))
                {
                  JsonObject child = processFile(childId);

                  if (child != null)
                  {
                    obj = child;
                  }
                }
              }
            }

            if (i == 0 && root == null)
            {
              root = obj;
            }
            else
            {
              JsonArray children = root.get("children").getAsJsonArray();
              children.add(obj);
            }
          }
        }
      }
      else
      {
        int offset = entry.offset;
        System.err.println("curl \"https://api.sam.gov/prod/federalorganizations/v1/org/hierarchy?fhorgid=" + rootId + "&offset=" + offset + "&limit=100&api_key=" + this.apiKey + "\" | json_pp > hierarchy_" + rootId + ( offset != 0 ? "_" + offset : "" ) + ".json");
      }

      count++;
    }

    return root;
  }

  private JsonObject createObject(String name, long orgId)
  {
    return createNode(name, Long.toString(orgId));
  }

  private JsonObject createNode(String name, String code)
  {
    JsonObject obj = new JsonObject();
    obj.addProperty("code", code);
    obj.addProperty("enabled", true);
    obj.add("label", getLocalizedValue(name));
    obj.add("contactInfo", getLocalizedValue(""));
    obj.add("children", new JsonArray());
    return obj;
  }

  private JsonObject getLocalizedValue(String name)
  {
    JsonObject localeValue = new JsonObject();
    localeValue.addProperty("locale", "defaultLocale");
    localeValue.addProperty("value", name);

    JsonArray localeValues = new JsonArray();
    localeValues.add(localeValue);

    JsonObject label = new JsonObject();
    label.addProperty("localizedValue", name);
    label.add("localeValues", localeValues);
    return label;
  }

  private JsonObject createEducation()
  {
    JsonObject cdc = this.createNode("Education", "Education");
    cdc.get("children").getAsJsonArray().add(this.createNode("LSHTM", "CDC/LSHTM"));

    return cdc;
  }

  private JsonObject createOther()
  {
    return this.createNode("Other", "OTHER");
  }

  public static void main(String[] args) throws Exception
  {
    new SamHierarchyBuilder(args[0], args[1], args[2]).run();
  }

}
