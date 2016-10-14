package file.handling.model;

import lombok.Data;

@Data
public class ElectricityDataModel extends BaseDataModel {
    private String hasAccountingDevice; //Наличие коллективного (общедомового) прибора учета
    private double expenseHouseFirstMonth;
    //Расход электрической энергии по показаниям коллективного (общедомового) прибора учета (первый месяц)
    private double expenseHouseSecondMonth;
    //Расход электрической энергии по показаниям коллективного (общедомового) прибора учета (второй месяц)
    private double expenseNotLivingFirstMonth;
    //Суммарный расход электрической энергии в нежилых помещениях (первый месяц)
    private double expenseNotLivingSecondMonth;
    //Суммарный расход электрической энергии в нежилых помещениях (второй месяц)
    private double expenseIndividFirstMonth;
    //расход электрической энергии по показаниям индивидуального прибора учета за июнь и ноябрь в l-м жилом помещении W1 (первый месяц)
    private double expenseIndividSecondMonth;
    //расход электрической энергии по показаниям индивидуального прибора учета за июнь и ноябрь в l-м жилом помещении W1 (второй месяц)

}
