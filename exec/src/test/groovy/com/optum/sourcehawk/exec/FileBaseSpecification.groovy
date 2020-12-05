package com.optum.sourcehawk.exec

import org.spockframework.util.IoUtil
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

class FileBaseSpecification extends Specification {

    @Shared
    protected Path testResourcesRoot = Paths.get(IoUtil.getResource("/marker" ).toURI())
            .getParent()

    @Shared
    protected Path repositoryRoot = Paths.get(IoUtil.getResource("/marker").toURI())
            .getParent() // test
            .getParent() // src
            .getParent() // exec
            .getParent() // (root)

}
