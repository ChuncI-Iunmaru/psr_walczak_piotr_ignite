import data.Animal;
import javafx.util.Pair;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheEntry;
import org.apache.ignite.cache.query.ScanQuery;

import javax.cache.Cache;
import java.util.*;
import java.util.stream.Collectors;

public class AnimalIgniteRepo implements AnimalRepo {
    private IgniteCache<Long, Animal> cache;

    public AnimalIgniteRepo(IgniteCache<Long, Animal> cache) {
        this.cache = cache;
    }

    @Override
    public Animal addAnimal(Long key, Animal a) {
        return cache.getAndPut(key, a);
    }

    @Override
    public Optional<Animal> getByKey(Long key) {
        return Optional.ofNullable(cache.get(key));
    }

    @Override
    public Animal updateAnimal(Long key, Animal newAnimal) {
        return cache.getAndReplace(key, newAnimal);
    }

    @Override
    public boolean removeAnimal(Long key) {
        return cache.getAndRemove(key) != null;
    }

    @Override
    public List<Animal> getAllAnimals() {
        return cache.query(new ScanQuery<Long, Animal>(
                (k, p) -> true), // Remote filter.
                Cache.Entry::getValue // Transformer.
        ).getAll();
    }

    @Override
    public Set<Long> getAllKeys() {
        return cache.query(new ScanQuery<Long, Animal>(
                        (k, p) -> true), // Remote filter.
                Cache.Entry::getKey // Transformer.
        ).getAll().stream().collect(Collectors.toSet());
    }

    @Override
    public List<Animal> getByNameOrSpecies(String name, String species) {
        if (name.equals("*") && species.equals("*")) {
            return getAllAnimals();
        } else if (!name.equals("*") && species.equals("*")) {
            return cache.query(new ScanQuery<Long, Animal>((k, p) -> p.getName().compareTo(name)==0), Cache.Entry::getValue).getAll();
        } else if (name.equals("*")) {
            return cache.query(new ScanQuery<Long, Animal>((k, p) -> p.getSpecies().compareTo(species)==0), Cache.Entry::getValue).getAll();
        } else return cache.query(new ScanQuery<Long, Animal>((k, p) -> p.getName().compareTo(name)==0 && p.getSpecies().compareTo(species)==0), Cache.Entry::getValue).getAll();
    }

}
