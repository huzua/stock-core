package com.blm.stockcore.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class MathUtils {

    public static String divide(BigDecimal son,BigDecimal mom){
        BigDecimal divide = son.divide(mom, 4,RoundingMode.HALF_DOWN);
        float percent = divide.floatValue();
        return keepTwoScale(percent*100);
    }

    public static String keepTwoScale(float f){
        DecimalFormat df = new DecimalFormat("#.00");
        return df.format(f);
    }
}
