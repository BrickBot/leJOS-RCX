
#include <rom.h>

#include "types.h"
#include "constants.h"
#include "classes.h"
#include "threads.h"
#include "specialclasses.h"
#include "specialsignatures.h"
#include "language.h"
#include "memory.h"
#include "interpreter.h"
#include "exceptions.h"
#include "configure.h"
#include "trace.h"

extern char _bss_start;
extern char _end;
extern char _text_end;
extern char _data_end;
extern char _text_begin;
extern char _data_begin;

Thread *bootThread;

#if !EMULATE && VERIFY
#error Should not VERIFY in RCX
#endif

#define MEM_START      (&_end)
#define RAM_START_ADDR 0x8000
#define RAM_SIZE       0x6F00
#define MEM_END_ADDR   (RAM_START_ADDR + RAM_SIZE)
#define MEM_END        ((char *) MEM_END_ADDR)
#define BUFSIZE        6
#define TDATASIZE      100
#define MAXNEXTBYTEPTR (MEM_END - TDATASIZE)

#ifdef VERIFY

void assert (boolean aCond, int aCode)
{
  // TBD
}

#endif

char timerdata1[6];
async_t timerdata0;
char buffer[BUFSIZE];
char numread;   
char valid, state0, state1;
char *nextByte;
boolean continueFlag;
short status;
TWOBYTES seqNumber;
TWOBYTES i;

static inline void
set_data_pointer (void *ptr)
{
  play_sound_or_set_data_pointer(0x1771, (short)ptr, 0);
}

#if DEBUG_RCX

void wait_for_view_press (void)
{
  short status;
  do {
    status = 0;
    read_buttons (BUTTONS_READ, &status);
  } while (status != 0x04);
  do {
    status = 0;
    read_buttons (BUTTONS_READ, &status);
  } while ((status & 0x04) != 0);
}

void wait_for_power_press (void)
{
  short status;
  status = 0;
  do {
    get_power_status(POWER_KEY, &status);
    // status != 0 means not pressed
  } while (status);
  do {
    get_power_status(POWER_KEY, &status);
    // status == 0 means pressed
  } while (!status);
}

void debug (short s, short n1, short n2)
{
  if (s != -1)
    play_system_sound (SOUND_QUEUED, s);
  set_lcd_number (LCD_UNSIGNED, (short) n1, 3002);
  set_lcd_number (LCD_PROGRAM, (short) n2, 0);
  refresh_display();
  wait_for_power_press();
  play_system_sound (SOUND_QUEUED, 0);
}

#endif

int main (void)
{
  init_timer (&timerdata0, &timerdata1[0]);
  init_power();
  init_serial (&state0, &state1, 1, 1);
 LABEL_DOWNLOAD:
  set_data_pointer (MEM_START);
  set_lcd_number (LCD_UNSIGNED, (short) 0, 3002);
  set_lcd_number (LCD_PROGRAM, (short) 0, 0);
  refresh_display();
  continueFlag = true;
  i = 1;
  do {
    check_for_data (&valid, &nextByte);
    if (valid)
    {
      receive_data (buffer, BUFSIZE, &numread);
      seqNumber = ((TWOBYTES) buffer[2] << 8) |  buffer[1]; 
      set_lcd_number (LCD_UNSIGNED, (short) i, 3002);
      set_lcd_number (LCD_PROGRAM, (short) 0, 0);
      refresh_display();
      if (i != 1 && seqNumber == 0)
        break;
      if (nextByte >= MAXNEXTBYTEPTR || seqNumber != i)
      {
        #if DEBUG_RCX
        debug (4, (TWOBYTES) nextByte / 10, seqNumber - i);
        #endif
        play_system_sound (SOUND_QUEUED, 4);
        goto LABEL_DOWNLOAD;
      }
      i++;
    }
    else
    {
      status = 0;
      read_buttons (BUTTONS_READ, &status);
      // Check if pgm & run are pressed
      if (status == 0x05)
      {
        get_power_status (POWER_KEY, &status);
        if (status == 0)
	{
          // power button pressed - wait
          for (;;)
	  {
            get_power_status (POWER_KEY, &status);
            if (status != 0)
              goto LABEL_EXIT;              
	  }
	}
      }
    }
  } while (1);
  play_system_sound (SOUND_QUEUED, 3);
  // Initialize binary image location
  #if 0
  debug (-1, ((TWOBYTES) MEM_START) / 10, 0);
  debug (-1, ((TWOBYTES) (&_text_end)) / 10, 6);
  debug (-1, ((TWOBYTES) (&_data_end)) / 10, 7);
  debug (-1, ((TWOBYTES) (&_text_begin)) / 10, 8);
  debug (-1, ((TWOBYTES) (&_data_begin)) / 10, 9);
  debug (-1, ((TWOBYTES) (&_bss_start)) / 10, 0);
  #endif
  install_binary (MEM_START);
  // Initialize heap location and size
  #if DEBUG_RCX
  debug (-1, ((TWOBYTES) nextByte) / 10, 1);
  #endif
  init_memory (nextByte, ((TWOBYTES) MEM_END - (TWOBYTES) nextByte) / 2);
  // Initialize special exceptions
  #if 0
  debug (-1, ((TWOBYTES) MEM_END - (TWOBYTES) nextByte) / 2, 2);
  #endif
  init_exceptions();
  // Create the boot thread (bootThread is a special global)
  bootThread = (Thread *) new_object_for_class (JAVA_LANG_THREAD);
  #if DEBUG_RCX
  debug (-1, ((TWOBYTES) bootThread) / 10, 3);
  #endif
  // Start/prepare thread
  init_thread (bootThread);
  // Execute the bytecode interpreter
  #if 0
  debug (-1, (short) get_master_record()->magicNumber, 4);
  #endif
  engine();
  // Engine returns when all threads are done
  if (continueFlag)
    goto LABEL_DOWNLOAD;
 LABEL_EXIT:
  shutdown_buttons();
  shutdown_timer();
  shutdown_power();
  return 0;
}




