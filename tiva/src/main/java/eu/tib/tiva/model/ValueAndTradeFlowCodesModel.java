package eu.tib.tiva.model;

import java.util.Set;

public interface ValueAndTradeFlowCodesModel {

    Set<String> getCountryCodeIds();
    Set<String> getCountryCodes();
    Set<String> getInternationalOrganizationCodes();
    Set<String> getIndustryCodes();
}
