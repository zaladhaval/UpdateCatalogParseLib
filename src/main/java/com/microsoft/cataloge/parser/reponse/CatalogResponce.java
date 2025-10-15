package com.microsoft.cataloge.parser.reponse;

import lombok.Getter;
import lombok.Setter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Getter
@Setter
public class CatalogResponce {

    private Elements rows;
    
    public Element nextPage;

    public CatalogResponce(Document doc) {

        Element Table = doc.getElementById("ctl00_catalogBody_updateMatches");
        rows = Table.getElementsByTag("tr");
        nextPage = doc.getElementById("ctl00_catalogBody_nextPageLinkText");
    }
}
