package sourcehawk


import spock.lang.Unroll

import java.util.concurrent.TimeUnit

class SourcehawkIntegrationSpec extends NativeImageSpecification {

    @Unroll
    def "sourcehawk #command"() {
        when:
        Process process = new ProcessBuilder([executable, command])
                .redirectErrorStream(false)
                .start()
        process.waitFor(5, TimeUnit.SECONDS)

        then:
        process.exitValue() == 0

        where:
        command << [ "-h", "--help", "help", "-V", "--version"]
    }

}
