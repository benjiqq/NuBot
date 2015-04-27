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

package com.nubits.nubot.trading;

import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.models.ApiError;

/**
 *
 */
public class ErrorManager {
    //ErrorManager Class wants to have an Exchange name as a property
    public Exchange exchangeName = null;

    //Set the errors
    public ApiError genericError = new ApiError(1, "Generic Error");
    public ApiError parseError = new ApiError(2, "Parsing Error");
    public ApiError noConnectionError = new ApiError(3, "No Connection");
    public ApiError nullReturnError = new ApiError(4, "Null Return");
    public ApiError apiReturnError = new ApiError(5, ""); //This shows an error returned by the Exchange API.
    // The description will be filled with the returned value
    public ApiError authenticationError = new ApiError(6, "Authentication Error");
    public ApiError orderNotFound = new ApiError(7, "Order not found");

    public void setExchangeName(Exchange exchange) {
        exchangeName = exchange;
    }

}
