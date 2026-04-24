package com.ccc.risk.credit.gai.service;

import com.ccc.risk.credit.gai.config.FeedProperties;
import com.jcraft.jsch.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

/**
 * Transfers feed files to the GAI SFTP target using JSch.
 *
 * <p>Retries the full transfer up to {@value MAX_ATTEMPTS} times on transient
 * network failures, with exponential backoff between attempts.
 *
 * <p>Transfer order: data files (EVENT, RECORD, ATTRIBUTE) first, then the
 * CONTROL file last. GAI uses the CONTROL file as the processing trigger —
 * all data files must be fully present before it arrives.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SftpTransferService {

    private static final int  MAX_ATTEMPTS       = 3;
    private static final long INITIAL_BACKOFF_MS = 5_000;  // 5 s → 10 s → 20 s

    private final FeedProperties properties;

    public void transfer(List<Path> paths) {
        FeedProperties.Sftp sftp = properties.getSftp();

        if (!sftp.isEnabled()) {
            log.warn("SFTP transfer disabled (gai.sftp.enabled=false). "
                   + "Files remain in: {}", properties.getOutputDirectory());
            paths.forEach(p -> log.info("  [local] {}", p.getFileName()));
            return;
        }

        if (paths.isEmpty()) {
            log.warn("No files to transfer via SFTP.");
            return;
        }

        Exception lastException = null;
        long backoffMs = INITIAL_BACKOFF_MS;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                log.info("SFTP attempt {}/{} — transferring {} file(s) to {}@{}:{}{}",
                        attempt, MAX_ATTEMPTS, paths.size(),
                        sftp.getUsername(), sftp.getHost(), sftp.getPort(),
                        sftp.getRemoteDirectory());

                doTransfer(sftp, paths);

                log.info("SFTP transfer completed successfully on attempt {}", attempt);
                return; // success — exit

            } catch (Exception e) {
                lastException = e;
                log.warn("SFTP attempt {}/{} failed: {}", attempt, MAX_ATTEMPTS, e.getMessage());

                if (attempt < MAX_ATTEMPTS) {
                    log.info("Retrying in {} ms...", backoffMs);
                    sleep(backoffMs);
                    backoffMs *= 2; // exponential backoff
                }
            }
        }

        throw new IllegalStateException(
                String.format("SFTP transfer failed after %d attempts. Last error: %s",
                        MAX_ATTEMPTS,
                        lastException != null ? lastException.getMessage() : "unknown"),
                lastException);
    }

    // -------------------------------------------------------------------------
    // Internal transfer
    // -------------------------------------------------------------------------

    private void doTransfer(FeedProperties.Sftp sftp, List<Path> paths)
            throws JSchException, SftpException {

        Session     session = null;
        ChannelSftp channel = null;

        try {
            session = buildSession(sftp);
            session.connect(30_000);

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(60_000);
            channel.cd(sftp.getRemoteDirectory());

            for (Path localFile : paths) {
                String remoteName = localFile.getFileName().toString();
                log.info("Transferring {} ...", remoteName);
                channel.put(localFile.toString(), remoteName, ChannelSftp.OVERWRITE);
                log.info("  ✓ {}", remoteName);
            }

        } finally {
            if (channel != null && channel.isConnected()) channel.disconnect();
            if (session != null && session.isConnected())  session.disconnect();
        }
    }

    private Session buildSession(FeedProperties.Sftp sftp) throws JSchException {
        JSch jsch = new JSch();
        if (sftp.getPrivateKeyPath() != null && !sftp.getPrivateKeyPath().isBlank()) {
            jsch.addIdentity(sftp.getPrivateKeyPath());
        }
        if (sftp.getKnownHostsPath() != null && !sftp.getKnownHostsPath().isBlank()) {
            jsch.setKnownHosts(sftp.getKnownHostsPath());
        }
        Session session = jsch.getSession(sftp.getUsername(), sftp.getHost(), sftp.getPort());
        if (sftp.getPassword() != null && !sftp.getPassword().isBlank()) {
            session.setPassword(sftp.getPassword());
        }
        if (sftp.getKnownHostsPath() == null || sftp.getKnownHostsPath().isBlank()) {
            session.setConfig("StrictHostKeyChecking", "no");
        }
        return session;
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }
}
