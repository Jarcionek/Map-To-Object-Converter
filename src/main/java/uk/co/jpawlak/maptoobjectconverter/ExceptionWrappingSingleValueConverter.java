package uk.co.jpawlak.maptoobjectconverter;

import uk.co.jpawlak.maptoobjectconverter.exceptions.RegisteredConverterException;

class ExceptionWrappingSingleValueConverter<T> implements SingleValueConverter<T> {

    private final SingleValueConverter<T> converter;

    ExceptionWrappingSingleValueConverter(SingleValueConverter<T> converter) {
        this.converter = converter;
    }

    @Override
    public T convert(Object value) {
        try {
            return converter.convert(value);
        } catch (Exception ex) {
            throw new RegisteredConverterException(ex);
        }
    }

}
