/*
 * Copyright (C) 2015 Nu Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.nubits.nubot.strategy.Primary;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.bot.NuBotBase;
import com.nubits.nubot.tasks.SubmitLiquidityinfoTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * simple NuBot
 */
public class NuBotSimple extends NuBotBase {

    final static Logger LOG = LoggerFactory.getLogger(NuBotSimple.class);

    public NuBotSimple() {

    }

    @Override
    public void configureStrategy() {
        // set liquidityinfo task to the strategy
        ((StrategyPrimaryPegTask) (Global.taskManager.getStrategyFiatTask().getTask()))
                .setSendLiquidityTask(((SubmitLiquidityinfoTask) (Global.taskManager.getSendLiquidityTask().getTask())));

        int delay = 7;
        Global.taskManager.getStrategyFiatTask().start(delay);
    }


}
