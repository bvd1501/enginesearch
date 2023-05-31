package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Component
//@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class LemmaService {
    private final static String[] PARTICLES ={"МЕЖД", "ПРЕДЛ", "СОЮЗ", "ЧАСТ"};
    private HashMap<String, Integer> lemmaCount(String text) throws IOException {
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();
        List<String> words = splitIntoWords(text);
        List<String> lemms = convertWordToLemm(words);
        HashMap<String, Integer> result = new HashMap<>();
        for (String lemm : lemms) {
            if (result.containsKey(lemm)) {
                result.put(lemm, result.get(lemm) + 1);
            } else {
                result.put(lemm, 1);
            }
        }
        return result;
    }

    private List<String> splitIntoWords(String inputText) {
        return null;
    }

    private List<String> convertWordToLemm(List<String> inputWords) {
        return null;
    }

    List<String> wordBaseForms =
//                luceneMorph.getNormalForms("воды");
//        wordBaseForms.forEach(System.out::println);
        //luceneMorph.getMorphInfo("ах").forEach(System.out::println);


}
