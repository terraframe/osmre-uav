package gov.geoplatform.uasdm;

import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.metadata.MdAttributeBlobDAO;
import com.runwaysdk.dataaccess.transaction.ThreadTransactionState;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.dataaccess.transaction.TransactionType;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import jnr.ffi.Struct;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.LPGTileCache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;

public class LPGGeometry extends LPGGeometryBase {
    @SuppressWarnings("unused")
    private static final long serialVersionUID = 617576499;

    public LPGGeometry() {
        super();
    }

    public static void delete(LabeledPropertyGraphTypeVersion version) {
        Database.executeStatement("DELETE FROM lpg_geometry WHERE version = '" + version + "'");
    }

    private static class PostgisVectorTileBuilder {
        private final String versionId;
        private final String typeCode;

        public PostgisVectorTileBuilder(String versionId, String typeCode) {
            this.versionId = versionId;
            this.typeCode = typeCode;
        }

        public byte[] write(int zoom, int x, int y) {
            StringBuilder statement = new StringBuilder();

            // Filter the data to remove entries which have points too close the poles
            // Those points cannot be transformed
            statement.append("WITH _fdata AS (" + "\n");
            statement.append(" SELECT * FROM lpg_geometry \n");
            statement.append(" WHERE version = '" + this.versionId + "'" + "\n");
            statement.append(" AND type_code = '" + this.typeCode + "'" + "\n");
            statement.append(" AND (ST_XMax(geometry) BETWEEN -180 AND 180)" + "\n");
            statement.append(" AND (ST_YMax(geometry) BETWEEN -89.9 AND 89.9)" + "\n");
            statement.append(")," + "\n");

            // Get the properties used in the tile and convert the geometry to its tile format
            statement.append("mvtgeom AS (" + "\n");
            statement.append(" SELECT " + "\n");
            statement.append("  ge.location_oid AS oid\n");
            statement.append(", ge.location_uuid AS uid\n");
            statement.append(", ge.location_code AS code \n");
            statement.append(", ge.location_label AS label" + "\n");
            statement.append(", ge.parent AS parent" + "\n");
            statement.append(", ST_AsMVTGeom(" + "\n");
            statement.append("    ST_Transform( ge.geometry, 3857 )" + "\n");
            statement.append("    , ST_TileEnvelope(" + zoom + ", " + x + ", " + y + ")" + "\n");
            statement.append("    , extent => 4096" + "\n");
            statement.append("    , buffer => 64" + "\n");
            statement.append("  ) AS geometry\n");
            statement.append(" FROM _fdata AS ge" + "\n");
            statement.append(" WHERE ST_Transform( ge.geometry, 3857 ) && ST_TileEnvelope(" + zoom + ", " + x + ", " + y + ", margin => (64.0 / 4096))" + "\n");
            statement.append(")" + "\n");

            // Create the tile layer
            statement.append("SELECT ST_AsMVT(mvtgeom.*, 'context')" + "\n");
            statement.append("FROM mvtgeom;" + "\n");

            try (ResultSet result = Database.query(statement.toString())) {
                if (result.next()) {
                    return result.getBytes(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return new byte[]{};

        }
    }


    public static class CacheCallable implements Callable<byte[]> {
        private final ThreadTransactionState state;

        private final String versionId;

        private final String typeCode;

        private final int x;

        private final int y;

        private final int zoom;

        public CacheCallable(ThreadTransactionState state, String versionId, String typeCode, int x, int y, int zoom) {
            super();
            this.state = state;
            this.versionId = versionId;
            this.typeCode = typeCode;
            this.x = x;
            this.y = y;
            this.zoom = zoom;
        }

        @Override
        public byte[] call() throws Exception {
            return this.call(this.state);
        }

        @Request(RequestType.THREAD)
        public byte[] call(ThreadTransactionState state) {
            try {
                PostgisVectorTileBuilder builder = new PostgisVectorTileBuilder(this.versionId, this.typeCode);
                byte[] tile = builder.write(zoom, x, y);

                if (tile.length < MdAttributeBlobDAO.getMaxLength()) {
                    this.populateTile(state, tile);
                }

                return tile;
            } catch (Exception e) {
                e.printStackTrace();

                return null;
            }
        }

        @Transaction(TransactionType.THREAD)
        private void populateTile(ThreadTransactionState state, byte[] tile) {
            LPGTileCache cache = new LPGTileCache();
            cache.setVersionId(this.versionId);
            cache.setTypeCode(this.typeCode);
            cache.setX(this.x);
            cache.setY(this.y);
            cache.setZ(this.zoom);
            cache.setTile(tile);
            cache.apply();
        }
    }

}
