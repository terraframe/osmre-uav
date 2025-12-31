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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Loads autoscaler config from classpath resource "autoscaler-aws-config.json", which is made available at runtime from a maven resource copy in the server pom.
 * Falls back to a built-in default if the resource is absent or unreadable.
 * Parsed data is cached in memory and exposed via static getters and a selection method.
 */
public final class AutoscalerAwsConfigService {

    private static final String RESOURCE_NAME = "autoscaler-aws-config.json";

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

    private static final AutoscalerAwsConfig CONFIG;
    private static final List<ImageSizeMapping> SORTED_MAPPINGS; // ascending by (maxImages, maxColSizeMb)

    static {
        CONFIG = loadConfigOrDefault();
        // Defensive copy + sort for deterministic selection
        List<ImageSizeMapping> copy = new ArrayList<>();
        if (CONFIG.getImageSizeMapping() != null) {
            copy.addAll(CONFIG.getImageSizeMapping());
        }
        copy.sort(Comparator
                .comparingInt(ImageSizeMapping::getMaxImages)
                .thenComparingInt(ImageSizeMapping::getMaxColSizeMb));
        SORTED_MAPPINGS = Collections.unmodifiableList(copy);
    }

    private AutoscalerAwsConfigService() {
        // static utility
    }

    /**
     * Returns an unmodifiable view of the parsed config object.
     */
    public static AutoscalerAwsConfig getConfig() {
        return CONFIG;
    }

    /**
     * Returns an unmodifiable list of all mappings.
     */
    public static List<ImageSizeMapping> getImageSizeMappings() {
        return SORTED_MAPPINGS;
    }

    /**
     * Returns the smallest instance mapping whose thresholds satisfy BOTH:
     *   imageCount <= maxImages AND (colSizeMb <= maxColSizeMb OR colSizeMb == 0).
     *
     * The zero-value special case allows for image-based scaling even when
     * collection size is unknown or irrelevant.
     *
     * If no mapping satisfies both conditions (i.e., the requested image count
     * and/or collection size exceed all available tiers), the method returns
     * the largest defined mapping — effectively the “max capacity” tier.
     *
     * @param imageCount number of images in the collection
     * @param colSizeMb  total collection size in MB (0 to ignore size constraint)
     * @return the best-fit ImageSizeMapping; never null if mappings are defined
     * @throws IllegalStateException if no mappings are loaded (should not occur if defaults exist)
     */
    public static ImageSizeMapping autoscalerMappingForConfig(int imageCount, int colSizeMb) {
        if (SORTED_MAPPINGS.isEmpty()) {
            throw new IllegalStateException("No image size mappings are available.");
        }
        for (ImageSizeMapping m : SORTED_MAPPINGS) {
            if (imageCount <= m.getMaxImages() && (colSizeMb <= m.getMaxColSizeMb() || colSizeMb == 0)) {
                return m;
            }
        }
        // If request exceeds biggest tier, return the largest available
        return SORTED_MAPPINGS.get(SORTED_MAPPINGS.size() - 1);
    }

    // ----------- Loading helpers -----------

    private static AutoscalerAwsConfig loadConfigOrDefault() {
        // Try classpath first
        try (InputStream in = getTCCl().getResourceAsStream(RESOURCE_NAME)) {
            if (in != null) {
                return MAPPER.readValue(in, AutoscalerAwsConfig.class);
            }
        } catch (IOException e) {
            // fall through to default
        }
        // Fallback to built-in default JSON
        try {
            return MAPPER.readValue(DEFAULT_JSON, AutoscalerAwsConfig.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse built-in default autoscaler config JSON.", e);
        }
    }

    private static ClassLoader getTCCl() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl != null ? cl : AutoscalerAwsConfigService.class.getClassLoader();
    }

    // ----------- POJOs -----------

    /**
     * Root config POJO.
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class AutoscalerAwsConfig {

        @JsonProperty("imageSizeMapping")
        private List<ImageSizeMapping> imageSizeMapping;

        public AutoscalerAwsConfig() {
        }

        public List<ImageSizeMapping> getImageSizeMapping() {
            return imageSizeMapping;
        }

        public void setImageSizeMapping(List<ImageSizeMapping> imageSizeMapping) {
            this.imageSizeMapping = imageSizeMapping;
        }
    }

    /**
     * One row of the size mapping.
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class ImageSizeMapping {

        @JsonProperty("maxImages")
        private int maxImages;

        @JsonProperty("maxColSizeMb")
        private int maxColSizeMb;

        @JsonProperty("slug")
        private String slug;

        @JsonProperty("spotPrice")
        private double spotPrice;

        @JsonProperty("storage")
        private int storage;

        public ImageSizeMapping() {
        }

        public int getMaxImages() {
            return maxImages;
        }

        public void setMaxImages(int maxImages) {
            this.maxImages = maxImages;
        }

        public int getMaxColSizeMb() {
            return maxColSizeMb;
        }

        public void setMaxColSizeMb(int maxColSizeMb) {
            this.maxColSizeMb = maxColSizeMb;
        }

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
        }

        public double getSpotPrice() {
            return spotPrice;
        }

        public void setSpotPrice(double spotPrice) {
            this.spotPrice = spotPrice;
        }

        public int getStorage() {
            return storage;
        }

        public void setStorage(int storage) {
            this.storage = storage;
        }

        @Override
        public String toString() {
            return "ImageSizeMapping{" +
                    "maxImages=" + maxImages +
                    ", maxColSizeMb=" + maxColSizeMb +
                    ", slug='" + slug + '\'' +
                    ", spotPrice=" + spotPrice +
                    ", storage=" + storage +
                    '}';
        }
    }

    // ----------- Default JSON (Java 11-compatible string) -----------

    private static final String DEFAULT_JSON =
            "{\n" +
            "  \"imageSizeMapping\": [\n" +
            "    {\"maxImages\": 18, \"maxColSizeMb\": 80, \"slug\": \"t3a.medium\", \"spotPrice\": 0.04, \"storage\": 100},\n" +
            "    {\"maxImages\": 60, \"maxColSizeMb\": 300, \"slug\": \"m5.large\", \"spotPrice\": 0.1, \"storage\": 160},\n" +
            "    {\"maxImages\": 200, \"maxColSizeMb\": 1000, \"slug\": \"m5.xlarge\", \"spotPrice\": 0.2, \"storage\": 320},\n" +
            "    {\"maxImages\": 800, \"maxColSizeMb\": 4500, \"slug\": \"m5.2xlarge\", \"spotPrice\": 0.4, \"storage\": 640},\n" +
            "    {\"maxImages\": 2000, \"maxColSizeMb\": 11000, \"slug\": \"r5.2xlarge\", \"spotPrice\": 0.6, \"storage\": 800},\n" +
            "    {\"maxImages\": 3000, \"maxColSizeMb\": 24000, \"slug\": \"r5.4xlarge\", \"spotPrice\": 1.1, \"storage\": 1000},\n" +
            "    {\"maxImages\": 6000, \"maxColSizeMb\": 60000, \"slug\": \"r5.8xlarge\", \"spotPrice\": 1.8, \"storage\": 1200},\n" +
            "    {\"maxImages\": 12000, \"maxColSizeMb\": 200000, \"slug\": \"r5.12xlarge\", \"spotPrice\": 2.0, \"storage\": 1600},\n" +
            "    {\"maxImages\": 24000, \"maxColSizeMb\": 500000, \"slug\": \"r5.16xlarge\", \"spotPrice\": 4.0, \"storage\": 2000}\n" +
            "  ]\n" +
            "}\n";
}

