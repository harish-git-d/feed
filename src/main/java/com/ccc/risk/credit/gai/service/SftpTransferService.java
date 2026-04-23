package com.ccc.risk.credit.gai.service;

import com.ccc.risk.credit.gai.config.FeedProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

@Service
public class SftpTransferService {

    private static final Logger log = LoggerFactory.getLogger(SftpTransferService.class);

    private final FeedProperties properties;

    public SftpTransferService(FeedProperties properties) {
        this.properties = properties;
    }

    public void transfer(List<Path> paths) {
        if (!properties.getSftp().isEnabled()) {
            log.info("SFTP transfer disabled; files left in {}", properties.getOutputDirectory());
            return;
        }

        for (Path path : paths) {
            log.info("TODO: transfer {} to {}@{}:{}{}",
                    path.getFileName(),
                    properties.getSftp().getUsername(),
                    properties.getSftp().getHost(),
                    properties.getSftp().getPort(),
                    properties.getSftp().getRemoteDirectory());
        }
    }
}
