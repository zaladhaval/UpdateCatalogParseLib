package com.ms.catalog.controller;

import com.ms.catalog.parser.UpdateCatalogParser;
import com.ms.catalog.reponse.CatalogResultRow;
import com.ms.catalog.reponse.CatalogUpdateBase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
public class CatalogController {

    @GetMapping("/getlist")
    public List<CatalogResultRow> getlist() {
        RestTemplate restTemplate = new RestTemplate();
        return UpdateCatalogParser.SendSearchQuery(restTemplate, "driver");
    }

    @GetMapping("/getbyid")
    public CatalogUpdateBase getdetailsByid() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        CatalogUpdateBase catalogUpdateBase = UpdateCatalogParser.GetUpdateDetails(restTemplate, "6f925f4a-6782-49bc-922c-77dfb55f9080");
        return catalogUpdateBase;
    }
}
