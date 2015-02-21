package com.bingo.fibonacci.bingo;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by Nathan on 2/20/2015.
 */
public class BingoData implements Serializable {
    long time = 0;
    long numBingo = 0;
    Long[] bingoTimes = new Long[1000];
    public BingoData () {
    }
    public void increaseBingoCount( ) {
        ++numBingo;
    }
    public void increaseTimePlayed(long time) {
        this.time += time;
    }

    public long getTime() {
        return time;
    }
    public long getNumBingo() {
        return numBingo;
    }
    public void addBingo(Long timeInMS) {
        bingoTimes[(int)(numBingo-1)] = timeInMS;
    }
    public int bingoThisWeek( ) {
        int count = 0;
        Calendar cal = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 1);

        cal2.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        cal2.set(Calendar.HOUR, 23);
        cal2.set(Calendar.MINUTE, 59);
        cal2.set(Calendar.SECOND, 59);

        long time1 = cal.getTimeInMillis();
        long time2 = cal2.getTimeInMillis();


        for(int i=0; i < numBingo; i++ ) {
            if(bingoTimes[i] > time1 && bingoTimes[i] < time2) {
                ++count;
            }
        }
        return count;
    }
    public int bingoThisMonth( ) {
        int count = 0;
        Calendar cal = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 1);

        cal2.set(Calendar.DAY_OF_MONTH, cal.getMaximum(Calendar.DAY_OF_MONTH));
        cal2.set(Calendar.HOUR, 23);
        cal2.set(Calendar.MINUTE, 59);
        cal2.set(Calendar.SECOND, 59);

        long time1 = cal.getTimeInMillis();
        long time2 = cal2.getTimeInMillis();


        for(int i=0; i<numBingo;i++) {
            if(bingoTimes[i] > time1 && bingoTimes[i] < time2) {
                ++count;
            }
        }
        return count;

    }
    public int bingoThisSemester( ) {
        int count = 0;
        Calendar cal = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        cal.set(Calendar.DAY_OF_YEAR, 12);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 1);

        cal2.set(Calendar.DAY_OF_YEAR, 128);
        cal2.set(Calendar.HOUR, 23);
        cal2.set(Calendar.MINUTE, 59);
        cal2.set(Calendar.SECOND, 59);

        long time1 = cal.getTimeInMillis();
        long time2 = cal2.getTimeInMillis();


        for(int i=0; i<numBingo;i++) {
            if(bingoTimes[i] > time1 && bingoTimes[i] < time2) {
                ++count;
            }
        }
        return count;


    }

}
