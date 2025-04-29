package com.michael.container;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.TestAbortedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DockerExtension implements BeforeAllCallback {
  private static final String DOCKER_NOT_RUNNING_LOCALLY =
      "Skipping test: Docker is not running locally. Skipping test.";
  private static final Logger logger = LoggerFactory.getLogger(DockerExtension.class);

  @Override
  public void beforeAll(ExtensionContext context) {
    if (isDockerRunning()) {
      return;
    }
    logger.warn(DOCKER_NOT_RUNNING_LOCALLY);
    throw new TestAbortedException(DOCKER_NOT_RUNNING_LOCALLY);
  }

  private boolean isDockerRunning() {
    try {
      ProcessBuilder builder = new ProcessBuilder("docker", "info");
      builder.redirectErrorStream(true);
      Process process = builder.start();
      int exitCode = process.waitFor();
      return exitCode == 0;
    } catch (Exception e) {
      return false;
    }
  }
}
