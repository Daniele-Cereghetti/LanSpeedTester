package Server;

import java.net.*;
import java.io.*;

/**
 * Classe che si occupa di gestire il processo di un server.
 * @author Daniele Cereghetti
 * @version 19.11.2020
 */
public class ServerThread extends Thread {

    /**
     * Attiburo che indica la porta sulla quale il client riceve i dati.
     */
    private int clientPort;

    /**
     * Attriburo che indica l'ip del client
     */
    private Inet4Address ipClient;

    /**
     * Attributo che indica la dimensione dei dati in arrivo dal client.
     */
    private int dimensionData;

    /**
     * Attributo che indica la porta sulla quale il server ascolta.
     */
    private int myPort;

    /**
     * Get the value of clientPort
     * @return the value of clientPort
     */
    public int getClientPort() {
        return clientPort;
    }

    /**
     * Get the value of dimData
     * @return the value of dimData
     */
    public int getDimensionData() {
        return dimensionData;
    }

    /**
     * Get the value of ipClient
     * @return the value of ipClient
     */
    public Inet4Address getIpClient() {
        return ipClient;
    }

    /**
     * Get the value of myPort
     * @return the value of myPort
     */
    public int getMyPort() {
        return myPort;
    }

    /**
     *
     * @param myPort
     * @param ipClient
     * @param clientPort
     * @param dimensionData
     */
    public ServerThread(int myPort, Inet4Address ipClient, int clientPort, int dimensionData) {
        this.myPort = myPort;
        this.ipClient = ipClient;
        this.clientPort = clientPort;
        this.dimensionData = dimensionData;
    }

    /**
     * Metodo che esegue le azioni che farà la thread.
     */
    @Override
    public void run() {
        try {
            DatagramSocket serverUDP = new DatagramSocket(getMyPort());
            byte[] data = new byte[getDimensionData()];
            serverUDP.setSoTimeout(2000);
            int i = 0;
            while (i < 10) {
                try {
                    //prepario richiesta
                    DatagramPacket dataClient = new DatagramPacket(
                            data, data.length, getIpClient(), getClientPort()
                    );
                    //aspetto richiesta
                    serverUDP.receive(dataClient);
                    //invio una risposta
                    serverUDP.send(dataClient);
                } catch (SocketTimeoutException ste) {

                }
                i++;
            }
            serverUDP.close();
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }
}
