package com.example.multiplex;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;

public class NMEAHttpHandler  implements HttpHandler {

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

            System.out.println("handle POST : (" +raceId + ") " + NMEASentence);

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

    private boolean VerifNMEAChecksum(final String sentence) {
        final int len = sentence.length();
        final String message = sentence.substring(1, sentence.indexOf(42));
        final String messageCrc = sentence.substring(len - 2);
        final String cpuCrc = getStringChecksum(getChecksum(message));
        return messageCrc.equals(cpuCrc);
    }

    private void Decode(final String sentence) {
        final boolean rc = VerifNMEAChecksum(sentence);

        if (!rc) {
            //this.DecodeNMEAMessage(sentence);
            System.out.println("bad NMEA sentence : "+sentence);
        }

    }

}
