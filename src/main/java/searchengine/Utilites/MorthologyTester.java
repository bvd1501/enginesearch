package searchengine.Utilites;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import searchengine.services.LemmaService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class MorthologyTester {

    public static void main(String[] args) throws IOException {
        LemmaService lemmaService = new LemmaService();
        String text = "азаровди азартный леса азнавур а a это еще что";

        Map<String, Integer> lemmas = lemmaService.lemmaCount(text);
        lemmas.entrySet().forEach(entry -> System.out.println(entry.getKey()
                + " - " + entry.getValue()));

        String textWithoutHTMLTags = Jsoup.clean(text, Safelist.none()).toLowerCase();
        List<String> words = Arrays.stream(textWithoutHTMLTags.replaceAll("([^а-я\\s])", " ")
                .trim()
                .replaceAll("\\s+", " ")
                .split("\\s+")).toList();
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();
        for (String word : words) {
            if (word.isEmpty()) {continue;}

            System.out.println("Word = " + word);
            List<String> normalFormsWord = luceneMorph.getNormalForms(word);
            System.out.println("    normalForms = " + normalFormsWord);
            List<String> normaFormsInfo = luceneMorph.getMorphInfo(word);
            System.out.println("    morthInfo = " + normaFormsInfo);

            String[] PARTICLES ={"МЕЖД", "ПРЕДЛ", "СОЮЗ", "ЧАСТ"};

            List<String> resultWords = new ArrayList<>();
            for (int i=0; i<normalFormsWord.size(); i++) {
                if (Arrays.stream(PARTICLES).noneMatch(normaFormsInfo.get(i)::contains)) {
                    resultWords.add(normalFormsWord.get(i));
                }
            }
            System.out.println("    matchLemma = " + resultWords);
        }
    }
}
