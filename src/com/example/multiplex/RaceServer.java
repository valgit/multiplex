package com.example.multiplex;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class RaceServer {
    int raceId;
    int port;
    boolean running = true;

    ServerSocket serverConnect;
    Socket dedicadedServer;

    public RaceServer(int raceid) {
        raceId = raceid;
        port = 10000 + raceid;

        try {
            serverConnect = new ServerSocket(port);
            System.out.println("Server started.\nListening for connections on port : " + port + " ...\n");

            // we listen until user halts server execution
            while (running) {
                dedicadedServer = serverConnect.accept();
                System.out.println("Connection opened. (" + new Date() + ")");

            }

        } catch (IOException e) {
            System.err.println("Server Connection error : " + e.getMessage());
        }
    }

    // new NMEA data
    public void updateRace(final String sentence) {
        // send to each client ...
        System.out.println("got NMEA data (" +raceId + ") : " + sentence);
        OutputStream clientOutput = null;
        try {
            clientOutput = dedicadedServer.getOutputStream();
            clientOutput.write(sentence.getBytes());
            clientOutput.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stopServer() {
        running = false;
    }

    /*
     * NMEA tools
     */
    private int getChecksum(final String sentence) {
        int checksum = 0;
        for (int i = 0; i < sentence.length(); ++i) {
            final char c = sentence.charAt(i);
            checksum ^= c;
        }
        return checksum;
    }

    private String getStringChecksum(final int checksum) {
        String strChecksum = Integer.toString(checksum, 16).toUpperCase();
        if (strChecksum.length() < 2) {
            strChecksum = "0" + strChecksum;
        }
        return strChecksum;
    }

    private boolean verifNMEAChecksum(final String sentence) {
        final int len = sentence.length();
        final String message = sentence.substring(1, sentence.indexOf(42));
        final String messageCrc = sentence.substring(len - 2);
        final String cpuCrc = getStringChecksum(getChecksum(message));
        return messageCrc.equals(cpuCrc);
    }

    private void Decode(final String sentence) {
        final boolean rc = verifNMEAChecksum(sentence);

        if (!rc) {
            //this.DecodeNMEAMessage(sentence);
            System.out.println("bad NMEA sentence : " + sentence);
        }

    }

}
