package com.optum.sourcehawk.exec.graal.nativeimage

import com.optum.sourcehawk.exec.Sourcehawk
import org.spockframework.util.IoUtil
import spock.lang.Specification

/**
 * The purpose of this spec is assert that we have proper configurations in the native-image.properties file
 */
class NativeImageConfigSpec extends Specification {

    String nativeImageConfigResourcePath = "/META-INF/native-image/sourcehawk-exec/native-image.properties"

    def "config"() {
        when:
        Properties nativeImageProperties = new Properties()
        nativeImageProperties.load(getClass().getResourceAsStream(nativeImageConfigResourcePath))

        then:
        nativeImageProperties.containsKey("Args")

        when:
        String args = nativeImageProperties.getProperty("Args")

        then:
        args.contains("-H:Class=${Sourcehawk.class.name}")
        args.contains("--no-server")
        args.contains("--enable-url-protocols=http,https")
        args.contains("--initialize-at-build-time=ch.qos.logback,org.fusesource.jansi,com.fasterxml.jackson,org.slf4j,org.yaml.snakeyaml,javax")
        args.contains("--initialize-at-build-time=com.sun.org.apache.xerces.internal.util,com.sun.org.apache.xerces.internal.impl")
        args.contains("--initialize-at-build-time=jdk.xml.internal,com.sun.xml.internal.stream.util,com.sun.xml.internal.stream.XMLEntityStorage,com.sun.org.apache.xerces.internal.xni,com.sun.org.apache.xerces.internal.utils")
    }

    def "all reflect configs are on classpath"() {
        expect:
        IoUtil.getResourceAsStream("/META-INF/native-image/sourcehawk-core/reflect-config.json")
        IoUtil.getResourceAsStream("/META-INF/native-image/sourcehawk-exec/reflect-config.json")
        IoUtil.getResourceAsStream("/META-INF/native-image/sourcehawk-generated/sourcehawk-enforcer-file/reflect-config.json")
    }

}
