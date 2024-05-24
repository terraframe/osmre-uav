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
package gov.geoplatform.uasdm.processing.gcp;

import gov.geoplatform.uasdm.odm.ODMFacade.ODMProcessingPayload;

import java.io.BufferedReader;
import java.io.StringReader;
import java.math.BigDecimal;

/**
 *
 * reference:
 * https://docs.opendronemap.org/gcp/#gcp-file-format
 *
 * @author jsmethie
 *
 */
public class ODMGroundControlPointFileValidator extends GroundControlPointFileValidator {

    public ODMGroundControlPointFileValidator(ODMProcessingPayload payload) {
        super(payload);
    }

    @Override
    public GroundControlPointValidationResults validate() {
        GroundControlPointValidationResults results = new GroundControlPointValidationResults();

        try (BufferedReader br = new BufferedReader(new StringReader(this.payload.getGroundControlPointFile()))) {
            int num = 1;
            String line;
            while ((line = br.readLine()) != null) {
                if (num == 1) {
                    if (!line.strip().equals("EPSG:4326")) {
                        results.addError("First line must be 'EPSG:4326'");
                    }
                } else {
                    String[] vals = line.split("\\s+");

                    if (vals.length < 6) {
                        results.addError("Line [" + num + "] must contain at least six values.");
                    }

                    for (int i = 0; i < vals.length; ++i) {
                        switch (i) {
                            case 1:
                                try {
                                    BigDecimal longitude = new BigDecimal(vals[i]);
                                    if (longitude.compareTo(new BigDecimal(180)) > 0 || longitude.compareTo(new BigDecimal(-180)) < 0) {
                                        results.addError("Line [" + num + "] contains a longitude which is out of bounds. Longitude must be between -180 and 180.");
                                    }
                                } catch (Throwable t) {
                                    results.addError("Line [" + num + "] contains a non-numeric longitude.");
                                }
                                break;
                            case 2:
                                try {
                                    BigDecimal latitude = new BigDecimal(vals[i]);
                                    if (latitude.compareTo(new BigDecimal(90)) > 0 || latitude.compareTo(new BigDecimal(-90)) < 0) {
                                        results.addError("Line [" + num + "] contains a latitude which is out of bounds. Latitude must be between -90 and 90.");
                                    }
                                } catch (Throwable t) {
                                    results.addError("Line [" + num + "] contains a non-numeric latitude.");
                                }
                                break;
                            case 3:
                            case 4:
                                try {
                                    new BigDecimal(vals[i]);
                                } catch (Throwable t) {
                                    results.addError("Line [" + num + "] contains a non-numeric value.");
                                }
                                break;
                            case 5:
                                if (!payload.getImageNames().contains(vals[i])) {
                                    results.addError("Line [" + num + "] references an image which does not exist. Image names must match exactly and are case sensitive.");
                                }
                                break;

                            default:
                                break;
                        }
                    }
                }

                num++;
            }
        } catch (GroundControlPointFileInvalidFormatException e) {
            throw e;
        } catch (Throwable t) {
            throw new GroundControlPointFileInvalidFormatException(t.getMessage(), t);
        }

        return results;
    }

}