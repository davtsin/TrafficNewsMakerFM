/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

 /*
Description:
- without lyambda expressions
- changing padej in getHighways
 */
package trafficnewsmakerfm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Avtsin Denis
 * @version 1.1 from 1.1.2
 */
public class TrafficParsing {

    private Document doc;
    private List<String> centerList;
    private List<String> oblastList;
    private List<String> sortedCenterList;
    private List<String> sortedOblastList;
    private String dateFromWebPage;

    public TrafficParsing(String file) throws IOException {
        File input = new File(file);
        doc = Jsoup.parse(input, "UTF-8");
    }

    public String getTimeInfo() {
        StringBuilder sb = new StringBuilder();
        Elements elements = doc.getElementsByAttributeValue(
            "class", "update-button__date");
        SimpleDateFormat dateFormat
            = new SimpleDateFormat("dd MMMM HH:mm");
        dateFromWebPage = elements.text();
        String warning = "дата отсутствует";
        sb.append("Актуальность веб-страницы: ")
            .append(dateFromWebPage.equals("") ? warning : dateFromWebPage);
        sb.append("\n");
        sb.append("Дата создания: ")
            .append(dateFormat.format(new Date())).append("\n");

        return sb.toString();
    }

    public String getTotal() {
        // block with main main traffic
        StringBuilder sb = new StringBuilder();
        sb.append("Информация предоставлена порталом Яндекс.пробки. ");
        Elements elements = doc.getElementsByAttributeValue(
            "class", "jam jam_type_total-length section__list section__list_type_ul");
        sb.append(elements.get(0).text());
        elements = doc.getElementsByAttributeValue("class", "informer");
        sb.append("\nСитуация оценивается в ");
        sb.append(elements.text()).append(".");

        return sb.toString();
    }

    public String getRingsBySectors() {
        // block with rings traffic
        StringBuilder sb = new StringBuilder();
        Elements elements = doc.getElementsByAttributeValue(
            "data-id", "rings-load-with-sectors");
        // move to next block "section_content" with rings
        Element e = elements.first().nextElementSibling();
        // split into separate rings
        Element mkad = e.child(0).child(0).child(0);
        Element ttk = e.child(0).child(0).child(1);
        Element sadovoe = e.child(0).child(0).child(2);
        Element bulvarnoe = e.child(0).child(0).child(3);

        // common information without loaded directions
        sb.append(bulvarnoe.getElementsByTag("h3").text()).append(": ")
            .append(bulvarnoe.getElementsByTag("h4").text()).append("\n")
            .append(sadovoe.getElementsByTag("h3").text()).append(": ")
            .append(sadovoe.getElementsByTag("h4").text()).append("\n")
            .append(ttk.getElementsByTag("h3").text()).append(": ")
            .append(ttk.getElementsByTag("h4").text()).append("\n")
            .append(mkad.getElementsByTag("h3").text()).append(": ")
            .append(mkad.getElementsByTag("h4").text());

        return sb.toString();
    }

    public Map<String, String> getCenterHighways() {

        Elements elements = doc.getElementsByAttributeValue(
            "data-id", "city-highways-jams");
        // move to "section content" block
        Element e2 = elements.first().nextElementSibling();

        elements = e2.getElementsByAttributeValue("class", "jam__section");
        Element center = elements.get(0); // block about center
        //Element oblast = elements.get(1); // block about oblast

        // check info about center header
        String centerHeader = center.getElementsByTag("h3").text();
        if (!centerHeader.toLowerCase().equals("в центр")) {
            System.out.println("getCenterHighways(): center header "
                + "is not correct");
        }

        // move info about highway jams into List
        centerList = new ArrayList<>();
        for (Element elem
            : center.getElementsByAttributeValue("class", "jam__highway")) {
            centerList.add(elem.getElementsByTag("h4").text());
        }
        // sort list of highways by watch direction
        sortedCenterList = sortByWatchDirection(centerList);

        Map<String, String> map = new LinkedHashMap<>();

        for (String s : sortedCenterList) {
            int index = s.indexOf("—");
            String street = s.substring(0, index - 1);
            String load = s.substring(index + 2, s.length());
            map.put(street, load);
        }
        return map;
    }

    public Map<String, String> getOblastHighways() {

        Elements elements = doc.getElementsByAttributeValue(
            "data-id", "city-highways-jams");
        // move to "section content" block
        Element e2 = elements.first().nextElementSibling();

        elements = e2.getElementsByAttributeValue("class", "jam__section");
        //Element center = elements.get(0); // block about center
        Element oblast = elements.get(1); // block about oblast

        // check info about oblast header
        String oblastHeader = oblast.getElementsByTag("h3").text();
        if (!oblastHeader.toLowerCase().equals("из центра")) {
            System.out.println("getOblastHighways(): oblast header "
                + "is not correct");
        }

        // move info about highway jams into List
        oblastList = new ArrayList<>();
        for (Element elem
            : oblast.getElementsByAttributeValue("class", "jam__highway")) {
            oblastList.add(elem.getElementsByTag("h4").text());
        }
        // sort list of highways by watch direction
        sortedOblastList = sortByWatchDirection(oblastList);

        Map<String, String> map = new LinkedHashMap<>();

        for (String s : sortedOblastList) {
            int index = s.indexOf("—");
            String street = s.substring(0, index - 1);
            String load = s.substring(index + 2, s.length());
            map.put(street, load);
        }
        return map;
    }

    public Map<String, String> changePadej(Map<String, String> map) {
        Map<String, String> changedMap = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry
            : map.entrySet()) {
            if (entry.getKey().toLowerCase().equals("дмитровское шоссе")) {
                changedMap.put("Дмитровском шоссе", entry.getValue());
            } else if (entry.getKey().toLowerCase().equals("алтуфьевское шоссе")) {
                changedMap.put("Алтуфьевском шоссе", entry.getValue());
            } else if (entry.getKey().toLowerCase().equals("проспект мира")) {
                changedMap.put("Проспекте мира", entry.getValue());
            } else if (entry.getKey().toLowerCase().equals("щёлковское шоссе")) {
                changedMap.put("Щёлковском шоссе", entry.getValue());
            } else if (entry.getKey().toLowerCase().equals("шоссе энтузиастов")) {
                changedMap.put("шоссе Энтузиастов", entry.getValue());
            } else if (entry.getKey().toLowerCase().equals("рязанский проспект")) {
                changedMap.put("Рязанском проспекте", entry.getValue());
            } else if (entry.getKey().toLowerCase().equals("волгоградский проспект")) {
                changedMap.put("Волгоградском проспекте", entry.getValue());
            } else if (entry.getKey().toLowerCase().equals("люблинская улица")) {
                changedMap.put("Люблинской улице", entry.getValue());
            } else if (entry.getKey().toLowerCase().equals("каширское шоссе")) {
                changedMap.put("Каширском шоссе", entry.getValue());
            } else if (entry.getKey().toLowerCase().equals("проспект андропова")) {
                changedMap.put("проспекте Андропова", entry.getValue());
            } else if (entry.getKey().toLowerCase().equals("липецкая улица")) {
                changedMap.put("Липецкой улице", entry.getValue());
            } else if (entry.getKey().toLowerCase().equals("варшавское шоссе")) {
                changedMap.put("Варшавском шоссе", entry.getValue());
            } else if (entry.getKey().toLowerCase().equals("севастопольский проспект")) {
                changedMap.put("Севастопольском проспекте", entry.getValue());
            } else if (entry.getKey().toLowerCase().equals("профсоюзная улица")) {
                changedMap.put("Профсоюзной улице", entry.getValue());
            } else if (entry.getKey().toLowerCase().equals("ленинский проспект")) {
                changedMap.put("Ленинском проспекте", entry.getValue());
            } else if (entry.getKey().toLowerCase().equals("проспект вернадского")) {
                changedMap.put("проспекте Вернадского", entry.getValue());
            } else if (entry.getKey().toLowerCase().equals("мичуринский проспект")) {
                changedMap.put("Мичуринском проспекте", entry.getValue());
            } else if (entry.getKey().toLowerCase().equals("кутузовский проспект")) {
                changedMap.put("Кутузовском проспекте", entry.getValue());
            } else if (entry.getKey().toLowerCase().equals("звенигородское шоссе")) {
                changedMap.put("Звенигородском шоссе", entry.getValue());
            } else if (entry.getKey().toLowerCase().equals("волоколамское шоссе")) {
                changedMap.put("Волоколамском шоссе", entry.getValue());
            } else if (entry.getKey().toLowerCase().equals("ленинградское шоссе")) {
                changedMap.put("Ленинградском шоссе", entry.getValue());
            }
        }
        return changedMap;
    }

    private List<String> sortByWatchDirection(List<String> list) {
        String[] highways = new String[21];

        for (String s : list) {
            String oldVal = s;
            s = s.toLowerCase();
            if (s.contains("дмитровское шоссе")) {
                highways[0] = oldVal;
            } else if (s.contains("алтуфьевское шоссе")) {
                highways[1] = oldVal;
            } else if (s.contains("проспект мира")) {
                highways[2] = oldVal;
            } else if (s.contains("щёлковское шоссе")) {
                highways[3] = oldVal;
            } else if (s.contains("шоссе энтузиастов")) {
                highways[4] = oldVal;
            } else if (s.contains("рязанский проспект")) {
                highways[5] = oldVal;
            } else if (s.contains("волгоградский проспект")) {
                highways[6] = oldVal;
            } else if (s.contains("люблинская улица")) {
                highways[7] = oldVal;
            } else if (s.contains("каширское шоссе")) {
                highways[8] = oldVal;
            } else if (s.contains("проспект андропова")) {
                highways[9] = oldVal;
            } else if (s.contains("липецкая улица")) {
                highways[10] = oldVal;
            } else if (s.contains("варшавское шоссе")) {
                highways[11] = oldVal;
            } else if (s.contains("севастопольский проспект")) {
                highways[12] = oldVal;
            } else if (s.contains("профсоюзная улица")) {
                highways[13] = oldVal;
            } else if (s.contains("ленинский проспект")) {
                highways[14] = oldVal;
            } else if (s.contains("проспект вернадского")) {
                highways[15] = oldVal;
            } else if (s.contains("мичуринский проспект")) {
                highways[16] = oldVal;
            } else if (s.contains("кутузовский проспект")) {
                highways[17] = oldVal;
            } else if (s.contains("звенигородское шоссе")) {
                highways[18] = oldVal;
            } else if (s.contains("волоколамское шоссе")) {
                highways[19] = oldVal;
            } else if (s.contains("ленинградское шоссе")) {
                highways[20] = oldVal;
            }
        }

        List<String> result = new ArrayList<>();
        for (String s : highways) {
            if (s != null) {
                result.add(s);
            }
        }
        return result;
    }

    // test method for console param -t
    public void showHigways() {
        System.out.println("center full list: " + centerList.size());
        for (String s : centerList) {
            System.out.println(s);
        }
        System.out.println("");

        System.out.println("center sorted list: " + sortedCenterList.size());
        for (String s : sortedCenterList) {
            System.out.println(s);
        }
        System.out.println("");

        System.out.println("oblast full list: " + oblastList.size());
        for (String s : oblastList) {
            System.out.println(s);
        }
        System.out.println("");

        System.out.println("oblast sorted list: " + sortedOblastList.size());
        for (String s : sortedOblastList) {
            System.out.println(s);
        }
    }

    public String getRights() {
        return "TrafficMaker v1.1 © Avtsin Denis, 2017";
    }

    public String border() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 27; i++) {
            sb.append("-");
        }
        sb.append("\n");
        return sb.toString();
    }

    // simple protection of using program by setting time-limit
    public void checkSystemDate() {
        // Die after August 1, 2018
        Calendar expireDate = Calendar.getInstance();
        // January is 0 (y, m, d)
        expireDate.set(2018, 8, 1, 0, 0, 0);
        // remain time of using
        long range = expireDate.getTimeInMillis()
            - Calendar.getInstance().getTimeInMillis();
        range = range / (24 * 60 * 60 * 1000);
        String timeOfUsing = expireDate.getTime() + ", " + range;
        // Get current date and compare
        if (Calendar.getInstance().after(expireDate)) {
            // Die
            String warning = timeOfUsing
                + "\nPlease connect with Avtsin Denis.\n"
                + "e-mail: davtsin@gmail.com";
            try (Writer writer
                = new BufferedWriter(new FileWriter("WARNING.txt"))) {
                System.out.println(warning);
                writer.write(warning);
            } catch (IOException ex) {
                System.out.println("Ошибка записи в файл");
            }
            System.exit(0);
        } else {
            System.out.println(timeOfUsing);
        }
    }
}
