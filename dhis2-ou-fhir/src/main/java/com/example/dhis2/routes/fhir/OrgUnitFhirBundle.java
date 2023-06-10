package com.example.dhis2.routes.fhir;

import com.example.dhis2.configuration.MainProperties;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.TypeConverters;
import org.apache.camel.component.fhir.internal.FhirConstants;
import org.hisp.dhis.api.model.v2_39_1.Metadata;
import org.hisp.dhis.api.model.v2_39_1.OrganisationUnit;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrgUnitFhirBundle implements TypeConverters {

    private final MainProperties properties;

    @Converter
    public Bundle toBundle(Metadata metadata, Exchange exchange) {
        Bundle bundle = new Bundle().setType(Bundle.BundleType.TRANSACTION);

        if (metadata.getOrganisationUnits().isPresent()) {
            for (OrganisationUnit ou : metadata.getOrganisationUnits().get()) {
                addOrganization(ou, bundle);
                addLocation(ou, bundle);
            }
        }

        exchange.getIn().setHeader(FhirConstants.PROPERTY_PREFIX + "bundle", bundle);

        return bundle;
    }

    private void addOrganization(OrganisationUnit ou, Bundle bundle) {
        Organization organization = new Organization();
        organization.setId(ou.getId().get());
        organization.setName(ou.getName().get());

        String baseUrl = properties.getSource().getBaseUrl().replace("/api", "");

        organization.getIdentifier().add(
                new Identifier().setSystem(baseUrl + "/api/organisationUnits/id")
                        .setValue(ou.getId().get()));

        if (ou.getCode().isPresent()) {
            organization.getIdentifier().add(
                    new Identifier().setSystem(baseUrl + "/api/organisationUnits/code")
                            .setValue(ou.getCode().get()));
        }

        bundle.addEntry()
                .setResource(organization)
                .getRequest()
                .setUrl("Organization?identifier=" + organization.getId())
                .setMethod(Bundle.HTTPVerb.PUT);
    }

    private void addLocation(OrganisationUnit ou, Bundle bundle) {
        Location location = new Location();
        location.setId(ou.getId().get());
        location.setName(ou.getName().get());

        String baseUrl = properties.getSource().getBaseUrl().replace("/api", "");

        location.getIdentifier().add(
                new Identifier().setSystem(baseUrl + "/api/organisationUnits/id")
                        .setValue(ou.getId().get()));

        if (ou.getCode().isPresent()) {
            location.getIdentifier().add(
                    new Identifier().setSystem(baseUrl + "/api/organisationUnits/code")
                            .setValue(ou.getCode().get()));
        }

        if (ou.getDescription().isPresent()) {
            location.setDescription(ou.getDescription().get());
        }

        // location.getManagingOrganization().setReference("Organization/" + ou.getId().get());
        location.setMode(Location.LocationMode.INSTANCE);

        if (ou.getParent().isPresent()) {
            location.getPartOf().setReference("Location/" + ou.getId().get());
        }

        location.setStatus(Location.LocationStatus.ACTIVE);
        location.getType().add(new CodeableConcept(new Coding().setCode("OF")));

        bundle.addEntry()
                .setResource(location)
                .getRequest()
                .setUrl("Location?identifier=" + location.getId())
                .setMethod(Bundle.HTTPVerb.PUT);
    }
}
