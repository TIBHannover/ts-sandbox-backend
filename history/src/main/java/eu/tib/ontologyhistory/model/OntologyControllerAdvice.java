//package eu.tib.ontologyhistory.model;
///*
//Might be used in a future instead of using MappingJacksonValue in the controller.
//Link to the supported stackoverflow answer:
//https://stackoverflow.com/a/40347560/20689527
// */
//
//import eu.tib.ontologyhistory.controller.OndetController;
//import eu.tib.ontologyhistory.view.Views;
//import org.springframework.core.MethodParameter;
//import org.springframework.http.MediaType;
//import org.springframework.http.converter.json.MappingJacksonValue;
//import org.springframework.http.server.ServerHttpRequest;
//import org.springframework.http.server.ServerHttpResponse;
//import org.springframework.http.server.ServletServerHttpRequest;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.servlet.mvc.method.annotation.AbstractMappingJacksonResponseBodyAdvice;
//import org.springframework.web.servlet.mvc.method.annotation.JsonViewResponseBodyAdvice;
//
//@ControllerAdvice(assignableTypes = OndetController.class)
//public class OntologyControllerAdvice extends AbstractMappingJacksonResponseBodyAdvice {
//
//    @Override
//    protected void beforeBodyWriteInternal(MappingJacksonValue bodyContainer,
//                                           MediaType contentType,
//                                           MethodParameter returnType,
//                                           ServerHttpRequest request,
//                                           ServerHttpResponse response) {
//        ServletServerHttpRequest req = (ServletServerHttpRequest) request;
//        String view = req.getServletRequest().getParameter("view");
//
//        if (view == null || view.isEmpty()) {
//            view = "full";
//        }
//
//        switch (view) {
//            case "short" -> bodyContainer.setSerializationView(Views.Short.class);
//            case "full" -> bodyContainer.setSerializationView(Views.Full.class);
//            default -> System.out.println("No view specified");
//        }
//    }
//
//    private void wrapWithJsonApi(MappingJacksonValue bodyContainer) {
//        JsonApiWrapper<Object> wrapper = new JsonApiWrapper<>(bodyContainer);
//        bodyContainer.setValue(wrapper);
//    }
//}
//
