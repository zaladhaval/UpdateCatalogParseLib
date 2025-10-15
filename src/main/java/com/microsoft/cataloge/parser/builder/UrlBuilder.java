package com.microsoft.cataloge.parser.builder;

public class UrlBuilder {

    private static final String CATALOGURL = "https://www.catalog.update.microsoft.com/Search.aspx";

    private static final String URLQUERY = "?q=";
    private static final String URLPAGE = "&p=";

    private String searchQuery;

    private int pageNumber;

    public UrlBuilder searchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
        return this;
    }

    public UrlBuilder pageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
        return this;
    }

    public String build() {
        StringBuilder stringBuilder = new StringBuilder(CATALOGURL);
        stringBuilder.append(URLQUERY).append(searchQuery);
        if (pageNumber > 1) {
            stringBuilder.append(URLPAGE).append(pageNumber);
        }

        return stringBuilder.toString();
    }
}
