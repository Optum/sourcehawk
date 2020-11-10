package sourcehawk


import spock.lang.Unroll

import java.util.concurrent.TimeUnit

class FixIntegrationSpec extends NativeImageSpecification {

    @Unroll
    def "sourcehawk #args"() {
        when:
        Process process = new ProcessBuilder([executable] + args)
                .redirectErrorStream(false)
                .start()
        process.waitFor(5, TimeUnit.SECONDS)

        then:
        process.exitValue() == 0

        where:
        args << [
                ["fix", "-h"],
                ["fix", "--help"],
                ["correct", "-h"],
                ["correct", "--help"],
                ["resolve", "-h"],
                ["resolve", "--help"]
        ]
    }

     // TODO

}
