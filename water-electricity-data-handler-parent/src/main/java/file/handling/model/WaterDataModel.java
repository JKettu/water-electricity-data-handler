package file.handling.model;

import lombok.Data;

@Data
public class WaterDataModel extends BaseDataModel {
    private int people; //численность проживающих жителей в i-м многоквартирном доме или жилом доме
    private String hasColdWaterAccountingDevice; //Наличие коллективного (общедомового) прибора учета холодной воды
    private String hasHotWaterAccountingDevice; //Наличие коллективного (общедомового) прибора учета горячей воды
    private double expenseHouseCold;
            //Расход холодной воды по показаниям коллективного (общедомового) прибора учета за отопительный период
    private double expenseHouseHot;
            //Расход горячей воды по показаниям коллективного (общедомового) прибора учета за отопительный период
}
