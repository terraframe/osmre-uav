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
package gov.geoplatform.uasdm.service.request;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.ThreadTransactionState;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;
import gov.geoplatform.uasdm.LPGGeometry;
import net.geoprism.registry.LPGTileCache;
import net.geoprism.registry.service.business.LabeledPropertyGraphTypeVersionBusinessServiceIF;
import net.geoprism.registry.service.request.LabeledPropertyGraphTypeVersionService;
import net.geoprism.registry.service.request.LabeledPropertyGraphTypeVersionServiceIF;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Service
@Primary
public class IDMLabeledPropertyGraphTypeVersionService extends LabeledPropertyGraphTypeVersionService implements LabeledPropertyGraphTypeVersionServiceIF {
    @Autowired
    private LabeledPropertyGraphTypeVersionBusinessServiceIF service;

    @Override
    @Request(RequestType.SESSION)
    public InputStream getTile(String sessionId, JSONObject object) {
        try {
            byte[] bytes = this.getTile(object);

            return new ByteArrayInputStream(Objects.requireNonNullElseGet(bytes, () -> new byte[]{}));

        } catch (JSONException e) {
            throw new ProgrammingErrorException(e);
        }
    }

    public byte[] getTile(JSONObject object) throws JSONException {
        String versionId = object.getString("oid");
        String typeCode = object.getString("typeCode");
        int x = object.getInt("x");
        int y = object.getInt("y");
        int zoom = object.getInt("z");

        return this.getTile(versionId, typeCode, x, y, zoom);
    }

    @Transaction
    public byte[] getTile(String versionId, String typeCode, int x, int y, int zoom) {
        byte[] cached = LPGTileCache.getCachedTile(versionId, typeCode, x, y, zoom);

        if (cached != null) {
            return cached;
        } else {
            /*
             * Store the tile into the cache for future reads
             */
            SessionIF session = Session.getCurrentSession();

            if (session != null) {
                ThreadTransactionState state = ThreadTransactionState.getCurrentThreadTransactionState();

                try {
                    return LPGTileCache.executor.submit(new LPGGeometry.CacheCallable(state, versionId, typeCode, x, y, zoom)).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new ProgrammingErrorException(e);
                }
            }

            return null;
        }
    }

}
