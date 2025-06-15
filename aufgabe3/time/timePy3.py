from datetime import datetime
import socket
import struct

def getTime(host, port):

    host = host
    port = port

    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.connect((host, port))
    #print('connecting to server')


    try:
        buffer = 48
        sock.sendto(b"", (host, port))
        response, address = sock.recvfrom(buffer)

        print(response)
        print(response[0])
        t = struct.unpack( "!I", response )[0]
        t -= 2208988800
        t = datetime.fromtimestamp(t)
        date = t.strftime("%Y-%m-%d")
        tFormated = t.strftime("%H:%M:%S")

        print(date)
        print(tFormated)

    finally:
        sock.close()


if __name__ == "__main__":
    getTime("time.nist.gov", 37)