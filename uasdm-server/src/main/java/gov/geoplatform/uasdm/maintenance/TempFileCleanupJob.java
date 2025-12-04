package gov.geoplatform.uasdm.maintenance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TempFileCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(TempFileCleanupJob.class);

    /**
     * Directory to clean.
     * Defaults to java.io.tmpdir if not set.
     */
    @Value("${app.temp-dir:#{systemProperties['java.io.tmpdir']}}")
    private String tempDir;

    /**
     * Retention in days. Files older than this are deleted.
     */
    @Value("${app.temp-retention-days:10}")
    private long retentionDays;

    /**
     * Runs once a day at 03:00.
     * Cron format: second minute hour day-of-month month day-of-week
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanUpTempFiles() {
        Path dir = Paths.get(tempDir);

        if (!Files.isDirectory(dir)) {
            log.trace("TempFileCleanupJob: '{}' is not a directory, skipping cleanup", dir);
            return;
        }

        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        log.trace("TempFileCleanupJob: cleaning '{}' for files older than {} ({} days)",
                 dir, cutoff, retentionDays);

        // First delete old files
        try (Stream<Path> paths = Files.walk(dir)) {
            paths
                .filter(Files::isRegularFile)
                .forEach(path -> deleteIfOlderThan(path, cutoff));
        } catch (IOException e) {
            log.trace("TempFileCleanupJob: error walking directory {}", dir, e);
        }

        // Optionally, delete empty directories afterwards
        try (Stream<Path> paths = Files.walk(dir)) {
            paths
                .filter(Files::isDirectory)
                .sorted((a, b) -> b.getNameCount() - a.getNameCount()) // deepest first
                .forEach(this::deleteIfEmpty);
        } catch (IOException e) {
            log.trace("TempFileCleanupJob: error removing empty directories in {}", dir, e);
        }
    }

    private void deleteIfOlderThan(Path path, Instant cutoff) {
        try {
            FileTime lastModified = Files.getLastModifiedTime(path);
            if (lastModified.toInstant().isBefore(cutoff)) {
                Files.delete(path);
                log.debug("TempFileCleanupJob: deleted {}", path);
            }
        } catch (IOException e) {
            log.trace("TempFileCleanupJob: failed to delete {}", path, e);
        }
    }

    private void deleteIfEmpty(Path dir) {
        try (Stream<Path> children = Files.list(dir)) {
            if (!children.findAny().isPresent() && !dir.equals(dir.getRoot())) {
                Files.delete(dir);
                log.debug("TempFileCleanupJob: deleted empty directory {}", dir);
            }
        } catch (IOException e) {
            // Not critical
        }
    }
}
