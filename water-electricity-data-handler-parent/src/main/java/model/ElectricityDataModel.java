package model;

/**
 * Created by Jay on 01.07.2016.
 */
public class ElectricityDataModel implements Comparable<ElectricityDataModel>{
    private String address=""; //адрес
    private int group; //группа
    private int region; //регион
    private int biggestFloor; // максимальный этаж
    private int smallestFloor; //минимальный этаж
    private double joint; // общая площадь помещений, входящих в состав общего имущества в многоквартирных домах
    private String hasAccountingDevice; //Наличие коллективного (общедомового) прибора учета
    private double expenseHouseFirstMonth; //Расход электрической энергии по показаниям коллективного (общедомового) прибора учета (первый месяц)
    private double expenseHouseSecondMonth; //Расход электрической энергии по показаниям коллективного (общедомового) прибора учета (второй месяц)
    private double expenseNotLivingFirstMonth; //Суммарный расход электрической энергии в нежилых помещениях (первый месяц)
    private double expenseNotLivingSecondMonth; //Суммарный расход электрической энергии в нежилых помещениях (второй месяц)
    private double expenseIndividFirstMonth; //расход электрической энергии по показаниям индивидуального прибора учета за июнь и ноябрь в l-м жилом помещении W1 (первый месяц)
    private double expenseIndividSecondMonth; //расход электрической энергии по показаниям индивидуального прибора учета за июнь и ноябрь в l-м жилом помещении W1 (второй месяц)

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public int getBiggestFloor() {
        return biggestFloor;
    }

    public void setBiggestFloor(int floor) {
        this.biggestFloor = floor;
    }

    public int getSmallestFloor() {
        return smallestFloor;
    }

    public void setSmallestFloor(int floor) {
        this.smallestFloor = floor;
    }

    public int getRegion() {
        return region;
    }

    public void setRegion(int region) {
        this.region = region;
    }

    public double getJoint() {
        return joint;
    }

    public void setJoint(double joint) {
        this.joint = joint;
    }

    public String getAccountingDevice() {
        return hasAccountingDevice;
    }

    public void setAccountingDevice(String AccountingDevice) {
        this.hasAccountingDevice = AccountingDevice;
    }

    public double getExpenseHouseFirstMonth() {
        return expenseHouseFirstMonth;
    }

    public void setExpenseHouseFirstMonth(double expenseHouseFirstMonth) {
        this.expenseHouseFirstMonth = expenseHouseFirstMonth;
    }

    public double getExpenseHouseSecondMonth() {
        return expenseHouseSecondMonth;
    }

    public void setExpenseHouseSecondMonth(double expenseHouseSecondMonth) {
        this.expenseHouseSecondMonth = expenseHouseSecondMonth;
    }

    public double getExpenseNotLivingFirstMonth() {
        return expenseNotLivingFirstMonth;
    }

    public void setExpenseNotLivingFirstMonth(double expenseNotLivingFirstMonth) {
        this.expenseNotLivingFirstMonth = expenseNotLivingFirstMonth;
    }

    public double getExpenseNotLivingSecondMonth() {
        return expenseNotLivingSecondMonth;
    }

    public void setExpenseNotLivingSecondMonth(double expenseNotLivingSecondMonth) {
        this.expenseNotLivingSecondMonth = expenseNotLivingSecondMonth;
    }

    public double getExpenseIndividFirstMonth() {
        return expenseIndividFirstMonth;
    }

    public void setExpenseIndividFirstMonth(double expenseIndividFirstMonth) {
        this.expenseIndividFirstMonth = expenseIndividFirstMonth;
    }

    public double getExpenseIndividSecondMonth() {
        return expenseIndividSecondMonth;
    }

    public void setExpenseIndividSecondMonth(double expenseIndividSecondMonth) {
        this.expenseIndividSecondMonth = expenseIndividSecondMonth;
    }

    @Override
    public int compareTo(ElectricityDataModel o) {
        return (this.getGroup() - o.getGroup());
    }
}
