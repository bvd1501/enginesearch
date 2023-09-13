package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Component
@Scope("prototype")
@Slf4j
public class LemmaService {
    private final LuceneMorphology luceneMorph;
    private static final String[] PARTICLES ={"МЕЖД", "ПРЕДЛ", "СОЮЗ", "ЧАСТ"};
    private static final String WORD_TYPE_REGEX = "\\W\\w&&[^а-яА-Я\\s]";

    public LemmaService() throws IOException {
        this.luceneMorph = new RussianLuceneMorphology();;
    }


    private HashMap<String, Integer> lemmaCount(String text) throws IOException {
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();
        //TODO разделяем текст на слова
        //TODO
        int startIndex = 0;
        int endIndex = text.indexOf(" ");
        HashMap<String, Integer> result = new HashMap<>();

        while (endIndex != -1) {
//            String word = inputString.substring(startIndex, endIndex);
//
//
//            if (uniqueWords.containsKey(word)) {
//                uniqueWords.put(word, uniqueWords.get(word) + 1);
//            } else {
//                uniqueWords.put(word, 1);
//            }
//
              startIndex = endIndex + 1;
//            endIndex = inputString.indexOf(" ", startIndex);
        }
//        String lastWord = inputString.substring(startIndex);
//
//
//        List<String> words = splitIntoWords(text);
//        List<String> lemms = convertWordToLemm(words);

//        for (String lemmItem : lemms) {
//            if (result.containsKey(lemmItem)) {
//                result.put(lemmItem, result.get(lemmItem) + 1);
//            } else {
//                result.put(lemmItem, 1);
//            }
//        }
        return result;
    }

    private HashMap<String, Integer> lemmAnalyse(HashMap<String, Integer> lemmMaps, List<String> lemms) {
        for (String lemmItem : lemms) {
            if (lemmMaps.containsKey(lemmItem)) {
                lemmMaps.put(lemmItem, lemmMaps.get(lemmItem) + 1);
            } else {
                lemmMaps.put(lemmItem, 1);
            }
        }
        return lemmMaps;
    }

    private List<String> convertWordToLemm(List<String> inputWords) {
        return null;
    }

//    List<String> wordBaseForms =
//                luceneMorph.getNormalForms("воды");
//        wordBaseForms.forEach(System.out::println);
//        luceneMorph
}
