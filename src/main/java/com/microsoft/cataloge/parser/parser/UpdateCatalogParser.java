package com.microsoft.cataloge.parser.parser;

import com.microsoft.cataloge.parser.builder.UrlBuilder;
import com.microsoft.cataloge.parser.exception.CatalogNoResultsException;
import com.microsoft.cataloge.parser.reponse.CatalogDriver;
import com.microsoft.cataloge.parser.reponse.CatalogResponce;
import com.microsoft.cataloge.parser.reponse.CatalogResultRow;
import com.microsoft.cataloge.parser.reponse.CatalogUpdateBase;
import com.microsoft.cataloge.parser.reponse.CatalogUpdates;
import org.apache.commons.lang3.NotImplementedException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class UpdateCatalogParser {

    public static List<CatalogResultRow> SendSearchQuery(RestTemplate client, String Query) {
        String url = new UrlBuilder().searchQuery(Query).build();
        CatalogResponce responce = null;
        while (responce == null) {
            try {
                responce = InvokeCatalogRequest(client, url);
            } catch (CatalogNoResultsException e) {
                return new ArrayList<>();
            }
        }
        List<CatalogResultRow> searchResults = new ArrayList<CatalogResultRow>();
        int count = 1;
        ParseSearchResults(responce, searchResults);
        while (responce.getNextPage() != null) {
            try {
                url = new UrlBuilder().searchQuery(Query).pageNumber(count++).build();
                responce = InvokeCatalogRequest(client, url);
            } catch (CatalogNoResultsException e) {
                continue;
            }
            ParseSearchResults(responce, searchResults);
        }
        return searchResults;
    }


    private static void ParseSearchResults(CatalogResponce responcePage,
            List<CatalogResultRow> existingUpdates) {
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

    private static CatalogResponce InvokeCatalogRequest(RestTemplate client, String Uri)
            throws CatalogNoResultsException {
        ResponseEntity<String> response = client.getForEntity(Uri, String.class);
        Document document = Jsoup.parse(response.getBody());
        if (document.getElementById("ctl00_catalogBody_noResultText") == null) {
            return new CatalogResponce(document);
        }

        throw new CatalogNoResultsException();

    }
}
