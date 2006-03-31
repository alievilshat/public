/*****************************************************************************
 * This file is part of the Prolog Development Tool (PDT)
 * 
 * Author: Lukas Degener (among others) 
 * E-mail: degenerl@cs.uni-bonn.de
 * WWW: http://roots.iai.uni-bonn.de/research/pdt 
 * Copyright (C): 2004-2006, CS Dept. III, University of Bonn
 * 
 * All rights reserved. This program is  made available under the terms 
 * of the Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * In addition, you may at your option use, modify and redistribute any
 * part of this program under the terms of the GNU Lesser General Public
 * License (LGPL), version 2.1 or, at your option, any later version of the
 * same license, as long as
 * 
 * 1) The program part in question does not depend, either directly or
 *   indirectly, on parts of the Eclipse framework and
 *   
 * 2) the program part in question does not include files that contain or
 *   are derived from third-party work and are therefor covered by special
 *   license agreements.
 *   
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *   
 * ad 1: A program part is said to "depend, either directly or indirectly,
 *   on parts of the Eclipse framework", if it cannot be compiled or cannot
 *   be run without the help or presence of some part of the Eclipse
 *   framework. All java classes in packages containing the "pdt" package
 *   fragment in their name fall into this category.
 *   
 * ad 2: "Third-party code" means any code that was originaly written as
 *   part of a project other than the PDT. Files that contain or are based on
 *   such code contain a notice telling you so, and telling you the
 *   particular conditions under which they may be used, modified and/or
 *   distributed.
 ****************************************************************************/

/*
 */
package org.cs3.pl.prolog.internal.socket.observable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Vector;

import org.cs3.pl.common.Debug;
import org.cs3.pl.prolog.ConsultServiceEvent;
import org.cs3.pl.prolog.ConsultServiceListener;



class ConsultOutputStream extends OutputStream {

    private String symbol;

    private OutputStream sink, recordStream;

    private Vector listeners = new Vector();

    private SocketClient consultClient;

    private boolean closed = false;

    /**
     * @param socket
     *                    TODO
     * @param recordStream
     *                    TODO
     * @throws IOException
     */
    public ConsultOutputStream(SocketClient client, String symbol)
            throws IOException {
        try {
            this.consultClient = client;
            client.lock();
            this.symbol = symbol;
            sink = client.getOutputStream();
            client.readUntil(SocketClient.GIVE_COMMAND);
            client.writeln(SocketClient.CONSULT);
            client.readUntil(SocketClient.GIVE_SYMBOL);
            client.writeln(symbol);
            client.readUntil(SocketClient.GO_AHEAD);
        } catch (Throwable t) {
            Debug.report(t);
            close();
            if (t.getClass().equals(IOException.class))
                throw (IOException) t;
            throw new RuntimeException( t);
        }
    }

    public void close() throws IOException {
        if (closed) {
            return;
        }
        try {
            // sink.close();
            consultClient.writeln("\n" + SocketClient.EOF+".");
            if (recordStream != null) {
                recordStream.close();
            }
            consultClient.readUntil(SocketClient.OK);
            fireConsultDataChanged(new ConsultServiceEvent(this, symbol));
        } finally {
            consultClient.unlock();
            closed = true;
        }
    }

    public void flush() throws IOException {
        try {
            if (closed) {
                throw new IOException("Stream is closed.");
            }
            sink.flush();
            if (recordStream != null) {
                recordStream.flush();
            }
        } catch (Throwable t) {
            close();
            if (t.getClass().equals(IOException.class))
                throw (IOException) t;
            throw new RuntimeException( t);
        }
    }

    public OutputStream getRecordStream() {
        return recordStream;
    }

    public void setRecordStream(OutputStream recordStream) {
        this.recordStream = recordStream;
    }

    public void write(byte[] b) throws IOException {
        try {
            if (closed) {
                throw new IOException("Stream is closed.");
            }
            //Debug.debug("writing: '" + new String(b) + "'");
            sink.write(b);
            if (recordStream != null) {
                recordStream.write(b);
            }
        } catch (Throwable t) {
            close();
            if (t.getClass().equals(IOException.class))
                throw (IOException) t;
            throw new RuntimeException( t);
        }
    }

    public void write(byte[] b, int off, int len) throws IOException {
        try {
            if (closed) {
                throw new IOException("Stream is closed.");
            }
            //Debug.debug("writing: '" + new String(b, off, len) + "'");
            sink.write(b, off, len);
            if (recordStream != null) {
                recordStream.write(b, off, len);
            }

        } catch (Throwable t) {
            close();
            if (t.getClass().equals(IOException.class))
                throw (IOException) t;
            Debug.report(t);
            throw new RuntimeException( t);
        }
    }

    public void write(int b) throws IOException {
        try {
            if (closed) {
                throw new IOException("Stream is closed.");
            }
            //Debug.debug("writing: '" + (char) b + "'");
            sink.write(b);
            if (recordStream != null) {
                recordStream.write(b);
            }
        } catch (Throwable t) {
            close();
            if (t.getClass().equals(IOException.class))
                throw (IOException) t;
            throw new RuntimeException( t);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cs3.pl.metadata.ConsultService#addConsultServiceListener(org.cs3.pl.metadata.ConsultServiceListener)
     */
    public void addConsultServiceListener(ConsultServiceListener l) {
        synchronized (listeners) {
            if (!listeners.contains(l)) {
                listeners.add(l);
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.cs3.pl.metadata.ConsultService#removeConsultServiceListener(org.cs3.pl.metadata.ConsultServiceListener)
     */
    public void removeConsultServiceListener(ConsultServiceListener l) {
        synchronized (listeners) {
            if (listeners.contains(l)) {
                listeners.remove(l);
            }
        }
    }

    protected void fireConsultDataChanged(ConsultServiceEvent e) {
        Vector cloned = null;
        synchronized (listeners) {
            cloned = (Vector) listeners.clone();
        }
        for (Iterator it = cloned.iterator(); it.hasNext();) {
            ConsultServiceListener l = (ConsultServiceListener) it.next();
            l.consultDataChanged(e);
        }
    }

}