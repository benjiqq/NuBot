package com.nubits.nubot.options;

import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.CurrencyPair;


public class PegOptions {

    /**
     * Return TRUE when it requires a dedicated NBT peg to something that is not USD
     */
    public static boolean requiresSecondaryPegStrategy(CurrencyPair pair) {
        if (pair.equals(CurrencyList.NBT_USD)) {
            return false;
        } else {
            return true;
        }
    }
}
