package sourcehawk


import spock.lang.Unroll

import java.util.concurrent.TimeUnit

class ValidateConfigIntegrationSpec extends NativeImageSpecification {

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
                ["validate-config", "-h"],
                ["validate-config", "--help"],
                ["vc", "-h"],
                ["vc", "--help"]
        ]
    }

    def "sourcehawk validate-config (valid)"() {
        when:
        Process process = new ProcessBuilder([executable, "validate-config", "${resourcesRoot}/passed".toString()])
                .redirectErrorStream(false)
                .start()
        process.waitFor(5, TimeUnit.SECONDS)
        String output = process.in.text

        then:
        process.exitValue() == 0

        and:
        output.contains("Congratulations, you have created a valid configuration file")
    }

    def "sourcehawk validate-config - stdin (valid)"() {
        when:
        Process process = new ProcessBuilder([executable, "validate-config", "${resourcesRoot}/passed".toString()])
                .redirectInput(new File("${resourcesRoot}/passed/sourcehawk.yml"))
                .redirectErrorStream(false)
                .start()
        process.waitFor(5, TimeUnit.SECONDS)
        String output = process.in.text

        then:
        process.exitValue() == 0

        and:
        output.contains("Congratulations, you have created a valid configuration file")
    }

    def "sourcehawk validate-config (invalid)"() {
        when:
        Process process = new ProcessBuilder([executable, "validate-config", "${resourcesRoot}/invalid".toString()])
                .redirectErrorStream(false)
                .start()
        process.waitFor(5, TimeUnit.SECONDS)
        String output = process.in.text

        then:
        process.exitValue() == 1

        and:
        output.contains("repositoryPath is marked non-null but is null")
    }

}
