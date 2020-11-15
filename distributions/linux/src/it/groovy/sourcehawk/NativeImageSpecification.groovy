package sourcehawk

import spock.lang.Shared
import spock.lang.Specification

class NativeImageSpecification extends Specification {

    @Shared
    protected String moduleRoot = new File(NativeImageSpecification.classLoader.getResource("marker").toURI())
            .getParentFile() // test
            .getParentFile() // src
            .getParentFile() // linux
            .getAbsolutePath()

    @Shared
    protected String resourcesRoot = new File(NativeImageSpecification.classLoader.getResource("marker").toURI())
            .getParentFile()
            .getAbsolutePath()

    protected String executable = "${moduleRoot}/target/dist/sourcehawk".toString()

}
