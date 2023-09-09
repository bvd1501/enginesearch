package searchengine.config;

import lombok.Getter;


@Getter
public enum BadLinks {
    SPACE1(" "),
    SPACE2("%20"),
    LATTICE("#"),
    DOWNLOAD("download"),
    //WEBP(".webp"),
    //PDF(".pdf"),
    //EPS(".eps"),
    //JPG(".jpg"),
    //JPEG(".jpeg"),
    //PNG(".png"),
    //GIF(".gif"),
    //SVG(".svg"),
    //DOC(".doc"),
    //PPT (".ppt"),
    //XLS (".xls"),
    //ZIP (".zip"),
    //RAR (".rar"),
    QUOTES ("\""),
    QUOTES2 ("%5c%22"),
    HTTP ("%20http"),
    SQUARE_BRACKET("%5c"),
    SQUARE_BRACKET1("["),
    PARENTHESIS ("{");

    private final String title;

    BadLinks (String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return getTitle();
    }

}
