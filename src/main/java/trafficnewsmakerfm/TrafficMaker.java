/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trafficnewsmakerfm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 *
 * @author denis
 * @version 1.1 from 1.1.2
 */
public class TrafficMaker {

    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        try {
            TrafficParsing tp = new TrafficParsing("Пробки.html");
            // simple checking for expire date of using program
            tp.checkSystemDate();

            sb.append(tp.getTotal()).append("\n\n")
                .append("Обзор начнём с колец от центра.").append("\n\n")
                .append(tp.getRingsBySectors()).append("\n\n")
                .append("Обзор радиальных магистралей начнем по часовой стрелке.")
                .append("\n\n");

            sb.append("В центр ");
            for (Map.Entry<String, String> entry
                : tp.changePadej(tp.getCenterHighways()).entrySet()) {
                sb.append(entry.getValue()).append(" на ")
                    .append(entry.getKey()).append(". ");
            }

            sb.append("\n\n");

            sb.append("Из центра ");
            for (Map.Entry<String, String> entry
                : tp.changePadej(tp.getOblastHighways()).entrySet()) {
                sb.append(entry.getValue()).append(" на ")
                    .append(entry.getKey()).append(". ");
            }

            sb.append("\n\n");

            sb.append("В Москве ХХ часов ХХ минут. Следующий выпуск "
                + "автонавигатора через ХХ.");

            sb.append("\n\n\n\n")
                .append(tp.border()).append(tp.getTimeInfo()).append("\n\n")
                .append(tp.getRights());

            // to console
            if (args.length > 0 && args[0].equals("-t")) {
                tp.showHigways();
                System.out.println();
                System.out.println(sb.toString());
            }

        } catch (IOException e) {
            System.out.println("Файл Пробки.html не найден");
        }

        try (Writer writer
                = new BufferedWriter(new FileWriter("TrafficNews.txt"));) {
            writer.write(sb.toString());
        } catch (IOException ex) {
            System.out.println("Ошибка записи отчёта в файл");
        }
    }
}
