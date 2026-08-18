package edu.eci.arsw.blacklistvalidator;

import java.util.List;
import java.util.Scanner;

public class MainParalelo {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        HostBlackListsValidator hblv = new HostBlackListsValidator();

        System.out.print("Dirección IP a validar: ");
        String ip = sc.next();

        int n = -1;
        while (n <= 0) {
            System.out.print("Número de hilos a usar: ");
            if (sc.hasNextInt()) {
                n = sc.nextInt();
                if (n <= 0) {
                    System.out.println("El número de hilos debe ser mayor a 0. Intente de nuevo.");
                }
            } else {
                System.out.println("Debe ingresar un número entero válido.");
                sc.next();
            }
        }

        try {
            long inicio = System.currentTimeMillis();
            List<Integer> blackListOcurrences = hblv.checkHost(ip, n);
            long fin = System.currentTimeMillis();

            System.out.println("The host was found in the following blacklists:" + blackListOcurrences);
            System.out.println("Tiempo de ejecución: " + (fin - inicio) + " ms");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}