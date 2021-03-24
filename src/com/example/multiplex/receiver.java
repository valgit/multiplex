package com.example.multiplex;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class receiver   {
    Hashtable<String,RaceServer> raceServers;
    HttpServer server = null;
    static int port = 8081;

    public synchronized  RaceServer getRaceServer(String raceid) {
        RaceServer server;
        //System.out.println("My Name is :- " + Thread.currentThread().getName());

        server = raceServers.get(raceid);
        if (server == null) {
            System.out.println("create new race server " + raceid);
            server = new RaceServer(Integer.parseInt(raceid));
            raceServers.put(raceid,server);

            Thread thread = new Thread(server);
            thread.start();
        }
        //System.out.println("return race");
        return server;
    }

    public static void main(String[] args) {
        receiver NMEAreceiver  = new receiver();

        // TODO: check args better
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Argument" + args[0] + " must be an integer.");
                System.exit(1);
            }
        }
        NMEAreceiver.run();
    }

    public void run() {
        raceServers = new Hashtable<String,RaceServer>();

        try {
            server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
            server.createContext("/nmea", new  NMEAHttpHandler(this));
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
