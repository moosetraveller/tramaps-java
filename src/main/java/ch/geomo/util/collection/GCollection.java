/*
 * Copyright (c) 2016-2018 Thomas Zuberbuehler. All rights reserved.
 */

package ch.geomo.util.collection;

import ch.geomo.util.collection.list.EnhancedList;
import ch.geomo.util.collection.list.GList;
import ch.geomo.util.collection.pair.Pair;
import ch.geomo.util.collection.set.EnhancedSet;
import ch.geomo.util.collection.set.GSet;
import ch.geomo.util.collection.tuple.Tuple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public enum GCollection {

    /* factory class */;

    @NotNull
    @SafeVarargs
    public static <E> EnhancedList<E> list(@NotNull E... elements) {
        return GList.createList(elements);
    }

    @NotNull
    public static <E> EnhancedList<E> list(@NotNull Collection<E> c) {
        return GList.createList(c);
    }

    @NotNull
    public static <E> EnhancedList<E> mergeLists(@NotNull Collection<E> c1, @NotNull Collection<E> c2) {
        return GList.merge(c1, c2);
    }

    @NotNull
    @SafeVarargs
    public static <E> EnhancedSet<E> set(@NotNull E... elements) {
        return GSet.createSet(elements);
    }

    @NotNull
    public static <E> Pair<E> pair(@Nullable E firstElement, @Nullable E secondElement) {
        return Pair.of(firstElement, secondElement);
    }

    @NotNull
    public static <E> Pair<E> mutablePair(@Nullable E firstElement, @Nullable E secondElement) {
        return Pair.of(firstElement, secondElement, true);
    }

    @NotNull
    public static <T, S> Tuple<T, S> tuple(@Nullable T firstElement, @Nullable S secondElement) {
        return Tuple.createTuple(firstElement, secondElement);
    }

    @NotNull
    public static <T, S> Tuple<T, S> mutableTuple(@Nullable T firstElement, @Nullable S secondElement) {
        return Tuple.createTuple(firstElement, secondElement, true);
    }

}
