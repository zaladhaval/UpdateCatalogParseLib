package com.ms.catalog.parser.reponse;

import com.ms.catalog.parser.exception.ParseHtmlPageException;
import lombok.Getter;
import lombok.Setter;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class CatalogDriver extends CatalogUpdateBase {

    public String Company;
    public String DriverManufacturer;
    public String DriverClass;
    public String DriverModel;
    public String DriverProvider;
    public String DriverVersion;
    public Date VersionDate;
    public List<String> HardwareIDs;

    public CatalogDriver() {

    }

    public CatalogDriver(CatalogUpdateBase catalogUpdateBase) {
        super(catalogUpdateBase);
    }

    public void CollectDriverDetails() throws ParseHtmlPageException {
        try {
            this.HardwareIDs = GetHardwareIDs();
            this.Company = _detailsPage.getElementById("ScopedViewHandler_company").text();
            this.DriverManufacturer = _detailsPage.getElementById("ScopedViewHandler_manufacturer").text();
            this.DriverClass = _detailsPage.getElementById("ScopedViewHandler_driverClass").text();
            this.DriverModel = _detailsPage.getElementById("ScopedViewHandler_driverModel").text();
            this.DriverProvider = _detailsPage.getElementById("ScopedViewHandler_driverProvider").text();
            this.DriverVersion = _detailsPage.getElementById("ScopedViewHandler_version").text();
            this.VersionDate = new Date(Date.parse(_detailsPage.getElementById("ScopedViewHandler_versionDate").text()));
        } catch (Exception e) {
            throw new ParseHtmlPageException("Failed to gather Driver details");
        }
    }

    protected List<String> GetHardwareIDs() {
        Element hwIdsDivs = _detailsPage.getElementById("driverhwIDs");

        if (hwIdsDivs == null) {
            return new ArrayList<>();
        }

        List<String> hwIds = new ArrayList<>();

        hwIdsDivs.children().stream().filter(c -> c.nodeName() == "div").toList()
                .forEach(node ->
                {
                    String hid = node.text()
                            .trim()
                            .replace("\r\n", "")
                            .toUpperCase();

                    if (hid != "") {
                        hwIds.add(hid);
                    }
                });

        return hwIds;
    }

}
