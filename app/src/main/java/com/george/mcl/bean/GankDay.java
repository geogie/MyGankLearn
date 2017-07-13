package com.george.mcl.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by georgeRen on 2017/7/10.
 */

public class GankDay extends BaseEntity implements Serializable {

    public List<String> category;
    public GankDayResults results;

    @Override
    public String toString() {
        return "GankDay{" +
                "category=" + category +
                ", results=" + results +
                '}';
    }
}
