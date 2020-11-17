package com.optum.sourcehawk.exec


import spock.lang.Shared

import java.security.Permission

class CliBaseSpecification extends FileBaseSpecification {

    @Shared
    private SecurityManager defaultSecurityManager

    def setupSpec() {
        defaultSecurityManager = System.getSecurityManager()
        System.setSecurityManager(new SystemExitSecurityManager())
    }

    def cleanupSpec() {
        System.setSecurityManager(defaultSecurityManager)
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
