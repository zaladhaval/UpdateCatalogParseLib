package com.ms.catalog.reponse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ms.catalog.exception.CatalogErrorException;
import com.ms.catalog.exception.UnableToCollectUpdateDetailsException;
import com.ms.catalog.exception.UpdateWasNotFoundException;
import lombok.Getter;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public class CatalogUpdateBase implements Serializable {

    private static final long serialVersionUID = -7043978461533401813L;

    private static final Pattern DOWNLOAD_PATTERN = Pattern.compile(
            "(downloadInformation)\\[\\d+\\]\\.files\\[\\d+\\].url = \\'[(http(s)?):\\/\\/(www\\.)?a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)\\'",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern urlRegex = Pattern.compile("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)");
    @JsonIgnore
    protected Document _detailsPage;
    private String Title;
    private String UpdateID;
    private List<String> Products;
    private String Classification;
    private Date LastUpdated;
    private String Size;
    private int SizeInBytes;
    private List<String> DownloadLinks;
    private String Description;
    private List<String> Architectures;
    private List<String> SupportedLanguages;
    private List<String> MoreInformation;
    private List<String> SupportUrl;
    private String RestartBehavior;
    private String MayRequestUserInput;
    private String MustBeInstalledExclusively;
    private String RequiresNetworkConnectivity;
    private String UninstallNotes;
    private String UnistallSteps;

    public CatalogUpdateBase() {
    }

    private CatalogUpdateBase(CatalogResultRow resultRow) {
        this.UpdateID = resultRow.getUpdateID();
        this.Title = resultRow.getTitle();
        this.Classification = resultRow.getClassification();
        this.LastUpdated = resultRow.getLastUpdate();
        this.Size = resultRow.getSize();
        this.SizeInBytes = resultRow.getSizeInBytes();
        this.Products = Arrays.stream(resultRow.getProducts().trim().split(",")).toList();
    }

    public CatalogUpdateBase(CatalogUpdateBase updateBase) {
        this._detailsPage = updateBase.get_detailsPage();
        this.Title = updateBase.getTitle();
        this.UpdateID = updateBase.getUpdateID();
        this.Products = updateBase.getProducts();
        this.Classification = updateBase.getClassification();
        this.LastUpdated = updateBase.getLastUpdated();
        this.Size = updateBase.getSize();
        this.SizeInBytes = updateBase.getSizeInBytes();
        this.DownloadLinks = updateBase.getDownloadLinks();
        this.Description = updateBase.getDescription();
        this.Architectures = updateBase.getArchitectures();
        this.SupportedLanguages = updateBase.getSupportedLanguages();
        this.MoreInformation = updateBase.getMoreInformation();
        this.SupportUrl = updateBase.getSupportUrl();
        this.RestartBehavior = updateBase.getRestartBehavior();
        this.MayRequestUserInput = updateBase.getMayRequestUserInput();
        this.MustBeInstalledExclusively = updateBase.getMustBeInstalledExclusively();
        this.RequiresNetworkConnectivity = updateBase.getRequiresNetworkConnectivity();
        this.UninstallNotes = updateBase.getUninstallNotes();
        this.UnistallSteps = updateBase.getUnistallSteps();
    }

    @Async
    public void CollectGenericInfo(RestTemplate client) throws Exception {
        GetDetailsPage(client);
        CollectBaseDetails(client);
    }

    protected void CollectDownloadLinks(RestTemplate client) throws Exception {
        String ReqiestUri = "https://www.catalog.update.microsoft.com/DownloadDialog.aspx";

        String data = "[{\"size\":0,\"languages\":\"\",\"uidInfo\":\"" + this.UpdateID + "\",\"updateID\":\"" + this.UpdateID
                + "\"}]";

        Document document = Jsoup.connect(ReqiestUri + "DownloadDialog.aspx").data("updateIDs", data)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .header("authority", "www.catalog.update.microsoft.com").header("user-agent",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36")
                .header("sec-fetch-mode", "navigate").header("sec-fetch-user", "?1")
                .header("sec-fetch-site", "none").timeout(0).post();
        Elements scriptTags = document.getElementsByTag("script");
        ArrayList<String> list = new ArrayList();
        for (Element tag : scriptTags) {
            for (DataNode node : tag.dataNodes()) {
                Matcher m = DOWNLOAD_PATTERN.matcher(node.getWholeData()); // you have to use html here
                while (m.find()) {
                    list.add(m.group());
                }
                for (String singleLine : list) {
                    String url = singleLine.split("=")[1].trim();
                    if (url.startsWith("'")) {
                        url = url.replaceAll("'", "");
                    }
                    DownloadLinks.add(url);
                }
            }
        }
    }

    private void GetDetailsPage(RestTemplate client) throws Exception {
        String reqiestUri = "https://www.catalog.update.microsoft.com/ScopedViewInline.aspx?updateid=" + this.UpdateID;
        ResponseEntity<String> responseEntity = client.getForEntity(reqiestUri, String.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new UnableToCollectUpdateDetailsException("Catalog responded with " + responseEntity.getStatusCode() + " code");
        }
        Document document = Jsoup.parse(responseEntity.getBody());

        Element errorDiv = document.getElementById("errorPageDisplayedError");
        if (errorDiv != null) {
            String errorCode = errorDiv.text().trim().replaceAll("]", "");
            if (errorCode == "8DDD0010") {
                throw new UnableToCollectUpdateDetailsException("Catalog cannot proceed your request right now. Send request again later");
            } else if (errorCode == "8DDD0024") {
                throw new UpdateWasNotFoundException("Update by this UpdateID does not exists or was removed");
            } else {
                throw new CatalogErrorException("Catalog returned unknown error code: {errorCode}");
            }
        }
        _detailsPage = document;
    }

    protected void CollectBaseDetails(RestTemplate client) throws Exception {
        CollectDownloadLinks(client);
        this.Title = _detailsPage.getElementById("ScopedViewHandler_titleText").text();
        this.Products = new ArrayList<>();
        Arrays.stream(_detailsPage.getElementById("productsDiv").text().trim().split(",")).toList().forEach(p -> this.Products.add(p.trim()));
        this.Classification = _detailsPage.getElementById("classificationDiv")
                .text().replace("Classification:", "").trim();
        this.LastUpdated = new Date(Date.parse(_detailsPage.getElementById("ScopedViewHandler_date").text()));
        this.Size = _detailsPage.getElementById("ScopedViewHandler_size").text();
        this.Description = _detailsPage.getElementById("ScopedViewHandler_desc").text();
        Architectures = new ArrayList<>();
        Arrays.stream(_detailsPage.getElementById("archDiv")
                        .text().trim()
                        .split(","))
                .toList()
                .forEach(arch ->
                {
                    Architectures.add(arch.replace("Architecture:", "").trim());
                });
        SupportedLanguages = new ArrayList<>();
        Arrays.stream(_detailsPage.getElementById("languagesDiv")
                        .text().trim()
                        .split(","))
                .toList()
                .forEach(lang ->
                {
                    SupportedLanguages.add(lang.trim());
                });
        String moreInfoDiv = _detailsPage.getElementById("moreInfoDiv").text();
        Matcher regexMatches = urlRegex.matcher(moreInfoDiv);
        if (regexMatches.groupCount() == 0) {
            this.MoreInformation = List.of("n/a");
        } else {
            this.MoreInformation = new ArrayList<>();
            while (regexMatches.find()) {
                MoreInformation.add(regexMatches.group());
            }
        }

        String supportUrlDiv = _detailsPage.getElementById("suportUrlDiv").text();
        regexMatches = urlRegex.matcher(supportUrlDiv);
        if (regexMatches.groupCount() == 0) {
            this.SupportUrl = List.of("n/a");
        } else {
            this.SupportUrl = new ArrayList<>();
            while (regexMatches.find()) {
                SupportUrl.add(regexMatches.group());
            }
        }

        this.RestartBehavior = _detailsPage.getElementById("ScopedViewHandler_rebootBehavior").text();

        this.MayRequestUserInput = _detailsPage.getElementById("ScopedViewHandler_userInput").text();

        this.MustBeInstalledExclusively = _detailsPage.getElementById("ScopedViewHandler_installationImpact").text();

        this.RequiresNetworkConnectivity = _detailsPage.getElementById("ScopedViewHandler_connectivity").text();

        Element uninstallNotesDiv = _detailsPage.getElementById("uninstallNotesDiv");
        if (uninstallNotesDiv.childNodeSize() == 3) {
            this.UninstallNotes = uninstallNotesDiv.child(uninstallNotesDiv.childNodeSize() - 1).text().trim();
        } else {
            this.UninstallNotes = _detailsPage.getElementById("uninstallNotesDiv")
                    .children().get(1)
                    .text().trim();
        }
        this.UnistallSteps = _detailsPage.getElementById("uninstallStepsDiv")
                .text().trim();

    }
}
