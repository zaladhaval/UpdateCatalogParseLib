package com.microsoft.cataloge.parser.parser;

import com.microsoft.cataloge.parser.builder.UrlBuilder;
import com.microsoft.cataloge.parser.exception.CatalogNoResultsException;
import com.microsoft.cataloge.parser.reponse.*;
import org.apache.commons.lang3.NotImplementedException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class UpdateCatalogParser {

    public static List<CatalogResultRow> SendSearchQuery(RestTemplate client, String Query) {
        String url = new UrlBuilder().searchQuery(Query).build();
        CatalogResponce responce = null;
        while (responce == null) {
            try {
                responce = InvokeCatalogRequest(client, url, HttpMethod.GET, "", "", "", "", "");
            } catch (CatalogNoResultsException e) {
                return new ArrayList<>();
            }
        }
        List<CatalogResultRow> searchResults = new ArrayList<CatalogResultRow>();

        ParseSearchResults(responce, searchResults);
        while (responce.getNextPage() != null) {
            try {
                CatalogResponce tempResponce = InvokeCatalogRequest(
                        client,
                        url,
                        HttpMethod.POST,
                        responce.getEventArgument(),
                        "ctl00$catalogBody$nextPageLinkText",
                        responce.getEventValidation(),
                        responce.getViewState(),
                        responce.getViewStateGenerator()
                );

                responce = tempResponce;
            } catch (CatalogNoResultsException e) {
                continue;
            }

            ParseSearchResults(responce, searchResults);
        }
        return searchResults;
    }


    private static void ParseSearchResults(CatalogResponce responcePage, List<CatalogResultRow> existingUpdates) {
        for (Element row : responcePage.getRows()) {
            if (!row.attr("id").equals("headerRow")) {
                existingUpdates.add(new CatalogResultRow(row));
            }
        }
    }

    public static CatalogUpdateBase GetUpdateDetails(RestTemplate client, String UpdateID) throws Exception {
        CatalogUpdateBase updateBase = new CatalogUpdates();
        updateBase.setUpdateID(UpdateID);
        updateBase.CollectGenericInfo(client);
        if (updateBase.getClassification().contains("Driver")) {
            CatalogDriver driverUpdate = new CatalogDriver(updateBase);
            driverUpdate.CollectDriverDetails();
            return driverUpdate;
        }
        switch (updateBase.getClassification()) {
            case "Security Updates":
            case "Critical Updates":
            case "Definition Updates":
            case "Feature Packs":
            case "Service Packs":
            case "Update Rollups":
            case "Updates":
            case "Hotfix":
                CatalogUpdates update = new CatalogUpdates(updateBase);
                update.CollectUpdateDetails();
                return update;

            default:
                throw new NotImplementedException();
        }
    }

    private static CatalogResponce InvokeCatalogRequest(RestTemplate client, String Uri, HttpMethod method, String EventArgument, String EventTarget, String EventValidation, String ViewState, String ViewStateGenerator) throws CatalogNoResultsException {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        ResponseEntity<String> response = null;
        if (method == HttpMethod.POST) {
            formData.add("__EVENTTARGET", EventTarget);
            formData.add("__EVENTARGUMENT", EventArgument);
            formData.add("__VIEWSTATE", ViewState);
            formData.add("__VIEWSTATEGENERATOR", ViewStateGenerator);
            formData.add("__EVENTVALIDATION", EventValidation);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request =
                    new HttpEntity<MultiValueMap<String, String>>(formData, headers);
            response = client.postForEntity(Uri, request, String.class);
        } else {
            response = client.getForEntity(Uri, String.class);
        }
        Document document = Jsoup.parse(response.getBody());
        if (document.getElementById("ctl00_catalogBody_noResultText") == null) {
            return new CatalogResponce(document);
        }

        throw new CatalogNoResultsException();

    }
}
