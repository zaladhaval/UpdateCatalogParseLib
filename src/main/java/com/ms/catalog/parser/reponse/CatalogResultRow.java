package com.ms.catalog.parser.reponse;

import lombok.Getter;
import lombok.Setter;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
public class CatalogResultRow {

    private String updateID;

    private String title;

    private String products;

    private String classification;

    private Date lastUpdate;

    private String version;

    private String size;

    private int SizeInBytes;

    public CatalogResultRow(Element Row) {
        Elements Cells = Row.getElementsByTag("td");

        this.title = Cells.get(1).text().trim();
        this.products = Cells.get(2).text().trim();
        this.classification = Cells.get(3).text().trim();
        try {
            this.lastUpdate = new SimpleDateFormat("MM/dd/yyyy").parse(Cells.get(4).text().trim());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        this.version = Cells.get(5).text().trim();
        this.size = Cells.get(6).getElementsByTag("span").get(0).text();
        this.SizeInBytes = Integer.parseInt(Cells.get(6).getElementsByTag("span").get(1).text());
        this.updateID = Cells.get(7).getElementsByTag("input").get(0).attr("id");
    }
}
