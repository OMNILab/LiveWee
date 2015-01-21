socat - udp4-listen:514,fork | tee >(socat - udp-sendto:127.0.0.1:30514) >(socat - udp-sendto:127.0.0.1:31514)
