package searchengine.Utilites;

import java.net.URI;

public class UriTester {
    public static void main(String[] args) {
        String baseSite = "https://www.example.com/path1/";
        URI baseURI = URI.create(baseSite);
        String pagesite = "https://www.example.com/path1/path2/path3?param1=value";
        URI pageURI = URI.create(pagesite);
        System.out.println("site = " + baseURI);
        System.out.println("page = " + pageURI);
        System.out.println("Relativize page = " + baseURI.relativize(pageURI));
        System.out.println("Authority page = " + pageURI.getAuthority());
        System.out.println("RawPath page - " + pageURI.getRawPath());
        System.out.println("reconstract = " + baseSite + pageURI.getRawPath().substring(1));

    }
}
