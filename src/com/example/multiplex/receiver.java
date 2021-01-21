package com.example.multiplex;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class receiver   {


    public static void main(String[] args) {
        HttpServer server = null;
        int port = 8081;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Argument" + args[0] + " must be an integer.");
                System.exit(1);
            }
        }
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
            server.createContext("/nmea", new  NMEAHttpHandler());
            //TODO: maybe ? final String url = "/nmea/" + String.valueOf(this.mVrId);
            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
            server.setExecutor(threadPoolExecutor);
            server.start();
            System.out.println("Server started on port : "+port);

        } catch (IOException e) {

            System.out.println("Server exception on port : "+ port);
            e.printStackTrace();
        }
    }

}
