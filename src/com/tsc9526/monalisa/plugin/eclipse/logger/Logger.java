/*******************************************************************************************
 *	Copyright (c) 2016, zzg.zhou(11039850@qq.com)
 * 
 *  Monalisa Eclipse Plugin is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Lesser General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.

 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Lesser General Public License for more details.

 *	You should have received a copy of the GNU Lesser General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************************/
package com.tsc9526.monalisa.plugin.eclipse.logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import com.tsc9526.monalisa.plugin.eclipse.activator.MonalisaPlugin;
import com.tsc9526.monalisa.plugin.eclipse.tools.MonalisaConstants;
 
/**
 * 
 * @author zzg.zhou(11039850@qq.com)
 */
public final class Logger {
    private static final String INFO  = MonalisaConstants.PLUGIN_ID + "/info";
    private static final String DEBUG = MonalisaConstants.PLUGIN_ID + "/debug";
    private static final String TRACE = MonalisaConstants.PLUGIN_ID + "/trace";

    private static final ThreadLocal<DateFormat> dateFormatter = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("HH:mm:ss.SSS");
        }
    };

     private Logger() {
    }

    /**
     * Logs a message with an 'error' severity.
     * 
     * @param message
     *            the message to log
     * @param t
     *            the throwable cause
     */
    public static void error(final String message, final Throwable t) {
        if (MonalisaPlugin.getDefault()!= null) {
        	MonalisaPlugin.getDefault().getLog().log(new Status(Status.ERROR, MonalisaConstants.PLUGIN_ID, message, t));
        } else {
            // at least write in the .log file
            t.printStackTrace();
        }
    }

    /**
     * Logs a message with an 'error' severity.
     * 
     * @param message
     *            the message to log
     */
    public static void error(final String message) {
    	MonalisaPlugin.getDefault().getLog()
                .log(new Status(Status.ERROR, MonalisaConstants.PLUGIN_ID, message));
    }

    /**
     * Logs a message with an 'warning' severity.
     * 
     * @param message
     *            the message to log
     * @param t
     *            the throwable cause
     */
    public static void warn(final String message, final Throwable t) {
    	MonalisaPlugin.getDefault().getLog()
                .log(new Status(Status.WARNING, MonalisaConstants.PLUGIN_ID, message, t));
    }

    /**
     * Logs a message with a 'warning' severity.
     * 
     * @param message
     *            the message to log
     */
    public static void warn(final String message) {
    	MonalisaPlugin.getDefault().getLog()
                .log(new Status(Status.WARNING, MonalisaConstants.PLUGIN_ID, message));
    }

    /**
     * Logs a message with an 'info' severity, if the 'INFO' tracing option is enabled, to avoid unwanted extra
     * messages in the error log.
     * 
     * @param message
     *            the message to log
     */
    public static void info(String message) {
        if (isOptionEnabled(INFO)) {
        	MonalisaPlugin.getDefault().getLog()
                    .log(new Status(Status.INFO, MonalisaConstants.PLUGIN_ID, message));
        }
    }

    /**
     * Outputs a debug message in the trace file (not the error view of the runtime workbench). Traces must be activated
     * for this plugin in order to see the output messages.
     * 
     * @param message
     *            the message to trace.
     */
    public static void debug(final String message) {
        debug(message, (Object[]) null);

    }

    /**
     * Outputs a 'debug' level message in the .log file (not the error view of the runtime workbench). Traces must be
     * activated for this plugin in order to see the output messages.
     * 
     * @param message
     *            the message to trace.
     */
    public static void debug(final String message, Object... items) {
        log(DEBUG, message, items);
    }

    /**
     * Outputs a 'trace' level message in the .log file (not the error view of the runtime workbench). Traces must be
     * activated for this plugin in order to see the output messages.
     * 
     * @param message
     *            the message to trace.
     */
    public static void trace(final String message, final Object... items) {
        log(TRACE, message, items);
    }

    private static void log(final String level, final String message, final Object... items) {
        try {
            if (isOptionEnabled(level)) {
                String valuedMessage = message;
                if (items != null) {
                    for (Object item : items) {
                        valuedMessage = valuedMessage.replaceFirst("\\{\\}", (item != null ? item.toString()
                                .replaceAll("\\$", ".") : "null"));
                    }
                }
                System.out.println(dateFormatter.get().format(new Date()) + " [" + Thread.currentThread().getName()
                        + "] " + valuedMessage);
            }
        } catch (RuntimeException e) {
            System.err.println("Failed to write proper debug message with template:\n " + message + "\n and items:");
            for (Object item : items) {
                System.err.println(" " + item);
            }
        }
    }

    private static boolean isOptionEnabled(String level) {
        final String debugOption = Platform.getDebugOption(level);
        return MonalisaPlugin.getDefault() != null && MonalisaPlugin.getDefault().isDebugging()
                && "true".equalsIgnoreCase(debugOption);
    }

    public static boolean isDebugEnabled() {
        return isOptionEnabled(DEBUG);
    }
}