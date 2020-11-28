package com.optum.sourcehawk.core.data;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

/**
 * Holds a pair of values
 *
 * @author Brian Wyka
 */
@Value
@AllArgsConstructor(staticName = "of")
public class Pair<T, S> {

    @NonNull
    T left;

    @NonNull
    S right;

}
