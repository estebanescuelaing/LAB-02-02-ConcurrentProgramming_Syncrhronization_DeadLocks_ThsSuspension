package edu.eci.arsw.blacklistvalidator;

import java.util.List;
import java.util.Scanner;

public class MainExperimentos {

    private static final String IP_DISPERSA = "202.24.34.55";

    public static void main(String[] args) {

        int nucleos = Runtime.getRuntime().availableProcessors();
        System.out.println("Núcleos disponibles en esta máquina: " + nucleos);
        System.out.println("IP fija a validar: " + IP_DISPERSA);
        System.out.println("------------------------------------------------");

        Scanner sc = new Scanner(System.in);

        int n = -1;
        while (n <= 0) {
            System.out.print("Número de hilos para esta corrida: ");
            if (sc.hasNextInt()) {
                n = sc.nextInt();
                if (n <= 0) {
                    System.out.println("Debe ser mayor a 0.");
                }
            } else {
                System.out.println("Ingrese un entero válido.");
                sc.next();
            }
        }

        HostBlackListsValidator hblv = new HostBlackListsValidator();

        System.out.println("Iniciando búsqueda con " + n + " hilo(s)...");
        long inicio = System.currentTimeMillis();
        List<Integer> blackListOcurrences = hblv.checkHost(IP_DISPERSA, n);
        long fin = System.currentTimeMillis();

        System.out.println("Resultado: " + blackListOcurrences);
        System.out.println("HILOS: " + n + "  |  TIEMPO: " + (fin - inicio) + " ms");
    }
}
