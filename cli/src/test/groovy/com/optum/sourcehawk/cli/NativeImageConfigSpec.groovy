package com.optum.sourcehawk.cli

import org.spockframework.util.IoUtil
import spock.lang.Specification

class NativeImageConfigSpec extends Specification {

    def "config"() {
        when:
        Properties nativeImageProperties = new Properties()
        nativeImageProperties.load(IoUtil.getResourceAsStream("/META-INF/native-image/sourcehawk-cli/native-image.properties"))

        then:
        nativeImageProperties.containsKey("Args")

        when:
        String args = nativeImageProperties.getProperty("Args")

        then:
        args.contains("-H:Class=${Sourcehawk.name}")
        args.contains("-H:Name=sourcehawk")
        args.contains("--no-server")
    }

    def "all native image configs are on classpath"() {
        expect:
        IoUtil.getResourceAsStream("/META-INF/native-image/sourcehawk-core/reflect-config.json")
        IoUtil.getResourceAsStream("/META-INF/native-image/sourcehawk-exec/native-image.properties")
        IoUtil.getResourceAsStream("/META-INF/native-image/sourcehawk-cli/reflect-config.json")
        IoUtil.getResourceAsStream("/META-INF/native-image/sourcehawk-generated/sourcehawk-enforcer-file/reflect-config.json")
    }

}
