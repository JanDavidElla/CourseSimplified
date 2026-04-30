package coursesimplified.archive;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Scrapper {
    public static void main(String[] args)
    {
        try {
            Document doc
                = Jsoup
                      .connect("https://catalog.sjsu.edu/preview_program.php?catoid=17&poid=15868&returnto=7889")
                      .get();
            Elements links = doc.select("a[href]");
            System.out.println("Links: ");
            for (Element link : links) {
                String href = link.attr("href");
                if (href.contains("preview_course")) {
                    System.out.println(link.text());
                }
            }
            System.out.println("\n-----\n");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
