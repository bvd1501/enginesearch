package searchengine.Utilites;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import searchengine.services.LemmaService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MorthologyTester {

    public static void main(String[] args) throws IOException {
        LemmaService lemmaService = new LemmaService();
        String text = "\n" +
                "<!DOCTYPE html>\n" +
                "<html lang=\"en\" class=\"standard-light\">\n" +
                "<head>\n" +
                "\t\n" +
                "\t<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "\t<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "\t<title>MQTT бесплатный брокер | clusterfly.ru / Главная</title>\n" +
                "\t\n" +
                "\n" +
                "<meta charset=\"utf-8\" /><meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\" /><meta name=\"description\" content=\"Доступ к mqtt брокеру предоставляется бесплатно и для всех желающих.\" /><meta property=\"og:title\" content=\"MQTT бесплатный брокер | clusterfly.ru / Главная\" /><meta property=\"og:description\" content=\"Доступ к mqtt брокеру предоставляется бесплатно и для всех желающих.\" /><meta property=\"og:type\" content=\"website\" /><meta property=\"og:url\" content=\"https://clusterfly.ru/\" /><meta property=\"og:image\" content=\"https://clusterfly.ru/cache/images/cached_e0cca23e70d6eab63c788a8a8ddcd132.png\" />\n" +
                "\t\t\n" +
                "\t\t\t\t\t\t<link href=\"/automad/blocks/dist/blocks.min.css?v=1.5.4\" rel=\"stylesheet\">\n" +
                "\t\t\t\t\t<script type=\"text/javascript\" src=\"/automad/blocks/dist/blocks.min.js?v=1.5.4\"></script>\n" +
                "<link href=\"/shared/logo_fly.png\" rel=\"shortcut icon\" type=\"image/x-icon\" />\n" +
                "\t<link href=\"/shared/logo_fly.png\" rel=\"apple-touch-icon\" />\n" +
                "\t\n" +
                "\t<link href=\"https://fonts.googleapis.com/css2?family=Fira+Mono&display=swap\" rel=\"stylesheet\">\n" +
                "\t<link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700;800&display=swap\" rel=\"stylesheet\">\t\n" +
                "\t<link href=\"/packages/standard/dist/standard.min.css?v=1.5.4\" rel=\"stylesheet\">\n" +
                "\t<script src=\"/packages/standard/dist/standard.min.js?v=1.5.4\"></script>\n" +
                "\t\n" +
                "\t\n" +
                "\t\n" +
                "</head>\n" +
                "\n" +
                "<body class=\"project\">\n" +
                "\t\t\n" +
                "\n" +
                "\n" +
                "\t<div class=\"navbar\">\n" +
                "\t\t<div class=\"uk-container uk-container-center\">\n" +
                "\t\t\t<nav class=\"uk-navbar\">\n" +
                "\t\t\t\t<a href=\"/\" class=\"uk-navbar-brand\"><?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<svg width=\"64\" height=\"64\" version=\"1.1\" id=\"Capa_1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\"\n" +
                "\t viewBox=\"0 0 512 512\" style=\"enable-background:new 0 0 512 512;\" xml:space=\"preserve\">\n" +
                "<g>\n" +
                "\t<g>\n" +
                "\t\t<path d=\"M486.3,347.5l-108-108h21.3c3.9,0,7.6,1.4,10.6,3.8l41.4,34.5c1.4,1.2,3.1,1.7,4.8,1.7c2.1,0,4.3-0.9,5.8-2.7\n" +
                "\t\t\tc2.7-3.2,2.2-7.9-1-10.6l-41.4-34.5c-5.7-4.7-12.8-7.3-20.2-7.3h-36.3l-13.7-13.7c-1.3-5.4-3.1-10.5-5.5-15.5l22.2-16.6\n" +
                "\t\t\tc5.9-4.4,9.4-11.5,9.4-18.8v-9.4c0-2.3,0.9-4.4,2.5-6l43.4-43.4c2.9-2.9,2.9-7.7,0-10.6c-2.9-2.9-7.7-2.9-10.6,0l-43.4,43.4\n" +
                "\t\t\tc-4.4,4.4-6.9,10.4-6.9,16.6v9.4c0,2.7-1.3,5.2-3.4,6.8l-21,15.8c-2.1-2.8-4.3-5.5-6.8-8c4-9.7,6.1-20,6.1-30.6\n" +
                "\t\t\tc0-34.3-21.8-63.6-52.2-74.8c37.1-46.4,85.4-54,85.9-54.1c4.1-0.6,6.9-4.4,6.3-8.5c-0.6-4.1-4.4-7-8.5-6.4\n" +
                "\t\t\tC364.8,0.4,308.9,9,267.6,65c-3.8-0.6-7.7-0.9-11.6-0.9c-3.9,0-7.8,0.3-11.6,0.9C203.1,9,147.2,0.4,144.8,0.1\n" +
                "\t\t\tc-4.1-0.6-7.9,2.3-8.5,6.4c-0.6,4.1,2.3,7.9,6.4,8.5c0.5,0.1,48.6,7.4,85.9,54.1c-30.4,11.2-52.2,40.5-52.2,74.8\n" +
                "\t\t\tc0,10.7,2.1,21,6.2,30.7c-2.4,2.5-4.7,5.2-6.7,8l-21.1-15.8c-2.1-1.6-3.4-4.2-3.4-6.8v-9.4c0-6.3-2.4-12.2-6.9-16.6L101,90.4\n" +
                "\t\t\tc-2.9-2.9-7.7-2.9-10.6,0c-2.9,2.9-2.9,7.7,0,10.6l43.4,43.4c1.6,1.6,2.5,3.8,2.5,6v9.4c0,7.4,3.5,14.4,9.4,18.8l22.2,16.7\n" +
                "\t\t\tc-2.4,4.9-4.3,10.1-5.6,15.5l-13.7,13.7h-36.3c-7.4,0-14.5,2.6-20.2,7.3l-41.4,34.5c-3.2,2.7-3.6,7.4-1,10.6\n" +
                "\t\t\tc1.5,1.8,3.6,2.7,5.8,2.7c1.7,0,3.4-0.6,4.8-1.7l41.4-34.5c3-2.5,6.7-3.8,10.6-3.8h21.3l-108,108C9.1,364,0,386,0,409.4\n" +
                "\t\t\tc0,12.8,6.5,24.5,17.3,31.3c6,3.8,12.8,5.7,19.6,5.7c5.5,0,11-1.2,16.1-3.7l53.7-25.9c0.7,0.9,1.4,1.8,2.2,2.7\n" +
                "\t\t\tc4.6,4.7,10.5,7.2,16.7,7.2c3,0,6-0.6,8.9-1.7l5.1-2c20.7,54.3,66,89.1,116.4,89.1c50.5,0,95.7-34.8,116.4-89.1l5,2\n" +
                "\t\t\tc2.9,1.2,5.9,1.7,8.9,1.7c6.2,0,12.1-2.5,16.7-7.2c0.8-0.8,1.6-1.7,2.2-2.7l53.7,25.9c5.1,2.5,10.6,3.7,16.1,3.7\n" +
                "\t\t\tc6.8,0,13.6-1.9,19.6-5.7c10.8-6.8,17.3-18.5,17.3-31.3C512,386,502.9,364,486.3,347.5z M256,159.3c5,0,10,0.4,14.9,1.2\n" +
                "\t\t\tc4.1,0.7,7.9-2.1,8.6-6.2c0.6-4.1-2.1-7.9-6.2-8.6c-5.6-0.9-11.4-1.3-17.2-1.3c-23.4,0-44.8,7.4-61.5,19.6\n" +
                "\t\t\tc-2.1-6.5-3.2-13.2-3.2-20.1c0-35.6,29-64.6,64.6-64.6s64.6,29,64.6,64.6c0,6.9-1.1,13.6-3.2,20c-3.1-2.2-6.3-4.3-9.7-6.2\n" +
                "\t\t\tc-3.6-2-8.2-0.8-10.2,2.9s-0.8,8.2,2.9,10.2c22.7,12.8,36.3,34.2,36.3,57.3c0,17.1-7.5,30.4-22.2,39.4\n" +
                "\t\t\tc-14.1,8.6-34.3,13.2-58.4,13.2s-44.4-4.6-58.4-13.2c-14.8-9-22.2-22.3-22.2-39.4C175.3,190.1,211.5,159.3,256,159.3z\n" +
                "\t\t\t M287.5,393.1c-0.5-4.1-4.2-7-8.3-6.6c-7.3,0.9-15.1,1.3-23.2,1.3c-28.4,0-52.2-5.3-69-15.4l3.9-30.6c17.7,6.5,39.9,9.9,65.1,9.9\n" +
                "\t\t\tc25.1,0,47.4-3.4,65.1-9.9l3.9,30.6c-4.6,2.8-9.8,5.2-15.4,7.2c-3.9,1.4-5.9,5.7-4.5,9.6c1.1,3.1,4,5,7.1,5c0.8,0,1.7-0.1,2.5-0.4\n" +
                "\t\t\tc4.4-1.6,8.5-3.4,12.4-5.3c0.8,4.7,2.6,9.1,5.3,12.9c-16.7,15.2-43.4,23.6-76.3,23.6c-32.8,0-59.7-8.3-76.3-23.6\n" +
                "\t\t\tc2.7-3.8,4.5-8.2,5.3-12.9c18.6,9.5,42.8,14.4,71,14.4c8.6,0,17-0.5,24.9-1.4C285,400.9,288,397.2,287.5,393.1z M192.9,326.5\n" +
                "\t\t\tl5.3-41.7c15.2,7,34.7,10.8,57.8,10.8s42.6-3.8,57.8-10.8l5.3,41.7c-16.4,6.7-38.1,10.2-63.1,10.2\n" +
                "\t\t\tC230.9,336.7,209.3,333.2,192.9,326.5z M104.2,393.7c-1.2,2.8-1.9,5.7-2,8.6l-55.7,26.9c-6.8,3.3-14.8,2.9-21.2-1.2\n" +
                "\t\t\tc-6.4-4-10.3-11-10.3-18.6c0-19.4,7.6-37.7,21.3-51.4l124.3-124.3c0.6,6.8,2.1,13.2,4.6,19L104.2,393.7z M129,411\n" +
                "\t\t\tc-4.9,2-8.4-1-9.3-2c-0.9-1-3.8-4.5-1.7-9.3L174.9,268c2.7,3,5.8,5.8,9.2,8.4l-13.9,108.9c-0.8,5.9-4.7,11-10.3,13.2L129,411z\n" +
                "\t\t\t M256,497c-44.2,0-84.1-31.2-102.5-79.7l12-4.8c0.9-0.4,1.8-0.8,2.7-1.3c19.6,18.8,49.7,28.7,87.7,28.7\n" +
                "\t\t\tc37.4,0,68.2-10.1,87.7-28.7c0.9,0.5,1.8,0.9,2.8,1.3l12,4.8C340.1,465.9,300.3,497,256,497z M486.7,428c-6.4,4-14.4,4.5-21.2,1.2\n" +
                "\t\t\tl-55.7-26.9c-0.1-2.9-0.8-5.8-2-8.6l-36.5-84.5c-1.6-3.8-6.1-5.6-9.9-3.9c-3.8,1.6-5.6,6.1-3.9,9.9l36.5,84.5\n" +
                "\t\t\tc2.1,4.9-0.8,8.4-1.7,9.3c-0.9,1-4.4,3.9-9.3,2l-31-12.4c-5.5-2.2-9.5-7.3-10.3-13.1l-13.9-109c3.4-2.6,6.5-5.4,9.2-8.4l6.5,15\n" +
                "\t\t\tc1.2,2.8,4,4.5,6.9,4.5c1,0,2-0.2,3-0.6c3.8-1.6,5.6-6.1,3.9-9.9l-10.5-24.3c2.5-5.8,4-12.2,4.6-19l124.3,124.3\n" +
                "\t\t\tc13.7,13.7,21.3,32,21.3,51.4C497,417,493.2,424,486.7,428z\"/>\n" +
                "\t</g>\n" +
                "</g>\n" +
                "</svg></a>\n" +
                "\t\t\t    <div class=\"uk-navbar-flip\">\n" +
                "\t\t\t        <ul class=\"uk-navbar-nav uk-visible-large\">\n" +
                "\t\t\t\t\t\t<li class=\"uk-active\">\n" +
                "\t\t\t\t\t\t\t\t\t<a href=\"/\">Главная</a>\n" +
                "\t\t\t\t\t\t\t\t</li><li>\n" +
                "\t\t\t\t\t\t\t\t\t<a href=\"/novosti\">Новости</a>\n" +
                "\t\t\t\t\t\t\t\t</li><li>\n" +
                "\t\t\t\t\t\t\t\t\t<a href=\"/mini-blog\">Мини блог</a>\n" +
                "\t\t\t\t\t\t\t\t</li><li>\n" +
                "\t\t\t\t\t\t\t\t\t<a href=\"/novosti/dashboard\">DashBoard</a>\n" +
                "\t\t\t\t\t\t\t\t</li><li>\n" +
                "\t\t\t\t\t\t\t\t\t<a href=\"/mini-market\">Мини-маркет</a>\n" +
                "\t\t\t\t\t\t\t\t</li></ul>\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t<a \n" +
                "\t\t\t\t\thref=\"#modal-nav\"\n" +
                "\t\t\t\t\tclass=\"navbar-toggle uk-navbar-content \" \n" +
                "\t\t\t\t\tdata-modal-toggle=\"#modal-nav\"\n" +
                "\t\t\t\t\t>\n" +
                "\t\t\t\t\t\t<span aria-hidden=\"true\"></span>\n" +
                "\t\t\t\t\t\t<span aria-hidden=\"true\"></span>\n" +
                "\t\t\t\t\t\t<span aria-hidden=\"true\"></span>\n" +
                "\t\t\t\t\t</a>\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t\t<div id=\"modal-nav\" class=\"uk-modal\">\t\t\n" +
                "\t\t\t\t\t\t<div class=\"uk-modal-dialog uk-modal-dialog-blank\">\n" +
                "\t\t\t\t\t\t\t<div class=\"uk-container uk-container-center\">\n" +
                "\t\t\t\t\t\t\t\t\t<div class=\"uk-block uk-margin-bottom-remove uk-margin-top-remove\">\n" +
                "\t\t\t\t\t\t\t\t\t\t<form \n" +
                "\t\t\t\t\t\t\t\t\t\tclass=\"uk-form\" \n" +
                "\t\t\t\t\t\t\t\t\t\taction=\"/\" \n" +
                "\t\t\t\t\t\t\t\t\t\tmethod=\"get\"\n" +
                "\t\t\t\t\t\t\t\t\t\t>\n" +
                "\t\t\t\t\t\t\t\t\t\t\t<script>\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tvar autocomplete = [{\"value\":\"\\u041e\\u0442\\u0431\\u043b\\u0430\\u0433\\u043e\\u0434\\u0430\\u0440\\u0438\\u0442\\u044c\"},{\"value\":\"\\u0427\\u0442\\u043e \\u0442\\u0430\\u043a\\u043e\\u0435 \\\"\\u043c\\u043e\\u0438 \\u0443\\u0441\\u0442\\u0440\\u043e\\u0439\\u0441\\u0442\\u0432\\u0430?\\\"\"},{\"value\":\"DashBoard\"},{\"value\":\"\\u0414\\u043e\\u0431\\u0430\\u0432\\u0438\\u043b\\u0438 \\u0447\\u0430\\u0442 \\u0434\\u043b\\u044f \\u043e\\u0431\\u0440\\u0430\\u0449\\u0435\\u043d\\u0438\\u044f \\u0432 \\u0441\\u043b\\u0443\\u0436\\u0431\\u0443 \\u043f\\u043e\\u0434\\u0434\\u0435\\u0440\\u0436\\u043a\\u0438\"},{\"value\":\"\\u0414\\u043e\\u0431\\u0430\\u0432\\u0438\\u043b\\u0438 \\u043b\\u043e\\u0433\\u0438\"},{\"value\":\"\\u0414\\u043e\\u0431\\u0430\\u0432\\u0438\\u043b\\u0438 \\u043c\\u043e\\u0431\\u0438\\u043b\\u044c\\u043d\\u043e\\u0441\\u0442\\u0438 \\u0432 \\u041b\\u041a\"},{\"value\":\"\\u0414\\u043e\\u0431\\u0430\\u0432\\u0438\\u043b\\u0438 \\u0432 \\u0442\\u0435\\u0441\\u0442\\u043e\\u0432\\u043e\\u043c \\u0440\\u0435\\u0436\\u0438\\u043c\\u0435 web client\"},{\"value\":\"\\u0414\\u043e\\u0431\\u0430\\u0432\\u0438\\u043b\\u0438 Web \\u043a\\u043b\\u0438\\u0435\\u043d\\u0442 \\u0434\\u043b\\u044f [SRV.1]\"},{\"value\":\"\\u041a\\u0430\\u043a \\u043d\\u0430\\u0441\\u0442\\u0440\\u043e\\u0438\\u0442\\u044c \\u0414\\u043e\\u043c\\u043e\\u0432\\u0435\\u043d\\u043a\\u0430.\\u041a\\u0443\\u0437\\u044e \\u043e\\u0442 \\u042f\\u043d\\u0434\\u0435\\u043a\\u0441\\u0430\"},{\"value\":\"\\u041a\\u0430\\u043a \\u043f\\u043e\\u0434\\u043f\\u0438\\u0441\\u044b\\u0432\\u0430\\u0442\\u044c\\u0441\\u044f \\u0438 \\u043f\\u0443\\u0431\\u043b\\u0438\\u043a\\u043e\\u0432\\u0430\\u0442\\u044c \\u0442\\u043e\\u043f\\u0438\\u043a\\u0438?\"},{\"value\":\"\\u041a\\u0430\\u043a \\u043f\\u0440\\u043e\\u0448\\u0438\\u0442\\u044c Sonoff Mini R2 \\u043d\\u0430 Tasmota \\u0447\\u0435\\u0440\\u0435\\u0437 UART\"},{\"value\":\"\\u041a\\u0430\\u043a \\u0441\\u043e\\u0437\\u0434\\u0430\\u0432\\u0430\\u0442\\u044c \\u0442\\u043e\\u043f\\u0438\\u043a\\u0438?\"},{\"value\":\"\\u041c\\u0438\\u043d\\u0438-\\u043c\\u0430\\u0440\\u043a\\u0435\\u0442\"},{\"value\":\"\\u041c\\u0438\\u043d\\u0438-\\u043c\\u0430\\u0440\\u043a\\u0435\\u0442\"},{\"value\":\"\\u041c\\u044b \\u043f\\u0435\\u0440\\u0435\\u0435\\u0437\\u0436\\u0430\\u0435\\u043c \\u043d\\u0430 \\u043d\\u043e\\u0432\\u044b\\u0439 \\u0434\\u043e\\u043c\\u0435\\u043d clusterfly.ru\"},{\"value\":\"\\u041d\\u0430\\u0441\\u0442\\u0440\\u043e\\u0439\\u043a\\u0430 \\u043f\\u043e\\u0434\\u043a\\u043b\\u044e\\u0447\\u0435\\u043d\\u0438\\u044f \\u043f\\u043e TCP (c ssl)\"},{\"value\":\"\\u041d\\u0430\\u0441\\u0442\\u0440\\u043e\\u0439\\u043a\\u0430 \\u043f\\u043e\\u0434\\u043a\\u043b\\u044e\\u0447\\u0435\\u043d\\u0438\\u044f \\u043f\\u043e Websocket (\\u0431\\u0435\\u0437 ssl)\"},{\"value\":\"\\u041d\\u0430\\u0441\\u0442\\u0440\\u043e\\u0439\\u043a\\u0430 \\u043f\\u043e\\u0434\\u043a\\u043b\\u044e\\u0447\\u0435\\u043d\\u0438\\u044f \\u043f\\u043e Websocket (\\u0441 ssl)\"},{\"value\":\"\\u041d\\u043e\\u0432\\u044b\\u0439 \\u041b\\u041a\"},{\"value\":\"\\u041d\\u043e\\u0432\\u044b\\u0439 \\u0441\\u0435\\u0440\\u0432\\u0435\\u0440 srv2 \\u0432 \\u0431\\u0435\\u0442\\u0430 \\u0442\\u0435\\u0441\\u0442\\u0435\"},{\"value\":\"\\u041e\\u0431\\u043d\\u043e\\u0432\\u043b\\u0435\\u043d\\u0438\\u0435 [SRV.2]\"},{\"value\":\"\\u041f\\u043e\\u0447\\u0435\\u043c\\u0443 \\u043d\\u0435 ... ?\"},{\"value\":\"\\u041f\\u043e\\u0447\\u0435\\u043c\\u0443 \\u043d\\u0435 \\u043f\\u0440\\u043e\\u0445\\u043e\\u0434\\u0438\\u0442 \\u0430\\u0432\\u0442\\u043e\\u0440\\u0438\\u0437\\u0430\\u0446\\u0438\\u044f?\"},{\"value\":\"\\u041f\\u043e\\u0434\\u043a\\u043b\\u044e\\u0447\\u0435\\u043d\\u0438\\u0435 \\u0438 \\u043d\\u0430\\u0441\\u0442\\u0440\\u043e\\u0439\\u043a\\u0430 Raspberry PI OS\"},{\"value\":\"\\u041f\\u043e\\u0434\\u043a\\u043b\\u044e\\u0447\\u0435\\u043d\\u0438\\u0435 \\u0441 \\u043f\\u0440\\u043e\\u0448\\u0438\\u0432\\u043a\\u043e\\u0439 ESP Easy\"},{\"value\":\"\\u041f\\u0440\\u043e\\u0448\\u0438\\u0432\\u043a\\u0430 ESP32/12 \\u043d\\u0430 Tasmota \\u0438 \\u043f\\u043e\\u0434\\u043a\\u043b\\u044e\\u0447\\u0435\\u043d\\u0438\\u0435 \\u043f\\u043e MQTT\"},{\"value\":\"\\u041f\\u0440\\u043e\\u0448\\u0438\\u0432\\u043a\\u0430 ESP32-CAM \\u043d\\u0430 Tasmota\"},{\"value\":\"\\u0420\\u0430\\u0437\\u0431\\u043e\\u0440 \\u0438 \\u043f\\u0440\\u043e\\u0448\\u0438\\u0432\\u043a\\u0430 \\u0443\\u043c\\u043d\\u043e\\u0433\\u043e \\u0430\\u0432\\u0442\\u043e\\u043c\\u0430\\u0442\\u0430 (wifi \\u0440\\u0435\\u043b\\u0435) Tuya 1P\"},{\"value\":\"\\u0420\\u0435\\u0433\\u0438\\u0441\\u0442\\u0440\\u0430\\u0446\\u0438\\u044f \\u043d\\u0430 [SRV.2]\"},{\"value\":\"\\u0421\\u043c\\u0435\\u043d\\u0430 IP \\u0430\\u0434\\u0440\\u0435\\u0441\\u0430 \\u0441\\u0435\\u0440\\u0432\\u0435\\u0440\\u0430 [SRV.1]\"},{\"value\":\"\\u0423\\u043f\\u0440\\u0430\\u0432\\u043b\\u0435\\u043d\\u0438\\u0435 \\u0447\\u0435\\u0440\\u0435\\u0437 Tasmota\"},{\"value\":\"\\u0423\\u0441\\u0442\\u0430\\u043d\\u043e\\u0432\\u043a\\u0430 Raspberry Pi OS Lite\"},{\"value\":\"\\u0412 \\u041b\\u041a \\u0434\\u043e\\u0431\\u0430\\u0432\\u043b\\u0435\\u043d web-client \\u0434\\u043b\\u044f [SRV.2]\"}]\n" +
                "\t\t\t\t\t\t\t\t\t\t\t</script>\n" +
                "\t\t\t\t\t\t\t\t\t\t\t<div \n" +
                "\t\t\t\t\t\t\t\t\t\t\tclass=\"uk-autocomplete uk-width-1-1\" \n" +
                "\t\t\t\t\t\t\t\t\t\t\tdata-uk-autocomplete='{source:autocomplete,minLength:2}'\n" +
                "\t\t\t\t\t\t\t\t\t\t\t>\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t<input \n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tclass=\"uk-form-controls uk-form-large uk-width-1-1\" \n" +
                "\t\t\t\t\t\t\t\t\t\t\t\ttype=\"search\" \n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tname=\"search\" \n" +
                "\t\t\t\t\t\t\t\t\t\t\t\tplaceholder=\"Search\" \n" +
                "\t\t\t\t\t\t\t\t\t\t\t\trequired \n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t/>\t\n" +
                "\t\t\t\t\t\t\t\t\t\t\t</div>\n" +
                "\t\t\t\t\t\t\t\t\t\t</form>\t\n" +
                "\t\t\t\t\t\t\t\t\t</div>\n" +
                "\t\t\t\t\t\t\t\t<div class=\"uk-block \">\n" +
                "\t\t\t\t\t\t\t\t\t\n" +
                "<ul class=\"uk-nav uk-nav-side\">\n" +
                "\t\t\t<li class=\"uk-active\">\n" +
                "\t\t\t\t<a href=\"/\">Главная</a>\n" +
                "\t\t\t</li>\n" +
                "\t\t</ul>\n" +
                "\t\t\t<ul class=\"uk-nav uk-nav-side\"><li>\n" +
                "\t\t\t\t\t\t\t<a href=\"/novosti\">Новости</a></li><li>\n" +
                "\t\t\t\t\t\t\t<a href=\"/mini-blog\">Мини блог</a></li><li>\n" +
                "\t\t\t\t\t\t\t<a href=\"/otblagodarit\">Отблагодарить</a></li><li>\n" +
                "\t\t\t\t\t\t\t<a href=\"/mini-market\">Мини-маркет</a></li>\n" +
                "\t\t\t</ul>\n" +
                "\t\t\t\t\t\t\t\t</div>\n" +
                "\t\t\t\t\t\t\t</div>\n" +
                "\t\t\t\t\t\t</div>\n" +
                "\t\t\t\t\t</div>\n" +
                "\t\t\t    </div>\n" +
                "\t\t\t</nav>\n" +
                "\t\t</div>\n" +
                "\t</div>\n" +
                "\t<div class=\"uk-container uk-container-center navbar-push\">\n" +
                "\t<div class=\"content uk-block\">\n" +
                "\t\t\n" +
                "\n" +
                "\t<h1 id=\"mqtt-broker\">MQTT брокер</h1><p><u class=\"cdx-underline\">Доступ </u>к mqtt брокеру предоставляется <u class=\"cdx-underline\">бесплатно </u>и для всех желающих.</p><p>Авторизация и регистрация на сервисе - через социальные сети.</p><p>Наша группа в&nbsp;<a href=\"https://t.me/clusterfly_ru\" target=\"_blank\">телеграмм</a>, начинающий&nbsp;<a href=\"https://www.youtube.com/channel/UCRNWBPUZEpy3ZOiFbKbEV9g/\" target=\"_blank\">YouTube канал</a>.</p><p>В <a href=\"/mini-blog\">блоге</a> и на <a href=\"https://www.youtube.com/channel/UCRNWBPUZEpy3ZOiFbKbEV9g/\">YouTube</a> выкладываем статьи и видео с инструкциями по работе с MQTT.</p><h2 id=\"avtorizaciya-and-amp-registraciya\">Авторизация & Регистрация</h2><p>Выберите социальную сеть для авторизации</p><script src=\"//ulogin.ru/js/ulogin.js\"></script>\n" +
                "<div class=\"content uk-block\"><div id=\"uLogin\" data-ulogin=\"display=panel;theme=flat;fields=first_name,last_name;providers=google,facebook,yandex,twitter,vkontakte,odnoklassniki,mailru;hidden=;redirect_uri=https%3A%2F%2Fclusterfly.ru%3Flogin%3Dtrue;mobilebuttons=0;\"></div><div id=\"auth_tg\"><script async src=\"https://telegram.org/js/telegram-widget.js?2\" data-telegram-login=\"clusterflyRuBot\" data-size=\"large\" data-auth-url=\"/\"></script></div></div><script>\n" +
                "window.replainSettings = { id: 'e8a58c4d-4af9-4ab7-a4e5-50f07d5f9c99' };\n" +
                "(function(u){var s=document.createElement('script');s.type='text/javascript';s.async=true;s.src=u;\n" +
                "var x=document.getElementsByTagName('script')[0];x.parentNode.insertBefore(s,x);\n" +
                "})('https://widget.replain.cc/dist/client.js');\n" +
                "</script><script>\n" +
                "      $(document).ready(function(){\n" +
                "function sayHi() {\n" +
                "           if ($('#uLogin').height() <= 0) {\n" +
                "               alert('Отключите блокировщик рекламы. Могут быть проблемы с отображением вариантов регистрации/авторизации.');\n" +
                "           }\n" +
                "}\n" +
                "setTimeout(sayHi, 2000);\n" +
                "      });\n" +
                "</script>\n" +
                "\n" +
                "\t</div>\n" +
                "\t<div class=\"content uk-block\">\n" +
                "\t\t\n" +
                "\t</div>\n" +
                "\t<div class=\"content uk-block\">\n" +
                "\t\t<div class=\"content uk-block\">\n" +
                "\t\n" +
                "\t\n" +
                "\t\n" +
                "\t\t  \n" +
                "\t<div class=\"masonry masonry-large am-stretched\">\n" +
                "\t\t\t\n" +
                "\t\t\t<div class=\"masonry-item\" style=\"grid-row: 1 / span 1;\">\n" +
                "\t\t\t\t<div class=\"masonry-content uk-panel uk-panel-box\">\n" +
                "\t\t\t\t\t<div class=\"uk-panel-title\">\n" +
                "\t\t\t\t\t\t<a href=\"/novosti/dashboard\" class=\"nav-link\" target=\"_blank\">\n" +
                "\t\t\t\t\t\t\tDashBoard\n" +
                "\t\t\t\t\t\t</a>\n" +
                "\t\t\t\t\t\t<div class=\"text-subtitle\">\n" +
                "\t\t\t\t\t\t\t04 January 2023\n" +
                "\t\t\t\t\t\t</div>\n" +
                "\t\t\t\t\t</div>\n" +
                "\t\t\t\t\t<p class=\"content uk-margin-bottom-remove\">Обзавелись собственным дашбордом. Дашборд доступен всем в рамках не коммерческого использования.</p>\n" +
                "\t\t\t\t\t<a href=\"/novosti/dashboard\" class=\"nav-link panel-more\" target=\"_blank\">\n" +
                "\t\t\t\t\t\t<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"1em\" height=\"1em\" viewBox=\"0 0 30 30\">\n" +
                "\t\t\t\t\t\t\t<polygon points=\"5,0 5,5 21.001,5 21,5 0,25.986 3.993,30 25,9 25,25 30,25 30,0 \"></polygon>\n" +
                "\t\t\t\t\t\t</svg>\n" +
                "\t\t\t\t\t</a>\n" +
                "\t\t\t\t</div>\n" +
                "\t\t\t</div>\n" +
                "\t\t\t\n" +
                "\t\t\t<div class=\"masonry-item\" style=\"grid-row: 1 / span 1;\">\n" +
                "\t\t\t\t<div class=\"masonry-content uk-panel uk-panel-box\">\n" +
                "\t\t\t\t\t<div class=\"uk-panel-title\">\n" +
                "\t\t\t\t\t\t<a href=\"/novosti/mini-market\" class=\"nav-link\" target=\"_blank\">\n" +
                "\t\t\t\t\t\t\tМини-маркет\n" +
                "\t\t\t\t\t\t</a>\n" +
                "\t\t\t\t\t\t<div class=\"text-subtitle\">\n" +
                "\t\t\t\t\t\t\t30 April 2022\n" +
                "\t\t\t\t\t\t</div>\n" +
                "\t\t\t\t\t</div>\n" +
                "\t\t\t\t\t<p class=\"content uk-margin-bottom-remove\">У нас появился небольшой магазин в котором собраны diy устройства для автоматизации. Ссылка на раздел доступна в верхнем меню.</p>\n" +
                "\t\t\t\t\t<a href=\"/novosti/mini-market\" class=\"nav-link panel-more\" target=\"_blank\">\n" +
                "\t\t\t\t\t\t<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"1em\" height=\"1em\" viewBox=\"0 0 30 30\">\n" +
                "\t\t\t\t\t\t\t<polygon points=\"5,0 5,5 21.001,5 21,5 0,25.986 3.993,30 25,9 25,25 30,25 30,0 \"></polygon>\n" +
                "\t\t\t\t\t\t</svg>\n" +
                "\t\t\t\t\t</a>\n" +
                "\t\t\t\t</div>\n" +
                "\t\t\t</div>\n" +
                "\t\t\t\n" +
                "\t\t</div>\n" +
                "\t\n" +
                "\t\n" +
                "</div>\n" +
                "\t</div>\n" +
                "\t<div class=\"content uk-block\">\n" +
                "\t\t<div class=\"content uk-block\">\n" +
                "\t\n" +
                "\t\n" +
                "\t\n" +
                "\t\t  \n" +
                "\t<div class=\"masonry masonry-large am-stretched\">\n" +
                "\t\t\t\n" +
                "\t\t\t<div class=\"masonry-item\" style=\"grid-row: 1 / span 1;\">\n" +
                "\t\t\t\t<div class=\"masonry-content uk-panel uk-panel-box\">\n" +
                "\t\t\t\t\t<div class=\"uk-panel-title\">\n" +
                "\t\t\t\t\t\t<a href=\"/mini-blog/kak-proshit-sonoff-mini-r2-na-tasmota-cherez-uart\" class=\"nav-link\" target=\"_blank\">\n" +
                "\t\t\t\t\t\t\tКак прошить Sonoff Mini R2 на Tasmota через UART\n" +
                "\t\t\t\t\t\t</a>\n" +
                "\t\t\t\t\t\t<div class=\"text-subtitle\">\n" +
                "\t\t\t\t\t\t\t18 December 2021\n" +
                "\t\t\t\t\t\t</div>\n" +
                "\t\t\t\t\t</div>\n" +
                "\t\t\t\t\t<p class=\"content uk-margin-bottom-remove\">Как прошить Sonoff Mini R2 через UART на Tasmota или восстановить после неудачной прошивки. (How to firmware Sonoff Mini R2 on Tasmota by UART or recovery it)</p>\n" +
                "\t\t\t\t\t<a href=\"/mini-blog/kak-proshit-sonoff-mini-r2-na-tasmota-cherez-uart\" class=\"nav-link panel-more\" target=\"_blank\">\n" +
                "\t\t\t\t\t\t<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"1em\" height=\"1em\" viewBox=\"0 0 30 30\">\n" +
                "\t\t\t\t\t\t\t<polygon points=\"5,0 5,5 21.001,5 21,5 0,25.986 3.993,30 25,9 25,25 30,25 30,0 \"></polygon>\n" +
                "\t\t\t\t\t\t</svg>\n" +
                "\t\t\t\t\t</a>\n" +
                "\t\t\t\t</div>\n" +
                "\t\t\t</div>\n" +
                "\t\t\t\n" +
                "\t\t\t<div class=\"masonry-item\" style=\"grid-row: 1 / span 1;\">\n" +
                "\t\t\t\t<div class=\"masonry-content uk-panel uk-panel-box\">\n" +
                "\t\t\t\t\t<div class=\"uk-panel-title\">\n" +
                "\t\t\t\t\t\t<a href=\"/mini-blog/podklyuchenie-i-nastroika-raspberry-pi-os\" class=\"nav-link\" target=\"_blank\">\n" +
                "\t\t\t\t\t\t\tПодключение и настройка Raspberry PI OS\n" +
                "\t\t\t\t\t\t</a>\n" +
                "\t\t\t\t\t\t<div class=\"text-subtitle\">\n" +
                "\t\t\t\t\t\t\t18 February 2021\n" +
                "\t\t\t\t\t\t</div>\n" +
                "\t\t\t\t\t</div>\n" +
                "\t\t\t\t\t<p class=\"content uk-margin-bottom-remove\">Четвертая часть видео из серии \"Умный дом\". Подключение и настройка Raspberry PI OS. Третье видео доступно по&nbsp;ссылке.&nbsp;&nbsp;</p>\n" +
                "\t\t\t\t\t<a href=\"/mini-blog/podklyuchenie-i-nastroika-raspberry-pi-os\" class=\"nav-link panel-more\" target=\"_blank\">\n" +
                "\t\t\t\t\t\t<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"1em\" height=\"1em\" viewBox=\"0 0 30 30\">\n" +
                "\t\t\t\t\t\t\t<polygon points=\"5,0 5,5 21.001,5 21,5 0,25.986 3.993,30 25,9 25,25 30,25 30,0 \"></polygon>\n" +
                "\t\t\t\t\t\t</svg>\n" +
                "\t\t\t\t\t</a>\n" +
                "\t\t\t\t</div>\n" +
                "\t\t\t</div>\n" +
                "\t\t\t\n" +
                "\t\t</div>\n" +
                "\t\n" +
                "\t\n" +
                "</div>\n" +
                "\t</div>\n" +
                "\t\t<div class=\"uk-block\">\n" +
                "\t\t\t<div class=\"footer uk-margin-bottom\">\n" +
                "\t\t\t\t<ul class=\"uk-grid uk-grid-width-medium-1-2 uk-margin-top\" data-uk-grid-margin>\n" +
                "\t\t\t\t<!-- <ul class=\"uk-grid uk-grid-width-medium-1-1 uk-margin-top\" data-uk-grid-margin> -->\n" +
                "\t\t\t\t\t<li>\n" +
                "\t\t\t\t\t</li>\n" +
                "\t\t\t\t\t<li class=\"uk-text-right uk-text-left-small\">\n" +
                "\t\t\t\t\t\t<a href=\"/\">\n" +
                "\t\t\t\t\t\t\t&copy; 2023 MQTT бесплатный брокер | clusterfly.ru\n" +
                "\t\t\t\t\t\t</a>\n" +
                "\t\t\t\t\t</li>\n" +
                "\t\t\t\t</ul>\n" +
                "\t\t\t\t\n" +
                "\t\t\t\t\n" +
                "\t\t\t</div>\n" +
                "\t\t</div>\n" +
                "\t</div>\n" +
                "<!-- Yandex.Metrika counter -->\n" +
                "<script type=\"text/javascript\" >\n" +
                "   (function(m,e,t,r,i,k,a){m[i]=m[i]||function(){(m[i].a=m[i].a||[]).push(arguments)};\n" +
                "   m[i].l=1*new Date();k=e.createElement(t),a=e.getElementsByTagName(t)[0],k.async=1,k.src=r,a.parentNode.insertBefore(k,a)})\n" +
                "   (window, document, \"script\", \"https://mc.yandex.ru/metrika/tag.js\", \"ym\");\n" +
                "\n" +
                "   ym(52016462, \"init\", {\n" +
                "        clickmap:true,\n" +
                "        trackLinks:true,\n" +
                "        accurateTrackBounce:true\n" +
                "   });\n" +
                "</script>\n" +
                "<noscript><div><img src=\"https://mc.yandex.ru/watch/52016462\" style=\"position:absolute; left:-9999px;\" alt=\"\" /></div></noscript>\n" +
                "<!-- /Yandex.Metrika counter -->\n" +
                "</body>\n" +
                "</html>";
        Map<String, Integer> lemmas = lemmaService.lemmaCount(text);
        lemmas.entrySet().stream().forEach(entry -> {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        });

    }
}
