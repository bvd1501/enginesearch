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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.RecursiveAction;

@Component
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class PageIndexService extends RecursiveAction{
    private final SiteRepo siteRepo;
    private final PageRepo pageRepo;
    private final JsoupCfg jsoupCfg;
    private final String urlSite;
    private final String path;


    @Override
    protected void compute() {
        if (IndexingServiceImp.stopFlag) {
            log.error("Отсанов индексации: " + urlSite + "  " + path);
            throw new RuntimeException("Индексация прервана пользователем");
            //return;
        }
        URL siteAddress;
        try {
            siteAddress = URI.create(urlSite).toURL();
        } catch (MalformedURLException e) {
            log.error("Неверный адрес сайта: " + urlSite);
            throw new RuntimeException(e);
            //return;
        }
        String pageAddress = siteAddress.getProtocol()
                + "://" + siteAddress.getHost() + path;
        try {
            Thread.sleep(210);
        } catch (InterruptedException e) {
            log.error("Таймаут прерван: " + urlSite + "  " + path);
            //return;
            throw new RuntimeException(e);
        }
        Document doc;
        SiteEntity siteEntity = siteRepo.findByUrl(urlSite);
        if (siteEntity == null) {
            return;
        }
        try {
            doc = Jsoup
                    .connect(pageAddress)
                    .userAgent(jsoupCfg.getUserAgent())
                    .referrer(jsoupCfg.getReferrer())
                    .timeout(jsoupCfg.getTimeout())
                    .followRedirects(jsoupCfg.isFollowRedirects())
                    .ignoreHttpErrors(jsoupCfg.isIgnoreHttpErrors())
                    .ignoreContentType(true)
                    .get();
        } catch (IOException e) {
            log.error("Ошибка чтения: " + urlSite + "  " + path + " - " + e.getMessage());
            //throw new RuntimeException(e);
            siteEntity.setLast_error("Ошибка чтения: " + urlSite + "  " + path);
            siteEntity.setStatusTime(new java.util.Date());
            siteRepo.save(siteEntity);
            return;
        }

        synchronized (this) {
            if (pageRepo.findBySiteAndPath(siteEntity, path).isPresent()) {
                return;
            }
            PageEntity pageEntity = new PageEntity(siteEntity, path);
            pageEntity.setCode(doc.connection().response().statusCode());
            pageEntity.setContent("");
            //TODO Для startWith возможен NullPointerException
            if ((pageEntity.getCode() == 200) && doc
                    .connection().response().contentType().startsWith("text/html")) {
                pageEntity.setContent(doc.html());
            }
            siteEntity.setStatusTime(new java.util.Date());

            try {
                siteRepo.save(siteEntity);
                pageRepo.save(pageEntity);
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                log.error("Ошибка добавления записи в БД: " + urlSite + "  " + path);
                return;
            }
        }

        HashSet<String> links = new HashSet<>();
        Elements elements = doc.select("a");
        for (Element e : elements) {
            String linkString = e.absUrl("href");
            if (linkString.contains("#") || linkString.contains("download") ||
                    linkString.contains(" ")) {
                continue;
            }
            links.add(linkString);
            log.debug("add " + linkString);
        }

        List<PageIndexService> subPageTasks = new ArrayList<>();
        for (String itemLink : links) {
            if (!itemLink.startsWith(urlSite) || itemLink.equals(pageAddress)
            ) {
                continue;
            }
            URI itemURI = URI.create(itemLink);
            if (pageRepo.findBySiteAndPath(siteEntity, itemURI.getPath()).isEmpty()) {
                PageIndexService subPageIndexService = new PageIndexService(
                        siteRepo, pageRepo, jsoupCfg, urlSite, itemURI.getPath());
                subPageTasks.add(subPageIndexService);
            }
        }
        //log.info("Найдено " + subPageTasks.size() + "/" + links.size() + " ссылок");
        invokeAll(subPageTasks);
    }
}
