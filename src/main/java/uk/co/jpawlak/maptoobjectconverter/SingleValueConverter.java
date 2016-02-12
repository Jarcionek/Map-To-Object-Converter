package uk.co.jpawlak.maptoobjectconverter;

public interface SingleValueConverter<T> {

    T convert(Object value);

}
