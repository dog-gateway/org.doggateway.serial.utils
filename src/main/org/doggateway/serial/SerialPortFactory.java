// CHECKSTYLE:OFF
/**    
 * Serial Utilities , copyright(c) 2019, Dario Bonino
 *    
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
// CHECKSTYLE:ON

package org.doggateway.serial;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import org.osgi.service.log.Logger;

import java.io.File;

/**
 * A utility factory for getting a reference to the serial port, initialized
 * with the correct parameters.
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
            // build a port identifier given the port id as a string
            CommPortIdentifier portIdentifier = CommPortIdentifier
                    .getPortIdentifier(portName);

            File portFile = new File(portName);

            // check that the port exists and is free
            if (portIdentifier.isCurrentlyOwned())
            {
                if (logger != null)
                {
                    logger.error("Error: Port is currently in use");
                }

                throw new PortInUseException();
            }
            // check that the file pointed by the port name exist, only for
            // Linux/Unix systems
            else if (!System.getProperty("os.name").toLowerCase()
                    .contains("windows") && !portFile.exists())
            {
                if (logger != null)
                {
                    logger.error("The serial port: {} does not exist",
                            portFile.getAbsolutePath());
                }

                throw new NoSuchPortException();
            }
            // then try to open the port
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

                    // set the serial port parameters
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
                        + "for communication:\n "+e);
            }
            // rethrow
            throw e;
        }

        return serialPort;
    }
}
