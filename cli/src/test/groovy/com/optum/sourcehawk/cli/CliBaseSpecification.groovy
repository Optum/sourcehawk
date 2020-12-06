package com.optum.sourcehawk.cli

import org.spockframework.util.IoUtil
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.Permission

class CliBaseSpecification extends Specification {

    @Shared
    private SecurityManager defaultSecurityManager

    @Shared
    protected Path testResourcesRoot = Paths.get(IoUtil.getResource("/marker" ).toURI())
            .getParent()

    @Shared
    protected Path repositoryRoot = Paths.get(IoUtil.getResource("/marker").toURI())
            .getParent() // test
            .getParent() // src
            .getParent() // cli
            .getParent() // (root)

    def setupSpec() {
        defaultSecurityManager = System.getSecurityManager()
        System.setSecurityManager(new SystemExitSecurityManager())
    }

    def cleanupSpec() {
        System.setSecurityManager(defaultSecurityManager)
    }

    protected void createParentDirectories(final File child) {
        try {
            Files.createDirectories(Paths.get(child.getParent()))
        } catch (Exception e) { }
    }

    protected static class SystemExitSecurityManager extends SecurityManager {

        @Override
        void checkPermission(Permission perm) { }

        @Override
        void checkPermission(Permission perm, Object context) { }

        @Override
        void checkExit(int status) {
            super.checkExit(status);
            throw new SystemExit(status);
        }
    }

    protected static class SystemExit extends SecurityException {

        public final int status

        SystemExit(int status) {
            super("Status: ${status}")
            this.status = status
        }

    }

}
