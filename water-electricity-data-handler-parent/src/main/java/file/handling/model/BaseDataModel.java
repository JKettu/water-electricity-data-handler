package file.handling.model;

import lombok.Data;

@Data
public abstract class BaseDataModel implements Comparable<BaseDataModel> {
    private int group; //группа
    private String address; //адрес
    private int region; //регион
    private int biggestFloor; // максимальный этаж
    private int smallestFloor; //минимальный этаж
    private double joint; // общая площадь помещений, входящих в состав общего имущества в многоквартирных домах

    @Override
    public int compareTo(BaseDataModel o) {
        return (this.group - o.group);
    }
}
