package josx.platform.rcx;

/**
 * Message API for infra-red (IR) communication
 * that is compatible with the SendMessage command of the LEGO firmware.
 * It is similar to SendMessage(), Message() and ClearMessage() of NQC,
 * but also keeps track whether a message has been received.
 */
public class Message implements Opcode
{
    private static boolean available = false;
    private static byte message = 0;
    private static byte[] packet =
	new byte[] {OPCODE_SET_MESSAGE, 0};

    private Message()
    {
    }

    /**
     * Send the byte using the SetMessage opcode
     *
     * @param  m  the byte to send
     */
    public static void send(byte m)
    {
	packet[1] = m;
	Serial.sendPacket (packet, 0, 2);
    }

    /**
     * Get the last received message
     *
     * @return   the last received message
     *           or 0 if none was received after startup or clear.
     */
    public static byte get()
    {
	return message;
    }

    /**
     * Clear the received message
     */
    public static void clear()
    {
	available = false;
	message = 0;
    }

    /**
     * Return true if message is available
     *
     * @return   true if a message was received after startup or clear.
     */
    public static boolean isAvailable()
    {
	if (Memory.readByte (0xefb2) == 6) {
	    /* Serial receiver is in long transfer state, may be blocked.
	     * We abort to unblock it.  That is safe because
	     * we are not interested in long transfers here.
	     */
	    Memory.writeByte (0xefb2, (byte)2);	// reset transfer state
	    Memory.writeByte (0xefb3, (byte)0);	// not expecting complement
	    Memory.writeByte (0xefba, (byte)0xff); // last received byte
	}
	return available;
    }

    /* initialize listener */
    static
    {
	Serial.addSerialListener (new SerialListener ()
	    {
		public void packetAvailable (byte[] packet, int length)
		{
		    if (packet[0] == OPCODE_SET_MESSAGE)
		    {
			available = true;
			message = packet[1];
		    }
		}
	    });
    }
}
