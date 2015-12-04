package com.feaoes.gesturesvg.util;

/**
 * Created by ff on 2015/12/3.
 * 用于记录num的走势
 */
public class MemoryInt {

    public void setCurrentNum(int currentNum) {
        this.currentNum = currentNum;
    }

    private int currentNum;
    private int historyNum;

    /**
     * 用于记录num的走势
     */
    public MemoryInt(int currentNum){
        this.currentNum = historyNum = currentNum;
    }
    public void increase(){
        historyNum = currentNum;
        currentNum += 1;
    }
    public void decrease(){
        historyNum = currentNum;
        currentNum -= 1;
    }
    public boolean isIncreased(){
        return (currentNum -historyNum)>0;
    }
    public boolean isDecreased(){
        return (currentNum -historyNum)<0;
    }
}
