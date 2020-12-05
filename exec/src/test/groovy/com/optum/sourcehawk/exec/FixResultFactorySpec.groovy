package com.optum.sourcehawk.exec

import com.optum.sourcehawk.core.scan.FixResult
import com.optum.sourcehawk.enforcer.ResolverResult
import com.optum.sourcehawk.enforcer.file.common.StringPropertyEquals
import com.optum.sourcehawk.core.protocol.file.FileProtocol
import spock.lang.Specification

class FixResultFactorySpec extends Specification {

    def "resolverResult - no updates"() {
        given:
        Map<String, Object> enforcers = [
                "path/file.ext": StringPropertyEquals.equals("key", "value")
        ]
        FileProtocol fileProtocol = FileProtocol.builder()
                .repositoryPath("path/file.ext")
                .name("protocol")
                .enforcers([ enforcers ])
                .build()
        ResolverResult resolverResult = ResolverResult.NO_UPDATES

        when:
        FixResult fixResult = FixResultFactory.resolverResult(fileProtocol, resolverResult, false)

        then:
        fixResult
        !fixResult.fixesApplied
        fixResult.fixCount == 0
        !fixResult.error
        fixResult.errorCount == 0
        !fixResult.noResolver
        !fixResult.messages
        !fixResult.formattedMessages
    }

    def "resolverResult - updates"() {
        given:
        Map<String, Object> enforcers = [
                "path/file.ext": StringPropertyEquals.equals("key", "value")
        ]
        FileProtocol fileProtocol = FileProtocol.builder()
                .repositoryPath("path/file.ext")
                .name("protocol")
                .enforcers([ enforcers ])
                .build()
        ResolverResult resolverResult = ResolverResult.updatesApplied("All fixed up")

        when:
        FixResult fixResult = FixResultFactory.resolverResult(fileProtocol, resolverResult, false)

        then:
        fixResult
        fixResult.fixesApplied
        fixResult.fixCount == 1
        !fixResult.error
        fixResult.errorCount == 0
        !fixResult.noResolver
        fixResult.messages
        fixResult.messages.size() == 1
        fixResult.messages["path/file.ext"].size() == 1
        fixResult.messages["path/file.ext"][0].repositoryPath == "path/file.ext"
        fixResult.messages["path/file.ext"][0].message == "All fixed up"
        fixResult.formattedMessages
        fixResult.formattedMessages.size() == 1
        fixResult.formattedMessages[0] == "path/file.ext :: All fixed up"
    }

    def "resolverResult - error"() {
        given:
        Map<String, Object> enforcers = [
                "path/file.ext": StringPropertyEquals.equals("key", "value")
        ]
        FileProtocol fileProtocol = FileProtocol.builder()
                .repositoryPath("path/file.ext")
                .name("protocol")
                .enforcers([ enforcers ])
                .build()
        ResolverResult resolverResult = ResolverResult.error("BOOM")

        when:
        FixResult fixResult = FixResultFactory.resolverResult(fileProtocol, resolverResult, false)

        then:
        fixResult
        !fixResult.fixesApplied
        fixResult.fixCount == 0
        fixResult.error
        fixResult.errorCount == 1
        !fixResult.noResolver
        fixResult.messages
        fixResult.messages.size() == 1
        fixResult.messages["path/file.ext"].size() == 1
        fixResult.messages["path/file.ext"][0].repositoryPath == "path/file.ext"
        fixResult.messages["path/file.ext"][0].message == "BOOM"
        fixResult.formattedMessages
        fixResult.formattedMessages.size() == 1
        fixResult.formattedMessages[0] == "path/file.ext :: BOOM"
    }

    def "resolverResult - updates - dry run"() {
        given:
        Map<String, Object> enforcers = [
                "path/file.ext": StringPropertyEquals.equals("key", "value")
        ]
        FileProtocol fileProtocol = FileProtocol.builder()
                .repositoryPath("path/file.ext")
                .name("protocol")
                .enforcers([ enforcers ])
                .build()
        ResolverResult resolverResult = ResolverResult.updatesApplied("All fixed up")

        when:
        FixResult fixResult = FixResultFactory.resolverResult(fileProtocol, resolverResult, true)

        then:
        fixResult
        !fixResult.fixesApplied
        fixResult.fixCount == 1
        !fixResult.error
        fixResult.errorCount == 0
        !fixResult.noResolver
        fixResult.messages
        fixResult.messages.size() == 1
        fixResult.messages["path/file.ext"].size() == 1
        fixResult.messages["path/file.ext"][0].repositoryPath == "path/file.ext"
        fixResult.messages["path/file.ext"][0].message == "All fixed up"
        fixResult.formattedMessages
        fixResult.formattedMessages.size() == 1
        fixResult.formattedMessages[0] == "path/file.ext :: All fixed up"
    }

    def "error"() {
        given:
        FixResult fixResult = FixResultFactory.error("path/file.ext", "BOOM")

        expect:
        fixResult
        !fixResult.fixesApplied
        fixResult.fixCount == 0
        fixResult.error
        fixResult.errorCount == 1
        !fixResult.noResolver
        fixResult.messages.size() == 1
        fixResult.messages['path/file.ext'].size() == 1
        fixResult.messages['path/file.ext'][0].repositoryPath == 'path/file.ext'
        fixResult.messages['path/file.ext'][0].message == 'BOOM'
        fixResult.formattedMessages.size() == 1
        fixResult.formattedMessages[0] == 'path/file.ext :: BOOM'
    }

    def "noResolver"() {
        given:
        FixResult fixResult = FixResultFactory.noResolver('path/file.ext', '.common.Sha256ChecksumEquals')

        expect:
        fixResult
        !fixResult.fixesApplied
        fixResult.fixCount == 0
        !fixResult.error
        fixResult.errorCount == 0
        fixResult.noResolver
        fixResult.messages.size() == 1
        fixResult.messages['path/file.ext'].size() == 1
        fixResult.messages['path/file.ext'][0].repositoryPath == 'path/file.ext'
        fixResult.messages['path/file.ext'][0].message == 'No fixes applied, file enforcer .common.Sha256ChecksumEquals does not have any resolutions'
        fixResult.formattedMessages.size() == 1
        fixResult.formattedMessages[0] == 'path/file.ext :: No fixes applied, file enforcer .common.Sha256ChecksumEquals does not have any resolutions'
    }

    def "fileNotFound"() {
        given:
        Map<String, Object> enforcers = [
                "path/file.ext": StringPropertyEquals.equals("key", "value")
        ]
        FileProtocol fileProtocol = FileProtocol.builder()
                .repositoryPath("path/file.ext")
                .name("protocol")
                .enforcers([ enforcers ])
                .build()

        when:
        FixResult fixResult = FixResultFactory.fileNotFound(fileProtocol)

        then:
        fixResult
        !fixResult.fixesApplied
        fixResult.fixCount == 0
        fixResult.error
        fixResult.errorCount == 1
        !fixResult.noResolver
        fixResult.messages.size() == 1
        fixResult.messages['path/file.ext'].size() == 1
        fixResult.messages['path/file.ext'][0].repositoryPath == 'path/file.ext'
        fixResult.messages['path/file.ext'][0].message == 'File not found'
        fixResult.formattedMessages.size() == 1
        fixResult.formattedMessages[0] == 'path/file.ext :: File not found'
    }

}
