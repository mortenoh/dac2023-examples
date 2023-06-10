package com.example.dhis2.routes;

import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DhisFhirOrgUnitRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("timer:foo?repeatCount=1")
                .log("FHIR");
    }
}
