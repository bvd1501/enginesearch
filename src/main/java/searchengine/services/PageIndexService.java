package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
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
import java.net.URL;
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
    //private final URI uriPage;
    private final URL urlPage;



    @Override
    protected void compute() {//
        Document doc;
        try {
            if (IndexingServiceImp.stopFlag) {
                throw new RuntimeException("Индексация прервана пользователем");            }
            Thread.sleep(110);
            doc = readPage(urlPage);
            if (doc == null) return;
        } catch (IOException e) {
            log.error("Ошибка чтения страницы: " + urlPage.toString() + "\n" +
                    e.getMessage());
            return;
        } catch (InterruptedException e) {
            log.error("Отсанов индексации: " + urlPage.toString());
            throw new RuntimeException("Индексация прервана");
        }
        HashSet<URL> links = parsePage(doc);
        List<PageIndexService> subPageTasks = new ArrayList<>();
        for (URL itemURL : links) {
            if (pageRepo.findBySite_IdAndPath(site_id, itemURL.getPath()).isPresent()) continue;
            PageIndexService subPageIndexService = new PageIndexService(siteRepo, pageRepo,
                    jsoupCfg, site_id, itemURL);
            subPageTasks.add(subPageIndexService);
        }
        invokeAll(subPageTasks);
    }

    private Document readPage(URL pageAddress) throws IOException {
        try {
            Document resultDoc = Jsoup
                    .connect(pageAddress.toString())
                    .userAgent(jsoupCfg.getUserAgent())
                    .referrer(jsoupCfg.getReferrer())
                    .timeout(jsoupCfg.getTimeout())
                    .followRedirects(jsoupCfg.isFollowRedirects())
                    .ignoreHttpErrors(jsoupCfg.isIgnoreHttpErrors())
                    .ignoreContentType(true)
                    .get();
            Integer statusCode = resultDoc.connection().response().statusCode();
            String content = "";
            if ((statusCode == 200) && (resultDoc.connection().response().contentType().startsWith("text/html"))) {
                content = resultDoc.html();
            }
            savePage(statusCode, content);
            return resultDoc;
        } catch (HttpStatusException e) {
            log.error("Ошибка чтения: " + pageAddress + " code=" + e.getStatusCode());
            savePage(e.getStatusCode(), "");
            return null;
        }
    }

    private void savePage(Integer pageStatusCode, String pageContent) throws RuntimeException {
        Optional<SiteEntity> siteEntityOptional = siteRepo.findById(site_id);
        if (!siteEntityOptional.isPresent()) {
            log.error("Отсутствует в БД: " + urlPage.toString());
            throw new RuntimeException("Сайт не найден в БД");
        }
        SiteEntity siteEntity = siteEntityOptional.get();
        PageEntity pageEntity = new PageEntity(siteEntity, urlPage.getPath());
        pageEntity.setCode(pageStatusCode);
        pageEntity.setContent(pageContent);
        synchronized (pageRepo) {
            try {
                if (!pageRepo.findBySiteAndPath(pageEntity.getSite(), pageEntity.getPath()).isPresent()) {
                    pageRepo.save(pageEntity);
                    siteEntity.setStatusTime(new java.util.Date());
                    siteRepo.save(siteEntity);
                }
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("Ошибка при добавлении записи в БД: " + urlPage + "\n" +
                    e.getMessage());
            }
        }
    }

    private HashSet<URL> parsePage(Document inputDoc) {
        HashSet<URL> resultLinks = new HashSet<>();
        Elements elements = inputDoc.select("a");
        for (Element element : elements) {
            String linkString = element.absUrl("href").toLowerCase();
            if (linkString.contains("#") ||
                    !linkString.startsWith("http") ||
                    linkString.contains("download") ||
                    linkString.contains(" ") ||
                    linkString.contains("%20") ||
                    linkString.contains(".pdf") ||
                    linkString.contains(".jpg") ||
                    linkString.contains(".jpeg") ||
                    linkString.contains(".docx") ||
                    linkString.contains(".doc")) continue;
            try {
                URL newURL = URI.create(linkString).toURL();
                String siteHost = urlPage.getHost().startsWith("www.")
                        ? urlPage.getHost().substring(4) : urlPage.getHost();
                String pageHost = newURL.getHost().startsWith("www.")
                        ? newURL.getHost().substring(4) : newURL.getHost();
                if (!siteHost.equals(pageHost)) continue;
                if (newURL.getPath().contains("http")) continue;
                if (newURL.getPath().equals(urlPage.getPath())) continue;
                newURL = URI.create(newURL.getProtocol() + "://" + newURL.getHost() + newURL.getPath()).toURL();
                resultLinks.add(newURL);
            } catch (Exception e) {
                log.error("URI Exception: " + e.getMessage() + " on " + linkString);
            }
        }
        return resultLinks;
    }

}
