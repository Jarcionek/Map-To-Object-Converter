package uk.co.jpawlak.maptoobjectconverter;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

class CaseInsensitiveMap implements Map<String, Object> {

    private final Map<String, Object> map;

    CaseInsensitiveMap(Map<String, Object> map) {
        this.map = map;
    }

    @Override
    public Object get(Object key) {
        if (!(key instanceof String)) {
            return null;
        }

        return map.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase((String) key))
                .findFirst()
                .map(Entry::getValue)
                .orElse(null);
    }

    @Override
    public Set<String> keySet() {
        return new CaseInsensitiveSet(map.keySet());
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
    public boolean containsKey(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object put(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Object> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super Object> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super Object, ?> function) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object putIfAbsent(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace(String key, Object oldValue, Object newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object replace(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object computeIfAbsent(String key, Function<? super String, ?> mappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object computeIfPresent(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object compute(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object merge(String key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        throw new UnsupportedOperationException();
    }

}
