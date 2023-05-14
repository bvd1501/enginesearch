package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
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
    private final URI uriPage;
    private final Connection connection;


    @Override
    protected void compute() {//
        Document doc;
        try {
            if (IndexingServiceImp.stopFlag) {
                throw new RuntimeException("Индексация прервана пользователем");            }
            Thread.sleep(110);
            doc = readPage(uriPage.getScheme() + "://" + uriPage.getHost() + uriPage.getPath());
            if (doc == null) return;
        } catch (IOException e) {
            log.error("Ошибка чтения страницы: " + uriPage.toString() + "\n" +
                    e.getMessage());
            return;
        } catch (InterruptedException e) {
            log.error("Отсанов индексации: " + uriPage.toString());
            throw new RuntimeException("Индексация прервана");
        }
        HashSet<URI> links = parsePage(doc);
        List<PageIndexService> subPageTasks = new ArrayList<>();
        Connection subConnection = Jsoup.connect(uriPage.toString())
                .userAgent(jsoupCfg.getUserAgent())
                .referrer(jsoupCfg.getReferrer())
                .timeout(jsoupCfg.getTimeout())
                .followRedirects(jsoupCfg.isFollowRedirects())
                .ignoreHttpErrors(jsoupCfg.isIgnoreHttpErrors())
                .ignoreContentType(true);
        for (URI itemURI : links) {
            if (pageRepo.findBySite_IdAndPath(site_id, itemURI.getPath()).isPresent()) continue;
            PageIndexService subPageIndexService = new PageIndexService(siteRepo, pageRepo,
                    jsoupCfg, site_id, itemURI, subConnection);
            subPageTasks.add(subPageIndexService);
        }
        invokeAll(subPageTasks);
    }

    private Document readPage(String pageAddress) throws IOException {
        try {
//            Document resultDoc = Jsoup
//                    .connect(pageAddress)
//                    .userAgent(jsoupCfg.getUserAgent())
//                    .referrer(jsoupCfg.getReferrer())
//                    .timeout(jsoupCfg.getTimeout())
//                    .followRedirects(jsoupCfg.isFollowRedirects())
//                    .ignoreHttpErrors(jsoupCfg.isIgnoreHttpErrors())
//                    .ignoreContentType(true)
//                    .get();
            Document resultDoc = connection.url(pageAddress).get();

            Integer statusCode = resultDoc.connection().response().statusCode();
            String content = "";
            if ((statusCode == 200) && (resultDoc.connection().response().contentType().startsWith("text/html"))) {
                content = resultDoc.html();
            }
            savePage(statusCode, content);
            return resultDoc;
        } catch (HttpStatusException e) {
            log.error("Ошибка чтения: " + pageAddress + "code=" + e.getStatusCode());
            savePage(e.getStatusCode(), " ");
            return null;
        }
    }

    private void savePage(Integer pageStatusCode, String pageContent) throws RuntimeException {
        Optional<SiteEntity> siteEntityOptional = siteRepo.findById(site_id);
        if (!siteEntityOptional.isPresent()) {
            log.error("Отсутствует в БД: " + uriPage.toString());
            throw new RuntimeException("Сайт не найден в БД");
        }
        SiteEntity siteEntity = siteEntityOptional.get();
        PageEntity pageEntity = new PageEntity(siteEntity, uriPage.getPath());
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
            log.error("Ошибка при добавлении записи в БД: " + uriPage.toString() + "\n" +
                    e.getMessage());
            }
        }
    }

    private HashSet<URI> parsePage(Document inputDoc) {
        HashSet<URI> resultLinks = new HashSet<>();
        Elements elements = inputDoc.select("a");
        for (Element element : elements) {
            String linkString = element.absUrl("href").toLowerCase();
            if (linkString.contains("#") ||
                    linkString.contains("download") ||
                    linkString.contains(" ") ||
                    linkString.contains(".pdf") ||
                    linkString.contains(".jpg") ||
                    linkString.contains(".jpeg") ||
                    linkString.contains(".docx") ||
                    linkString.contains(".doc")) continue;
            URI itemURI;
            try {
                itemURI = URI.create(linkString);
                if (!itemURI.getScheme().contains("http")) continue;
                String siteDomen = uriPage.getHost().startsWith("www.")
                        ? uriPage.getHost().substring(4) : uriPage.getHost();
                String pageDomen = itemURI.getHost().startsWith("www.")
                        ? itemURI.getHost().substring(4) : itemURI.getHost();
                if (!siteDomen.equals(pageDomen)) continue;
                if (itemURI.getPath().equals(uriPage.getPath())) continue;
                itemURI = URI.create(itemURI.getScheme() + "://" + itemURI.getHost() + itemURI.getPath());
                resultLinks.add(itemURI);
            } catch (Exception e) {
                log.error("URI Exception: " + e.getMessage() + " on " + linkString);
            }
        }
        return resultLinks;
    }

}
