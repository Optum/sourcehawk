package sourcehawk


import spock.lang.Unroll

import java.util.concurrent.TimeUnit

class ScanIntegrationSpec extends NativeImageSpecification {

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
                ["scan", "-h"],
                ["scan", "--help"],
                ["scan", "help"],
                ["flyover", "-h"],
                ["flyover", "--help"],
                ["flyover", "help"],
                ["survey", "-h"],
                ["survey", "--help"],
                ["survey", "help"]
        ]
    }

    def "sourcehawk scan (passed)"() {
        when:
        Process process = new ProcessBuilder([executable, "scan", "${resourcesRoot}/passed".toString()])
                .redirectErrorStream(false)
                .start()
        process.waitFor(5, TimeUnit.SECONDS)
        String output = process.in.text

        then:
        output.contains("Scan passed without any errors")

        and:
        process.exitValue() == 0
    }

    def "sourcehawk scan (passed - warning)"() {
        when:
        Process process = new ProcessBuilder([executable, "scan", "${resourcesRoot}/passed-warning".toString()])
                .redirectErrorStream(false)
                .start()
        process.waitFor(5, TimeUnit.SECONDS)
        String output = process.in.text

        then:
        output.contains("Scan passed without any errors")

        and:
        process.exitValue() == 0
    }

    def "sourcehawk scan (failed)"() {
        when:
        Process process = new ProcessBuilder([executable, "scan", "${resourcesRoot}/failed".toString()])
                .redirectErrorStream(false)
                .start()
        process.waitFor(5, TimeUnit.SECONDS)
        String output = process.in.text

        then:
        output.contains("Scan resulted in failure. Error(s): 1, Warning(s): 0")
        output.contains("Property [abc] with value [456] does not equal [123]")

        and:
        process.exitValue() == 1
    }

    def "sourcehawk (parse error)"() {
        when:
        Process process = new ProcessBuilder([executable, "scan", "-a", "bad"])
                .redirectErrorStream(false)
                .start()
        process.waitFor(5, TimeUnit.SECONDS)

        then:
        process.exitValue() == 2

        and:
        process.getErrorStream().text.contains("Unknown option: '-a'")
    }

    def "sourcehawk (scan error)"() {
        when:
        Process process = new ProcessBuilder([executable, "scan", "/temp/file-not-found"])
                .redirectErrorStream(false)
                .start()
        process.waitFor(5, TimeUnit.SECONDS)

        then:
        process.exitValue() == 1

        when:
        String output = process.in.text

        then:
        output.contains("Configuration file not found")
    }

}
