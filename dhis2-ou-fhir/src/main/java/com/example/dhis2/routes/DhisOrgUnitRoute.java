package com.example.dhis2.routes;

import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.hisp.dhis.api.model.v2_39_1.Metadata;
import org.hisp.dhis.api.model.v2_39_1.OrganisationUnit;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DhisOrgUnitRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        // trigger once every startup
        from("timer:foo?repeatCount=1")
                .routeId("Initial Sync")
                .to("direct:trigger-sync");

        // Route for triggering sync
        from("direct:trigger-sync")
                .routeId("Triggering sync")
                .to("direct:read-dhis-ou")
                .to("direct:write-fhir-ou");

        // Read OU from source and unmarshall into MetaData
        from("direct:read-dhis-ou")
                .routeId("Read OrgUnits")
                .setHeader("CamelDhis2.queryParams", () -> Map.of(
                        "fields", "id,code,name,shortName,description,openingDate,parent[id]",
                        "order", "level",
                        "filter", List.of("level:le:2"),
                        "paging", "false"
                ))
                .to("dhis2://get/resource?path=organisationUnits&client=#dhis2Client")
                .unmarshal().json(Metadata.class)
                .convertBodyTo(Bundle.class);

        // Write OU to target
        from("direct:write-fhir-ou")
                .to("fhir://transaction/withBundle?client=#fhirClient")
                .marshal().fhirJson("R4", true);

        // Expose as API
        from("direct:get-dhis-ou")
                .to("direct:read-dhis-ou")
                .marshal().fhirJson("R4", true);

        rest("/")
                .get("/baseR4/Bundle")
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .to("direct:get-dhis-ou")
                .post("/trigger")
                .to("direct:trigger-sync");
    }
}
