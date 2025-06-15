import socket
import struct
import sys

def getTime(host, port):

    host = host
    port = port

    msg = '\x1b' + 48 * '\0'

    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.connect(('time.nist.gov', 37))
    print('connecting to server')


    try:
        buffer = 48
        sock.sendto(msg.encode(), ('time.nist.gov', 37))
        msg, address = sock.recvfrom(buffer)

        time = struct.unpack( "!12I", bytes(buffer) )[10]
        print(time)
    
    finally:
        sock.close()


if __name__ == "__main__":
    getTime("time.nist.gov", 37)