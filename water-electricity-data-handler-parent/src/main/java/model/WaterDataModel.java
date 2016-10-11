package model;

import lombok.Data;

@Data
public class WaterDataModel implements Comparable<WaterDataModel>{
    private String address=""; //адрес
    private int group; //группа
    private int region; //регион
    private int biggestFloor; // максимальный этаж
    private int smallestFloor; //минимальный этаж
    private double joint; // общая площадь помещений, входящих в состав общего имущества в многоквартирных домах
    private int people; //численность проживающих жителей в i-м многоквартирном доме или жилом доме
    private String hasColdWaterAccountingDevice; //Наличие коллективного (общедомового) прибора учета холодной воды
    private String hasHotWaterAccountingDevice; //Наличие коллективного (общедомового) прибора учета горячей воды
    private double expenseHouseCold; //Расход холодной воды по показаниям коллективного (общедомового) прибора учета за отопительный период
    private double expenseHouseHot; //Расход горячей воды по показаниям коллективного (общедомового) прибора учета за отопительный период

    @Override
    public int compareTo(WaterDataModel o) {
        return (this.group - o.group);
    }
}
