
import Helpers.ValidateInputs;
import java.net.*;
import Client.*;
import Server.*;

/**
 * Classe main del programma.
 *
 * @author Daniele Cerghetti
 * @version 19.11.2020
 */
public class LanSpeedTester {

    /**
     * Attributo che indica un oggetto client.
     */
    private static Client client;

    /**
     * Attributo che indica un oggetto server.
     */
    private static Server server;

    public static void main(String[] args) throws UnknownHostException, SocketException {
        //richiesta ruolo del pc
        int ruolo = ValidateInputs.getInfo(
                "Che ruolo ha questo pc[1 => server; 2=> Client]: ",
                "Errore, inserisci un numero che sia 1 o 2!",
                1,
                2
        );
        if (ruolo == 1) { //server
            int porta = ValidateInputs.getPort();
            server = new Server(porta);
            if (!Inet4Address.getLocalHost().toString().contains("127.0.0.1")) {
                if (server.testClient()) {
                    server.getClientStatus(true, false, true);
                    server.createThred();
                    server.getClientStatus(false, true, false);
                }else{
                    System.out.println("Nessun client si è connesso");
                }
            }else{
                System.out.println("Server non connesso ad una rete!\n"
                        + "Connneterlo ad una rete e poi riprovare");
            }

        } else { //client
            int connessioni = ValidateInputs.getInfo(
                    "Numero di connessioni da testare con il server[1-->200]: ",
                    "Errore, inserisci un numero compreso tra 1 e 200",
                    1,
                    200
            );
            int dimData = ValidateInputs.getInfo(
                    "Dimensione di ogni pacchetto[32-->65500 byte]: ",
                    "Errore, inserisci un numero che sia 32 o 65500!",
                    32,
                    65500
            );
            
            Inet4Address ipServer = ValidateInputs.getIpServer();
            int port = ValidateInputs.getInfo(
                    "La porta sul quale il server ascolta[1024-->65535]: ",
                    "Errore, inserisci un numero compreso tra 1024 e 65535",
                    1024,
                    65535
            );
            client = new Client(connessioni, ipServer, port, dimData);

            if (client.testServer()) {
                System.out.println("Server Connesso");
                System.out.println("Inizio test ...\n");
                client.createThread();
                while (true) {
                    if (client.getTimes().size() == 10 * client.getConnactions()) {
                        client.doStatistics();
                        return;
                    }
                }
            }else{
                System.out.println("Server non trovato");
            }
        }
    }
}
