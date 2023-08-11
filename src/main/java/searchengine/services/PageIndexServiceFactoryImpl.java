package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import searchengine.model.SiteEntity;

import java.net.URL;
import java.util.concurrent.ForkJoinPool;

@Component
public class PageIndexServiceFactoryImpl implements PageIndexServiceFactory{
    private final ApplicationContext context;

    @Autowired
    public PageIndexServiceFactoryImpl(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public PageIndexService create(SiteEntity site, URL page, ForkJoinPool pool) {
        return new PageIndexService(context, site, page, pool);
    }
}
