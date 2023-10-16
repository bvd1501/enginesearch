package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@Slf4j
public class LemmaService {
    private final LuceneMorphology luceneMorph;
    private static final String[] PARTICLES ={" МЕЖД", " ПРЕДЛ", " СОЮЗ", " ЧАСТ"};
    //private static final String WORD_TYPE_REGEX = "\\W\\w&&[^а-яА-Я\\s]";

    public LemmaService() throws IOException {
        this.luceneMorph = new RussianLuceneMorphology();
    }


    public Map<String, Integer> lemmaCount(String text) {
        HashMap<String, Integer> lemms = new HashMap<>();
        List<String> words = splitIntoWords(text);
        for (String word : words) {
            if (word.length()<2) {continue;}
            List<String> wordLemmas = getNormalFormWords(word);
            if (wordLemmas == null) {continue;}
            for (String l : wordLemmas) {
                if (lemms.containsKey(l)) {
                    lemms.put(l, lemms.get(l) + 1);
                } else {
                    lemms.put(l, 1);
                }
            }
        }
        return lemms;
    }

    private List<String> getNormalFormWords(String word) {
        try {
            List<String> normalFormsWord = luceneMorph.getNormalForms(word);
            List<String> normaFormsInfo = luceneMorph.getMorphInfo(word);
            List<String> resultWords = new ArrayList<>();
            for (int i=0; i<normalFormsWord.size(); i++) {
                if (Arrays.stream(PARTICLES).noneMatch(normaFormsInfo.get(i)::contains)) {
                    resultWords.add(normalFormsWord.get(i));
                }
            }
            return resultWords;
        } catch (Exception e) {
            log.error("Error on getNormalFormWord for " + word);
            return null;
        }
    }

    private List<String> splitIntoWords(String text) {
        String textWithoutHTMLTags = Jsoup.clean(text, Safelist.none()).toLowerCase();        
        return Arrays.stream(textWithoutHTMLTags.replaceAll("([^а-я\\s])", " ")
                        .trim()
                        .replaceAll("\\s+", " ")
                        .split("\\s+")).toList();
    }

}
