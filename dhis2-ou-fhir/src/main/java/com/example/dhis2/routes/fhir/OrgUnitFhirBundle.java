package com.example.dhis2.routes.fhir;

import lombok.RequiredArgsConstructor;
import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.TypeConverters;
import org.apache.camel.component.fhir.internal.FhirConstants;
import org.hisp.dhis.api.model.v2_39_1.Metadata;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrgUnitFhirBundle implements TypeConverters {
    @Converter
    public Bundle toBundle(Metadata metadata, Exchange exchange) {
        Bundle bundle = new Bundle().setType(Bundle.BundleType.BATCH);

        exchange.getIn().setHeader(FhirConstants.PROPERTY_PREFIX + "bundle", bundle);

        return bundle;
    }
}
