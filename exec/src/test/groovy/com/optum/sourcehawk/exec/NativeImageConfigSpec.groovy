package com.optum.sourcehawk.exec

import org.spockframework.util.IoUtil
import spock.lang.Specification

/**
 * The purpose of this spec is assert that we have proper configurations for the native image to function correctly
 */
class NativeImageConfigSpec extends Specification {

    def "config"() {
        when:
        Properties nativeImageProperties = new Properties()
        nativeImageProperties.load(IoUtil.getResourceAsStream("/META-INF/native-image/sourcehawk-exec/native-image.properties"))

        then:
        nativeImageProperties.containsKey("Args")

        when:
        String args = nativeImageProperties.getProperty("Args")

        then:
        args.contains("--gc=epsilon")
        args.contains("--enable-url-protocols=http,https")
        args.contains("--initialize-at-build-time=com.optum.sourcehawk.enforcer.file")
        args.contains("--initialize-at-build-time=com.fasterxml.jackson,org.yaml.snakeyaml")
        args.contains("--initialize-at-build-time=javax,jdk.xml.internal")
        args.contains("--initialize-at-build-time=com.sun.xml.internal.stream.util,com.sun.xml.internal.stream.XMLEntityStorage")
        args.contains("--initialize-at-build-time=com.sun.org.apache.xerces.internal.impl,com.sun.org.apache.xerces.internal.util,com.sun.org.apache.xerces.internal.xni,com.sun.org.apache.xerces.internal.utils")
    }

}
