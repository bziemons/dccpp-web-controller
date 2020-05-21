package de.ch94.dccpp

import jssc.SerialPort
import jssc.SerialPortEvent
import jssc.SerialPortEventListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BaseStation : BareBaseStation() {
    private val serialPort = SerialPort("/dev/ttyACM0")

    init {
        if (!serialPort.openPort()) {
            error("Could not open serial port")
        }
        if (!serialPort.setParams(
                SerialPort.BAUDRATE_115200,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE
            )
        ) {
            error("Could not set parameters on serial port")
        }

        serialPort.addEventListener {
            println("${it.portName} event: ${it.eventType} -> ${it.eventValue}")
            if (it.eventType == SerialPortEvent.RXCHAR) {
                println(serialPort.readString())
            }
        }
    }

    override fun serialWrite(asciiStr: String) = serialPort.writeBytes(getBytes(asciiStr))

    override fun close() {
        serialPort.closePort()
    }
}

open class BareBaseStation : AutoCloseable {
    protected open val maxBytes: Int = 32
    protected val logger: Logger = LoggerFactory.getLogger("BaseStation")

    protected fun getBytes(str: String) = str.toByteArray(Charsets.US_ASCII)
        .takeIf { it.size <= maxBytes } ?: error("Given string is too long")

    protected open fun serialWrite(asciiStr: String): Boolean =
        true.also { logger.info("Writing '${getBytes(asciiStr).toString(Charsets.US_ASCII)}'") }

    private var powered: Boolean = false

    fun switchPower() {
        powered = if (powered) {
            false.takeIf { serialWrite("<0>") }
        } else {
            true.takeIf { serialWrite("<1>") }
        } ?: powered.also { logger.warn("Could not switch power. Currently powered: $it") }
    }

    override fun close() {
        logger.info("BaseStation closed")
    }
}
