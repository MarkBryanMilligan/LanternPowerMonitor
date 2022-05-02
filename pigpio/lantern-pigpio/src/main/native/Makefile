ARCH        := armhf
DEBUG	       = -O3
CC           = $(CROSS_PREFIX)gcc
AR           = $(CROSS_PREFIX)ar
RANLIB       = $(CROSS_PREFIX)ranlib
SIZE         = $(CROSS_PREFIX)size
STRIP        = $(CROSS_PREFIX)strip
SHLIB        = $(CC) -shared
STRIPLIB     = $(STRIP) --strip-unneeded
INCLUDE	     = -I. -Ipigpio                                              \
               -I/$(JAVA_HOME)/include                                   \
               -I/$(JAVA_HOME)/include/linux                             \
               -I/usr/local/include -I/usr/local/include/linux

CFLAGS      := $(DEBUG) -Wall $(INCLUDE) -Winline -pipe $(CARGS) -fPIC
LIBS         =  -L lib/$(ARCH) -L pigpio -lpigpio -lrt

TARGET=lantern-pigpio.so

###############################################################################

SRC	=   com_lanternsoftware_pigpio_PIGPIO.c

OBJ =	$(SRC:.c=.o)

all:	$(OBJ)
	    @echo [LINK with DYNAMICALLY linked libraries]
	    @$(CC) $(OBJ) -shared -o $(TARGET) $(INCLUDE) $(LIBS)

.c.o:
	@echo [COMPILE] $<
	@$(CC) -c $(CFLAGS) $< -o $@

clean:
	rm -f $(OBJ) $(TARGET) *~ core tags Makefile.bak

tags:	$(SRC)
	@echo [ctags]
	@ctags $(SRC)

depend:
	makedepend -Y $(SRC)

install:	$(TARGET)
	@echo [install]
	install -m 0755 -d /usr/local/lib
	install -m 0755 -d /usr/local/include
	install -m 0644 $(TARGET)	/usr/local/lib

uninstall:
	@echo [uninstall]
	rm -f /usr/local/lib/$(TARGET)

com_lanternsoftware_pigpio_PIGPIO.o: com_lanternsoftware_pigpio_PIGPIO.h
