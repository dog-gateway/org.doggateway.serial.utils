// CHECKSTYLE:OFF
/**    
 *  @formatter:on
 *  <p>Serial Utilities , copyright(c) 2019, Dario Bonino
 *  <br/>
 *  Author: Dario Bonino - dario.bonino@gmail.com
 *  </p>
 */
// CHECKSTYLE:ON

package org.doggateway.serial;

import org.osgi.service.log.Logger;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

/**
 * A utility factory for getting a reference to the serial port used for
 * communicating with the physical EnOcean transceiver, initialized with the
 * correct parameters.
 * 
 * @author <a href="mailto:dario.bonino@gmail.com">Dario Bonino</a>
 * @authr <a href="mailto:biasiandrea04@gmail.com">Andrea Biasi </a>
 * 
 */
public class SerialPortFactory
{

    /**
     * Provides a correctly configured serial port connection, given a port
     * identifier and a transmission timeout.
     * 
     * @param portName
     *            The name of the port to connect to (e.g., COM1, /dev/tty0,
     *            ...)
     * @param timeout
     *            The connection timeout
     * @return a {@link SerialPort} instance representing the port identified by
     *         the given data, if existing, or null otherwise.
     * @throws Exception
     */
    public static SerialPort getPort(String portName, int timeout, int baudRate,
            int dataBits, int stopbits, int parity, Logger logger)
            throws UnsupportedCommOperationException, NoSuchPortException,
            PortInUseException

    {
        // the serial port reference, initially null
        SerialPort serialPort = null;

        try
        {

            // sets the port name (TODO: check if needed)
            // System.setProperty("gnu.io.rxtx.SerialPorts", "");

            // build a port identifier given the port id as a string
            CommPortIdentifier portIdentifier = CommPortIdentifier
                    .getPortIdentifier(portName);

            // check that the port exists and is free
            if (portIdentifier.isCurrentlyOwned())
            {
                if (logger != null)
                {
                    logger.error("Error: Port is currently in use");
                }
            }
            else
            {
                // open the serial port
                CommPort commPort = portIdentifier
                        .open(SerialPortFactory.class.getName(), timeout);

                // check that the just opened communication port is actually a
                // serial port.
                if (commPort instanceof SerialPort)
                {
                    // store the serial port reference
                    serialPort = (SerialPort) commPort;

                    // set the serial port parameters according to the ESP3
                    // specification:
                    // speed = 57600 bps
                    // data = 8 byte
                    // stop bit = 1
                    // parity = none
                    serialPort.setSerialPortParams(baudRate, dataBits, stopbits,
                            parity);
                }
                else
                {
                    if (logger != null)
                    {
                        logger.error("Error while opening and setting up "
                                + "the serial port.");
                    }
                }
            }
        }
        catch (UnsupportedCommOperationException | NoSuchPortException
                | PortInUseException e)
        {
            if (logger != null)
            {
                logger.error("Exception while opening the serial port "
                        + "for communication:\n {}", e);
            }
            // rethrow
            throw e;
        }

        return serialPort;
    }
}
