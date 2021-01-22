package com.example.multiplex;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;

public class NMEAHttpHandler  implements HttpHandler {
    private final receiver raceHandler;

    public NMEAHttpHandler(receiver racehandler) {
        // there is an implied super() here
        this.raceHandler = racehandler;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String requestParamValue=null;

        OutputStream os = httpExchange.getResponseBody();

        if("GET".equals(httpExchange.getRequestMethod())) {
            System.out.println("handle GET");
            String response = "Hi there!";
            httpExchange.sendResponseHeaders(200, response.getBytes().length);//response code and length
            os.write(response.getBytes());

        } else if("POST".equals(httpExchange.getRequestMethod())) {
            //String response = httpExchange.getRequestBody().toString();
            InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
            final String NMEASentence = readBodyAsString(isr);
            // get POST /nmea/442.1 HTTP/1.1
            // content is NMEA message
            String raceId = httpExchange.getRequestURI().toString().substring(6,9);
            RaceServer racesrv = raceHandler.getRaceServer(raceId);
            if (racesrv != null) {
                racesrv.updateRace(NMEASentence);
            }
            //System.out.println("handle POST : (" +raceId + ") " + NMEASentence);

            final String response = "OK";
            httpExchange.sendResponseHeaders(200, response.getBytes().length);//response code and length
            os.write(response.getBytes());
        } else {
            final String response = "error";
            System.out.println("handle unknown");
            httpExchange.sendResponseHeaders(404, response.getBytes().length);//response code and length
            os.write(response.getBytes());
        }
        os.flush();
        os.close();
    }

    private String readBodyAsString(final InputStreamReader is) {
        BufferedReader br = new BufferedReader(is);
        int b = 0;
        StringBuilder buf = new StringBuilder();
        while (true) {
            try {
                if (!((b = br.read()) != -1)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            buf.append((char) b);
        }

        try {
            br.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buf.toString();
    }


}
