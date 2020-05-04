import data.Animal;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;

import java.util.Collections;
import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) throws IgniteException {
        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setPeerClassLoadingEnabled(true);
        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        ipFinder.setAddresses(Collections.singletonList("127.0.0.1:47500..47509"));
        cfg.setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(ipFinder));
        //Uruchom jako klient
        cfg.setClientMode(true);

        try (Ignite ignite = Ignition.start(cfg)) {
            IgniteCache<Long, Animal> animals = ignite.getOrCreateCache("animals");
            AnimalIgniteRepo repo = new AnimalIgniteRepo(animals);
            AnimalUtils service = new AnimalUtils(repo);

            //Pętla główna programu
            System.out.println("Numer tematu: " + 85297%10);
            System.out.println("Witaj w aplikacji ZOO - Ignite w trybie klient\nPiotr Walczak gr. 1ID22B");
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("\n1.[d]odaj zwierzę" +
                        "\n2.[u]suń zwierzę" +
                        "\n3.[a]ktualizuj zwierzę" +
                        "\n4.pobierz po [k]luczu" +
                        "\n5.pobierz [w]szystkie" +
                        "\n6.pobierz po [i]mieniu i/lub gatunku" +
                        "\n7.[o]blicz zapotrzebowanie dla zwierzat" +
                        "\n8.[s]tandaryzuj pisownię" +
                        "\n9.[z]akoncz");
                try {
                    switch (scanner.nextLine().toLowerCase().charAt(0)) {
                        case 'd': service.addAnimal(); break;
                        case 'u': service.removeAnimal(); break;
                        case 'a': service.updateAnimal(); break;
                        case 'k': service.getByKey(); break;
                        case 'w': service.getAndPrintAll(); break;
                        case 'i': service.getByNameAndSpecies(); break;
                        //Przetwarzanie rozsyłane do wszystkich klientów
                        case 'o': {
                            ignite.compute(ignite.cluster().forClients()).broadcast(new GetFeedTotalRunnable());
                            System.out.println("Przesłano zadanie. Sprawdz status u klientow.");
                            break;
                        }
                        //Przetwarzanie rozysłane do wszystkich serwerów
                        case 's': {
                            ignite.compute(ignite.cluster().forServers()).broadcast(new StandarizeTextRunnable());
                            System.out.println("Przesłano zadanie. Sprawdz status na serwerach.");
                            break;
                        }
                        case 'z': return;
                        default:
                            System.out.println("Podano nieznaną operację. Spróbuj ponownie.");
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    System.out.println("Podano nieprawidłową operację.");
                }
            }

        }
    }
}
