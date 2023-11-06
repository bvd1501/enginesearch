package searchengine.services;

import searchengine.model.LemmaEntity;

import java.util.Comparator;

public class LemmaComparator implements Comparator<LemmaEntity> {
    @Override
    public int compare(LemmaEntity o1, LemmaEntity o2) {
        int result = o1.getFrequency().compareTo(o2.getFrequency());
        if (result!=0) {return result;}
        return 1;
    }
}
