// Low-level communication functions to send and receive single bytes.
// Based on code in Legos 0.2.6 by Markus L. Noga.
// These functions do the minimum necessary to send and receive single 
// bytes, leaving it to Java code to do timing and error handling, and to
// implement communication protocols. This means that the impact on the 
// footprint of the lejos firmware is minimized.
//
// Author Lawrie Griffiths (lawrie.griffiths@ntlworld.com)

//
// Serial port definitions
//

// Serial receive data register 
extern unsigned char S_RDR;

// Serial transmit data register
extern unsigned char S_TDR;

// Serial mode register
extern unsigned char S_MR;

#define SMR_SYNC	0x80	   // in sync mode, the other settings
#define SMR_ASYNC	0x00	   // have no effect.
#define SMR_7BIT	0x40
#define SMR_8BIT	0x00
#define SMR_P_NONE	0x00
#define SMR_P_EVEN	0x20
#define SMR_P_ODD	0x30
#define SMR_1STOP	0x00
#define SMR_2STOP	0x08

// Serial control register
extern unsigned char S_CR;

#define SCR_TX_IRQ	0x80	   // Transmit irq enable
#define SCR_RX_IRQ	0x40	   // Receive / recv err irq enable
#define SCR_TRANSMIT	0x20	   // Enable transmission
#define SCR_RECEIVE	0x10	   // Enable receiving
#define SCR_TE_IRQ	0x04	   // Transmit end irq enable

// Serial status register
extern unsigned char S_SR;

#define SSR_TRANS_EMPTY	0x80	   // Transmit buffer empty
#define SSR_RECV_FULL	0x40	   // Receive buffer full
#define SSR_OVERRUN_ERR	0x20	   // Overrun error
#define SSR_FRAMING_ERR	0x10	   // Framing error
#define SSR_PARITY_ERR	0x08	   // Parity error
#define SSR_ERRORS      0x38       // All errors
#define SSR_TRANS_END	0x04	   // Transmission end because buffer empty

// Serial baud rate register
extern unsigned char S_BRR;

//
// values for the bit rate register BRR
// assuming CMR_CLOCK selected on 16 MHz processor
// error <= 0.16%
//

#define B2400		207
#define B4800		103
#define B9600		51
#define B19200		25
#define B38400		12

// Definitions for Ports 4 and 5

// Port 4 data direction register
extern unsigned char PORT4_DDR;

// Port 4 I/O register
extern unsigned char PORT4;

// Port 5 data direction register
extern unsigned char PORT5_DDR;

extern unsigned char rom_port4_ddr;	//!< ROM shadow of port 4 DDR
extern unsigned char rom_port5_ddr;	//!< ROM shadow of port 5 DDR

// Definitions for Timer 1

// Timer 1 control register
extern unsigned char T1_CR;

// Timer 1 control / status register
extern unsigned char T1_CSR;

// Timer 1 constant A register
extern          unsigned char T1_CORA;

// IRQ Vectors

extern void *eri_vector;        // ERI interrupt vector
extern void *rxi_vector;        // RXI interrupt vector
extern void *txi_vector;        // TXI interrupt vector
extern void *tei_vector;        // TEI interrupt vector

///////////////////////////////////////////////////////////////////////////////
//
// Variables
//
///////////////////////////////////////////////////////////////////////////////

#define MAX_BUFFER 64

static short sending;		// transmission state

#define NOT_SENDING 0
#define SENDING 1
#define SENT_BUT_NOT_VALIDATED 2

extern unsigned char serial_output_buffer;

static unsigned char send_byte;
static unsigned char start, next;
static unsigned char *buffer = &serial_output_buffer;


///////////////////////////////////////////////////////////////////////////////
//
// Functions
//
///////////////////////////////////////////////////////////////////////////////

// Macro to wrap interrupt handler

#define HANDLER_WRAPPER(wrapstring,handstring) \
__asm__ (".text\n.align 1\n.global _" wrapstring "\n_" wrapstring \
": push r0\npush r1\npush r2\npush r3\njsr @_" handstring \
"\npop r3\npop r2\npop r1\npop r0\nrts\n")

void llc_rx_handler(void);
void llc_rxerror_handler(void);
void llc_tx_handler(void);
void llc_rxerror_core(void);
void llc_show(short);

void llc_init(void) {
  sending = NOT_SENDING;         // Not transmitting
  start = 0;                     // Intitialise cyclic receive buffer pointers
  next = 0;
  eri_vector = &llc_rxerror_handler; // Set IRQ handlers
  rxi_vector = &llc_rx_handler;
  txi_vector = &llc_tx_handler;
}

// Send a single byte to the IR port.
// Just initiates the transfer and returns

void llc_write(unsigned char b) {
  send_byte = b;
  sending = SENDING;
  S_SR &= ~(SSR_TRANS_EMPTY | SSR_TRANS_END);	  // clear flags
  S_CR |= SCR_TRANSMIT | SCR_TX_IRQ | SCR_TE_IRQ; // enable transmit & irqs
}

// Read a byte from the input cyclic buffer

int llc_read(void) {
  if (next != start) {
    unsigned char b = buffer[start];
    if (++start == MAX_BUFFER) start = 0;
    return b;
  } else {
    return -1; // No data
  } 
}

// The byte received interrupt handler
// Adds the byte to the input cyclic buffer.
// Note that data is silently discarded if 
// the buffer becomes full.

HANDLER_WRAPPER("llc_rx_handler","llc_rx_core");
void llc_rx_core(void) {
  if (!sending) {
    // received a byte from PC
    buffer[next] = S_RDR;
    if (++next == MAX_BUFFER) next = 0;
  } else {
    // echos of own bytes -> collision detection
    if (S_RDR != send_byte) {
      S_CR &= ~(SCR_TX_IRQ | SCR_TRANSMIT | SCR_TE_IRQ); // Disable transmit
      S_SR &= ~(SSR_ERRORS | SSR_TRANS_EMPTY | SSR_TRANS_END); // Clear error etc.
    }
    sending = NOT_SENDING;
  }
  S_SR &= ~SSR_RECV_FULL;
}

// The receive error interrupt handler
// Does not currently record the parity check error -
// the erroneous byte is just discarded
// Clear transmit as this is also used after a collision

HANDLER_WRAPPER("llc_rxerror_handler","llc_rxerror_core");
void llc_rxerror_core(void) {
  S_CR &= ~(SCR_TX_IRQ | SCR_TRANSMIT | SCR_TE_IRQ); // Disable transmit
  S_SR &= ~(SSR_ERRORS | SSR_TRANS_EMPTY | SSR_TRANS_END); // Clear error etc.
}

// The transmit byte interrupt handler
// This is called twice for a single byte transfer.
// Note the three values for the sending state indicator.
// It is called before the echoed byte is received 
// by the receive handler.
// Write the byte if not done, otherwise unhook irq.

HANDLER_WRAPPER("llc_tx_handler","llc_tx_core");
void llc_tx_core(void) {
  if(sending == SENDING) {
    S_TDR = send_byte ;      // transmit byte
    sending = SENT_BUT_NOT_VALIDATED; 
    S_SR &= ~SSR_TRANS_EMPTY;
  } else {
    S_CR &= ~SCR_TX_IRQ;     // disable transmission interrupt
  }
}
 

