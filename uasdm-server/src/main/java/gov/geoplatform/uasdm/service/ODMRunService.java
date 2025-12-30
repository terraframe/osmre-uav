package gov.geoplatform.uasdm.service;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.uasdm.graph.Collection;
import gov.geoplatform.uasdm.graph.ODMRun;
import gov.geoplatform.uasdm.model.ProcessConfiguration;

@Service
public class ODMRunService
{
  @Request(RequestType.SESSION)
  public JSONObject estimateRuntime(String sessionId, String collectionId, String configJson)
  {
    final JSONObject response = new JSONObject();
    var collection = Collection.get(collectionId);
    
    final long rawSizeMb = collection.getDocuments().stream().filter(d -> d != null).map(d -> d.getFileSize() == null ? 0 : d.getFileSize()).reduce(0l, (a,b) -> a + b);
    
    ProcessConfiguration configuration = ProcessConfiguration.parse(configJson);
    final boolean processOrtho = Boolean.TRUE.equals(configuration.toODM().getProcessOrtho());
    final boolean processPointcloud = Boolean.TRUE.equals(configuration.toODM().getProcessPtcloud());
    final boolean processDem = Boolean.TRUE.equals(configuration.toODM().getProcessDem());
    final boolean processVideo = collection.getFormat() != null && Boolean.TRUE.equals(collection.getFormat().isVideo());
    final boolean isMultispectral = Boolean.TRUE.equals(collection.isMultiSpectral());
    final boolean isRadiometric = Boolean.TRUE.equals(collection.isRadiometric());
    
    final String clazz = MdVertexDAO.getMdVertexDAO(ODMRun.CLASS).getDBClassName();

    // Deduped, stable-ordered filter set
    final LinkedHashSet<String> filters = new LinkedHashSet<>();

    // Query parameters
    final Map<String, Object> params = new HashMap<>();
    params.put("rawSizeMb", rawSizeMb);
    params.put("rawSizeTolPct", 0.20);

    // Dense reconstruction knobs (shared params)
    if (processOrtho || processPointcloud || processDem) {
      filters.add("AND pcQuality = :pcQuality");
      filters.add("AND featureQuality = :featureQuality");
      filters.add("AND abs(matcherNeighbors - :matcherNeighbors) <= :matcherNeighborsTol");
      filters.add("AND abs(minNumFeatures - :minNumFeatures) <= :minNumFeaturesTol");

      params.put("pcQuality", configuration.toODM().getPcQuality());

      params.put("featureQuality", configuration.toODM().getFeatureQuality());

      params.put("matcherNeighbors", configuration.toODM().getMatcherNeighbors());
      params.put("matcherNeighborsTol", 0);

      params.put("minNumFeatures", configuration.toODM().getMinNumFeatures());
      params.put("minNumFeaturesTol", 1000);
    }

    // Gridded outputs
    if (processOrtho || processDem) {
      filters.add("AND abs(resolution - :resolution) <= :resolutionTol");
      params.put("resolution", configuration.toODM().getResolution());
      params.put("resolutionTol", 1.0);
    }

    // Video knobs
    if (processVideo) {
      filters.add("AND abs(videoResolution - :videoResolution) <= :videoResolutionTol");
      filters.add("AND abs(videoLimit - :videoLimit) <= :videoLimitTol");
      params.put("videoResolution", configuration.toODM().getVideoResolution());
      params.put("videoResolutionTol", 300);
      params.put("videoLimit", configuration.toODM().getVideoLimit());
      params.put("videoLimitTol", 20);
    }
    
    // Multispectral
    if (isMultispectral || isRadiometric) {
      filters.add("AND radiometricCalibration = :radiometricCalibration");
      params.put("radiometricCalibration", configuration.toODM().getRadiometricCalibration().getCode().toUpperCase());
    }
    
    String statement = "SELECT\n"
        + "  avg(runtimeSeconds) AS estimatedRuntimeSeconds,\n"
        + "  count(*) AS similarJobs\n"
        + "FROM (\n"
        + "  SELECT\n"
        + "    rawSizeMb,\n"
        + "    (coalesce(coalesce(runEnd, runStart), sysdate()).asLong() - coalesce(coalesce(runStart, runEnd), sysdate()).asLong()) / 1000 AS runtimeSeconds\n"
        + "  FROM (\n"
        + "    SELECT\n"
        + "      @rid AS rid,\n"
        + "      runStart,\n"
        + "      runEnd,\n"
        + "      sum(coalesce(in().fileSize, 0)) / 1024 / 1024 AS rawSizeMb,\n"
        + "      resolution,\n"
        + "      pcQuality,\n"
        + "      featureQuality,\n"
        + "      matcherNeighbors,\n"
        + "      minNumFeatures,\n"
        + "      videoResolution,\n"
        + "      videoLimit,\n"
        + "      radiometricCalibration\n"
        + "    FROM " + clazz + "\n"
        + "    GROUP BY @rid\n"
        + "  )\n"
        + "  WHERE\n"
        + "    abs(rawSizeMb - :rawSizeMb) <= (:rawSizeMb * :rawSizeTolPct)\n"
        + "    " + String.join("\n    ", filters) + "\n"
        + ");";
    
    final GraphQuery<Map<String, Object>> query = new GraphQuery<Map<String, Object>>(statement, params);

    Map<String, Object> result = query.getSingleResult();
    if (result == null) return response;
    
    Double runtime = (Double) result.get("estimatedRuntimeSeconds");
    Long similarJobs = (Long) result.get("similarJobs");
    if (similarJobs == 0) return response;
    
    double confidence = 1.0 - Math.exp(-similarJobs / 5.0);
    
    response.put("runtime", runtime);
    response.put("confidence", confidence);
    response.put("similarJobs", similarJobs);
    return response;
  }
}
