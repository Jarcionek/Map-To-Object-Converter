package uk.co.jpawlak.maptoobjectconverter;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

class CaseInsensitiveSet implements Set<String> {

    private final Set<String> set;

    CaseInsensitiveSet(Set<String> set) {
        this.set = set;
    }

    @Override
    public boolean contains(Object item) {
        return item instanceof String && set.stream()
                .filter(element -> element.equalsIgnoreCase((String) item))
                .findFirst()
                .isPresent();
    }

    @Override
    public Stream<String> stream() {
        return set.stream();
    }

    // not implemented

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<String> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEach(Consumer<? super String> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeIf(Predicate<? super String> filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Spliterator<String> spliterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<String> parallelStream() {
        throw new UnsupportedOperationException();
    }

}
