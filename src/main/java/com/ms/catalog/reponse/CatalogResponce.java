package com.ms.catalog.reponse;

import lombok.Getter;
import lombok.Setter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Getter
@Setter
public class CatalogResponce {

    private Elements rows;
    private String eventArgument;
    private String eventValidation;
    private String viewState;
    private String viewStateGenerator;
    public Element nextPage;
    
    public CatalogResponce(Document doc) {

        Element Table = doc.getElementById("ctl00_catalogBody_updateMatches");
        rows = Table.getElementsByTag("tr");
        try {
            eventArgument = doc.getElementById("__EVENTARGUMENT").attr("value");
        } catch (Exception e) {
            eventArgument = "";
        }
        eventValidation = doc.getElementById("__EVENTVALIDATION").attr("value");
        viewState = doc.getElementById("__VIEWSTATE").attr("value");
        viewStateGenerator = doc.getElementById("__VIEWSTATEGENERATOR").attr("value");
        nextPage = doc.getElementById("ctl00_catalogBody_nextPageLinkText");
    }
}
