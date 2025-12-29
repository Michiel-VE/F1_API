package be.michielve.f1_api.utils;

import org.jsoup.nodes.Element;

public class Helpers {
    /**
     * Maps a country name to its three-letter country code.
     * This is a simple lookup table; a more robust solution would use a dedicated library.
     *
     * @param country The country name.
     * @return The three-letter country code.
     */
    public static String getCountryCode(String country) {
        return switch (country.toLowerCase()) {
            case "united kingdom" -> "GBR";
            case "monaco" -> "MCO";
            case "netherlands" -> "NLD";
            case "australia" -> "AUS";
            case "canada" -> "CAN";
            case "spain" -> "ESP";
            case "france" -> "FRA";
            case "germany" -> "GER";
            case "japan" -> "JPN";
            case "thailand" -> "THA";
            case "new zealand" -> "NZL";
            case "italy" -> "ITA";
            case "brazil" -> "BRA";
            case "argentina" -> "ARG";
            default -> "N/A";
        };
    }

    /**
     * Safely extracts text from a Jsoup Element, returning an empty string if the element is null.
     *
     * @param element The Jsoup Element to process.
     * @return The trimmed text of the element or an empty string.
     */
    public static String safeSelectText(Element element) {
        return element != null ? element.text().trim() : "";
    }
}
