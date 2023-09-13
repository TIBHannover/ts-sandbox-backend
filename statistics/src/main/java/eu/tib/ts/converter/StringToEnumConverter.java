package eu.tib.ts.converter;

import eu.tib.ts.model.ontology.CharacteristicsType;
import org.springframework.core.convert.converter.Converter;

public class StringToEnumConverter implements Converter<String, CharacteristicsType> {
    @Override
    public CharacteristicsType convert(String source) {
        return CharacteristicsType.valueOf(source.toUpperCase());
    }
}