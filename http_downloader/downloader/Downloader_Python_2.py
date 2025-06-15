import math
import socket
import os
from datetime import datetime
import sys

def make_get_request(host, blocksize):


    ######## http

    # split host
    host = host[7:]
    print("Host: " + host)
    domain, subdomain = host.split("/", 1)
    print("Domain: " + domain + " | Subdomain: " + subdomain)

    # handling blocksize
    if( blocksize.endswith("KB")):
        blocksize = blocksize[:2]
        blocksize = int(blocksize)
        blocksize = blocksize * 1024
    elif (blocksize.endswith("MB")):
        blocksize = blocksize[:2]
        blocksize = int(blocksize)
        blocksize = blocksize * 1024 * 1024
    else:
        print("Filesize not allowed")
        return

    # Define start_block and end_block the first time
    buffer = 4096
    start_block = 0
    end_block = blocksize
    block_counter = 1


    ####### file

    # Create timestamp for filename
    now = datetime.now()
    time = now.strftime("%m_%d_%Y_%H-%M-%S")
    #print(time)

    
    file = subdomain

    log_file = r"log_file.txt"

    # check logfile
    if os.path.exists("log_file.txt"):
        # read logfile
        with open(log_file, 'r') as flo:
            line_count = 1
            for line in flo:
                if line_count == 1:
                    file = line.strip()
                if line_count == 2:
                    end_block = line.strip()
                    end_block = int(end_block)
                    start_block = end_block - buffer
                line_count += 1



    while True:
        # create a socket object
        client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        
        # Connect to the host
        client_socket.connect((domain, 80)) # (IPaddress, Port)

        # Create GET request
        request_str = "GET /" + subdomain +  " HTTP/1.1\r\nHost: " + domain + "\r\nConnection: close\r\nRange: bytes=" + str(start_block) + "-" + str(end_block) + "\r\n\r\n"
        request = request_str.encode("utf-8")
        print("Request: " + request_str)

        # Send the GET request
        client_socket.sendall(request)

        # Get the response from the server
        response = b""
        while True:
            data = client_socket.recv(buffer)
            #print("receiving data...")
            temp_data = data
            # print('data: ' + temp_data.decode("utf-8"))
            if not data:
                break
            response += data

        # Seperate Header and Body
        header, _, body = response.partition(b"\r\n\r\n")

        # Handling the header
        # setting new 
        header = header.decode("utf-8")
        if("Content-Range: " in header):

            _, header_end = header.rsplit("Content-Range: ")
            _, file_size = header_end.rsplit("/")
            file_size = int(file_size)
            #print("Filesize: " + str(file_size))

            #print("Header: " + header) # print header


        # Handling the body
        # Write the response body in the file
        with open(file, "ab") as f:
            f.write(body)

        # Write / Update log-file
        temp_end_block = end_block
        temp_end_block = str(temp_end_block)
        with open(log_file, "w") as fl:
            fl.write(file + "\n")
        with open(log_file, "a") as fl:
            fl.write(temp_end_block)



        print("Block " + str(block_counter) + " downloaded")
        print("Downloading Bytes: " + str(start_block) + " - " + str(end_block) + " / " + str(file_size))
        print("Downloaded: " + str((end_block / file_size) * 100) +  "%" )

        # Break point for the while-loop
        if(end_block >= file_size):
            os.remove("log_file.txt") # delete log file, if the download is complete
            break
            
        # Update variables
        block_counter += 1
        start_block = end_block + 1
        end_block += blocksize

    client_socket.close()
    print("Downloader complete")

    return response


if __name__ == "__main__":
    host = sys.argv[1]
    #host = "http://speedtest.belwue.net/BelWue_logo.svg"
    print(host)
    blocksize = sys.argv[2]
    #blocksize = 16384
    print(blocksize)
    response = make_get_request(host, blocksize)