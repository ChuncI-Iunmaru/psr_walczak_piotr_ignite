import data.Animal;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.lang.IgniteRunnable;
import org.apache.ignite.resources.IgniteInstanceResource;

import javax.cache.Cache;
import java.util.List;

public class GetFeedTotalRunnable implements IgniteRunnable {
    @IgniteInstanceResource
    Ignite ignite;

    @Override
    public void run() {
        IgniteCache<Long, Animal> animals = ignite.getOrCreateCache("animals");
        System.out.println("Przetwarzanie danych - klient");
        List<Long> allKeys = animals.query(new ScanQuery<Long, Animal>((k, p) -> true), Cache.Entry::getKey).getAll();
        double total = 0;
        for (Long k : allKeys) {
            System.out.println("Klucz "+k+" => "+animals.get(k));
            total += animals.get(k).getFeedPerDay();
        }
        System.out.println("Razem "+total+"kg karmy wszystkich rodzajów.");
    }
}
