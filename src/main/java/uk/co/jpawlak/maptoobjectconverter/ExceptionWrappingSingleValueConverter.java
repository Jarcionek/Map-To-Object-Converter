package uk.co.jpawlak.maptoobjectconverter;

class ExceptionWrappingSingleValueConverter<T> implements SingleValueConverter<T> {

    private final SingleValueConverter<T> converter;

    public ExceptionWrappingSingleValueConverter(SingleValueConverter<T> converter) {
        this.converter = converter;
    }

    @Override
    public T convert(Object value) {
        return convertedValue(converter, value);
    }

    private T convertedValue(SingleValueConverter<T> converter, Object value) {
        try {
            return converter.convert(value);
        } catch (Exception ex) {
            throw new RegisteredConverterException(ex);
        }
    }

}
