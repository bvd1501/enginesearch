package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import searchengine.config.JsoupCfg;
import searchengine.model.PageEntity;


import java.io.IOException;
import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;


@Service
@Scope("prototype")
@Slf4j
public class PageIndexService extends RecursiveAction {
    private final ApplicationContext context;
    private final JsoupCfg jsoupCfg;
    private final IndexingService indexingService;
    private final LemmaService lemmaService;
    private final DatabaseService databaseService;
    private final PageEntity pageEntity;


    @Autowired
    public PageIndexService(ApplicationContext context, PageEntity pageEntity) {
        this.context = context;
        this.indexingService = context.getBean(IndexingService.class);
        this.lemmaService = context.getBean(LemmaService.class);
        this.databaseService = context.getBean(DatabaseService.class);
        this.jsoupCfg = context.getBean(JsoupCfg.class);
        this.pageEntity = pageEntity;
    }



    @Override
    public void compute() {
        if (indexingService.isStopFlag() || ForkJoinTask.getPool().isShutdown()) {
            return;
        }
        if (!singePageHandler()) {return;}
        Set<PageIndexService> childrenPageIndex = pageHandler(pageEntity);
        ForkJoinTask.invokeAll(childrenPageIndex);
        //childrenPageIndex.stream().forEach(p->p.fork());
    }


    /**
     * Обработчик отдельной страницы. Выполняет следующе действия:
     * - читает страницу из сети;
     * - обрабатывет (поводит анализ лемм) содержимое страницы;
     * - записывает в базу страницу, леммы, индексы и обновляет в базе данные по сайту.
     * @return true, если процесс завершен успешно, и можно(нужно)
     * переходить к обработке дочерних страниц, иначе - false
     **/
    public boolean singePageHandler() {
        Map<String, Integer> lemmaMap = new HashMap<>();
        try {
            long sleepTime = 30L;
            Thread.sleep(sleepTime);
            Connection.Response responsePage = pageReader();
            if (responsePage.statusCode() == HttpStatus.OK.value()) {
                lemmaMap = lemmaService.lemmaCount(responsePage.body().toLowerCase());
            }
            pageEntity.setCode(responsePage.statusCode());
            pageEntity.setContent(responsePage.body());
            if (!databaseService.saveUsedPage(pageEntity, lemmaMap)) {return false;}
        } catch (Exception e) {
            if (!(e instanceof UnsupportedMimeTypeException)) {
                handlerConnectException(e);
                return false;
            }
        }
        return pageEntity.getCode() == HttpStatus.OK.value()
                && !pageEntity.getContent().isEmpty();
    }


    private void handlerConnectException(Exception e) {
        String errorMsg = e + " on page: " + pageEntity.getFullPath();
        log.error(errorMsg);
        databaseService.updateLastErrorOnSite(pageEntity.getSite(), errorMsg);
        if (!(e instanceof  IOException)) {
            log.error("shutdown pool on " + pageEntity.getFullPath());
            ForkJoinTask.getPool().shutdown();
        }
    }

    private Connection.Response pageReader() throws IOException {
        return Jsoup.connect(pageEntity.getFullPath())
                .userAgent(jsoupCfg.getUserAgent())
                .referrer(jsoupCfg.getReferrer())
                .timeout(jsoupCfg.getTimeout())
                .followRedirects(jsoupCfg.isFollowRedirects())
                .ignoreHttpErrors(jsoupCfg.isIgnoreHttpErrors())
                .ignoreContentType(jsoupCfg.isIgnoreContentType())
                .execute();
    }


    private HashSet<PageIndexService> pageHandler(PageEntity page) {
        HashSet<PageIndexService> resultPageServices = new HashSet<>();
        Document doc = Jsoup.parse(page.getContent(), page.getFullPath());
        Elements elements = doc.select("a");
        for (Element element : elements) {
            String link = element.absUrl("href").toLowerCase();
            PageEntity childPage = new PageEntity(page.getSite(), link);
            if (childPage.getPath().isEmpty()) {continue;}
            if (databaseService.existPage(childPage)) {continue;}
            resultPageServices.add(context.getBean(PageIndexService.class, context, childPage));
        }
        return resultPageServices;
    }


}
