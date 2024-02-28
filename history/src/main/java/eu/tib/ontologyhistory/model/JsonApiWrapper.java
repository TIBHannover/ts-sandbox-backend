package eu.tib.ontologyhistory.model;

public class JsonApiWrapper<T> {
    private T data;

    public JsonApiWrapper(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
