package com.optum.sourcehawk.enforcer.file.xml;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.optum.sourcehawk.enforcer.EnforcerResult;
import com.optum.sourcehawk.enforcer.file.AbstractFileEnforcer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.val;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An enforcer implementation which enforces that the result of a JsonPath query equals a specific value
 *
 * @see javax.xml.xpath.XPath
 *
 * @author Brian Wyka
 */
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = XPathEquals.Builder.class)
@AllArgsConstructor(staticName = "equals")
public class XPathEquals extends AbstractFileEnforcer {

    private static final String XPATH_SETUP_ERROR = "XPath initialization resulted in error [%s]";
    private static final String QUERY_ERROR_TEMPLATE = "Execution of query [%s] yielded error [%s]";
    private static final String MISSING_MESSAGE_TEMPLATE = "Execution of query [%s] yielded no result";
    private static final String NOT_EQUAL_MESSAGE_TEMPLATE = "Execution of query [%s] yielded result [%s] which is not equal to [%s]";

    /**
     * Key: The XPath query to retrieve the value
     * @see XPathEquals
     *
     * Value: The expected value which the query should evaluate to
     */
    private final Map<String, String> expectations;

    /**
     * Create with a single path query and exepected value
     *
     * @param xPathQuery the xPath query
     * @param expectedValue the expected value
     * @return the enforcer
     */
    public static XPathEquals equals(final String xPathQuery, final String expectedValue) {
        return XPathEquals.equals(Collections.singletonMap(xPathQuery, expectedValue));
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("squid:S2755") // https://community.sonarsource.com/t/java-rule-squid-s2755-false-positive/10554
    public EnforcerResult enforceInternal(@NonNull final InputStream actualFileInputStream) {
        final Document xmlDocument;
        final XPath xPath;
        try {
            val documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            val documentBuilder = documentBuilderFactory.newDocumentBuilder();
            xmlDocument = documentBuilder.parse(actualFileInputStream);
            xPath = XPathFactory.newInstance().newXPath();
        } catch (final Exception e) {
            return EnforcerResult.failed(String.format(XPATH_SETUP_ERROR, e.getMessage()));
        }
        val messages = expectations.entrySet()
                .stream()
                .map(entry -> enforce(xmlDocument, xPath, entry.getKey(), entry.getValue()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        return EnforcerResult.create(messages);
    }

    /**
     * Enforce individual XPath queries
     *
     * @param xmlDocument the XML document
     * @param xPath the xPath query
     * @param xPathQuery the xPath query
     * @param expectedValue the expected value
     * @return The message to be added, otherwise {@link Optional#empty()}
     */
    private static Optional<String> enforce(final Document xmlDocument, final XPath xPath, final String xPathQuery, final String expectedValue) {
        try {
            val node = (Node) xPath.compile(xPathQuery).evaluate(xmlDocument, XPathConstants.NODE);
            if (node == null || node.getNodeValue() == null) {
                return Optional.of(String.format(MISSING_MESSAGE_TEMPLATE, xPathQuery));
            }
            val actualValue = node.getNodeValue();
            if (Objects.equals(expectedValue, actualValue)) {
                return Optional.empty();
            }
            return Optional.of(String.format(NOT_EQUAL_MESSAGE_TEMPLATE, xPathQuery, actualValue, expectedValue));
        } catch (final Exception e) {
            return Optional.of(String.format(QUERY_ERROR_TEMPLATE, xPathQuery, e.getMessage()));
        }
    }

}
