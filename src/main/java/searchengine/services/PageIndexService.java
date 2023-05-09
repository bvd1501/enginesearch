package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.config.JsoupCfg;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repo.PageRepo;
import searchengine.repo.SiteRepo;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.RecursiveAction;

@Component
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class PageIndexService extends RecursiveAction{
    private final SiteRepo siteRepo;
    private final PageRepo pageRepo;
    private final JsoupCfg jsoupCfg;
    private final Integer site_id;
    private final URI uriPage;


    @Override
    protected void compute() {
        if (IndexingServiceImp.stopFlag) {
            log.error("Отсанов индексации: " + uriPage.toString());
            throw new RuntimeException("Индексация прервана пользователем");
        }

        Document doc;
        try {
            Thread.sleep(210);
            doc = readPage(uriPage.getScheme() + "://" + uriPage.getHost() + uriPage.getPath());
            savePage(doc);
        } catch (Exception e) {
            log.error("Ошибка чтения/сохранения страницы: " + uriPage.toString());
            return;
            //throw new RuntimeException("Индексация прервана");
        }

        HashSet<URI> links = parsePage(doc);
        List<PageIndexService> subPageTasks = new ArrayList<>();
        for (URI itemURI : links) {
            if (pageRepo.findBySite_IdAndPath(site_id, itemURI.getPath()).isPresent()) continue;
            PageIndexService subPageIndexService = new PageIndexService(siteRepo, pageRepo,
                    jsoupCfg, site_id, itemURI);
            subPageTasks.add(subPageIndexService);
        }
        invokeAll(subPageTasks);
    }

    private Document readPage(String pageAddress) throws IOException {
        Document resultDoc = Jsoup
                .connect(pageAddress)
                .userAgent(jsoupCfg.getUserAgent())
                .referrer(jsoupCfg.getReferrer())
                .timeout(jsoupCfg.getTimeout())
                .followRedirects(jsoupCfg.isFollowRedirects())
                .ignoreHttpErrors(jsoupCfg.isIgnoreHttpErrors())
                .ignoreContentType(true)
                .get();
        //if (resultDoc == null) throw new IOException();
        return resultDoc;
    }

    private void savePage(Document readingDoc) {
        Optional<SiteEntity> siteEntityOptional = siteRepo.findById(site_id);
        if (!siteEntityOptional.isPresent()) {
            log.error("Отсутствует в БД: " + uriPage.toString());
            throw new RuntimeException("Сайт не найден в БД");
        }
        SiteEntity siteEntity = siteEntityOptional.get();
        PageEntity pageEntity = new PageEntity(siteEntity, uriPage.getPath());

        pageEntity.setCode(readingDoc.connection().response().statusCode());
        if ((pageEntity.getCode() == 200) && readingDoc
                .connection().response().contentType().startsWith("text/html")) {
            pageEntity.setContent(readingDoc.html());
        } else {
            log.error("Ошибка чтения " + uriPage.toString() + " - " + pageEntity.getCode());
        }
        synchronized (pageRepo) {
            try {
                if (!pageRepo.findBySiteAndPath(pageEntity.getSite(), pageEntity.getPath()).isPresent()) {
                    pageRepo.save(pageEntity);
                    siteEntity.setStatusTime(new java.util.Date());
                    siteRepo.save(siteEntity);
                }
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("Ошибка при добавлении записи в БД: " + uriPage.toString() + "\n" +
                    e.getMessage());
            }
        }
    }

    private HashSet<URI> parsePage(Document inputDoc) {
        HashSet<URI> resultLinks = new HashSet<>();
        Elements elements = inputDoc.select("a");
        for (Element element : elements) {
            String linkString = element.absUrl("href");
            if (linkString.contains("#") ||
                    linkString.toLowerCase().contains("download") ||
                    linkString.contains(" ") ||
                    linkString.toLowerCase().contains(".pdf") ||
                    linkString.toLowerCase().contains(".jpg") ||
                    linkString.toLowerCase().contains(".jpeg") ||
                    linkString.toLowerCase().contains(".docx") ||
                    linkString.toLowerCase().contains(".doc")) continue;
            URI itemURI;
            try {
                itemURI = new URI(linkString);
                //if(!itemURI.getScheme().equals(uriPage.getScheme())) continue;
                if (!itemURI.getScheme().toLowerCase().contains("http")) continue;
                if (!itemURI.getHost().equals(uriPage.getHost())) continue;
                if (itemURI.getPath().equals(uriPage.getPath())) continue;
                resultLinks.add(itemURI);
            } catch (Exception e) {
                log.error("URI Exception: " + linkString);
            }
        }
        return resultLinks;
    }

}
