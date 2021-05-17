/*
 * Copyright (c) 2006 by Cosylab
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

package org.epics.pvaccess.server.impl.remote;

import org.epics.pvaccess.PVAException;
import org.epics.pvaccess.PVAVersion;
import org.epics.pvaccess.Version;
import org.epics.pvaccess.impl.remote.utils.PVAForwarder;
import org.epics.pvaccess.util.InetAddressUtil;

import java.io.IOException;
import java.io.PrintStream;
import java.net.UnknownHostException;

/**
 * Implementation of <code>ServerContext</code>.
 *
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class ServerForwarderImpl {
    static {
        // force only IPv4 sockets, since EPICS does not work right with IPv6 sockets
        // see http://java.sun.com/j2se/1.5.0/docs/guide/net/properties.html
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    /**
     * Version.
     */
    public static final Version VERSION = new Version(
            "pvAccess Message Forwarder",
            PVAVersion.VERSION_MAJOR, PVAVersion.VERSION_MINOR,
            PVAVersion.VERSION_MAINTENANCE, PVAVersion.VERSION_DEVELOPMENT);

    private Thread runner;


    /**
     * Server state enum.
     */
    enum State {
        NOT_RUNNING,
        RUNNING,
        DESTROYED
    }

    /**
     * Initialization status.
     */
    private volatile State state = State.NOT_RUNNING;

    /**
     * Run lock.
     */
    protected final Object runLock = new Object();

    /**
     * GUID.
     */
    private final byte[] guid = new byte[12];

    /**
     * Constructor.
     */
    public ServerForwarderImpl() {
    }

    /**
     * Returns GUID (12-byte array)
     *
     * @return GUID.
     */
    public byte[] getGUID() {
        return guid;
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.server.ServerContext#getVersion()
     */
    public Version getVersion() {
        return VERSION;
    }

    private boolean runTerminated;

    private void start() throws IOException {
        PVAForwarder.run(this);
    }

    /**
     * Run Forwarder (process events).
     *
     * @throws IllegalStateException if server is already destroyed.
     * @throws PVAException          exception.
     */
    public void run() throws PVAException, IllegalStateException, IOException {
        if (state == State.DESTROYED) {
            throw new IllegalStateException("Context destroyed.");
        } else if (state == State.RUNNING) {
            throw new IllegalStateException("Context is already running.");
        }

        synchronized (this) {
            state = State.RUNNING;
        }

        // run...
        synchronized (runLock) {
            runTerminated = false;

            // Run the forwarder
            runner = new Thread(
                    new Runnable() {
                        public void run() {
                            try {
                                start();
                            } catch (Throwable th) {
                                System.out.println("Forwarder Failure:");
                                th.printStackTrace();
                            }
                        }
                    }, "pvAccess message forwarder server");
            runner.start();

            try {
                while (!runTerminated) {
                    runLock.wait();
                }
            } catch (InterruptedException e) { /* noop */ }
        }

        synchronized (this) {
            state = State.DESTROYED;
        }

    }


    public synchronized void destroy() throws PVAException, IllegalStateException {

        if (state == State.DESTROYED)
            throw new IllegalStateException("Context already destroyed.");

        // notify to stop running...
        synchronized (runLock) {
            runTerminated = true;
            runLock.notifyAll();
        }
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.server.ServerContext#printInfo()
     */
    public void printInfo() {
        printInfo(System.out);
    }

    /* (non-Javadoc)
     * @see org.epics.pvaccess.server.ServerContext#printInfo(java.io.PrintStream)
     */
    public void printInfo(PrintStream out) {
        out.println("CLASS   : " + getClass().getName());
        out.println("VERSION : " + getVersion());
        try {
            out.println("MULTICAST_GROUP : " + InetAddressUtil.getMulticastGroup().getHostAddress());
        } catch (UnknownHostException ignored) {
        }
    }

    public boolean isRunning() {
        return state == State.RUNNING && !runTerminated;
    }

}
