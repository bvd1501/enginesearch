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
import java.net.URL;
import java.util.HashSet;
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
    //private final Integer site_id;
    private final SiteEntity siteEntity;
    //private final URI uriPage;
    private final URL urlPage;



    @Override
    protected void compute() {
        PageEntity pageEntity = new PageEntity(siteEntity, urlPage.getPath());
        Document pageDoc = null;
        try {
            if (IndexingServiceImp.stopFlag) {
                throw new RuntimeException("Индексация прервана пользователем");
            }
            //int randomTime = (int) Math.random() * 5000;
            //Thread.sleep(100 + randomTime);
            Thread.sleep(110);
            pageDoc = readPage(urlPage);
            if (pageDoc == null) return;
            pageEntity.setCode(pageDoc.connection().response().statusCode());
            if (pageEntity.getCode() == 200) {
                pageEntity.setContent(pageDoc.html());
            }
            synchronized (pageRepo) {
                Optional<PageEntity> pageEntityOptional =
                    pageRepo.findBySiteAndPath(siteEntity, urlPage.getPath());
                if (pageEntityOptional.isPresent()) return;
                pageRepo.save(pageEntity);
                siteEntity.setStatusTime(new java.util.Date());
                siteRepo.save(siteEntity);
            }
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("Ошибка при добавлении записи в БД: " + urlPage + "\n" +
                    e.getMessage());
        } catch (InterruptedException e) {
            log.error("Отсанов индексации: " + urlPage);
            throw new RuntimeException("Индексация прервана");
        }
        assert pageDoc != null;
        HashSet<URL> links = getValidLinks(pageDoc);
        HashSet<PageIndexService> subPageTasks = new HashSet<>();
        for (URL itemURL : links) {
            PageIndexService subPageIndexService = new PageIndexService(siteRepo, pageRepo,
                    jsoupCfg, siteEntity, itemURL);
            subPageTasks.add(subPageIndexService);
        }
        invokeAll(subPageTasks);
    }

    private Document readPage(URL pageAddress) {
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
            if (!resultDoc.connection().response().contentType().startsWith("text/html")) return null;
            return resultDoc;
        } catch (org.jsoup.HttpStatusException e) {
            log.error("Ошибка чтения: " + pageAddress + " code=" + e.getStatusCode());
        } catch (NullPointerException e) {
            log.error("Ошибка определения типа контента: " + pageAddress + " - " + e.getMessage());
        } catch (IOException e) {
            log.error("Ошибка чтения: " + pageAddress + " - " + e.getMessage());
        }
        return null;
    }


    private HashSet<URL> getValidLinks(Document inputDoc) {
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
