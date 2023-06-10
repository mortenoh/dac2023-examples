package com.example.dhis2.routes;

import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.hisp.dhis.api.model.v2_39_1.Metadata;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class DhisOrgUnitRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        // trigger once every startup
        from("timer:foo?repeatCount=1")
                .to("direct:read-dhis-ou");

        // Read OU from source and unmarshall into MetaData
        from("direct:read-dhis-ou")
                .routeId("Read OrgUnits")
                .setHeader("CamelDhis2.queryParams", () -> Map.of(
                        "fields", "id,code,name,shortName,description,openingDate,parent[id]",
                        "paging", "false"
                ))
                .to("dhis2://get/resource?path=organisationUnits&client=#dhis2Client")
                .unmarshal().json(Metadata.class)
                .convertBodyTo( Bundle.class );

        // Expose as API
        from("direct:get-dhis-ou")
                .to("direct:read-dhis-ou")
                .marshal().fhirJson( "R4", true );

        rest("/")
                .get("/ou")
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .to("direct:get-dhis-ou");
    }
}