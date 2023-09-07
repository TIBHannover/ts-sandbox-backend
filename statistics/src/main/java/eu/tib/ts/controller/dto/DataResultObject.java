package eu.tib.ts.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DataResultObject<T> {

    @JsonProperty(value = "data")
    private T data;

    public DataResultObject(T data) {
        this.data = data;
    }
}
