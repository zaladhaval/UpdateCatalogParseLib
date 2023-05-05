package com.ms.catalog.reponse;

import com.ms.catalog.exception.ParseHtmlPageException;
import lombok.Getter;
import lombok.Setter;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CatalogUpdates extends CatalogUpdateBase {


    public String MSRCNumber;
    public String MSRCSeverity;
    public String KBArticleNumbers;
    public List<String> supersededBy;
    public List<String> supersedes;

    public CatalogUpdates() {
    }

    public CatalogUpdates(CatalogUpdateBase updateBase) {
        super(updateBase);
    }

    public void CollectUpdateDetails() throws ParseHtmlPageException {
        try {
            this.MSRCNumber = _detailsPage.getElementById("securityBullitenDiv").text().replace("MSRC Number:", "").trim();
            this.MSRCSeverity = _detailsPage.getElementById("ScopedViewHandler_msrcSeverity").text();
            this.KBArticleNumbers = _detailsPage.getElementById("kbDiv").text().replace("KB article numbers:", "").trim();
            this.supersededBy = CollectSupersededBy();
            this.supersedes = CollectSupersedes();
        } catch (Exception e) {
            throw new ParseHtmlPageException("Failed to gather Update details");
        }
    }

    private List<String> CollectSupersededBy() {
        Element supersededByDivs = _detailsPage.getElementById("supersededbyInfo");
        List<String> supersededBy = new ArrayList<>();
        // If first child isn't a div - than it's just a n/a and there's nothing to gather
        if (supersededByDivs.child(0).text().trim() == "n/a") {
            return supersededBy;
        }
        supersededByDivs.childNodes().stream().filter(n -> n.nodeName().equals("div")).toList()
                .forEach(n -> {
                    String updateId = n.childNode(1).attr("href").replace("ScopedViewInline.aspx?updateid=", "");
                    supersededBy.add(updateId);
                });
        return supersededBy;
    }

    private List<String> CollectSupersedes() {
        Element supersedesDivs = _detailsPage.getElementById("supersedesInfo");
        List<String> supersedes = new ArrayList<>();

        // If first child isn't a div - than it's just a n/a and there's nothing to gather
        if (supersedesDivs.child(0).text().trim() == "n/a") {
            return supersedes;
        }

        supersedesDivs.childNodes().stream().filter(n -> n.nodeName().equals("div")).toList()
                .forEach(n -> {
                    String updateId = n.childNode(0).toString().trim();
                    supersedes.add(updateId);
                });

        return supersedes;
    }
}
