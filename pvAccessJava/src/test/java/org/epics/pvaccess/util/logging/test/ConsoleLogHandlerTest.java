/*
 * Copyright (c) 2004 by Cosylab
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the file "LICENSE-CAJ". If the license is not included visit Cosylab web site,
 * <http://www.cosylab.com>.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */

package org.epics.pvaccess.util.logging.test;

import junit.framework.TestCase;
import org.epics.pvaccess.util.logging.ConsoleLogHandler;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ConsoleLogHandlerTest extends TestCase {

    public ConsoleLogHandlerTest(String methodName) {
        super(methodName);
    }

    /*
     * Test handler.
     */
    public void testHandler() {
        Handler handler = new ConsoleLogHandler();
        handler.setLevel(Level.CONFIG);

        LogRecord msg = new LogRecord(Level.INFO, "This is a simple message.");
        handler.publish(msg);

        assertTrue(handler.isLoggable(msg));

        handler.setLevel(Level.OFF);
        assertFalse(handler.isLoggable(msg));
        // and is not published
        handler.publish(msg);

        handler.flush();
        handler.close();
    }

}
