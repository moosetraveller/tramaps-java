/*
 * Copyright (c) 2016-2018 Thomas Zuberbuehler. All rights reserved.
 */

package ch.geomo.util.collection.pair;

import ch.geomo.util.collection.tuple.Tuple;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a pair of one type. Two pairs are always are equal if both set the first pair values
 * are equals in any combination to the other pair's values.
 */
public interface Pair<T> extends Tuple<T, T> {

    /**
     * @return false if element is equals to null at the given index
     * @throws IndexOutOfBoundsException if index is neither 1 or 0
     */
    default boolean isNull(int index) {
        return get(index) == null;
    }

    /**
     * Gets the other value. Throws a {@link NoSuchElementException} if given value is not an item of the current pair.
     * @throws NoSuchElementException if given value is not a value of the current {@link Pair}
     */
    default T getOtherValue(T value) {
        if (Objects.equals(get(0), value)) {
            return get(1);
        }
        if (Objects.equals(get(1), value)) {
            return get(0);
        }
        throw new NoSuchElementException("Given value is not an item of this pair!");
    }

    /**
     * @return a {@link Stream} for this pair, note: stream may contain null values!
     */
    default Stream<T> stream() {
        return Stream.of(getFirst(), getSecond());
    }

    /**
     * @return a {@link Stream} for this pair without null value
     */
    default Stream<T> nonNullStream() {
        return Stream.of(getFirst(), getSecond())
                .filter(node -> node != null);
    }

    /**
     * @return true if given value is contained in this instance
     */
    default boolean contains(T value) {
        return Objects.equals(value, get(0)) || Objects.equals(value, get(1));
    }

    /**
     * Gets the first or second value by index. Index start with 0 in order to be consistent with other APIs. Only
     * 0 and 1 are allowed since only two items are hold by a pair.
     * @return the element at the given index
     * @throws IndexOutOfBoundsException if index is neither 1 or 0
     */
    default T get(int index) {
        if (index > 1 && index < 0) {
            throw new IndexOutOfBoundsException("An index > 1 or < 0 is not allowed.");
        }
        return index == 0 ? getFirst() : getSecond();
    }

    @NotNull
    default List<T> toList() {
        return stream()
                .filter(value -> value != null)
                .collect(Collectors.toList());
    }

    default boolean hasNonNullValues() {
        return first() != null || second() != null;
    }

    /**
     * @return a new {@link ImmutablePair} with given elements.
     */
    @NotNull
    static <T> Pair<T> of(T first, T second) {
        return new ImmutablePair<>(first, second);
    }

    /**
     * @return a new {@link MutablePair} or {@link ImmutablePair} with given elements depending the given boolean
     */
    @NotNull
    static <T> Pair<T> of(T first, T second, boolean mutable) {
        if (mutable) {
            return new MutablePair<>(first, second);
        }
        return new ImmutablePair<>(first, second);
    }

    /**
     * @return a {@link Set} of {@link ImmutablePair} with given collections
     */
    @NotNull
    static <T> Set<Pair<T>> from(Collection<T> col1, Collection<T> col2) {
        if (col1 == null || col2 == null) {
            return Collections.emptySet();
        }
        return col1.stream()
                .flatMap(v1 -> col2.stream()
                        .map(v2 -> Pair.of(v1, v2)))
                .collect(Collectors.toSet());
    }

}
