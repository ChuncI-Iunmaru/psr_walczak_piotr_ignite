import data.Animal;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.lang.IgniteRunnable;
import org.apache.ignite.resources.IgniteInstanceResource;

import javax.cache.Cache;
import java.util.Set;
import java.util.stream.Collectors;

public class StandarizeTextRunnable implements IgniteRunnable {
    @IgniteInstanceResource
    Ignite ignite;

    @Override
    public void run() {
        IgniteCache<Long, Animal> animals = ignite.getOrCreateCache("animals");
        System.out.println("Przetwarzanie danych - serwer");
        System.out.println("Zamienia pisownię wszystkich na format 'Imię gatunek'");
        Set<Long> keys = animals.query(new ScanQuery<Long, Animal>((k, p) -> true), Cache.Entry::getKey).getAll().stream().collect(Collectors.toSet());
        animals.invokeAll(keys, (entry, object) -> {
            Animal tmp = entry.getValue();
            //Imię powinno się zaczynać z dużej
            if (tmp.getName().length() > 1) {
                tmp.setName(tmp.getName().substring(0, 1).toUpperCase() + tmp.getName().substring(1).toLowerCase());
            } else tmp.setName(tmp.getName().toUpperCase());
            //Gatunek wyłącznie małymi
            tmp.setSpecies(tmp.getSpecies().toLowerCase());
            entry.setValue(tmp);
            return null;
        });
    }
}
