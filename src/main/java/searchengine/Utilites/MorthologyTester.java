package searchengine.Utilites;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.List;

public class MorthologyTester {
    public static void main(String[] args) throws IOException {
        LuceneMorphology luceneMorph =
                new RussianLuceneMorphology();
//        List<String> wordBaseForms =
//                luceneMorph.getNormalForms("воды");
//        wordBaseForms.forEach(System.out::println);
        System.out.println(luceneMorph.getNormalForms("воды"));
        luceneMorph.getMorphInfo("леса").forEach(System.out::println);
//        luceneMorph.getMorphInfo("ой").forEach(System.out::println);
//        luceneMorph.getMorphInfo("или").forEach(System.out::println);
//        luceneMorph.getMorphInfo("чтобы").forEach(System.out::println);
//        luceneMorph.getMorphInfo("на").forEach(System.out::println);
//        luceneMorph.getMorphInfo("из").forEach(System.out::println);
//        luceneMorph.getMorphInfo("бы").forEach(System.out::println);
//        luceneMorph.getMorphInfo("же").forEach(System.out::println);
        luceneMorph.getMorphInfo("леопарды").forEach(System.out::println);
    }
}
