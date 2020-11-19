package com.optum.sourcehawk.enforcer.file.docker;

import com.optum.sourcehawk.core.utils.StringUtils;
import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.file.docker.utils.DockerfileParser;
import lombok.AllArgsConstructor;
import lombok.val;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Enforce that the Dockerfile has a tag in the FROM line
 *
 * @author Brian Wyka
 */
@AllArgsConstructor(staticName = "allowLatest")
public class DockerfileFromHasTag extends AbstractDockerfileTokenEnforcer {

    private static final String MISSING_TAG_MESSAGE = "Dockerfile FROM [%s] is missing tag";
    private static final String LATEST_TAG_MESSAGE = "Dockerfile FROM [%s] has 'latest' tag";
    private static final String TAG_LATEST = "latest";

    /**
     * Whether or not to allow the "latest" tag
     */
    private final boolean allowLatest;

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
     * Enforce the FROM tag is as expected
     *
     * @param fromValue the from value
     * @return the enforcer result
     */
    @Override
    protected EnforcerResult enforceToken(final String fromValue) {
        if (fromValue.contains(":")) {
            val fromPieces = fromValue.split(":");
            if (fromPieces.length > 1) {
                val tag = fromPieces[1];
                if (StringUtils.equals(tag, TAG_LATEST) && !allowLatest) {
                    return EnforcerResult.failed(String.format(LATEST_TAG_MESSAGE, fromPieces[0]));
                }
                return EnforcerResult.passed();
            }
        }
        return EnforcerResult.failed(String.format(MISSING_TAG_MESSAGE, fromValue));
    }

}
