package com.optum.sourcehawk.enforcer.file.docker;

import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.file.docker.utils.DockerfileParser;
import lombok.AllArgsConstructor;
import lombok.val;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Enforce that the Dockerfile has a specific image in the FROM line
 *
 * @author Brian Wyka
 */
@AllArgsConstructor(staticName = "equals")
public class DockerfileFromImageEquals extends AbstractDockerfileTokenEnforcer {

    private static final String INCORRECT_FROM_MESSAGE = "Dockerfile FROM image [%s] does not contain [%s]";

    /**
     * The expected host that should be included in the FROM line
     */
    private final String expectedFromImage;

    /** {@inheritDoc} */
    @Override
    protected String getToken() {
        return DockerfileParser.FROM_TOKEN;
    }

    /** {@inheritDoc} */
    @Override
    protected Optional<Predicate<String>> getTokenValueFilter() {
        return Optional.of(tokenValue -> !DockerfileParser.FROM_SCRATCH.equals(tokenValue));
    }

    /**
     * Enforce the FROM image is as expected
     *
     * @param fromValue the FROM value
     * @return the enforcer result
     */
    @Override
    protected EnforcerResult enforceToken(final String fromValue) {
        val fromPieces = fromValue.split("/");
        val piece = fromPieces.length - 1;
        if (fromPieces[piece].contains(expectedFromImage)) {
            return EnforcerResult.passed();
        }
        return EnforcerResult.failed(String.format(INCORRECT_FROM_MESSAGE, fromPieces[piece], expectedFromImage));
    }

}
