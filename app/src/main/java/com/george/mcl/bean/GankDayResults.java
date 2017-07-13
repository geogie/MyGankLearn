package com.george.mcl.bean;

import java.util.List;

/**
 * Created by georgeRen on 2017/7/10.
 */

public class GankDayResults {
    public List<Result> 福利;
    public List<Result> Android;
    public List<Result> iOS;
    public List<Result> App;
    public List<Result> 瞎推荐;
    public List<Result> 休息视频;

    @Override
    public String toString() {
        return "GankDayResults{" +
                "福利=" + 福利 +
                ", Android=" + Android +
                ", iOS=" + iOS +
                ", App=" + App +
                ", 瞎推荐=" + 瞎推荐 +
                ", 休息视频=" + 休息视频 +
                '}';
    }
}
