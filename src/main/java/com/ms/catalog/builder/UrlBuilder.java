package com.ms.catalog.builder;

public class UrlBuilder {

    private static final String CATALOGURL = "https://www.catalog.update.microsoft.com/Search.aspx";

    private static final String URLQUERY = "?q=";

    private String searchQuery;

    public UrlBuilder searchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
        return this;
    }

    public String build() {
        StringBuilder stringBuilder = new StringBuilder(CATALOGURL);
        stringBuilder.append(URLQUERY).append(searchQuery);
        return stringBuilder.toString();
    }
}
